package com.rwtema.funkylocomotion.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

public class WordDictionary implements IResourceManagerReloadListener {
	static final private char[] chars = new char[]{
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
			'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
			'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
			'Y', 'Z'
	};
	private static String[] words;

	static {
		((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new WordDictionary());
	}

	public static String[] getWords() {
		if (words != null) return words;

		LanguageMap translator = ObfuscationReflectionHelper.getPrivateValue(LanguageMap.class, null, "field_74817_a", "instance");
		Map<String, String> languageList = ObfuscationReflectionHelper.getPrivateValue(LanguageMap.class, translator, "field_74816_c", "languageList");

		HashSet<String> wordSet = new HashSet<String>();

		for (char letter : chars) {
			wordSet.add(Character.toString(letter));
		}

		for (String s : languageList.values()) {
			String s1 = s.toLowerCase(Locale.US);
			for (String word : s1.split("[^{A-Za-z}]")) {
				int n = word.length();
				if (n <= 1)
					continue;

				wordSet.add(word.substring(0, 1).toUpperCase(Locale.ENGLISH) + word.substring(1).toLowerCase(Locale.ENGLISH));
			}
		}

		words = wordSet.toArray(new String[wordSet.size()]);

		return words;
	}

	@Override
	public void onResourceManagerReload(IResourceManager p_110549_1_) {
		words = null;
	}

}
