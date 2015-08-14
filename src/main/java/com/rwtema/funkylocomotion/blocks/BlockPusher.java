package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.helper.ItemHelper;
import com.rwtema.funkylocomotion.movers.MoverEventHandler;
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
        this.setHardness(1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 6));
    }

    @Override
    public int damageDropped(int meta) {
        return meta < 6 ? 0 : 6;
    }

    @Override
    public void registerBlockIcons(IIconRegister register) {
        iconFront = register.registerIcon("funkylocomotion:pusherFront");
        iconFront2 = register.registerIcon("funkylocomotion:pullerFront");
        iconSide = register.registerIcon("funkylocomotion:pusherSide");
        iconSide2 = register.registerIcon("funkylocomotion:pullerSide");
        super.registerBlockIcons(register);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
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
    public IIcon getIcon(int side, int meta) {
        final int dir = Facing.oppositeSide[meta % 6];
        return side == dir ? meta >= 6 ? iconFront2 : iconFront : side == Facing.oppositeSide[dir] ? blockIcon : meta >= 6 ? iconSide2 : iconSide;
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

        if (tilePush.powered)
            MoverEventHandler.registerMover(tilePush);

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
