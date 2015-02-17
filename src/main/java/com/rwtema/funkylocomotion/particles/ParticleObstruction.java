package com.rwtema.funkylocomotion.particles;

import net.minecraft.client.particle.EntityReddustFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class ParticleObstruction extends EntityReddustFX {
    public static final Random RANDOM = new Random();
    public static final float r = 98/255.0F;
    public static final float g = 142/255.0F;
    public static final float b = 94/255.0F;

    public ParticleObstruction(World world, int x, int y, int z, byte side) {
        super(world,
                x + (side == 4 ? 0 : side == 5 ? 1 : RANDOM.nextDouble()),
                y + (side == 0 ? 0 : side == 1 ? 1 : RANDOM.nextDouble()),
                z + (side == 2 ? 0 : side == 3 ? 1 : RANDOM.nextDouble()),
                0,0,0);
        this.noClip = true;
        this.particleMaxAge *= 2;
    }

    @Override
    public void renderParticle(Tessellator tessellator, float p_70539_2_, float p_70539_3_, float p_70539_4_, float p_70539_5_, float p_70539_6_, float p_70539_7_) {
        if(!ObstructionHelper.shouldRenderParticles()) return;
        super.renderParticle(tessellator, p_70539_2_, p_70539_3_, p_70539_4_, p_70539_5_, p_70539_6_, p_70539_7_);
        tessellator.draw();

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        tessellator.startDrawingQuads();
        super.renderParticle(tessellator, p_70539_2_, p_70539_3_, p_70539_4_, p_70539_5_, p_70539_6_, p_70539_7_);
        tessellator.draw();
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        tessellator.startDrawingQuads();
    }
}

