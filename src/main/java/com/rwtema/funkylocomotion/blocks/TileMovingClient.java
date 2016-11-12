package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.description.Describer;
import com.rwtema.funkylocomotion.fakes.FakeWorldClient;
import com.rwtema.funkylocomotion.helper.BlockHelper;
import com.rwtema.funkylocomotion.rendering.ChunkRerenderer;
import com.rwtema.funkylocomotion.rendering.PassHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;

public class TileMovingClient extends TileMovingBase {
	public static final HashMap<BlockPos, WeakReference<TileEntity>> cachedTiles = new HashMap<>();
	public static final HashSet<Class> renderBlackList = new HashSet<>();
	public static final HashSet<Class> renderErrorList = new HashSet<>();
	public final boolean[] skipPass = new boolean[2];
	public Block block = Blocks.AIR;
	public int meta = 0;
	public TileEntity tile = null;
	public boolean render;
	public boolean error = false;
	public boolean rawTile = false;
	public boolean init = false;

	public TileMovingClient() {
		super(Side.CLIENT);
	}

	@Override
	public void update() {
		if (time < maxTime) {
			super.update();
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (rawTile && tile != null)
			tile.invalidate();
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		block = Block.getBlockById(tag.getInteger("Block"));
		meta = tag.getInteger("Meta");

		time = tag.getInteger("Time");
		maxTime = tag.getInteger("MaxTime");

		lightLevel = tag.getInteger("Light");
		lightOpacity = tag.getShort("Opacity");

		isAir = block == Blocks.AIR;

		if (tag.hasKey("Collisions", 9)) {
			collisions = AxisTags(tag.getTagList("Collisions", 10));
		}

//		BlockHelper.postUpdateBlock(worldObj, pos);

		dir = tag.getByte("Dir");

		TileEntity tile = null;

		if (this.dir >= 0 && this.dir < 8 && this.dir != 6) {
			EnumFacing d = getDir();

			WeakReference<TileEntity> ref = cachedTiles.remove(d != null ? pos.offset(d, -1) : pos);

//			if (ref != null)
//				tile = ref.get();
		}

		if (tile != null && FakeWorldClient.isValid(worldObj) && tile.getWorld() == this.worldObj) {
			rawTile = true;
			tile.setPos(pos.toImmutable());
			tile.setWorldObj(FakeWorldClient.getFakeWorldWrapper(this.worldObj));
			tile.updateContainingBlockInfo();
			this.tile = tile;
			render = true;
		} else {
			render = !tag.getBoolean("DNR");

			if (render) {
				this.tile = Describer.recreateTileEntity(tag, getState(), pos, FakeWorldClient.getFakeWorldWrapper(this.worldObj));
			}
		}


		if (checkClass(this.block) || checkClass(this.tile))
			this.tile = null;

		if (render && !init)
			ChunkRerenderer.markBlock(pos);

		init = true;

		worldObj.markBlockRangeForRenderUpdate(pos, pos);
	}

	public boolean checkClass(Object o) {
		if (o == null)
			return false;
		if (renderBlackList.contains(o.getClass())) {
			this.render = false;
			return true;
		}

		if (renderErrorList.contains(o.getClass())) {
			this.render = false;
			this.error = true;
			return true;
		}

		return false;
	}

	@Nonnull
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {

		AxisAlignedBB other;
		if (tile != null)
			other = tile.getRenderBoundingBox();
		else
			other = getState().getCollisionBoundingBox(FakeWorldClient.getFakeWorldWrapper(worldObj), pos);

		if (other == null)
			other = Block.FULL_BLOCK_AABB;
		else
			other = other.union(Block.FULL_BLOCK_AABB);

		other = other.addCoord(pos.getX(), pos.getY(), pos.getZ());

		double h = offset(true);
		EnumFacing d = getDir();
		return h != 0 && d != null ? other.offset(h * d.getFrontOffsetX(), h * d.getFrontOffsetY(), h * d.getFrontOffsetZ()) : other;
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		if (maxTime == 0)
			return false;

		if (!render || error)
			return pass == 0;

		IBlockState state = getState();

		if (block == Blocks.AIR || (tile == null && block.getRenderType(state) == EnumBlockRenderType.INVISIBLE))
			return false;

		if (tile != null) {
			if (tile.shouldRenderInPass(pass))
				return true;
		}

		for (BlockRenderLayer layer : PassHandler.getHandler(pass).layers) {
			if (block.canRenderInLayer(state, layer)) {
				return true;
			}
		}

		return false;
	}

	public IBlockState getState() {
		return block.getStateFromMeta(meta);
	}
}