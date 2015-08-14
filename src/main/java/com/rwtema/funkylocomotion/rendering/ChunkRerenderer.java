package com.rwtema.funkylocomotion.rendering;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

public class ChunkRerenderer {
	static final TIntHashSet toRerenderSet = new TIntHashSet();

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void reRenderChunks(TickEvent.RenderTickEvent event) {
		if (toRerenderSet.isEmpty()) return;

		final WorldRenderer[] worldRenderers = Minecraft.getMinecraft().renderGlobal.worldRenderers;
		final EntityLivingBase renderViewEntity = Minecraft.getMinecraft().renderViewEntity;



		ArrayList<WorldRenderer> renderersToUpdate = new ArrayList<WorldRenderer>();

		for (TIntIterator iterator = toRerenderSet.iterator(); iterator.hasNext(); ) {
			WorldRenderer worldRenderer = worldRenderers[iterator.next()];

			if(worldRenderer != null) {
				worldRenderer.markDirty();
				renderersToUpdate.add(worldRenderer);
			}
		}

		if(FunkyLocomotion.redrawChunksInstantly) {
			for (WorldRenderer worldRenderer : renderersToUpdate) {
				worldRenderer.updateRenderer(renderViewEntity);
			}
		}else{
			List worldRenderersToUpdate = Minecraft.getMinecraft().renderGlobal.worldRenderersToUpdate;

			worldRenderersToUpdate.removeAll(renderersToUpdate);
			worldRenderersToUpdate.addAll(0, renderersToUpdate);
		}

		toRerenderSet.clear();
	}

	@SideOnly(Side.CLIENT)
	public static void markBlock(int x, int y, int z) {
		Minecraft mc = Minecraft.getMinecraft();
		RenderGlobal renderGlobal = mc.renderGlobal;

		int dx = MathHelper.bucketInt(x, 16) % renderGlobal.renderChunksWide;
		if (dx < 0) dx += renderGlobal.renderChunksWide;

		int dy = MathHelper.bucketInt(y, 16) % renderGlobal.renderChunksTall;
		if (dy < 0) dy += renderGlobal.renderChunksTall;

		int dz = MathHelper.bucketInt(z, 16) % renderGlobal.renderChunksDeep;
		if (dz < 0) dz += renderGlobal.renderChunksDeep;

		int i = (dz * renderGlobal.renderChunksTall + dy) * renderGlobal.renderChunksWide + dx;

		toRerenderSet.add(i);

		renderGlobal.markBlockForUpdate(x,y,z);
	}
}
