package com.rwtema.funkylocomotion;

import com.rwtema.funkylocomotion.blocks.FLBlocks;
import com.rwtema.funkylocomotion.items.FLItems;
import com.rwtema.funkylocomotion.items.ItemBlockTeleporter;
import com.rwtema.funkylocomotion.items.ItemWrench;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Mod.EventBusSubscriber
public class Recipes {
	public static boolean shouldAddRecipes;
	public static boolean shouldAddFrameCopyResetRecipes;

	@SubscribeEvent
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
		IForgeRegistry<IRecipe> registry = event.getRegistry();

		for (String s : OreDictionary.getOreNames()) {
			if (s.startsWith("ingot"))
				LogHelper.info(s);
		}

		if (shouldAddRecipes) {
			Object lapis = "gemLapis";
			Object gearEnderium = getOreWithVanillaFallback(Blocks.PISTON, "thermalexpansion:machineFrame", "itemMachineChassi");
			Object diamond = "gemDiamond";
			Object nuggetSignalum = getOreWithVanillaFallback("dustRedstone", "nuggetSignalum", "itemRedstoneAlloy");
			Object ingotInvar = getOreWithVanillaFallback("ingotIron", "ingotInvar");
			Object IngotInvarIron = getOreWithVanillaFallback("ingotIron", "ingotInvar", "ingotSteel");
			Object nuggetInvar = getOreWithVanillaFallback("stickWood", "nuggetInvar", "nuggetIron");
			Object nuggetIron = getOreWithVanillaFallback("stickWood", "nuggetIron");
			Object nuggetEnderium = getOreWithVanillaFallback(Items.ENDER_EYE, "nuggetEnderium", "ingotPulsatingIron");
			Object dustEnderium = getOreWithVanillaFallback(Items.ENDER_PEARL, "dustEnderium", "nuggetPulsatingIron");
			Object ingotElectrum = getOreWithVanillaFallback("ingotGold", "ingotElectrum", "ingotVibrantAlloy");
			Object dustGlowstone = getOreWithVanillaFallback("dustGlowstone", "ingotEnergeticAlloy");

			ResourceLocation name;
			name = new ResourceLocation("funkylocomotion:frame");
			registry.register(new ShapedOreRecipe(name, new ItemStack(FLBlocks.FRAMES[0], 8, 0), "III", "i i", "III", 'I', ingotInvar, 'i', nuggetInvar).setRegistryName(name));

			name = new ResourceLocation("funkylocomotion:wrench_normal");
			registry.register(new ShapedOreRecipe(name, new ItemStack(FLItems.WRENCH, 1, ItemWrench.metaWrenchNormal), "I  ", " i ", "  I", 'I', "ingotIron", 'i', nuggetIron).setRegistryName(name));

			name = new ResourceLocation("funkylocomotion:wrench_eye");
			registry.register(new ShapelessOreRecipe(name, new ItemStack(FLItems.WRENCH, 1, ItemWrench.metaWrenchEye), Items.ENDER_EYE, dustEnderium, dustEnderium, new ItemStack(FLItems.WRENCH, 1, ItemWrench.metaWrenchNormal)).setRegistryName(name));

			name = new ResourceLocation("funkylocomotion:wrench_hammer");
			registry.register(new ShapedOreRecipe(name, new ItemStack(FLItems.WRENCH, 1, ItemWrench.metaWrenchHammer), "WIW", " i ", " i ", 'I', IngotInvarIron, 'W', new ItemStack(FLItems.WRENCH, 1, ItemWrench.metaWrenchNormal), 'i', "ingotIron").setRegistryName(name));

			name = new ResourceLocation("funkylocomotion:pusher");
			registry.register(new ShapedOreRecipe(name, new ItemStack(FLBlocks.PUSHER, 1, 0), "EEE", "CGC", "CTC", 'E', nuggetEnderium, 'G', gearEnderium, 'C', ingotInvar, 'T', diamond).setRegistryName(name));

			name = new ResourceLocation("funkylocomotion:puller");
			registry.register(new ShapelessOreRecipe(name, new ItemStack(FLBlocks.PUSHER, 1, 1), new ItemStack(FLBlocks.PUSHER, 1, 0), "slimeball", "dustRedstone", "dustRedstone", "dustRedstone").setRegistryName(name));

			name = new ResourceLocation("funkylocomotion:slider");
			registry.register(new ShapelessOreRecipe(name, new ItemStack(FLBlocks.SLIDER, 1, 0), new ItemStack(FLBlocks.PUSHER, 1, 0), nuggetSignalum, lapis, lapis, lapis).setRegistryName(name));

			name = new ResourceLocation("funkylocomotion:booster");
			registry.register(new ShapedOreRecipe(name, new ItemStack(FLBlocks.BOOSTER, 1, 0), "EEE", "CGC", "CTC", 'E', ingotElectrum, 'G', gearEnderium, 'C', ingotInvar, 'T', FLBlocks.PUSHER).setRegistryName(name));

			name = new ResourceLocation("funkylocomotion:projector");
			registry.register(new ShapedOreRecipe(name, new ItemStack(FLBlocks.FRAME_PROJECTOR, 1, 0), "EEE", "CGC", "CTC", 'E', dustGlowstone, 'G', gearEnderium, 'C', ingotInvar, 'T', new ItemStack(FLBlocks.PUSHER, 1, 1)).setRegistryName(name));

			name = FLBlocks.TELEPORTER.getRegistryName();
			registry.register(new ShapedOreRecipe(name,
					ItemBlockTeleporter.assignNullID(new ItemStack(FLBlocks.TELEPORTER, 2)),
					"EEE", "PNY", "EEE",
					'E', nuggetEnderium,
					'P', new ItemStack(FLBlocks.PUSHER, 1, 0),
					'N', Items.ENDER_PEARL,
					'Y', new ItemStack(FLBlocks.PUSHER, 1, 1)
			) {
				@Nonnull
				@Override
				public ItemStack getCraftingResult(@Nonnull InventoryCrafting var1) {
					return ItemBlockTeleporter.assignRandomID(super.getCraftingResult(var1));
				}
			}.setRegistryName(name));

			name = new ResourceLocation("funkylocomotion:teleporter_id");
			registry.register(new ShapelessOreRecipe(name,
					ItemBlockTeleporter.assignNullID(new ItemStack(FLBlocks.TELEPORTER, 2)),
					FLBlocks.TELEPORTER, FLBlocks.TELEPORTER
			) {
				@Nonnull
				@Override
				public ItemStack getCraftingResult(@Nonnull InventoryCrafting var1) {
					return ItemBlockTeleporter.assignRandomID(super.getCraftingResult(var1));
				}
			}.setRegistryName(name));


			name = new ResourceLocation("funkylocomotion:mass_frame_corner");
			registry.register(new ShapedOreRecipe(
					name,
					new ItemStack(FLBlocks.MASS_FRAME_CORNER),
					"opo",
					"pep",
					"opo",
					'p', FLBlocks.MASS_FRAME_EDGE,
					'o', ingotElectrum,
					'e', FLBlocks.PUSHER
			).setRegistryName(name));

			name = new ResourceLocation("funkylocomotion:mass_frame_edge");
			registry.register(new ShapedOreRecipe(
					name,
					new ItemStack(FLBlocks.MASS_FRAME_EDGE, 6),
					"fff",
					"p p",
					"fff",
					'f', Ingredient.fromStacks(Stream.of(FLBlocks.FRAMES).flatMap(blockIn -> IntStream.range(0, 16).mapToObj(i -> new ItemStack(blockIn, 1, i))).toArray(ItemStack[]::new)),
					'p', nuggetEnderium

			).setRegistryName(name));
		}

		if (shouldAddFrameCopyResetRecipes) {
			ItemStack basicFrame = new ItemStack(FLBlocks.FRAMES[0], 1, 0);
			//ArrayList<ItemStack> list = new ArrayList<ItemStack>(64);
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 16; j++) {
					if (i != 0 || j != 0) {
						ItemStack stack = new ItemStack(FLBlocks.FRAMES[i], 1, j);
						//list.add(stack);

						ResourceLocation name = new ResourceLocation("funkylocomotion:frames_" + i + "_" + j);
						registry.register(new ShapelessOreRecipe(name, new ItemStack(FLBlocks.FRAMES[i], 2, j), stack, basicFrame).setRegistryName(name));
					}
				}
			}

			/* FIXME 1.12 - what was this doing exactly?
			ShapelessOreRecipe t = new ShapelessOreRecipe(FLBlocks.FRAMES[0], new ItemStack(FLBlocks.FRAMES[0]));
			t.getInput().clear();
			t.getInput().add(list);

			registry.register(t);
			*/
		}
	}

	public static Object getOreWithVanillaFallback(Object vanillaFallback, String... moddedOre) {
		for (String modOre : moddedOre) {
			if (OreDictionary.getOres(modOre).size() > 0)
				return modOre;
		}
		return vanillaFallback;
	}

	/*
	public static void registerBlockRecipe(IForgeRegistry<IRecipe> registry, Block block, IRecipe recipe) {
		registry.register(recipe);
		RecipeSorter.register("funky:recipe:" + recipe.getClass().getName(), recipe.getClass(), SHAPELESS, "");
	}
	*/
}
