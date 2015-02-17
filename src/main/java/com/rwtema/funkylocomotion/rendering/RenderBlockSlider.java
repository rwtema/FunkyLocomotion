package com.rwtema.funkylocomotion.rendering;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.blocks.BlockSlider;
import com.rwtema.funkylocomotion.blocks.TileSlider;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

public class RenderBlockSlider extends RenderBlockPusher {


    @Override
    public void renderInventoryBlock(Block block, int meta, int modelId, RenderBlocks renderer) {
        meta = 0;
        int slide = 3;
        int orth = 4;

        block.setBlockBoundsForItemRender();
        renderer.setRenderBoundsFromBlock(block);


        GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

        setRotations(renderer, Facing.oppositeSide[meta % 6]);
        for (int s = 0; s < 6; s++) {
            if (s == slide)
                FLRenderHelper.renderItemFace(BlockSlider.iconSliderPush, s, renderer);
            else if (Facing.oppositeSide[s] == slide)
                FLRenderHelper.renderItemFace(BlockSlider.iconSlider, s, renderer);
            else
                FLRenderHelper.renderItemFace(block, meta, s, renderer);

        }
        resetRotations(renderer);

        setRotations(renderer, slide);
        FLRenderHelper.renderItemFace(BlockSlider.iconSlider1, orth, renderer);
        FLRenderHelper.renderItemFace(BlockSlider.iconSlider1, Facing.oppositeSide[orth], renderer);
        resetRotations(renderer);

        GL11.glTranslatef(0.5F, 0.5F, 0.5F);


    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        boolean flag = super.renderWorldBlock(world, x, y, z, block, modelId, renderer);
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile == null || tile.getClass() != TileSlider.class)
            return flag;

        renderer.renderAllFaces = false;

        ForgeDirection dir = ((TileSlider) tile).getFacing();
        ForgeDirection arrowDir = ((TileSlider) tile).getSlideDir();
        BlockSlider.renderSide = TileSlider.getOrthogonal(arrowDir, dir).ordinal();
        renderer.setOverrideBlockTexture(BlockSlider.iconSlider1);
        setRotations(renderer, arrowDir.ordinal());
        flag = flag | renderer.renderStandardBlock(block, x, y, z);
        resetRotations(renderer);
        renderer.clearOverrideBlockTexture();
        BlockSlider.renderSide = -1;

        return flag;

    }

    @Override
    public void setRotations(RenderBlocks renderer, int meta) {
        super.setRotations(renderer, meta);
    }

    @Override
    public int getRenderId() {
        return FunkyLocomotion.proxy.sliderRendererId;
    }
}
