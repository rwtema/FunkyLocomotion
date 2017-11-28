package com.rwtema.funkylocomotion.rendering;

import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ChunkRerenderer {
	static final TIntHashSet toRerenderSet = new TIntHashSet();

	@SideOnly(Side.CLIENT)
	public static void markBlock(BlockPos pos) {

//		Minecraft mc = Minecraft.getMinecraft();
//
//		int d = mc.gameSettings.renderDistanceChunks;
//
//		int k1 = pos.getX();
//
//		int l1 = k1 % d;
//
//		if (l1 < 0) {
//			l1 += d;
//		}
//
//		int i2 = pos.getY();
//		int j2 = i2 % 16;
//
//		if (j2 < 0) {
//			j2 += 16;
//		}
//
//		int k2 = pos.getZ();
//		int l2 = k2 % d;
//
//		if (l2 < 0) {
//			l2 += d;
//		}
//
//		int i3 = (l2 * 16 + j2) * d + l1;
//		toRerenderSet.add(i3);


//
//		Minecraft mc = Minecraft.getMinecraft();
//		RenderGlobal renderGlobal = mc.renderGlobal;
//
//		int dx = MathHelper.bucketInt(x, 16) % renderGlobal.renderChunksWide;
//		if (dx < 0) dx += renderGlobal.renderChunksWide;
//
//		int dy = MathHelper.bucketInt(y, 16) % renderGlobal.renderChunksTall;
//		if (dy < 0) dy += renderGlobal.renderChunksTall;
//
//		int dz = MathHelper.bucketInt(z, 16) % renderGlobal.renderChunksDeep;
//		if (dz < 0) dz += renderGlobal.renderChunksDeep;
//
//		int i = (dz * renderGlobal.renderChunksTall + dy) * renderGlobal.renderChunksWide + dx;
//
//		toRerenderSet.add(i);
//
//		renderGlobal.markBlockForUpdate(x,y,z);
	}

	//@SuppressWarnings("unchecked")
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void reRenderChunks(TickEvent.RenderTickEvent event) {
		if (toRerenderSet.isEmpty()) return;
//
//		Minecraft mc = Minecraft.getMinecraft();
//		RenderGlobal renderGlobal = mc.renderGlobal;
//		ViewFrustum view;
//		ChunkRenderDispatcher renderDispatcher;
//		try {
//			view = ObfuscationReflectionHelper.getPrivateValue(RenderGlobal.class, renderGlobal, "viewFrustrum");
//			renderDispatcher = ObfuscationReflectionHelper.getPrivateValue(RenderGlobal.class, renderGlobal, "renderDispatcher");
//		} catch (ReflectionHelper.UnableToFindFieldException | ReflectionHelper.UnableToAccessFieldException e) {
//			e.printStackTrace();
//			return;
//		}
//
//		for (TIntIterator iterator = toRerenderSet.iterator(); iterator.hasNext(); ) {
//			RenderChunk renderchunk = view.renderChunks[iterator.next()];
//			renderDispatcher.updateChunkNow(renderchunk);
//		}


//		final VertexBuffer[] worldRenderers = Minecraft.getMinecraft().renderGlobal.worldRenderers;
//		final EntityLivingBase renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
//
//
//
//		ArrayList<VertexBuffer> renderersToUpdate = new ArrayList<VertexBuffer>();
//
//		for (TIntIterator iterator = toRerenderSet.iterator(); iterator.hasNext(); ) {
//			VertexBuffer worldRenderer = worldRenderers[iterator.next()];
//
//			if(worldRenderer != null) {
//				worldRenderer.markDirty();
//				renderersToUpdate.add(worldRenderer);
//			}
//		}
//
//		if(FunkyLocomotion.redrawChunksInstantly) {
//			for (VertexBuffer worldRenderer : renderersToUpdate) {
//				worldRenderer.updateRenderer(renderViewEntity);
//			}
//		}else{
//			List worldRenderersToUpdate = Minecraft.getMinecraft().renderGlobal.worldRenderersToUpdate;
//
//			worldRenderersToUpdate.removeAll(renderersToUpdate);
//			worldRenderersToUpdate.addAll(0, renderersToUpdate);
//		}
//
//		toRerenderSet.clear();
	}
}
