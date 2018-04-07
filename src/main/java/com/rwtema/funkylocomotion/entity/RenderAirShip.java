package com.rwtema.funkylocomotion.entity;

import com.rwtema.funkylocomotion.rendering.PassHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Biomes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static com.rwtema.funkylocomotion.entity.EntityAirShip.PI_DIV;

public class RenderAirShip extends Render<EntityAirShip> {


	public RenderAirShip(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public boolean shouldRender(EntityAirShip livingEntity, ICamera camera, double camX, double camY, double camZ) {
		return true;
	}

	@Override
	public void doRender(@Nonnull EntityAirShip entity, double x, double y, double z, float entityYaw, float partialTicks) {
		BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		GlStateManager.pushMatrix();
		{
			boolean flag = false;
			if (this.bindEntityTexture(entity)) {
				this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).setBlurMipmap(false, false);
				flag = true;
			}

			AirShipBlockAccess airShipBlockAccess = new AirShipBlockAccess(entity);

			GlStateManager.enableRescaleNormal();
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.disableBlend();
			RenderHelper.disableStandardItemLighting();

			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			for (int pass = 0; pass < 1; pass++) {
				PassHandler.getHandler(pass).setupRendering();
				GlStateManager.pushMatrix();
				{
					GlStateManager.translate(x, y, z);
//
//					GlStateManager.rotate((float)entity.rotation.x, 0, 1, 0 );
					GlStateManager.translate(entity.center.x, entity.center.y, entity.center.z);
					GlStateManager.rotate((float) (entity.prevRotation.x + (entity.rotation.x - entity.prevRotation.x) * partialTicks), 0, 1, 0);
					GlStateManager.rotate((float) (entity.rotation.y), 1, 0, 0);
					GlStateManager.rotate((float) (entity.rotation.z), 0, 0, 1);
					GlStateManager.translate(-entity.center.x, -entity.center.y, -entity.center.z);

					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder renderer = tessellator.getBuffer();
					renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//					renderer.setTranslation(-entity.center.x, -entity.center.y, -entity.center.z);

					for (Map.Entry<BlockPos, EntityAirShip.Entry> entry : entity.structure.entrySet()) {
						IBlockState state = entry.getValue().state;
						for (BlockRenderLayer blockrenderlayer1 : PassHandler.getHandler(pass).layers) {
							if (!state.getBlock().canRenderInLayer(state, blockrenderlayer1)) continue;
							net.minecraftforge.client.ForgeHooksClient.setRenderLayer(blockrenderlayer1);

							if (state.getRenderType() != EnumBlockRenderType.INVISIBLE) {
								flag = flag | blockRenderer.renderBlock(state, entry.getKey(), airShipBlockAccess, renderer);
							}

						}
						net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
					}
					renderer.setTranslation(0, 0, 0);

					tessellator.draw();

				}
				GlStateManager.popMatrix();
			}
			GlStateManager.disableRescaleNormal();
			GlStateManager.disableBlend();
			this.bindEntityTexture(entity);
			if (flag) {
				this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).restoreLastBlurMipmap();
			}
		}
		GlStateManager.popMatrix();

		super.doRender(entity, x, y, z, entityYaw, partialTicks);

		if (Minecraft.getMinecraft().getRenderManager().isDebugBoundingBox()) {
			renderBounds(entity, x, y, z);
		}

	}

	public void renderBounds(EntityAirShip entityAirShip, double x, double y, double z) {
		RenderManager manager = Minecraft.getMinecraft().getRenderManager();
		GlStateManager.depthMask(false);
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.disableBlend();
		GlStateManager.depthFunc(GL11.GL_ALWAYS);

		float f = entityAirShip.width / 2.0F;
		for (AxisAlignedBB axisalignedbb : entityAirShip.getCollisions()) {
			RenderGlobal.drawBoundingBox(axisalignedbb.minX - entityAirShip.posX + x, axisalignedbb.minY - entityAirShip.posY + y, axisalignedbb.minZ - entityAirShip.posZ + z, axisalignedbb.maxX - entityAirShip.posX + x, axisalignedbb.maxY - entityAirShip.posY + y, axisalignedbb.maxZ - entityAirShip.posZ + z, 0.0F, 1.0F, 1.0F, 1.0F);
		}
		GlStateManager.depthFunc(GL11.GL_LEQUAL);
		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
	}

	@Nonnull
	@Override
	protected ResourceLocation getEntityTexture(@Nonnull EntityAirShip entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

	public static class AirShipBlockAccess implements IBlockAccess {
		final EntityAirShip airShip;

		public AirShipBlockAccess(EntityAirShip airShip) {
			this.airShip = airShip;
		}

		@Nullable
		@Override
		public TileEntity getTileEntity(BlockPos pos) {
			return null;
		}

		@Override
		public int getCombinedLight(BlockPos pos, int lightValue) {
			float yaw_c = MathHelper.cos(airShip.rotationYaw * PI_DIV);
			float yaw_s = MathHelper.sin(airShip.rotationYaw * PI_DIV);

			double x0 = airShip.position.x + airShip.center.x;
			double y0 = airShip.position.y + airShip.center.y;
			double z0 = airShip.position.z + airShip.center.z;

			double x = pos.getX() + 0.5;
			double y = pos.getY() + 0.5;
			double z = pos.getY() + 0.5;
			int newx = (int) Math.round(x0 + (x * yaw_c - z * yaw_s));
			int newy = (int) Math.round(y0 + y);
			int newz = (int) Math.round(z0 + (x * yaw_s + z * yaw_c));

			return airShip.world.getCombinedLight(new BlockPos(newx, newy, newz), lightValue);
		}

		@Override
		public IBlockState getBlockState(BlockPos pos) {
			return airShip.structure.getOrDefault(pos, EntityAirShip.Entry.AIR).state;
		}

		@Override
		public boolean isAirBlock(BlockPos pos) {
			IBlockState state = getBlockState(pos);
			return state.getBlock().isAir(state, this, pos);
		}

		@Override
		public Biome getBiome(BlockPos pos) {
			return Biomes.FOREST;
		}

		@Override
		public int getStrongPower(BlockPos pos, EnumFacing direction) {
			return 0;
		}

		@Override
		public WorldType getWorldType() {
			return airShip.world.getWorldType();
		}

		@Override
		public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
			return getBlockState(pos).isSideSolid(this, pos, side);
		}
	}
}
