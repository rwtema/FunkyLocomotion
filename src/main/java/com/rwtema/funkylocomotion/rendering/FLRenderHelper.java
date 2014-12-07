package com.rwtema.funkylocomotion.rendering;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

@SideOnly(Side.CLIENT)
public class FLRenderHelper {
    public static void renderItemFace(Block b, int meta, int side, RenderBlocks renderBlocks) {
        renderItemFace(renderBlocks.getBlockIconFromSideAndMetadata(b, side, meta), side, renderBlocks);
    }

    public static void renderItemFace(IIcon icon, int side, RenderBlocks renderBlocks) {
        ForgeDirection s = ForgeDirection.getOrientation(side);
        Tessellator tes = Tessellator.instance;
        tes.startDrawingQuads();
        tes.setNormal(s.offsetX, s.offsetY, s.offsetZ);
        renderFace(0, 0, 0, icon, side, renderBlocks);
        tes.draw();
    }

    public static void clearTessellator() {
        if (Tessellator.instance.isDrawing) {
            Tessellator.instance.draw();
        }
    }


    public static void renderFace(int x, int y, int z, IIcon icon, int side, RenderBlocks renderBlocks) {
        Blocks.stone.setBlockBounds(0, 0, 0, 1, 1, 1);
        switch (side) {
            case 0:
                renderBlocks.renderFaceYNeg(Blocks.stone, x, y, z, icon);
                break;
            case 1:
                renderBlocks.renderFaceYPos(Blocks.stone, x, y, z, icon);
                break;
            case 2:
                renderBlocks.renderFaceZNeg(Blocks.stone, x, y, z, icon);
                break;
            case 3:
                renderBlocks.renderFaceZPos(Blocks.stone, x, y, z, icon);
                break;
            case 4:
                renderBlocks.renderFaceXNeg(Blocks.stone, x, y, z, icon);
                break;
            case 5:
                renderBlocks.renderFaceXPos(Blocks.stone, x, y, z, icon);
                break;
            default:
        }

    }

    public static void renderBlock(Block block, int meta, RenderBlocks renderer, int renderFlag) {
        Tessellator tessellator = Tessellator.instance;
        RenderBlocks.getInstance();
        if ((renderFlag & (1 << 0)) == 0) {
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, -1.0F, 0.0F);
            renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 0, meta));
            tessellator.draw();
        }
        if ((renderFlag & (1 << 1)) == 0) {
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 1.0F, 0.0F);
            renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 1, meta));
            tessellator.draw();
        }
        if ((renderFlag & (1 << 2)) == 0) {
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, -1.0F);
            renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 2, meta));
            tessellator.draw();
        }
        if ((renderFlag & (1 << 3)) == 0) {
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, 1.0F);
            renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 3, meta));
            tessellator.draw();
        }
        if ((renderFlag & (1 << 4)) == 0) {
            tessellator.startDrawingQuads();
            tessellator.setNormal(-1.0F, 0.0F, 0.0F);
            renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 4, meta));
            tessellator.draw();
        }
        if ((renderFlag & (1 << 5)) == 0) {
            tessellator.startDrawingQuads();
            tessellator.setNormal(1.0F, 0.0F, 0.0F);
            renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 5, meta));
            tessellator.draw();
        }
    }

}
