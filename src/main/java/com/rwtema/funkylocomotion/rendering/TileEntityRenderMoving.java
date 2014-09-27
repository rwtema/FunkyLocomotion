package com.rwtema.funkylocomotion.rendering;

import com.rwtema.funkylocomotion.blocks.BlockMoving;
import com.rwtema.funkylocomotion.blocks.TileMovingClient;
import com.rwtema.funkylocomotion.fakes.FakeWorldClient;
import net.minecraft.block.Block;
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
import net.minecraftforge.client.MinecraftForgeClient;
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
        if (!mover.init)
            return;

        if (mover.maxTime == 0 || mover.block == Blocks.air)
            return;

        double h = mover.offset(true);
        ForgeDirection dir = mover.dir;


        this.bindTexture(TextureMap.locationBlocksTexture);
        boolean flag = false;

        Tessellator tessellator = Tessellator.instance;

        int pass = MinecraftForgeClient.getRenderPass();
        if (mover.render && mover.block.canRenderInPass(pass)) {
            GL11.glPushMatrix();

            GL11.glTranslated(x, y, z);
            GL11.glTranslated(-mover.xCoord, -mover.yCoord, -mover.zCoord);
            GL11.glTranslated(dir.offsetX * h, dir.offsetY * h, dir.offsetZ * h);

            flag = renderStatic(mover, pass);

            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_CULL_FACE);

        }

        flag = flag | renderDynamic(x, y, z, f, mover, h, dir, pass);

        if (pass == 0) {
            mover.failedToRenderInFirstPass = !flag;
        } else if (!flag && mover.failedToRenderInFirstPass) {
            GL11.glPushMatrix();

            GL11.glTranslated(x, y, z);
            GL11.glTranslated(-mover.xCoord, -mover.yCoord, -mover.zCoord);
            GL11.glTranslated(dir.offsetX * h, dir.offsetY * h, dir.offsetZ * h);

            RenderHelper.disableStandardItemLighting();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_BLEND);

            if (Minecraft.isAmbientOcclusionEnabled()) {
                GL11.glShadeModel(GL11.GL_SMOOTH);
            } else {
                GL11.glShadeModel(GL11.GL_FLAT);
            }

            tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
            tessellator.startDrawingQuads();
            renderBlocks.setOverrideBlockTexture(mover.error ? BlockMoving.crate_error : BlockMoving.crate);
            renderBlocks.renderBlockByRenderType(Blocks.stone, mover.xCoord, mover.yCoord, mover.zCoord);
            renderBlocks.clearOverrideBlockTexture();
            tessellator.draw();
            GL11.glPopMatrix();

            RenderHelper.enableStandardItemLighting();
        }
    }

    protected boolean renderDynamic(double x, double y, double z, float f, TileMovingClient mover, double h, ForgeDirection dir, int pass) {
        boolean flag = false;
        if (mover.tile != null && mover.tile.shouldRenderInPass(pass)) {
            TileEntitySpecialRenderer specialRenderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(mover.tile);
            if (specialRenderer != null) {
                GL11.glPushMatrix();
                GL11.glTranslated(dir.offsetX * h, dir.offsetY * h, dir.offsetZ * h);
                try {
                    specialRenderer.func_147496_a(fakeWorld);
                    specialRenderer.renderTileEntityAt(mover.tile, x, y, z, f);
                    specialRenderer.func_147496_a(world);
                } catch (Exception e) {
                    FLRenderHelper.clearTessellator();

                    (new RuntimeException(
                            "Unable to render TSER " + mover.tile.getClass().getName() + " for "
                                    + Block.blockRegistry.getNameForObject(mover.block)
                                    + " with meta " + mover.meta + " at ("
                                    + mover.xCoord + "," + mover.yCoord + mover.zCoord + "). Disabling Rendering."
                            , e
                    )).printStackTrace();

                    TileMovingClient.renderErrorList.add(mover.tile.getClass());
                    mover.error = true;
                    mover.tile = null;
                    mover.render = false;
                }

                flag = true;
                GL11.glPopMatrix();

            }
        }
        return flag;
    }

    protected boolean renderStatic(TileMovingClient mover, int pass) {
        boolean flag = false;

        Tessellator tessellator = Tessellator.instance;
        RenderHelper.disableStandardItemLighting();

        if (pass != 0) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        GL11.glEnable(GL11.GL_CULL_FACE);

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        } else {
            GL11.glShadeModel(GL11.GL_FLAT);
        }

        try {
            tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
            tessellator.startDrawingQuads();

            if (renderBlocks.renderBlockByRenderType(mover.block, mover.xCoord, mover.yCoord, mover.zCoord))
                flag = true;

            tessellator.draw();
        } catch (Exception e) {
            FLRenderHelper.clearTessellator();

            (new RuntimeException(
                    "Unable to render block " + Block.blockRegistry.getNameForObject(mover.block)
                            + " with meta " + mover.meta + " at ("
                            + mover.xCoord + "," + mover.yCoord + mover.zCoord + "). Disabling Rendering."
                    , e
            )).printStackTrace();

            TileMovingClient.renderErrorList.add(mover.block.getClass());
            mover.tile = null;
            mover.render = false;
            mover.error = true;
        }
        RenderHelper.enableStandardItemLighting();
        return flag;
    }

    public void func_147496_a(World world) {
        this.world = world;
        this.fakeWorld = FakeWorldClient.getFakeWorldWrapper(world);
        this.renderBlocks = new RenderBlocks(fakeWorld);
    }
}
