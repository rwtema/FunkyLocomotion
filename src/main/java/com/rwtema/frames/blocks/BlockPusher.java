package com.rwtema.frames.blocks;

import com.rwtema.frames.groups.MoveManager;
import com.rwtema.frames.helper.BlockHelper;
import framesapi.BlockPos;
import framesapi.ISlipperyBlock;
import framesapi.IStickyBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockPusher extends Block implements ISlipperyBlock {
    IIcon icon;

    public BlockPusher() {
        super(Material.rock);
        this.setBlockName("newframes:pusher");
        this.setBlockTextureName("newframes:pusher");
    }

    @Override
    public void registerBlockIcons(IIconRegister p_149651_1_) {
        icon = p_149651_1_.registerIcon("newframes:pusherFront");
        super.registerBlockIcons(p_149651_1_);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
        if (!world.isRemote) {
            if (player.isSneaking()) {
                if (startMoving(world, x, y, z)) return false;
            } else
                world.setBlockMetadataWithNotify(x, y, z, side, 3);
        }
        return true;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random p_149674_5_) {
        super.updateTick(world, x, y, z, p_149674_5_);
        if (world.isBlockIndirectlyGettingPowered (x, y, z)) {
            startMoving(world, x, y, z);
        }

    }

    private boolean startMoving(World world, int x, int y, int z) {
        ForgeDirection dir = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));
        if (dir == ForgeDirection.UNKNOWN)
            return true;

        List<BlockPos> posList = getBlocks(world, x, y, z, dir);
        if (posList != null)
            MoveManager.startMoving(world, posList, dir);
        return false;
    }

    private List<BlockPos> getBlocks(World world, int x, int y, int z, ForgeDirection dir) {
        ArrayList<BlockPos> posList = new ArrayList<BlockPos>();
        ArrayList<BlockPos> toIterate = new ArrayList<BlockPos>();
        BlockPos home = new BlockPos(x, y, z);

        if (BlockHelper.canStick(world, home.advance(dir), dir.getOpposite()))
            toIterate.add(home.advance(dir));

        for (int i = 0; i < toIterate.size(); i++) {
            BlockPos pos = toIterate.get(i);

            posList.add(pos);

            Block b = BlockHelper.getBlock(world, pos);
            if (b instanceof IStickyBlock) {
                final IStickyBlock stickyBlock = (IStickyBlock) b;
                for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                    if (stickyBlock.isStickySide(world, pos, side)) {
                        BlockPos newPos = pos.advance(side);

                        if (home.equals(newPos))
                            continue;

                        if (toIterate.contains(newPos))
                            continue;

                        if (BlockHelper.canStick(world, newPos, side.getOpposite()))
                            toIterate.add(pos.advance(side));
                    }
                }
            }
        }

        for (BlockPos pos : posList) {
            BlockPos adv = pos.advance(dir);
            if (!posList.contains(adv) && !BlockHelper.canReplace(world, adv)) {
                return null;
            }
        }

        return posList;
    }


    @Override
    public IIcon getIcon(int p_149691_1_, int p_149691_2_) {
        if (p_149691_1_ == p_149691_2_)
            return icon;
        return super.getIcon(p_149691_1_, p_149691_2_);
    }

    @Override
    public boolean canStickTo(World world, BlockPos pos, ForgeDirection dir) {
        return dir != ForgeDirection.UNKNOWN && world.getBlockMetadata(pos.x, pos.y, pos.z) != dir.ordinal();
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (world.isBlockIndirectlyGettingPowered (x, y, z)) {
            world.scheduleBlockUpdate(x, y, z, this, 4);
        }
        super.onNeighborBlockChange(world, x, y, z, block);
    }
}
