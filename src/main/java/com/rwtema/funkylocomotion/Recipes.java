package com.rwtema.funkylocomotion;

import com.rwtema.funkylocomotion.items.ItemWrench;
import cpw.mods.fml.common.registry.GameRegistry;
import java.util.ArrayList;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class Recipes {
	public static boolean shouldAddRecipes;
	public static boolean shouldAddFrameCopyResetRecipes;

	public static void addRecipes() {
		for (String s : OreDictionary.getOreNames()) {
			if (s.startsWith("ingot"))
				LogHelper.info(s);
		}

		if (shouldAddRecipes) {
			Object lapis = "gemLapis";
			Object gearEnderium = getOreWithVanillaFallback(Blocks.piston, "thermalexpansion:machineFrame");
			Object tesseract = "gemDiamond";
			Object nuggetSignalum = getOreWithVanillaFallback("dustRedstone", "nuggetSignalum");
			Object ingotInvar = getOreWithVanillaFallback(Blocks.heavy_weighted_pressure_plate, "ingotInvar");
			Object nuggetInvar = getOreWithVanillaFallback("stickWood", "nuggetInvar", "nuggetIron");
			Object nuggetIron = getOreWithVanillaFallback("stickWood", "nuggetIron");
			Object nuggetEnderium = getOreWithVanillaFallback(Items.ender_eye, "nuggetEnderium", "ingotPhasedIron");
			Object dustEnderium = getOreWithVanillaFallback(Items.ender_pearl, "dustEnderium", "nuggetPhasedIron");
			Object ingotElectrum = getOreWithVanillaFallback("ingotGold", "ingotElectrum", "ingotPhasedGold");

			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(FunkyLocomotion.frame[0], 8, 0), "III", "i i", "III", 'I', ingotInvar, 'i', nuggetInvar));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(FunkyLocomotion.wrench, 1, ItemWrench.metaWrenchNormal), "I  ", " i ", "  I", 'I', "ingotIron", 'i', nuggetIron));
			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(FunkyLocomotion.wrench, 1, ItemWrench.metaWrenchEye), Items.ender_eye, dustEnderium, dustEnderium, new ItemStack(FunkyLocomotion.wrench, 1, ItemWrench.metaWrenchNormal)));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(FunkyLocomotion.pusher, 1, 0), "EEE", "CGC", "CTC", 'E', nuggetEnderium, 'G', gearEnderium, 'C', ingotInvar, 'T', tesseract));
			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(FunkyLocomotion.pusher, 1, 6), new ItemStack(FunkyLocomotion.pusher, 1, 0), "slimeball", "dustRedstone", "dustRedstone", "dustRedstone"));
			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(FunkyLocomotion.slider, 1, 0), new ItemStack(FunkyLocomotion.pusher, 1, 0), nuggetSignalum, lapis, lapis, lapis));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(FunkyLocomotion.booster, 1, 0), "EEE", "CGC", "CTC", 'E', ingotElectrum, 'G', gearEnderium, 'C', ingotInvar, 'T', FunkyLocomotion.pusher));
		}

		if (shouldAddFrameCopyResetRecipes) {
			ItemStack basicFrame = new ItemStack(FunkyLocomotion.frame[0], 1, 0);
			ArrayList<ItemStack> list = new ArrayList<ItemStack>(64);
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 16; j++) {
					if (i != 0 || j != 0) {
						ItemStack stack = new ItemStack(FunkyLocomotion.frame[i], 1, j);
						list.add(stack);

						GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(FunkyLocomotion.frame[i], 2, j), stack, basicFrame));

					}
				}
			}

			ShapelessOreRecipe t = new ShapelessOreRecipe(FunkyLocomotion.frame[0], new ItemStack(FunkyLocomotion.frame[0]));
			t.getInput().clear();
			t.getInput().add(list);

			GameRegistry.addRecipe(t);
		}
	}

	public static Object getOreWithVanillaFallback(Object vanillaFallback, String... moddedOre) {
		for (String modOre : moddedOre) {
			if (OreDictionary.getOres(modOre).size() > 0)
				return modOre;
		}
		return vanillaFallback;
	}
}
