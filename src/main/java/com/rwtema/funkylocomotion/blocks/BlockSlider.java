package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.helper.ItemHelper;
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

public class BlockSlider extends BlockPusher {
    public static IIcon iconSlider0;
    public static IIcon iconSlider1;
    public static IIcon iconSlider;
    public static IIcon iconSliderPush;
    public static IIcon iconSliderFront;
    public static int renderSide = -1;

    public BlockSlider() {
        super();
        this.setBlockName("funkylocomotion:slider");
    }

    @Override
    public void registerBlockIcons(IIconRegister p_149651_1_) {
        iconSlider = p_149651_1_.registerIcon("funkylocomotion:sliderSide");
        iconSliderPush = p_149651_1_.registerIcon("funkylocomotion:sliderSideFront");
        iconSlider0 = p_149651_1_.registerIcon("funkylocomotion:sliderSide0");
        iconSlider1 = p_149651_1_.registerIcon("funkylocomotion:sliderSide1");
        iconSliderFront = p_149651_1_.registerIcon("funkylocomotion:sliderFront");
        super.registerBlockIcons(p_149651_1_);
    }

    @Override
    public int damageDropped(int meta) {
        return 0;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return (renderSide == -1 || (side == renderSide || side == Facing.oppositeSide[renderSide])) &&
                super.shouldSideBeRendered(world, x, y, z, side);
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile != null && tile.getClass() == TileSlider.class) {
            ForgeDirection slideDir = ((TileSlider) tile).getSlideDir();

            if (slideDir.ordinal() == side)
                return iconSliderPush;
            else if (slideDir.getOpposite().ordinal() == side)
                return iconSlider;
        }

        return super.getIcon(world, x, y, z, side);


    }

    @Override
    public void getSubBlocks(Item p_149666_1_, CreativeTabs p_149666_2_, List p_149666_3_) {
        p_149666_3_.add(new ItemStack(p_149666_1_, 1, 0));
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
        if (!world.isRemote) {
            ItemStack item = player.getHeldItem();
            if (!(ItemHelper.isWrench(item)))
                return false;

            final int meta = world.getBlockMetadata(x, y, z);
            TileEntity tile = world.getTileEntity(x, y, z);
            if (player.isSneaking()) {
                if (tile != null && tile.getClass() == TileSlider.class) {
                    ((TileSlider) tile).rotateAboutAxis();
                    world.markBlockForUpdate(x, y, z);
                }
            } else {

                if (side == meta)
                    side = Facing.oppositeSide[side];

                world.setBlockMetadataWithNotify(x, y, z, (meta < 6 ? 0 : 6) + side, 3);

                if (tile != null && tile.getClass() == TileSlider.class) {
                    ((TileSlider) tile).getSlideDir();
                    world.markBlockForUpdate(x, y, z);
                }
            }


        }
        return true;
    }

    @Override
    public IIcon getIcon(int p_149691_1_, int p_149691_2_) {
        int dir = Facing.oppositeSide[p_149691_2_ % 6];
        return (p_149691_1_ == dir) ? iconSliderFront : ((p_149691_1_ == Facing.oppositeSide[dir]) ? blockIcon : iconSlider0);
    }


    @Override
    public int getRenderType() {
        return FunkyLocomotion.proxy.sliderRendererId;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileSlider();
    }


}
