package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.fakes.FakeWorldClient;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;
import java.util.Random;

public class BlockMoving extends Block {
    public static BlockMoving instance;

    public static IIcon crate;
    public static IIcon crate_error;

    public BlockMoving() {
        super(Material.rock);
        this.setBlockUnbreakable();
        this.setBlockName("funkylocomotion:moving");
        this.setBlockTextureName("funkylocomotion:crate");
        instance = this;
    }

    public static boolean _Immoveable() {
        return true;
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axis, List list, Entity entity) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileMovingBase))
            return;

        for (AxisAlignedBB bb : ((TileMovingBase) tile).getTransformedColisions())
            if (axis.intersectsWith(bb))
                list.add(bb);

        ForgeDirection d = ((TileMovingBase) tile).dir;
        TileEntity tile2 = world.getTileEntity(x + d.offsetX, y + d.offsetY, z + d.offsetZ);
        if (!(tile2 instanceof TileMovingBase))
            return;

        for (AxisAlignedBB bb : ((TileMovingBase) tile2).getTransformedColisions())
            if (axis.intersectsWith(bb))
                list.add(bb);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        this.setBlockBounds(0, 0, 0, 1, 1, 1);
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileMovingBase) {
            if (((TileMovingBase) tile).isAir)
                this.setBlockBounds(0, 0, 0, 0, 0, 0);
        }
//        if (world instanceof World) {
//            TileEntity tile = world.getTileEntity(x, y, z);
//            if (tile instanceof TileMovingBase) {
//                AxisAlignedBB bb = ((TileMovingBase) tile).getCombinedCollisions();
//
//                if (bb != null) {
//                    bb = bb;
//                    this.setBlockBounds((float) bb.minX, (float) bb.minY, (float) bb.minZ,
//                            (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ);
//                }
//            }
//
//        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
//        TileEntity tile = world.getTileEntity(x, y, z);
//        return tile instanceof TileMovingBase ? ((TileMovingBase) tile).getCombinedCollisions().offset(x, y, z) : null;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        return super.getSelectedBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public void registerBlockIcons(IIconRegister p_149651_1_) {
        crate = this.blockIcon = p_149651_1_.registerIcon(this.getTextureName());
        crate_error = p_149651_1_.registerIcon("funkylocomotion:crate_error");
    }

    @Override
    public void onEntityCollidedWithBlock(World p_149670_1_, int p_149670_2_, int p_149670_3_, int p_149670_4_, Entity p_149670_5_) {
        super.onEntityCollidedWithBlock(p_149670_1_, p_149670_2_, p_149670_3_, p_149670_4_, p_149670_5_);
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        if (world.isRemote || FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            return new TileMovingClient();
        else
            return new TileMovingServer();
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileMovingBase ? ((TileMovingBase) tile).lightLevel : super.getLightValue(world, x, y, z);
    }

    @Override
    public boolean isBlockSolid(IBlockAccess p_149747_1_, int p_149747_2_, int p_149747_3_, int p_149747_4_, int p_149747_5_) {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileMovingBase ? ((TileMovingBase) tile).lightOpacity : super.getLightOpacity(world, x, y, z);
    }

    @Override
    public int getLightOpacity() {
        return super.getLightOpacity();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote)
            return true;

        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileMovingServer) {
            ((TileMovingServer) tile).cacheActivate(player, side, hitX, hitY, hitZ);
        }

        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileMovingClient) {
            TileMovingClient mover = (TileMovingClient) tile;
            FakeWorldClient fakeWorld = FakeWorldClient.getFakeWorldWrapper(world);
            fakeWorld.offset = mover.offset(true);
            fakeWorld.dir = mover.dir;
            mover.block.randomDisplayTick(fakeWorld, mover.xCoord, mover.yCoord, mover.zCoord, rand);
            fakeWorld.offset = 0;
        }
    }

    @Override
    public boolean isNormalCube(IBlockAccess world, int x, int y, int z) {
        return false;
    }
}
