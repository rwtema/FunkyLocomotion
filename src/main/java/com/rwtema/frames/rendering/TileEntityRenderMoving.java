package com.rwtema.frames.rendering;

import com.rwtema.frames.blocks.TileMovingClient;
import com.rwtema.frames.fakes.FakeWorldClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

public class TileEntityRenderMoving extends TileEntitySpecialRenderer {
    private RenderBlocks renderBlocks;
    private World world;
    private FakeWorldClient fakeWorld;

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f) {
        if (!(tile instanceof TileMovingClient))
            return;

        TileMovingClient mover = (TileMovingClient) tile;

        if (mover.maxTime == 0 || mover.block == Blocks.air)
            return;

        double h = mover.offset(f);
        ForgeDirection dir = mover.dir;

        GL11.glPushMatrix();

        GL11.glTranslated(x, y, z);
        GL11.glTranslated(-mover.xCoord, -mover.yCoord, -mover.zCoord);
        GL11.glTranslated(dir.offsetX * h, dir.offsetY * h, dir.offsetZ * h);

        this.bindTexture(TextureMap.locationBlocksTexture);

        RenderHelper.disableStandardItemLighting();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        } else {
            GL11.glShadeModel(GL11.GL_FLAT);
        }
        Tessellator tessellator = Tessellator.instance;
        tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
        tessellator.startDrawingQuads();
        for (int k2 = 0; k2 < 2; ++k2) {
            int k3 = mover.block.getRenderBlockPass();
            if (!mover.block.canRenderInPass(k2)) continue;
            renderBlocks.renderBlockByRenderType(mover.block, mover.xCoord, mover.yCoord, mover.zCoord);
        }
        tessellator.draw();
        GL11.glPopMatrix();

        RenderHelper.enableStandardItemLighting();

        if (mover.tile != null) {
            GL11.glPushMatrix();
            GL11.glTranslated(dir.offsetX * h, dir.offsetY * h, dir.offsetZ * h);
            TileEntitySpecialRenderer specialRenderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(mover.tile);
            if (specialRenderer != null) {
                specialRenderer.func_147496_a(fakeWorld);
                specialRenderer.renderTileEntityAt(mover.tile, x, y, z, f);
                specialRenderer.func_147496_a(world);
            }
            GL11.glPopMatrix();
        }

    }

    public void func_147496_a(World world) {
        this.world = world;
        this.fakeWorld = FakeWorldClient.getFakeWorldWrapper(world);
        this.renderBlocks = new RenderBlocks(fakeWorld);
    }


}
