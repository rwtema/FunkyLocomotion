package com.rwtema.funkylocomotion.rendering;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.items.ItemWrench;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class RenderItemWrench implements IItemRenderer {
    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return item.getItemDamage() == ItemWrench.metaWrenchEye && type == ItemRenderType.INVENTORY;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return false;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack itemstack, Object... data) {

        int k = itemstack.getItemDamage();
        int l;

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(0, 0, 0, 0);
        GL11.glColorMask(false, false, false, true);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(-1);
        tessellator.addVertex((double) (-2), (double) (18), 0);
        tessellator.addVertex((double) (18), (double) (18), 0);
        tessellator.addVertex((double) (18), (double) (-2), 0);
        tessellator.addVertex((double) (-2), (double) (-2), 0);
        tessellator.draw();
        GL11.glColorMask(true, true, true, true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

        if (currentScreen == null) {
            for (l = 0; l < FunkyLocomotion.wrench.getRenderPasses(k); ++l) {
                IIcon iicon = FunkyLocomotion.wrench.getIcon(itemstack, l);
                this.renderIcon(0, 0, iicon);
            }
        } else {
            int mouseX = Mouse.getX() * currentScreen.width / Minecraft.getMinecraft().displayWidth;
            int mouseY = currentScreen.height - Mouse.getEventY() * currentScreen.height / Minecraft.getMinecraft().displayHeight - 1;

            FloatBuffer matrixData = BufferUtils.createFloatBuffer(16);
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrixData);

            float x = (matrixData.get(12) + 8);
            float y = (matrixData.get(13) + 8);

            double dx = (mouseX - x) * 0.125*0.25;
            double dy = (mouseY - y) * 0.125*0.25;

            double d = dx * dx + dy * dy;
            double r = 0.5;
            if (d > r*r) {
                d = Math.sqrt(d);
                dx *= r / d;
                dy *= r / d;
            }

            this.renderIcon(0, 0, FunkyLocomotion.wrench.iconWrenchEye_base);
            this.renderIcon(dx, dy, FunkyLocomotion.wrench.iconWrenchEye_pupil);
            this.renderIcon(0, 0, FunkyLocomotion.wrench.iconWrenchEye_outline);
        }

        GL11.glEnable(GL11.GL_LIGHTING);


        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    public void renderIcon(double x, double y, IIcon icon) {
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x + 0, y + 16, 0, (double) icon.getMinU(), (double) icon.getMaxV());
        tessellator.addVertexWithUV(x + 16, y + 16, 0, (double) icon.getMaxU(), (double) icon.getMaxV());
        tessellator.addVertexWithUV(x + 16, y + 0, 0, (double) icon.getMaxU(), (double) icon.getMinV());
        tessellator.addVertexWithUV(x + 0, y + 0, 0, (double) icon.getMinU(), (double) icon.getMinV());
        tessellator.draw();
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
    }
}
