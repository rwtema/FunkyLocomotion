package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.helper.BlockHelper;
import com.rwtema.funkylocomotion.helper.ItemHelper;
import com.rwtema.funkylocomotion.movers.MoveManager;
import framesapi.BlockPos;
import framesapi.ISlipperyBlock;
import framesapi.IStickyBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockPusher extends Block implements ISlipperyBlock {
    IIcon iconFront;
    IIcon iconSide;
    IIcon iconSide2;
    IIcon iconFront2;

    public BlockPusher() {
        super(Material.rock);
        this.setBlockName("funkylocomotion:pusher");
        this.setBlockTextureName("funkylocomotion:pusher");
        this.setCreativeTab(FunkyLocomotion.creativeTabFrames);
    }

    @Override
    public void getSubBlocks(Item p_149666_1_, CreativeTabs p_149666_2_, List p_149666_3_) {
        p_149666_3_.add(new ItemStack(p_149666_1_, 1, 0));
        p_149666_3_.add(new ItemStack(p_149666_1_, 1, 6));
    }

    @Override
    public int damageDropped(int meta) {
        return meta < 6 ? 0 : 6;
    }

    @Override
    public void registerBlockIcons(IIconRegister p_149651_1_) {
        iconFront = p_149651_1_.registerIcon("funkylocomotion:pusherFront");
        iconFront2 = p_149651_1_.registerIcon("funkylocomotion:pullerFront");
        iconSide = p_149651_1_.registerIcon("funkylocomotion:pusherSide");
        iconSide2 = p_149651_1_.registerIcon("funkylocomotion:pullerSide");
        super.registerBlockIcons(p_149651_1_);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
        if (!world.isRemote) {
            if (player.isSneaking()) {
                if (startMoving(world, x, y, z)) return false;
            } else {
                ItemStack item = player.getHeldItem();
                if (!(ItemHelper.isWrench(item)))
                    return false;

                final int meta = world.getBlockMetadata(x, y, z);
                if ((meta < 6 ? 0 : 6) + side == meta)
                    side = Facing.oppositeSide[side];


                world.setBlockMetadataWithNotify(x, y, z, (meta < 6 ? 0 : 6) + side, 3);
            }
        }
        return true;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random p_149674_5_) {
        super.updateTick(world, x, y, z, p_149674_5_);
        if (world.isBlockIndirectlyGettingPowered(x, y, z)) {
            startMoving(world, x, y, z);
        }

    }

    private boolean startMoving(World world, int x, int y, int z) {
        final int meta = world.getBlockMetadata(x, y, z);
        ForgeDirection dir = ForgeDirection.getOrientation(meta % 6).getOpposite();
        boolean push = meta < 6;
        if (dir == ForgeDirection.UNKNOWN)
            return true;

        List<BlockPos> posList = getBlocks(world, x, y, z, dir, push);
        if (posList != null)
            MoveManager.startMoving(world, posList, push ? dir : dir.getOpposite());
        return false;
    }

    private List<BlockPos> getBlocks(World world, int x, int y, int z, ForgeDirection dir, boolean push) {
        ArrayList<BlockPos> posList = new ArrayList<BlockPos>();
        ArrayList<BlockPos> toIterate = new ArrayList<BlockPos>();
        BlockPos home = new BlockPos(x, y, z);

        BlockPos advance = home.advance(dir);

        if (push) {
            if (BlockHelper.canStick(world, advance, dir.getOpposite()))
                toIterate.add(advance);
        } else {
            if (!world.isAirBlock(advance.x, advance.y, advance.z))
                return null;

            if (BlockHelper.canStick(world, advance.advance(dir), dir.getOpposite()))
                toIterate.add(advance.advance(dir));

            dir = dir.getOpposite();
        }

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
        final int dir = Facing.oppositeSide[p_149691_2_ % 6];
        return p_149691_1_ == dir ? p_149691_2_ >= 6 ? iconFront2 : iconFront : p_149691_1_ == Facing.oppositeSide[dir] ? blockIcon : p_149691_2_ >= 6 ? iconSide2 : iconSide;
    }

    @Override
    public int getRenderType() {
        return FunkyLocomotion.proxy.pusherRendererId;
    }

    @Override
    public boolean canStickTo(World world, BlockPos pos, ForgeDirection dir) {
//        return true;
        return dir != ForgeDirection.UNKNOWN && world.getBlockMetadata(pos.x, pos.y, pos.z) != dir.getOpposite().ordinal();
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (world.isBlockIndirectlyGettingPowered(x, y, z)) {
            world.scheduleBlockUpdate(x, y, z, this, 4);
        }
        super.onNeighborBlockChange(world, x, y, z, block);
    }
}
