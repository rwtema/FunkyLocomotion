package com.rwtema.funkylocomotion.rendering;

import org.lwjgl.opengl.GL11;
import com.rwtema.funkylocomotion.blocks.TileFrameProjector;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TESRProjector extends TileEntitySpecialRenderer<TileFrameProjector> {
	public static final TESRProjector INSTANCE;

	static {
		INSTANCE = new TESRProjector();
		MinecraftForge.EVENT_BUS.register(INSTANCE);
	}

	@Override
	public final void render(TileFrameProjector te, double x, double y, double z, float partialTicks, int destroyStage, float partial) {
		if (!te.powered) return;
		float r = te.range;
		if (r <= 0) return;

		r += 0.5F - 1 / 512F;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder renderer = tessellator.getBuffer();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.disableCull();
		GlStateManager.disableTexture2D();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.depthMask(false);


		BlockPos pos = te.getPos();
		EnumFacing facing = te.facing;


		double ax = pos.getX() + 0.5;
		double ay = pos.getY() + 0.5;
		double az = pos.getZ() + 0.5;

		double bx = ax + facing.getFrontOffsetX() * r;
		double by = ay + facing.getFrontOffsetY() * r;
		double bz = az + facing.getFrontOffsetZ() * r;

//		double cx = pos.getX() + 0.5 + 0.5 * facing.getFrontOffsetX();
//		double cy = pos.getY() + 0.5 + 0.5 * facing.getFrontOffsetY();
//		double cz = pos.getZ() + 0.5 + 0.5 * facing.getFrontOffsetZ();


		EnumFacing.Axis axis = facing.getAxis();

		double dx1 = 0, dx2 = 0, dy1 = 0, dy2 = 0, dz1 = 0, dz2 = 0;
		switch (axis) {
			case X:
				dy1 = 1;
				dz2 = 1;
				break;
			case Y:
				dx1 = 1;
				dz2 = 1;
				break;
			case Z:
				dy1 = 1;
				dx2 = 1;
				break;
			default:
				throw new IllegalStateException();
		}

		renderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
		renderer.setTranslation(x - pos.getX(), y - pos.getY(), z - pos.getZ());
		renderer.pos(ax, ay, az).color(255, 255, 255, 160).endVertex();
		addQuadVertices(renderer, bx, by, bz, dx1, dx2, dy1, dy2, dz1, dz2, r);
		renderer.setTranslation(0, 0, 0);
		tessellator.draw();

		GlStateManager.enableTexture2D();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		GlStateManager.depthMask(true);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableAlpha();
		RenderHelper.enableStandardItemLighting();
	}

	@Override
	public boolean isGlobalRenderer(TileFrameProjector te) {
		return true;
	}

	private void addQuadVertices(BufferBuilder renderer, double bx, double by, double bz, double dx1, double dx2, double dy1, double dy2, double dz1, double dz2, double r) {
		dx1 *= r;
		dx2 *= r;
		dy1 *= r;
		dy2 *= r;
		dz1 *= r;
		dz2 *= r;
		renderer.pos(bx + dx1 - dx2, by + dy1 - dy2, bz + dz1 - dz2).color(255, 255, 0, 40).endVertex();
		renderer.pos(bx + dx1 + dx2, by + dy1 + dy2, bz + dz1 + dz2).color(255, 255, 0, 40).endVertex();
		renderer.pos(bx - dx1 + dx2, by - dy1 + dy2, bz - dz1 + dz2).color(255, 255, 0, 40).endVertex();
		renderer.pos(bx - dx1 - dx2, by - dy1 - dy2, bz - dz1 - dz2).color(255, 255, 0, 40).endVertex();
		renderer.pos(bx + dx1 - dx2, by + dy1 - dy2, bz + dz1 - dz2).color(255, 255, 0, 40).endVertex();
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {

	}
}
