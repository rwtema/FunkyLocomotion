package com.rwtema.funkylocomotion.fmp;

import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.TSlottedPart;
import codechicken.multipart.minecraft.McMetaPart;
import com.rwtema.funkylocomotion.FunkyLocomotion;
import net.minecraft.block.Block;

public class FramePart extends McMetaPart implements TSlottedPart {
    private int slotMask;

    {
        slotMask = 0;
        for (int i = 15; i <= 26; i++)
            slotMask = slotMask | (1 << i);
    }

    @Override
    public String getType() {
        return FunkyLocomotion.MODID + ":frame";
    }

    @Override
    public int getSlotMask() {
        return slotMask;
    }

    @Override
    public Block getBlock() {
        return null;
    }

    @Override
    public Cuboid6 getBounds() {
        return null;
    }
}
