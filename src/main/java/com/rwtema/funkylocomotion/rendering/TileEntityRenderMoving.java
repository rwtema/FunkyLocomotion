package com.rwtema.funkylocomotion.rendering;

import com.rwtema.funkylocomotion.blocks.TileMovingClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.client.model.animation.FastTESR;

import javax.annotation.Nonnull;

public class TileEntityRenderMoving extends FastTESR<TileMovingClient> {

	//private World world;
	//private FakeWorldClient fakeWorldClient;

	//	@Override
	public void r(TileMovingClient tile, double x, double y, double z, float partialTicks, int destroyStage) {
		/*
		if (!tile.init)
			return;

		if (tile.maxTime == 0 || tile.block == Blocks.AIR)
			return;

		double h = tile.offset(true);

		int dir = tile.dir;
		if (dir == -1) {
			return;
		}

		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		boolean flag = false;
		*/
//
//		Tessellator tessellator = Tessellator.instance;
//
//		int pass = MinecraftForgeClient.getRenderPass();
//		if (tile.render && tile.block.canRenderInPass(pass) && tile.block.getRenderType() >= 0) {
//			GL11.glPushMatrix();
//
//			setupTranslations(x, y, z, tile, h, dir);
//
//			flag = renderStatic(tile, pass);
//
//			GL11.glPopMatrix();
//			GL11.glEnable(GL11.GL_CULL_FACE);
//		}
//
//		flag = flag | renderDynamic(x, y, z, f, tile, h, dir, pass);
//
//		if (pass == 0) {
//			tile.failedToRenderInFirstPass = !flag;
//		} else if (!flag && tile.failedToRenderInFirstPass) {
//			GL11.glPushMatrix();
//
//			setupTranslations(x, y, z, tile, h, dir);
//
//			RenderHelper.disableStandardItemLighting();
//			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//			GL11.glEnable(GL11.GL_BLEND);
//
//			if (Minecraft.isAmbientOcclusionEnabled()) {
//				GL11.glShadeModel(GL11.GL_SMOOTH);
//			} else {
//				GL11.glShadeModel(GL11.GL_FLAT);
//			}
//
//			tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
//			tessellator.startDrawingQuads();
//			renderBlocks.setOverrideBlockTexture(tile.error ? BlockMoving.crate_error : BlockMoving.crate);
//			renderBlocks.renderBlockByRenderType(Blocks.stone, tile.xCoord, tile.yCoord, tile.zCoord);
//			renderBlocks.clearOverrideBlockTexture();
//
//			tile.cachedState[0] = leadVertexState();
//			tile.skipPass[0] = false;
//			tile.cachedState[1] = null;
//			tile.skipPass[1] = true;
//
//			tessellator.draw();
//			GL11.glPopMatrix();
//
//			RenderHelper.enableStandardItemLighting();
//		}
	}

	@Override
	public void renderTileEntityFast(@Nonnull TileMovingClient te, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder VertexBuffer) {

	}
//
//	private void setupTranslations(double x, double y, double z, TileMovingClient mover, double h, int dir) {
//		if(dir < 6) {
//			GL11.glTranslated(x, y, z);
//			GL11.glTranslated(-mover.xCoord, -mover.yCoord, -mover.zCoord);
//			GL11.glTranslated(Facing.offsetsXForSide[dir] * h, Facing.offsetsYForSide[dir] * h, Facing.offsetsZForSide[dir] * h);
//		}else{
//			GL11.glTranslated(x, y, z);
//			GL11.glTranslated(0.5, 0.5, 0.5);
//			double dh = dir == 6 ?  h + 1 : -h;
//			GL11.glScaled(dh, dh, dh);
//			GL11.glTranslated(-0.5, -0.5, -0.5);
//			GL11.glTranslated(-mover.xCoord, -mover.yCoord, -mover.zCoord);
//		}
//	}
//
//	protected boolean renderDynamic(double x, double y, double z, float f, TileMovingClient mover, double h, int dir, int pass) {
//		if (mover.tile == null || !mover.tile.shouldRenderInPass(pass))
//			return false;
//
//		if (fakeWorldClient == null && !createCache())
//			return false;
//
//		TileEntitySpecialRenderer specialRenderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(mover.tile);
//		if (specialRenderer == null)
//			return false;
//
//		GL11.glPushMatrix();
//		setupTranslations(x, y, z, mover, h, dir);
//		try {
//			specialRenderer.func_147496_a(fakeWorldClient);
//			WorldClient prevWorld1 = Minecraft.getMinecraft().theWorld;
//			World prevWorld2 = Minecraft.getMinecraft().thePlayer.worldObj;
//
//			try {
//				Minecraft.getMinecraft().theWorld = fakeWorldClient;
//				Minecraft.getMinecraft().thePlayer.worldObj = fakeWorldClient;
//				specialRenderer.renderTileEntityAt(mover.tile, mover.xCoord, mover.yCoord, mover.zCoord, f);
//			} finally {
//				Minecraft.getMinecraft().theWorld = prevWorld1;
//				Minecraft.getMinecraft().thePlayer.worldObj = prevWorld2;
//			}
//			specialRenderer.func_147496_a(world);
//		} catch (Exception e) {
//			FLRenderHelper.clearTessellator();
//
//			(new RuntimeException(
//					"Unable to render TSER " + mover.tile.getClass().getName() + " for "
//							+ Block.blockRegistry.getNameForObject(mover.block)
//							+ " with meta " + mover.meta + " at ("
//							+ mover.xCoord + "," + mover.yCoord + mover.zCoord + "). Disabling Rendering."
//					, e
//			)).printStackTrace();
//
//			TileMovingClient.renderErrorList.add(mover.tile.getClass());
//			mover.error = true;
//			mover.tile = null;
//			mover.render = false;
//		}
//
//		GL11.glPopMatrix();
//		return true;
//	}
//
//	protected boolean renderStatic(TileMovingClient mover, int pass) {
//		if (mover.skipPass[pass])
//			return true;
//
//
//		RenderHelper.disableStandardItemLighting();
//
//		if (pass != 0) {
//			GL11.glEnable(GL11.GL_BLEND);
//			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//		}
//
//		GL11.glEnable(GL11.GL_CULL_FACE);
//
//		if (Minecraft.isAmbientOcclusionEnabled()) {
//			GL11.glShadeModel(GL11.GL_SMOOTH);
//		} else {
//			GL11.glShadeModel(GL11.GL_FLAT);
//		}
//
//		Tessellator tessellator = Tessellator.instance;
//
//		if (mover.cachedState[pass] != null) {
//			tessellator.startDrawingQuads();
//			tessellator.setVertexState(mover.cachedState[pass]);
//			tessellator.draw();
//		} else {
//
//			tessellator.startDrawingQuads();
//			tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
//
//			mover.cachedState[pass] = null;
//			mover.skipPass[pass] = true;
//
//			try {
//				if (renderBlocks.renderBlockByRenderType(mover.block, mover.xCoord, mover.yCoord, mover.zCoord)) {
//					if (tessellator.rawBufferIndex > 0) {
//						mover.cachedState[pass] = leadVertexState();
//						mover.skipPass[pass] = false;
//					}
//				}
//
//			} catch (Exception e) {
//				(new RuntimeException(
//						"Unable to render block " + Block.blockRegistry.getNameForObject(mover.block)
//								+ " with meta " + mover.meta + " at ("
//								+ mover.xCoord + "," + mover.yCoord + mover.zCoord + "). Disabling Rendering."
//						, e
//				)).printStackTrace();
//
//				TileMovingClient.renderErrorList.add(mover.block.getClass());
//				mover.tile = null;
//				mover.render = false;
//				mover.error = true;
//
//				mover.cachedState[pass] = null;
//				mover.skipPass[pass] = true;
//			}
//
//
//			tessellator.draw();
//		}
//		RenderHelper.enableStandardItemLighting();
//		return true;
//	}
//
//	private static TesselatorVertexState leadVertexState() {
//
//		return Tessellator.instance.getVertexState(
//				(float) Minecraft.getMinecraft().thePlayer.posX,
//				(float) Minecraft.getMinecraft().thePlayer.posY,
//				(float) Minecraft.getMinecraft().thePlayer.posZ);
//
//	}
//
//	public void func_147496_a(World world) {
//		this.world = world;
//		createCache();
//	}
//
//	private boolean createCache() {
//		if (!FakeWorldClient.isValid(world)) {
//			fakeWorldClient = null;
//			renderBlocks = null;
//			return false;
//		} else {
//			this.fakeWorldClient = FakeWorldClient.getFakeWorldWrapper(this.world);
//			this.renderBlocks = new RenderBlocks(fakeWorldClient);
//			return true;
//		}
//	}
}
