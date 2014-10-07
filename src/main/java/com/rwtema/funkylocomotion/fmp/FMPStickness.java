package com.rwtema.funkylocomotion.fmp;

import codechicken.lib.vec.BlockCoord;
import codechicken.microblock.BlockMicroMaterial;
import codechicken.microblock.MicroMaterialRegistry;
import codechicken.microblock.Microblock;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.proxydelegates.ProxyRegistry;
import framesapi.IStickyBlock;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class FMPStickness implements IStickyBlock {
    private static final String keyOpen = BlockMicroMaterial.materialKey(FunkyLocomotion.frame[0], 0);


    public static void init(Block block) {
        ProxyRegistry.register(block, new FMPStickness(), IStickyBlock.class);
        MicroMaterialRegistry.registerMaterial(new BlockMicroMaterial(FunkyLocomotion.frame[0], 0), keyOpen);
//        BlockMicroMaterial.createAndRegister(FunkyLocomotion.frame[3], 15);
    }

    @Override
    public boolean isStickySide(World world, int x, int y, int z, ForgeDirection side) {
        TileMultipart t = TileMultipart.getTile(world, new BlockCoord(x, y, z));
        if (t == null)
            return false;

        TMultiPart tMultiPart = t.partMap(side.ordinal());
        if (tMultiPart == null)
            return false;

        if (!(tMultiPart instanceof Microblock))
            return false;

        String s = MicroMaterialRegistry.materialName(((Microblock) tMultiPart).getMaterial());
        return keyOpen.equals(s);

    }
}
