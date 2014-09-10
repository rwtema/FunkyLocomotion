package com.rwtema.funkylocomotion.items;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class ItemWrench extends Item {
    public ItemWrench() {
        super();
        this.setMaxStackSize(1);
        this.setTextureName("funkylocomotion:wrench");
        this.setUnlocalizedName("funkylocomotion:wrench");
        this.setCreativeTab(FunkyLocomotion.creativeTabFrames);
    }

    @Override
    public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
        return true;
    }
}
