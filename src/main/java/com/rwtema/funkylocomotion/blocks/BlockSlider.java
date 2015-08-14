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
    public void registerBlockIcons(IIconRegister register) {
        iconSlider = register.registerIcon("funkylocomotion:sliderSide");
        iconSliderPush = register.registerIcon("funkylocomotion:sliderSideFront");
        iconSlider0 = register.registerIcon("funkylocomotion:sliderSide0");
        iconSlider1 = register.registerIcon("funkylocomotion:sliderSide1");
        iconSliderFront = register.registerIcon("funkylocomotion:sliderFront");
        super.registerBlockIcons(register);
    }

    @Override
    public int damageDropped(int meta) {
        return 0;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return renderSide != 6 && (renderSide == -1 || (side == renderSide || side == Facing.oppositeSide[renderSide])) &&
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

    @SuppressWarnings("unchecked")
    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
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
    public IIcon getIcon(int side, int meta) {
        int dir = Facing.oppositeSide[meta % 6];
        return (side == dir) ? iconSliderFront : ((side == Facing.oppositeSide[dir]) ? blockIcon : iconSlider0);
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
