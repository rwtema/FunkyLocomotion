package com.rwtema.funkylocomotion.rendering;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.Facing;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderBlockPusher implements ISimpleBlockRenderingHandler {

    @Override
    public void renderInventoryBlock(Block block, int meta, int modelId, RenderBlocks renderer) {
        meta = 0;
        block.setBlockBoundsForItemRender();
        renderer.setRenderBoundsFromBlock(block);

        setRotations(renderer, Facing.oppositeSide[meta % 6]);

        GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        RenderHelper.renderBlock(block, meta, renderer, 0);
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);

        resetRotations(renderer);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        int meta = Facing.oppositeSide[world.getBlockMetadata(x, y, z) % 6];
        setRotations(renderer, meta);
        boolean flag = renderer.renderStandardBlock(block, x, y, z);
        resetRotations(renderer);
        return flag;
    }

    public static void resetRotations(RenderBlocks renderer) {
        renderer.uvRotateTop = renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateWest = renderer.uvRotateNorth = renderer.uvRotateSouth = 0;
    }

    public void setRotations(RenderBlocks renderer, int meta) {
        switch (meta) {
            case 0:
                renderer.uvRotateEast = renderer.uvRotateWest = renderer.uvRotateNorth = renderer.uvRotateSouth = 3;
                break;

            case 1:
            default:
                break;

            case 2:
                renderer.uvRotateNorth = 2;
                renderer.uvRotateSouth = 1;
                break;

            case 3:
                renderer.uvRotateNorth = 1;
                renderer.uvRotateSouth = 2;
                renderer.uvRotateTop = renderer.uvRotateBottom = 3;
                break;

            case 4:
                renderer.uvRotateTop = 2;
                renderer.uvRotateBottom = 1;
                renderer.uvRotateEast = 1;
                renderer.uvRotateWest = 2;
                break;


            case 5:
                renderer.uvRotateTop = 1;
                renderer.uvRotateBottom = 2;
                renderer.uvRotateEast = 2;
                renderer.uvRotateWest = 1;
                break;
        }
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return FunkyLocomotion.proxy.pusherRendererId;
    }
}
