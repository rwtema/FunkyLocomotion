package com.rwtema.funkylocomotion.rendering;

import com.rwtema.funkylocomotion.blocks.TileMovingClient;
import com.rwtema.funkylocomotion.fakes.FakeWorldClient;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;

public class TESRMoving extends TileEntitySpecialRenderer<TileMovingClient> {
	private BlockRendererDispatcher blockRenderer;

	@Override
	public final void renderTileEntityAt(TileMovingClient te, double x, double y, double z, float partialTicks, int destroyStage) {
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer renderer = tessellator.getBuffer();
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();

		if (Minecraft.isAmbientOcclusionEnabled()) {
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
		} else {
			GlStateManager.shadeModel(GL11.GL_FLAT);
		}


		renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, renderer);
		renderer.setTranslation(0, 0, 0);


		RenderHelper.enableStandardItemLighting();
	}


	@Override
	public void renderTileEntityFast(TileMovingClient te, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer renderer) {
		if (blockRenderer == null) blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos pos1 = te.getPos();

		if (!te.init)
			return;

		if (te.maxTime == 0 || te.block == Blocks.AIR)
			return;

		Tessellator tessellator = Tessellator.getInstance();

		double h = te.offset(true);

		int dir = te.dir;
		if (dir == -1) {
			return;
		}
//
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		boolean flag = false;

		int pass = MinecraftForgeClient.getRenderPass();
		if (te.render && te.getState().getRenderType() != EnumBlockRenderType.INVISIBLE) {
			GL11.glPushMatrix();
			renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			setupTranslations(x, y, z, te, h, dir, renderer);
			flag = renderStatic(te, pass, renderer);
			tessellator.draw();
			GL11.glPopMatrix();
			GL11.glEnable(GL11.GL_CULL_FACE);
		}

		flag = flag | renderDynamic(x, y, z, partialTicks, te, h, dir, pass, renderer);
	}

	protected boolean renderDynamic(double x, double y, double z, float f, TileMovingClient mover, double h, int dir, int pass, VertexBuffer renderer) {
		if (mover.tile == null || !mover.tile.shouldRenderInPass(pass))
			return false;

		World world = mover.getWorld();

		if (!FakeWorldClient.isValid(world)) {
			return false;
		}
		FakeWorldClient fakeWorldClient = FakeWorldClient.getFakeWorldWrapper(world);
		EnumFacing dir1 = mover.getDir();
		if(dir1 != null) {
			fakeWorldClient.offset = mover.offset(true);
			fakeWorldClient.dir = dir1;
		}else{
			fakeWorldClient.offset = 0;
			fakeWorldClient.dir = null;
		}
		fakeWorldClient.dir_id = mover.dir;

		TileEntitySpecialRenderer specialRenderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(mover.tile);
		if (specialRenderer == null)
			return false;

		GL11.glPushMatrix();
		setupTranslations(x, y, z, mover, h, dir, renderer);
		try {
			WorldClient prevWorld1 = Minecraft.getMinecraft().theWorld;
			World prevWorld2 = Minecraft.getMinecraft().thePlayer.worldObj;
			World prevWorld3 = rendererDispatcher.worldObj;
			try {
				Minecraft.getMinecraft().theWorld = fakeWorldClient;
				Minecraft.getMinecraft().thePlayer.worldObj = fakeWorldClient;
				TileEntityRendererDispatcher.instance.worldObj = fakeWorldClient;
				renderer.setTranslation(0, 0, 0);
				mover.tile.updateContainingBlockInfo();
				RenderHelper.enableStandardItemLighting();
				specialRenderer.renderTileEntityAt(mover.tile, mover.getPos().getX(), mover.getPos().getY(), mover.getPos().getZ(), f, -1);
				RenderHelper.disableStandardItemLighting();
				renderer.setTranslation(0, 0, 0);
			} finally {
				Minecraft.getMinecraft().theWorld = prevWorld1;
				Minecraft.getMinecraft().thePlayer.worldObj = prevWorld2;
				TileEntityRendererDispatcher.instance.worldObj = prevWorld3;
			}
		} catch (Exception e) {
			FLRenderHelper.clearTessellator();

			(new RuntimeException(
					"Unable to render TSER " + mover.tile.getClass().getName() + " for "
							+ Block.REGISTRY.getNameForObject(mover.block)
							+ " with meta " + mover.meta + " at ("
							+ mover.getPos() + "). Disabling Rendering."
					, e
			)).printStackTrace();

			TileMovingClient.renderErrorList.add(mover.tile.getClass());
			mover.error = true;
			mover.tile = null;
			mover.render = false;
		}

		GL11.glPopMatrix();
		return true;
	}

	private void setupTranslations(double x, double y, double z, TileMovingClient mover, double h, int dir, VertexBuffer renderer) {
		BlockPos pos = mover.getPos();
		if (dir < 6) {
			EnumFacing dir1 = EnumFacing.values()[dir];
			renderer.setTranslation(0, 0, 0);
			GL11.glTranslated((x - pos.getX() + dir1.getFrontOffsetX() * h),
					(y - pos.getY() + dir1.getFrontOffsetY() * h),
					(z - pos.getZ() + dir1.getFrontOffsetZ() * h));
//			renderer.setTranslation(
//					x - pos.getX() + dir1.getFrontOffsetX() * h,
//					y - pos.getY() + dir1.getFrontOffsetY() * h,
//					z - pos.getZ() + dir1.getFrontOffsetZ() * h);
		} else {
			renderer.setTranslation(0, 0, 0);
			GL11.glTranslated(x, y, z);
			GL11.glTranslated(0.5, 0.5, 0.5);
			double dh = dir == 6 ? h + 1 : -h;
			GL11.glScaled(dh, dh, dh);
			GL11.glTranslated(-0.5, -0.5, -0.5);
			GL11.glTranslated(-pos.getX(), -pos.getY(), -pos.getZ());
		}
	}

	private boolean renderStatic(TileMovingClient tile, int pass, VertexBuffer vertexbuffer) {
		IBlockState state = tile.getState();
		Block block = state.getBlock();
		FakeWorldClient fakeWorldWrapper = FakeWorldClient.getFakeWorldWrapper(tile.getWorld());
		EnumFacing dir1 = tile.getDir();
		if(dir1 != null) {
			fakeWorldWrapper.offset = tile.offset(true);
			fakeWorldWrapper.dir = dir1;
		}else{
			fakeWorldWrapper.offset = 0;
			fakeWorldWrapper.dir = null;
		}
		fakeWorldWrapper.dir_id = tile.dir;

		boolean flag = false;
		for (BlockRenderLayer blockrenderlayer1 : PassHandler.getHandler(pass).layers) {
			if (!block.canRenderInLayer(state, blockrenderlayer1)) continue;
			net.minecraftforge.client.ForgeHooksClient.setRenderLayer(blockrenderlayer1);

			if (block.getDefaultState().getRenderType() != EnumBlockRenderType.INVISIBLE) {
				flag = flag | blockRenderer.renderBlock(state, tile.getPos(), fakeWorldWrapper, vertexbuffer);
			}
		}
		net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
		return flag;
	}


}
