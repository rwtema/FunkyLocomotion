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


        this.bindTexture(TextureMap.locationBlocksTexture);
        boolean flag = false;

        try {
            if (mover.render) {
                GL11.glPushMatrix();

                GL11.glTranslated(x, y, z);
                GL11.glTranslated(-mover.xCoord, -mover.yCoord, -mover.zCoord);
                GL11.glTranslated(dir.offsetX * h, dir.offsetY * h, dir.offsetZ * h);

//            if (mover.block.canRenderInPass(0) || mover.block.canRenderInPass(1)) {
                RenderHelper.disableStandardItemLighting();
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_CULL_FACE);

                if (Minecraft.isAmbientOcclusionEnabled()) {
                    GL11.glShadeModel(GL11.GL_SMOOTH);
                } else {
                    GL11.glShadeModel(GL11.GL_FLAT);
                }
                Tessellator tessellator = Tessellator.instance;
                tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
                tessellator.startDrawingQuads();
                for (int k2 = 0; k2 < 2; ++k2) {
                    if (!mover.block.canRenderInPass(k2)) continue;
                    if(k2 != 0 )
                        GL11.glEnable(GL11.GL_BLEND);
                    if (renderBlocks.renderBlockByRenderType(mover.block, mover.xCoord, mover.yCoord, mover.zCoord))
                        flag = true;
                }
                tessellator.draw();

                RenderHelper.enableStandardItemLighting();
//            }        }
                GL11.glPopMatrix();
                GL11.glEnable(GL11.GL_CULL_FACE);
            }
        } catch (Exception e) {
            mover.render = false;
            mover.error = true;
            (new RuntimeException(
                    "Unable to render block " + Block.blockRegistry.getNameForObject(mover.block)
                            + " with meta " + mover.meta + " at ("
                            + mover.xCoord + "," + mover.yCoord + mover.zCoord + "). Disabling Rendering."
                    , e
            )).printStackTrace();
        }

        if (mover.tile != null) {
            TileEntitySpecialRenderer specialRenderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(mover.tile);
            if (specialRenderer != null) {
                try {

                    GL11.glPushMatrix();
                    GL11.glTranslated(dir.offsetX * h, dir.offsetY * h, dir.offsetZ * h);
                    specialRenderer.func_147496_a(fakeWorld);
                    specialRenderer.renderTileEntityAt(mover.tile, x, y, z, f);
                    specialRenderer.func_147496_a(world);
                    flag = true;
                    GL11.glPopMatrix();
                } catch (Exception e) {
                    mover.tile = null;
                    mover.error = true;
                    (new RuntimeException(
                            "Unable to render TSER " + mover.tile.getClass().getSimpleName() + " for "
                                    + Block.blockRegistry.getNameForObject(mover.block)
                                    + " with meta " + mover.meta + " at ("
                                    + mover.xCoord + "," + mover.yCoord + mover.zCoord + "). Disabling Rendering."
                            , e
                    )).printStackTrace();
                }
            }
        }


        if (!flag) {
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
            Tessellator tessellator = Tessellator.instance;
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

    public void func_147496_a(World world) {
        this.world = world;
        this.fakeWorld = FakeWorldClient.getFakeWorldWrapper(world);
        this.renderBlocks = new RenderBlocks(fakeWorld);
    }


}
