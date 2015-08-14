package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.helper.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public class BlockStickyFrame extends BlockFrame {
    public static final BlockStickyFrame[] blocks = new BlockStickyFrame[4];
    public final int index;
    IIcon filled;

    public BlockStickyFrame(int index) {
        super();
        this.index = index * 16;
        blocks[index] = this;
        this.setBlockName("funkylocomotion:frame");
        if (index == 0)
            this.setCreativeTab(FunkyLocomotion.creativeTabFrames);
        this.setLightOpacity(0);
    }

    @Override
    public boolean isStickySide(World world, int x, int y, int z, ForgeDirection side) {
        return isMetaSticky(world.getBlockMetadata(x, y, z), side);
    }

    public boolean isMetaSticky(int meta, ForgeDirection side) {
        return ((index + meta) & (1 << side.ordinal())) == 0;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return ((index + meta) & (1 << side)) != 0 ? filled : blockIcon;
    }

    @Override
    public void registerBlockIcons(IIconRegister register) {
        filled = register.registerIcon("funkylocomotion:frame_closed");
        super.registerBlockIcons(register);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        ItemStack item = player.getHeldItem();
        if (!(ItemHelper.isWrench(item)))
            return false;

        int i = (index + world.getBlockMetadata(x, y, z)) ^ (1 << side);

        if (i > 63 || i < 0)
            i = 0;

        int meta = i % 16;
        Block block = blocks[(i - meta) / 16];

        world.setBlock(x, y, z, block, meta, 2);
        return true;
    }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }
}
