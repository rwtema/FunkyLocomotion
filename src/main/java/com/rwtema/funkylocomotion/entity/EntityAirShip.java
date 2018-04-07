package com.rwtema.funkylocomotion.entity;

import com.google.common.collect.ImmutableList;
import com.rwtema.funkylocomotion.IClientTickable;
import com.rwtema.funkylocomotion.api.IMoveFactory;
import com.rwtema.funkylocomotion.blocks.TileMassFrameController;
import com.rwtema.funkylocomotion.blocks.TileMovingBase;
import com.rwtema.funkylocomotion.description.Describer;
import com.rwtema.funkylocomotion.factory.FactoryRegistry;
import com.rwtema.funkylocomotion.helper.BlockHelper;
import com.rwtema.funkylocomotion.helper.NBTHelper;
import com.rwtema.funkylocomotion.network.FLNetwork;
import com.rwtema.funkylocomotion.network.MessageClearTile;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityAirShip extends Entity implements IEntityAdditionalSpawnData, IClientTickable {

	public static final float PI_DIV = 3.14159265358979323846F / 180F;
	public static final int RESET_TURNING_SPEED = 3;
	public static final double AIR_RESISTANCE = 0.9166666666666666;

	static {
		MinecraftForge.EVENT_BUS.register(EntityAirShip.class);
	}


	HashMap<BlockPos, Entry> structure = new HashMap<>();
	Vec3d center = Vec3d.ZERO;
	@Nullable
	List<AxisAlignedBB> collide_bounds = null;
	Vec3d position = Vec3d.ZERO, prevPosition = Vec3d.ZERO;
	Vec3d velocity = Vec3d.ZERO, prevVelocity = Vec3d.ZERO;
	Vec3d rotation = Vec3d.ZERO, prevRotation = Vec3d.ZERO;

	public EntityAirShip(World worldIn) {
		super(worldIn);
	}

	@SubscribeEvent
	public static void addCollisions(GetCollisionBoxesEvent event) {
		if (!(event.getEntity() instanceof EntityPlayer)) {
			return;
		}
		World world = event.getWorld();

		List<Entity> list1 = world.getEntitiesWithinAABBExcludingEntity(event.getEntity(), event.getAabb().grow(5D));
		for (Entity entity : list1) {
			if (entity instanceof EntityAirShip) {
				EntityAirShip airShip = (EntityAirShip) entity;
				airShip.addAxisAlignedBB(event.getAabb(), event.getCollisionBoxesList());
			}
		}
	}

	public static void create(TileMassFrameController controller, EntityPlayer player) {
		BlockPos a = controller.getPos().add(controller.start).add(1, 1, 1);
		BlockPos b = controller.getPos().add(controller.end).add(-1, -1, -1);
		World world = controller.getWorld();

		HashMap<BlockPos, Entry> entryHashMap = new HashMap<>();
		for (BlockPos.MutableBlockPos pos : BlockPos.getAllInBoxMutable(a, b)) {
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock().isAir(state, world, pos)) continue;


			Block block = state.getBlock();
			int meta = block.getMetaFromState(state);

			int lightopacity = state.getLightOpacity(world, pos);

			int lightlevel = state.getLightValue(world, pos);

			List<AxisAlignedBB> axes = new ArrayList<>();
			state.addCollisionBoxToList(world, pos, TileEntity.INFINITE_EXTENT_AABB, axes, null, false);

			AxisAlignedBB[] bounds;
			if (axes.size() > 0) {
				bounds = axes.stream().map(
						bb -> new AxisAlignedBB(
								bb.minX - pos.getX(),
								bb.minY - pos.getY(),
								bb.minZ - pos.getZ(),
								bb.maxX - pos.getX(),
								bb.maxY - pos.getY(),
								bb.maxZ - pos.getZ()
						)
				).toArray(AxisAlignedBB[]::new);
			} else {
				bounds = TileMovingBase.BLANK;
			}

			NBTTagCompound descriptor = new NBTTagCompound();
			descriptor.setString("Block", Block.REGISTRY.getNameForObject(block).toString());
			if (meta != 0)
				descriptor.setByte("Meta", (byte) meta);

			TileEntity tile = world.getTileEntity(pos);
			if (tile != null) {
				Describer.addDescriptionToTags(descriptor, tile);
			}

			entryHashMap.put(pos.toImmutable(), new Entry(state, descriptor, descriptor, lightlevel, lightopacity, bounds));
		}

		if (entryHashMap.isEmpty()) return;

		for (Chunk chunk : (Iterable<Chunk>) entryHashMap.keySet().stream().map(world::getChunkFromBlockCoords).distinct()::iterator) {

			List<NextTickListEntry> ticks = world.getPendingBlockUpdates(chunk, false);
			if (ticks != null) {
				long k = world.getTotalWorldTime();
				for (NextTickListEntry tick : ticks) {
					BlockPos pos = tick.position;

					if (chunk.getBlockState(pos).getBlock() != tick.getBlock())
						continue;

					Entry entry = entryHashMap.get(pos);
					if (entry == null) continue;

					entry.scheduledTickTime = (int) (tick.scheduledTime - k);
					entry.scheduledTickPriority = tick.priority;
				}
			}

		}

		for (Map.Entry<BlockPos, Entry> entry : entryHashMap.entrySet()) {
			IMoveFactory factory = FactoryRegistry.getFactory(world, entry.getKey());
			entry.getValue().block = factory.destroyBlock(world, entry.getKey());
		}


		for (BlockPos pos : entryHashMap.keySet()) {
			BlockHelper.silentClear(world.getChunkFromBlockCoords(pos), pos);
			FLNetwork.sendToAllWatchingChunk(world, pos, new MessageClearTile(pos));
			world.removeTileEntity(pos);
		}

		for (BlockPos pos : entryHashMap.keySet()) {
			BlockHelper.postUpdateBlock(world, pos);
			PlayerChunkMapEntry watcher = FLNetwork.getChunkWatcher(world, pos);
			if (watcher != null) {
				SPacketBlockChange pkt = new SPacketBlockChange(world, pos);
				watcher.sendPacket(pkt);
			}
		}


		BlockPos maxpos = entryHashMap.keySet().stream().reduce(
				(a0, b0) -> new BlockPos(Math.max(a0.getX(), b0.getX()), Math.max(a0.getY(), b0.getY()), Math.max(a0.getZ(), b0.getZ()))
		).orElseThrow(RuntimeException::new);

		BlockPos minpos = entryHashMap.keySet().stream().reduce(
				(a0, b0) -> new BlockPos(Math.min(a0.getX(), b0.getX()), Math.min(a0.getY(), b0.getY()), Math.min(a0.getZ(), b0.getZ()))
		).orElseThrow(RuntimeException::new);

		BlockPos center = new BlockPos((minpos.getX() + maxpos.getX()) / 2, (minpos.getY() + maxpos.getY())/2 , (minpos.getZ() + maxpos.getZ()) / 2);

		EntityAirShip ship = new EntityAirShip(world);
		entryHashMap.forEach((blockPos, entry) -> {
			ship.structure.put(blockPos.subtract(center), entry);
		});

		ship.position = new Vec3d(center);
		ship.posX = center.getX();
		ship.posY = center.getY();
		ship.posZ = center.getZ();
		ship.velocity = Vec3d.ZERO;
		ship.resetSize();

		world.spawnEntity(ship);
	}

	public static double moveAngleTowards(double value, double target, double speed) {
		return moveAngleTowards(value, target, speed, 360);
	}

	public static double moveAngleTowards(double value, double target, double speed, int MODULO) {

		double diff = target - value;
		double d = Math.abs(diff) % MODULO;
		if (d > (MODULO / 2)) d = d - MODULO;
		if (Math.abs(d) < speed) return target;

		return value + d * Math.signum(diff);
	}

	@Override
	public void setPosition(double x, double y, double z) {
		super.setPosition(x, y, z);
	}

	public void recalcBoundingBox() {

	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox() {
		return null;
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
		if (player.isSneaking()) {
			return false;
		} else {
			if (!this.world.isRemote) {
				player.startRiding(this);
			}

			return true;
		}
	}

	@Override
	public boolean canPassengerSteer() {
		return false;
	}

	@Override
	public void onEntityUpdate() {

		prevPosition = position;
		prevVelocity = velocity;
		prevRotation = rotation;
//		world.spawnParticle(EnumParticleTypes.REDSTONE, posX, posY, posZ, 0, 0, 0);
//		world.spawnParticle(EnumParticleTypes.REDSTONE, position.x + center.x,
//				position.y + center.y,
//				position.z + center.z, 0.1, 0, 1);
		collide_bounds = null;

		posX = position.x;
		posY = position.y;
		posZ = position.z;
		motionX = velocity.x;
		motionY = velocity.y;
		motionZ = velocity.z;
		rotationYaw = (float) rotation.x;
		rotationPitch = (float) rotation.y;
		this.lastTickPosX = this.position.x;
		this.lastTickPosY = this.position.y;
		this.lastTickPosZ = this.position.z;

		super.onEntityUpdate();


		if (world.isRemote) {
			runClientCode();


			position = position.add(velocity);
			velocity = velocity.scale(AIR_RESISTANCE);

		} else {
			if (getControllingPassenger() == null) {
//				if (rotation.y != 0) {
//					double y = rotation.y;
//
//					if (y > RESET_TURNING_SPEED) y -= RESET_TURNING_SPEED;
//					else if (y < -RESET_TURNING_SPEED) y += RESET_TURNING_SPEED;
//					else y = 0;
//
//					rotation = new Vec3d(rotation.x, y, rotation.z);
//				} else if (rotation.z != 0) {
//					rotation = new Vec3d(rotation.x, rotation.y, moveAngleTowards(rotation.z, 0, RESET_TURNING_SPEED));
//				} else if (rotation.x != 0) {
//					double v = (360 * 4 + rotation.x) % 360;
//					double target;
//					if (v < 45) {
//						target = 0;
//					} else if (v < 135) {
//						target = 90;
//					} else if (v < 225) {
//						target = 180;
//					} else if (v < 315) {
//						target = 270;
//					} else {
//						target = 0;
//					}
//
//					rotation = new Vec3d(moveAngleTowards(rotation.x, target, RESET_TURNING_SPEED), 0, 0);
//				}
			}

			position = position.add(velocity);
			velocity = velocity.scale(1 - 1 / 12.0);

			if (!position.equals(prevPosition) || !velocity.equals(prevVelocity) || !rotation.equals(prevRotation)) {
				sendUpdatesToPlayers();
			}
		}

		posX = position.x;
		posY = position.y;
		posZ = position.z;
		motionX = velocity.x;
		motionY = velocity.y;
		motionZ = velocity.z;
		rotationYaw = (float) rotation.x;
		rotationPitch = (float) rotation.y;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void runClientCode() {
		if (getControllingPassenger() == Minecraft.getMinecraft().player) {
			AirshipClientControls.BALLOON.control(this);
			FLNetwork.net.sendToServer(new MessageCustomPositionSpeed(this));
		}
	}

	private void sendUpdatesToPlayers() {
		((WorldServer) world).getEntityTracker().sendToTracking(this, FLNetwork.net.getPacketFrom(new MessageCustomPositionSpeed(this)));
	}

	public void addAxisAlignedBB(AxisAlignedBB axisAlignedBB, List<AxisAlignedBB> bbList) {
		List<AxisAlignedBB> collide_bounds = getCollisions();
		for (AxisAlignedBB transform : collide_bounds) {
			if (transform.intersects(axisAlignedBB)) {
				bbList.add(transform);
			}
		}
	}

	public List<AxisAlignedBB> getCollisions() {
		List<AxisAlignedBB> collide_bounds = this.collide_bounds;
		if (collide_bounds == null) {
			AxisAlignedBB totalBB = new AxisAlignedBB(position, position);
			ImmutableList.Builder<AxisAlignedBB> builder = ImmutableList.builder();
			for (Map.Entry<BlockPos, Entry> entry : structure.entrySet()) {
				BlockPos pos = entry.getKey();

				for (AxisAlignedBB bb : entry.getValue().collisions) {
					AxisAlignedBB transform = AABBHelper.transform(bb.offset(pos)
							.offset(-center.x, -center.y, -center.z)
							,
							v -> AABBHelper.rotate(v, rotation.x * PI_DIV, rotation.y * PI_DIV, rotation.z * PI_DIV)
					).offset(position).offset(center);

					builder.add(transform);
					if (transform.minX < totalBB.minX ||
							totalBB.maxX < transform.maxX ||
							transform.minY < totalBB.minY ||
							totalBB.maxY < transform.maxY ||
							transform.minZ < totalBB.minZ ||
							totalBB.maxZ < transform.maxZ) {
						totalBB = totalBB.union(transform);
					}
				}
				setEntityBoundingBox(totalBB);
			}
			this.collide_bounds = collide_bounds = builder.build();

		}
		return collide_bounds;
	}

	@Override
	protected void entityInit() {

	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBox(Entity entityIn) {
		return null;
	}

	public void updatePassenger(@Nonnull Entity passenger) {
		if (this.isPassenger(passenger)) {
			passenger.setPosition(
					position.x + center.x,
					position.y + center.y + passenger.getYOffset(),
					position.z + center.z);
		}
	}

	@Override
	protected void readEntityFromNBT(@Nonnull NBTTagCompound compound) {
		structure.clear();
		NBTHelper.<NBTTagCompound>wrapList(compound.getTagList("entries", Constants.NBT.TAG_COMPOUND))
				.forEach(t -> structure.put(
						NBTHelper.BLOCK_POS.fromNBT(t.getCompoundTag("coord")),
						new Entry(t.getCompoundTag("entry"))));
		position = NBTHelper.VEC3D.fromNBT(compound.getCompoundTag("position"));
		velocity = NBTHelper.VEC3D.fromNBT(compound.getCompoundTag("velocity"));
		rotation = NBTHelper.VEC3D.fromNBT(compound.getCompoundTag("rotation"));
		resetSize();
	}

	@Override
	protected void writeEntityToNBT(@Nonnull NBTTagCompound compound) {


		NBTTagList list = new NBTTagList();
		structure.entrySet().stream()
				.map(e -> NBTHelper.builder()
						.set("coord", e.getKey(), NBTHelper.BLOCK_POS)
						.setTag("entry", e.getValue().getTag())
						.build())
				.forEach(list::appendTag);
		compound.setTag("entries", list);

		compound.setTag("position", NBTHelper.VEC3D.toNBT(position));
		compound.setTag("velocity", NBTHelper.VEC3D.toNBT(velocity));
		compound.setTag("rotation", NBTHelper.VEC3D.toNBT(rotation));
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		PacketBuffer packetBuffer = new PacketBuffer(buffer);
		packetBuffer.writeVarInt(structure.size());
		structure.forEach((v, e) -> {
			packetBuffer.writeInt(v.getX());
			packetBuffer.writeInt(v.getY());
			packetBuffer.writeInt(v.getZ());
			e.writeToPacketBuffer(packetBuffer);
		});

		FLNetwork.writeVec3d(packetBuffer, position);
		FLNetwork.writeVec3d(packetBuffer, velocity);
		FLNetwork.writeVec3d(packetBuffer, rotation);
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		PacketBuffer packetBuffer = new PacketBuffer(additionalData);
		int n = packetBuffer.readVarInt();
		for (int i = 0; i < n; i++) {
			try {
				BlockPos vec3i = new BlockPos(packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readInt());
				Entry entry = new Entry(packetBuffer);
				structure.put(vec3i, entry);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		position = FLNetwork.readVec3d(additionalData);
		velocity = FLNetwork.readVec3d(additionalData);
		rotation = FLNetwork.readVec3d(additionalData);

		resetSize();
	}

	public void resetSize() {
		if (structure.isEmpty()) return;
		setSize(0, 0);
//		AxisAlignedBB axisAlignedBB = structure.entrySet().stream()
//				.flatMap(e -> Stream.of(e.getValue().collisions).map(t -> t.offset(e.getKey())))
//				.reduce(AxisAlignedBB::union)
//				.orElseThrow(RuntimeException::new);
//		center = new Vec3d(axisAlignedBB.minX + (axisAlignedBB.maxX - axisAlignedBB.minX) * 0.5D, axisAlignedBB.minY + (axisAlignedBB.maxY - axisAlignedBB.minY) * 0.5D, axisAlignedBB.minZ + (axisAlignedBB.maxZ - axisAlignedBB.minZ) * 0.5D);;

		double[] reduce = structure.entrySet().stream()
				.flatMap(e -> Stream.of(e.getValue().collisions).map(t -> t.offset(e.getKey())))
				.reduce(new double[]{0, 0, 0, 0},
						(arr, aabb) -> {
							double w = AABBHelper.volume(aabb);
							return new double[]{
									(aabb.minX + (aabb.maxX - aabb.minX) * 0.5D) * w,
									(aabb.minY + (aabb.maxY - aabb.minY) * 0.5D) * w,
									(aabb.minZ + (aabb.maxZ - aabb.minZ) * 0.5D) * w,
									w
							};
						},
						(arr1, arr2) -> {
							double[] doubles = new double[4];
							for (int i = 0; i < 4; i++) {
								doubles[i] = arr1[i] + arr2[i];
							}
							return doubles;
						});

		center = new Vec3d(reduce[0] / reduce[3], reduce[1] / reduce[3], reduce[2] / reduce[3]);
		center = new Vec3d(0.5,0.5,0.5);
//		Vec3d reduce = structure.keySet().stream()
//				.map(Vec3d::new)
//				.reduce(Vec3d.ZERO, (a, b) -> new Vec3d(a.x + 0.5 + b.x + 0.5, Math.min(a.y, b.y), a.z + 0.5 + b.z + 0.5));
//
//		center = new Vec3d(reduce.x / structure.size(), reduce.y, reduce.z / structure.size());

		collide_bounds = null;
		getCollisions();
	}

	@Nullable
	public Entity getControllingPassenger() {
		List<Entity> list = this.getPassengers();
		return list.isEmpty() ? null : list.get(0);
	}

	public static class Entry {
		public static final Entry AIR = new Entry(Blocks.AIR.getDefaultState(), null, null, 0, 0, TileMovingBase.BLANK);
		public IBlockState state;
		public NBTTagCompound block;
		public NBTTagCompound desc;
		public int lightLevel = 0;
		public int lightOpacity = 0;
		public AxisAlignedBB[] collisions = TileMovingBase.BLANK;
		public int scheduledTickTime;
		public int scheduledTickPriority;


		public Entry(IBlockState state, NBTTagCompound block, NBTTagCompound desc, int lightLevel, int lightOpacity, AxisAlignedBB[] collisions, int scheduledTickTime, int scheduledTickPriority) {
			this.state = state;
			this.block = block;
			this.desc = desc;
			this.lightLevel = lightLevel;
			this.lightOpacity = lightOpacity;
			this.collisions = collisions;
			this.scheduledTickTime = scheduledTickTime;
			this.scheduledTickPriority = scheduledTickPriority;
		}

		public Entry(IBlockState state, NBTTagCompound block, NBTTagCompound desc, int lightLevel, int lightOpacity, AxisAlignedBB[] collisions) {
			this.state = state;

			this.block = block;
			this.desc = desc;
			this.lightLevel = lightLevel;
			this.lightOpacity = lightOpacity;
			this.collisions = collisions;
		}

		public Entry(PacketBuffer buffer) throws IOException {
			this(FLNetwork.readState(buffer),
					null,
					buffer.readCompoundTag(),
					buffer.readInt(),
					buffer.readInt(),
					FLNetwork.readAxisArray(buffer));
		}

		public Entry(NBTTagCompound tag) {
			this(
					NBTHelper.IBLOCKSTATE.fromNBT(tag.getCompoundTag("blockstate")),
					tag.getCompoundTag("block"),
					tag.getCompoundTag("desc"),
					tag.getInteger("lightLevel"),
					tag.getInteger("lightOpacity"),
					NBTHelper.deserializeArray(tag.getTagList("collisions", Constants.NBT.TAG_COMPOUND), NBTHelper.AXISALIGNEDBB, AxisAlignedBB[]::new),
					tag.getInteger("scheduledTickTime"),
					tag.getInteger("scheduledTickPriority")
			);
		}

		public NBTTagCompound getTag() {
			return NBTHelper.builder()
					.setTag("blockstate", NBTHelper.IBLOCKSTATE.toNBT(state))
					.setTag("block", block)
					.setTag("des", desc)
					.setInteger("lightLevel", lightLevel)
					.setInteger("lightOpacity", lightOpacity)
					.setTag("collisions", NBTHelper.serializeArray(collisions, NBTHelper.AXISALIGNEDBB))
					.setInteger("scheduledTickPriority", scheduledTickPriority)
					.setInteger("scheduledTickTime", scheduledTickTime)
					.build();
		}

		public void writeToPacketBuffer(PacketBuffer buffer) {
			FLNetwork.writeBlockState(buffer, state);
			buffer.writeCompoundTag(desc);
			buffer.writeInt(lightLevel);
			buffer.writeInt(lightOpacity);
			FLNetwork.writeAxisArray(collisions, buffer);
		}
	}

	public static class MessageCustomPositionSpeed implements IMessage {
		int entity_id;
		Vec3d position;
		Vec3d velocity;
		Vec3d rotation;

		public MessageCustomPositionSpeed(int entity_id, Vec3d position, Vec3d velocity, Vec3d rotation) {
			this.entity_id = entity_id;
			this.position = position;
			this.velocity = velocity;
			this.rotation = rotation;
		}

		public MessageCustomPositionSpeed() {

		}

		public MessageCustomPositionSpeed(EntityAirShip entityAirShip) {
			this(entityAirShip.getEntityId(), entityAirShip.position, entityAirShip.velocity, entityAirShip.rotation);
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			entity_id = buf.readInt();
			position = FLNetwork.readVec3d(buf);
			velocity = FLNetwork.readVec3d(buf);
			rotation = FLNetwork.readVec3d(buf);
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(entity_id);
			FLNetwork.writeVec3d(buf, position);
			FLNetwork.writeVec3d(buf, velocity);
			FLNetwork.writeVec3d(buf, rotation);
		}

		public static class ClientHandler implements IMessageHandler<MessageCustomPositionSpeed, IMessage> {
			@Override
			@SideOnly(Side.CLIENT)
			public IMessage onMessage(final MessageCustomPositionSpeed message, final MessageContext ctx) {
				Minecraft.getMinecraft().addScheduledTask(() -> {
					Entity entityByID = Minecraft.getMinecraft().world.getEntityByID(message.entity_id);
					if (entityByID instanceof EntityAirShip) {
						EntityAirShip airShip = (EntityAirShip) entityByID;
						Entity controllingPassenger = airShip.getControllingPassenger();
						if (controllingPassenger == Minecraft.getMinecraft().player) {
							if (airShip.position.distanceTo(message.position) < 8) {

							}
							return;
						}

						airShip.position = message.position;
						airShip.velocity = message.velocity;
						airShip.rotation = message.rotation;
					}
				});
				return null;
			}
		}

		public static class ServerHandler implements IMessageHandler<MessageCustomPositionSpeed, IMessage> {
			@Override
			public IMessage onMessage(final MessageCustomPositionSpeed message, final MessageContext ctx) {
				FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
					Minecraft.getMinecraft().addScheduledTask(() -> {
						Entity entityByID = ctx.getServerHandler().player.world.getEntityByID(message.entity_id);
						if (!(entityByID instanceof EntityAirShip)) {
							ctx.getServerHandler().disconnect(new TextComponentString("Invalid Entity ID"));
							return;
						}
						EntityAirShip airShip = (EntityAirShip) entityByID;

						Entity controllingPassenger = airShip.getControllingPassenger();
						if (controllingPassenger != ctx.getServerHandler().player) {
							return;
						}

						double v = airShip.position.distanceTo(message.position);

						airShip.position = message.position;
						airShip.velocity = message.velocity;
						airShip.rotation = message.rotation;
					});
				});
				return null;
			}
		}

	}
}
