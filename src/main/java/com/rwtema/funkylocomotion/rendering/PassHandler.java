package com.rwtema.funkylocomotion.rendering;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockRenderLayer;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum PassHandler {

	ZERO(0, BlockRenderLayer.SOLID, BlockRenderLayer.CUTOUT, BlockRenderLayer.CUTOUT_MIPPED),
	ONE(1, BlockRenderLayer.TRANSLUCENT) {
		@Override
		public void setupRendering() {
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}
	};

	public final int pass;
	public final BlockRenderLayer[] layers;
	public final Set<BlockRenderLayer> layerSet;

	PassHandler(int pass, BlockRenderLayer... layers) {
		this.pass = pass;
		this.layers = layers;
		layerSet = EnumSet.noneOf(BlockRenderLayer.class);
		Collections.addAll(layerSet, layers);
	}

	public static PassHandler getHandler(int pass) {
		switch (pass) {
			case 0:
				return ZERO;
			case 1:
				return ONE;
			default:
				throw new IllegalArgumentException();
		}
	}

	public void setupRendering() {

	}
}

