package com.rwtema.funkylocomotion;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;

public class Recipes {
    public static void addRecipes() {
        if (Loader.isModLoaded("ThermalFoundation") && Loader.isModLoaded("ThermalExpansion")) {
            ItemStack gearEnderium = GameRegistry.findItemStack("ThermalFoundation", "gearEnderium", 1);
            ItemStack tesseract = new ItemStack(GameRegistry.findBlock("ThermalExpansion", "Tesseract"));
            ItemStack nuggetSignalum = GameRegistry.findItemStack("ThermalFoundation", "nuggetSignalum", 1);
            ItemStack ingotInvar = GameRegistry.findItemStack("ThermalFoundation", "ingotInvar", 1);

//            ItemStack capacitorReinforced = GameRegistry.findItemStack("ThermalExpansion", "capacitorReinforced", 1);
//            ItemStack capacitorResonant = GameRegistry.findItemStack("ThermalExpansion", "capacitorResonant", 1);
//            ItemStack powerCoilElectrumStack = GameRegistry.findItemStack("ThermalExpansion", "powerCoilElectrum", 1);
//
//            ItemStack enderiumIngot = GameRegistry.findItemStack("ThermalFoundation", "ingotEnderium", 1);
//            ItemStack enderiumNugget = GameRegistry.findItemStack("ThermalFoundation", "nuggetEnderium", 1);
//            ItemStack electrumIngot = GameRegistry.findItemStack("ThermalFoundation", "ingotElectrum", 1);
//
//            ItemStack machineResonant = new ItemStack(GameRegistry.findBlock("ThermalExpansion", "Frame"), 1, 3);
//            ItemStack machineRedstone = new ItemStack(GameRegistry.findBlock("ThermalExpansion", "Frame"), 1, 2);
//            ItemStack hardenedGlass = new ItemStack(GameRegistry.findBlock("ThermalExpansion", "Glass"));
//            ItemStack tankResonant = new ItemStack(GameRegistry.findBlock("ThermalExpansion", "Tank"), 1, 4);
//            ItemStack nuggerInvar = GameRegistry.findItemStack("ThermalFoundation", "nuggetInvar", 1);
//            ItemStack nuggerSilver = GameRegistry.findItemStack("ThermalFoundation", "nuggetIron", 1);

            GameRegistry.addRecipe(new ItemStack(FunkyLocomotion.frame[0], 8, 0), "III", "i i", "III", 'I', Items.iron_ingot, 'i', Blocks.heavy_weighted_pressure_plate);
            GameRegistry.addRecipe(new ItemStack(FunkyLocomotion.wrench, 1, 0), "I  ", " i ", "  I", 'I', Items.iron_ingot, 'i', Items.stick);
            GameRegistry.addRecipe(new ItemStack(FunkyLocomotion.pusher, 1, 0), "EEE", "CGC", "CTC", 'E', Items.ender_pearl, 'G', gearEnderium, 'C', ingotInvar, 'T', tesseract);
            GameRegistry.addShapelessRecipe(new ItemStack(FunkyLocomotion.pusher, 1, 6), new ItemStack(FunkyLocomotion.pusher, 1, 0), Items.slime_ball, nuggetSignalum, nuggetSignalum, nuggetSignalum
            );
        } else {
            GameRegistry.addRecipe(new ItemStack(FunkyLocomotion.frame[0], 8, 0), "III", "i i", "III", 'I', Blocks.heavy_weighted_pressure_plate, 'i', Items.iron_ingot);
            GameRegistry.addRecipe(new ItemStack(FunkyLocomotion.wrench, 1, 0), "I  ", " i ", "  I", 'I', Items.iron_ingot, 'i', Items.stick);
            GameRegistry.addRecipe(new ItemStack(FunkyLocomotion.pusher, 1, 0), "EEE", "CGC", "CTC", 'E', Items.ender_pearl, 'G', Items.ender_eye, 'C', Items.iron_ingot, 'T', Blocks.ender_chest);
            GameRegistry.addShapelessRecipe(new ItemStack(FunkyLocomotion.pusher, 1, 6), new ItemStack(FunkyLocomotion.pusher, 1, 0), Items.slime_ball, Items.redstone, Items.redstone, Items.redstone);
        }

        ArrayList<ItemStack> list = new ArrayList<ItemStack>(64);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 16; j++) {
                if (i != 0 || j != 0)
                    list.add(new ItemStack(FunkyLocomotion.frame[i], 1, j));
            }
        }

        ShapelessOreRecipe t = new ShapelessOreRecipe(FunkyLocomotion.frame[0], new ItemStack(FunkyLocomotion.frame[0]));
        t.getInput().clear();
        t.getInput().add(list);

        GameRegistry.addRecipe(t);


    }
}
