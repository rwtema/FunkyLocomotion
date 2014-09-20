package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.helper.ItemHelper;
import framesapi.BlockPos;
import framesapi.ISlipperyBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public class BlockPusher extends Block implements ISlipperyBlock {
    public static IIcon iconFront;
    public static IIcon iconSide;
    public static IIcon iconSide2;
    public static IIcon iconFront2;

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
            ItemStack item = player.getHeldItem();
            if (!(ItemHelper.isWrench(item)))
                return false;

            final int meta = world.getBlockMetadata(x, y, z);
            if ((meta < 6 ? 0 : 6) + side == meta)
                side = Facing.oppositeSide[side];

            world.setBlockMetadataWithNotify(x, y, z, (meta < 6 ? 0 : 6) + side, 3);
        }
        return true;
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
        return dir != ForgeDirection.UNKNOWN && (world.getBlockMetadata(pos.x, pos.y, pos.z) % 6) != dir.getOpposite().ordinal();
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TilePusher))
            return;

        TilePusher tilePush = (TilePusher) tile;

        tilePush.powered = world.isBlockIndirectlyGettingPowered(x, y, z);

        if (tilePush.countDown == 0)
            tilePush.countDown = 5;

        super.onNeighborBlockChange(world, x, y, z, block);
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return false;
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TilePusher();
    }
}
