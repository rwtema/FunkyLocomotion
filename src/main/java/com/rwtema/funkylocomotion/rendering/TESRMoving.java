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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;

public class TESRMoving extends TileEntitySpecialRenderer<TileMovingClient> {
	private BlockRendererDispatcher blockRenderer;

	public static final TESRMoving INSTANCE = new TESRMoving();

	@Override
	public final void render(TileMovingClient te, double x, double y, double z, float partialTicks, int destroyStage, float partial) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder renderer = tessellator.getBuffer();
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		GlStateManager.enableCull();

		if (Minecraft.isAmbientOcclusionEnabled()) {
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
		} else {
			GlStateManager.shadeModel(GL11.GL_FLAT);
		}

		renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, partial, renderer);
		renderer.setTranslation(0, 0, 0);

		RenderHelper.enableStandardItemLighting();
	}


	@Override
	public void renderTileEntityFast(TileMovingClient te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder renderer) {
		if (blockRenderer == null) blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();

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

		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		boolean flag = false;

		int pass = MinecraftForgeClient.getRenderPass();
		if (te.render && te.getState().getRenderType() != EnumBlockRenderType.INVISIBLE) {
			GlStateManager.pushMatrix();
			renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			setupTranslations(x, y, z, te, h, dir, renderer);
			flag = renderStatic(te, pass, renderer);
			if (pass == 1) {
				BlockPos pos = te.getPos();
				if (dir < 6) {
					EnumFacing dir1 = EnumFacing.values()[dir];
					renderer.sortVertexData(
							-(float) (x - pos.getX() + dir1.getFrontOffsetX() * h),
							-(float) (y - pos.getY() + dir1.getFrontOffsetY() * h),
							-(float) (z - pos.getZ() + dir1.getFrontOffsetZ() * h));
				} else {
					renderer.sortVertexData(
							-(float) (x - pos.getX()),
							-(float) (y - pos.getY()),
							-(float) (z - pos.getZ()));
				}


			}
			tessellator.draw();
			GlStateManager.popMatrix();
			GlStateManager.enableCull();
		}

		flag = flag | renderDynamic(x, y, z, partialTicks, partial, te, h, dir, pass, renderer);
	}

	protected boolean renderDynamic(double x, double y, double z, float f, float partial, TileMovingClient mover, double h, int dir, int pass, BufferBuilder renderer) {
		if (mover.tile == null || !mover.tile.shouldRenderInPass(pass))
			return false;

		World world = mover.getWorld();

		if (!FakeWorldClient.isValid(world)) {
			return false;
		}
		FakeWorldClient fakeWorldClient = FakeWorldClient.getFakeWorldWrapper(world);
		EnumFacing dir1 = mover.getDir();
		if (dir1 != null) {
			fakeWorldClient.offset = mover.offset(true);
			fakeWorldClient.dir = dir1;
		} else {
			fakeWorldClient.offset = 0;
			fakeWorldClient.dir = null;
		}
		fakeWorldClient.dir_id = mover.dir;

		TileEntitySpecialRenderer<TileEntity> specialRenderer = TileEntityRendererDispatcher.instance.getRenderer(mover.tile);
		if (specialRenderer == null)
			return false;

		if (!mover.tile.shouldRenderInPass(pass)) return false;

		GlStateManager.pushMatrix();
		setupTranslations(x, y, z, mover, h, dir, renderer);
		try {
			WorldClient prevWorld1 = Minecraft.getMinecraft().world;
			World prevWorld2 = Minecraft.getMinecraft().player.world;
			World prevWorld3 = rendererDispatcher.world;
			try {
				Minecraft.getMinecraft().world = fakeWorldClient;
				Minecraft.getMinecraft().player.world = fakeWorldClient;
				TileEntityRendererDispatcher.instance.world = fakeWorldClient;
				renderer.setTranslation(0, 0, 0);
				mover.tile.updateContainingBlockInfo();
				RenderHelper.enableStandardItemLighting();
				specialRenderer.render(mover.tile, mover.getPos().getX(), mover.getPos().getY(), mover.getPos().getZ(), f, -1, partial);
				RenderHelper.disableStandardItemLighting();
				renderer.setTranslation(0, 0, 0);
			} finally {
				Minecraft.getMinecraft().world = prevWorld1;
				Minecraft.getMinecraft().player.world = prevWorld2;
				TileEntityRendererDispatcher.instance.world = prevWorld3;
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

		GlStateManager.popMatrix();
		return true;
	}

	private void setupTranslations(double x, double y, double z, TileMovingClient mover, double h, int dir, BufferBuilder renderer) {
		BlockPos pos = mover.getPos();
		if (dir < 6) {
			EnumFacing dir1 = EnumFacing.values()[dir];
			renderer.setTranslation(0, 0, 0);
			GlStateManager.translate((x - pos.getX() + dir1.getFrontOffsetX() * h),
					(y - pos.getY() + dir1.getFrontOffsetY() * h),
					(z - pos.getZ() + dir1.getFrontOffsetZ() * h));
//			renderer.setTranslation(
//					x - pos.getX() + dir1.getFrontOffsetX() * h,
//					y - pos.getY() + dir1.getFrontOffsetY() * h,
//					z - pos.getZ() + dir1.getFrontOffsetZ() * h);
		} else {
			renderer.setTranslation(0, 0, 0);
			GlStateManager.translate(x, y, z);
			GlStateManager.translate(0.5, 0.5, 0.5);
			double dh = dir == 6 ? h + 1 : -h;
			GlStateManager.scale(dh, dh, dh);
			GlStateManager.translate(-0.5, -0.5, -0.5);
			GlStateManager.translate(-pos.getX(), -pos.getY(), -pos.getZ());
		}
	}

	private boolean renderStatic(TileMovingClient tile, int pass, BufferBuilder vertexbuffer) {
		IBlockState state = tile.getState();
		Block block = state.getBlock();
		FakeWorldClient fakeWorldWrapper = FakeWorldClient.getFakeWorldWrapper(tile.getWorld());
		EnumFacing dir1 = tile.getDir();
		if (dir1 != null) {
			fakeWorldWrapper.offset = tile.offset(true);
			fakeWorldWrapper.dir = dir1;
		} else {
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
