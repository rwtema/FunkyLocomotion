package com.rwtema.funkylocomotion.items;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.blocks.BlockStickyFrame;
import com.rwtema.funkylocomotion.movers.IMover;
import com.rwtema.funkylocomotion.movers.MoverEventHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class ItemWrench extends Item {
	public static final int metaWrenchNormal = 0;
	public static final int metaWrenchEye = 1;
	public static final int metaWrenchHammer = 2;

	public ItemWrench() {
		super();
		this.setMaxStackSize(1);
		this.setTextureName("funkylocomotion:wrench");
		this.setUnlocalizedName("funkylocomotion:wrench");
		this.setCreativeTab(FunkyLocomotion.creativeTabFrames);
		this.setHasSubtypes(true);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 1));
		list.add(new ItemStack(item, 1, 2));
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public IIcon iconWrenchHammer;
	@SideOnly(Side.CLIENT)
	public IIcon iconWrenchEye;
	@SideOnly(Side.CLIENT)
	public IIcon iconWrenchEye_base;
	@SideOnly(Side.CLIENT)
	public IIcon iconWrenchEye_pupil;
	@SideOnly(Side.CLIENT)
	public IIcon iconWrenchEye_outline;


	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		super.registerIcons(register);
		iconWrenchHammer = register.registerIcon("funkylocomotion:wrench_hammer");
		iconWrenchEye = register.registerIcon("funkylocomotion:wrench_eye");
		iconWrenchEye_base = register.registerIcon("funkylocomotion:wrench_eye_base");
		iconWrenchEye_pupil = register.registerIcon("funkylocomotion:wrench_eye_pupil");
		iconWrenchEye_outline = register.registerIcon("funkylocomotion:wrench_eye_outline");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta) {
		if (meta == metaWrenchEye) return iconWrenchEye;
		if (meta == metaWrenchHammer) return iconWrenchHammer;
		return super.getIconFromDamage(meta);
	}

	@Override
	public boolean onItemUseFirst(ItemStack item, EntityPlayer player, World world, int x, int y, int z, int face, float hitX, float hitY, float hitZ) {
		if (item.getItemDamage() != metaWrenchHammer)
			return false;

		if (face < 0 || face >= 6)
			return false;

		if (world.isRemote) {
			FunkyLocomotion.proxy.sendUsePacket(x, y, z, face, item, hitX, hitY, hitZ);
		}

		int d1 = offsetDir1[face];
		int d2 = offsetDir2[face];

		Block block = world.getBlock(x, y, z);

		if (block.isAir(world, x, y, z))
			return false;

		if (block instanceof BlockStickyFrame) {
			int open = ((BlockStickyFrame) block).index + world.getBlockMetadata(x, y, z) & 1 << face;

			Block otherBlock;
			for (int i = -2; i <= 2; i++) {
				for (int j = -2; j <= 2; j++) {
					int dx = x + Facing.offsetsXForSide[d1] * i + Facing.offsetsXForSide[d2] * j;
					int dy = y + Facing.offsetsYForSide[d1] * i + Facing.offsetsYForSide[d2] * j;
					int dz = z + Facing.offsetsZForSide[d1] * i + Facing.offsetsZForSide[d2] * j;

					int dx2 = dx + Facing.offsetsXForSide[face];
					int dy2 = dy + Facing.offsetsYForSide[face];
					int dz2 = dz + Facing.offsetsZForSide[face];

					Block blockingBlock = world.getBlock(dx2, dy2, dz2);

					if (!blockingBlock.isAir(world, dx2, dy2, dz2)) {
						if (blockingBlock.getCollisionBoundingBoxFromPool(world, dx2, dy2, dz2) != null && block.canCollideCheck(world.getBlockMetadata(dx2, dy2, dz2), false)) {
							if (block.collisionRayTrace(world, dx2, dy2, dz2,
									Vec3.createVectorHelper(
											dx2 + (face == 4 ? -0.1 : face == 5 ? 1.1 : hitX),
											dy2 + (face == 0 ? -0.1 : face == 1 ? 1.1 : hitY),
											dz2 + (face == 2 ? -0.1 : face == 3 ? 1.1 : hitZ)
									),
									Vec3.createVectorHelper(
											dx2 + (face == 4 ? 1.1 : face == 5 ? -0.1 : hitX),
											dy2 + (face == 0 ? 1.1 : face == 1 ? -0.1 : hitY),
											dz2 + (face == 2 ? 1.1 : face == 3 ? -0.1 : hitZ)
									)
							) != null) {
								continue;
							}
						}
					}

					if (i == 0 && j == 0) {
						block.onBlockActivated(world, dx, dy, dz, player, face, hitX, hitY, hitZ);
					} else {
						otherBlock = world.getBlock(dx, dy, dz);
						if (otherBlock instanceof BlockStickyFrame && ((((BlockStickyFrame) otherBlock).index + world.getBlockMetadata(dx, dy, dz)) & (1 << face)) == open) {
							otherBlock.onBlockActivated(world, dx, dy, dz, player, face, hitX, hitY, hitZ);
						}
					}
				}
			}


		} else {
			int meta = world.getBlockMetadata(x, y, z);

			for (int i = -2; i <= 2; i++) {
				for (int j = -2; j <= 2; j++) {
					int dx = x + Facing.offsetsXForSide[d1] * i + Facing.offsetsXForSide[d2] * j;
					int dy = y + Facing.offsetsYForSide[d1] * i + Facing.offsetsYForSide[d2] * j;
					int dz = z + Facing.offsetsZForSide[d1] * i + Facing.offsetsZForSide[d2] * j;

					Block testBlock = world.getBlock(dx, dy, dz);

					if(testBlock.isAir(world,dx,dy,dz))
						continue;

					if (i != 0 || j != 0) {
						if (isInaccessible(world, face ^ 1, hitX, hitY, hitZ, block, dx, dy, dz, testBlock)) {
							int dx2 = dx + Facing.offsetsXForSide[face];
							int dy2 = dy + Facing.offsetsYForSide[face];
							int dz2 = dz + Facing.offsetsZForSide[face];

							Block blockingBlock = world.getBlock(dx2, dy2, dz2);

							if (isInaccessible(world, face, hitX, hitY, hitZ, block, dx2, dy2, dz2, blockingBlock))
								continue;
						}
					}

					if (testBlock == block && world.getBlockMetadata(dx, dy, dz) == meta)
						block.onBlockActivated(world, dx, dy, dz, player, face, hitX, hitY, hitZ);
				}
			}
		}
		return true;
	}

	private boolean isInaccessible(World world, int face, float hitX, float hitY, float hitZ, Block block, int dx2, int dy2, int dz2, Block blockingBlock) {
		return !blockingBlock.isAir(world, dx2, dy2, dz2) &&
				blockingBlock.getCollisionBoundingBoxFromPool(world, dx2, dy2, dz2) != null &&
				block.canCollideCheck(world.getBlockMetadata(dx2, dy2, dz2), false) &&
				block.collisionRayTrace(world, dx2, dy2, dz2,
						Vec3.createVectorHelper(
								dx2 + (face == 4 ? -0.1 : face == 5 ? 1.1 : hitX),
								dy2 + (face == 0 ? -0.1 : face == 1 ? 1.1 : hitY),
								dz2 + (face == 2 ? -0.1 : face == 3 ? 1.1 : hitZ)),
						Vec3.createVectorHelper(
								dx2 + (face == 4 ? 0.1 : face == 5 ? 0.9 : hitX),
								dy2 + (face == 0 ? 0.1 : face == 1 ? 0.9 : hitY),
								dz2 + (face == 2 ? 0.1 : face == 3 ? 0.9 : hitZ))
				) != null;
	}

	private final static int[] offsetDir1 = new int[]{2, 3, 0, 1, 0, 1};
	private final static int[] offsetDir2 = new int[]{4, 5, 4, 5, 2, 3};

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, int X, int Y, int Z, EntityPlayer player) {
		if (!player.worldObj.isRemote) {
			TileEntity tileEntity = player.worldObj.getTileEntity(X, Y, Z);
			if (tileEntity instanceof IMover) {
				MoverEventHandler.registerMover((IMover) tileEntity);
			}
		}
		return true;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		if (itemstack.getItemDamage() == metaWrenchEye)
			return "item.funkylocomotion:wrench_eye";
		else
			return super.getUnlocalizedName(itemstack);
	}

	@SubscribeEvent
	public void leftClick(PlayerInteractEvent event) {
		if (event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) return;
		if (event.entityPlayer.getHeldItem() == null || event.entityPlayer.getHeldItem().getItem() != this) return;
		if (!event.world.isRemote) {
			TileEntity tileEntity = event.world.getTileEntity(event.x, event.y, event.z);
			if (tileEntity instanceof IMover) {
				MoverEventHandler.registerMover((IMover) tileEntity);
			}
		}
		event.setCanceled(true);
	}

	@Override
	public float getDigSpeed(ItemStack itemstack, Block block, int metadata) {
		return 0;
	}
}
