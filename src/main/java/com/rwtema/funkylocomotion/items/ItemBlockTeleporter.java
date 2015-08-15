package com.rwtema.funkylocomotion.items;

import com.rwtema.funkylocomotion.rendering.WordDictionary;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemBlockTeleporter extends ItemBlock {

	public static final String NBT_TELEPORTER_ID = "TeleportID";

	public ItemBlockTeleporter(Block block) {
		super(block);
		this.setMaxStackSize(2);
	}

	public static Random rand = new Random();

	public static ItemStack assignRandomID(ItemStack item) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger(NBT_TELEPORTER_ID, Math.abs(rand.nextInt()) & 0xFFFFF);
		item.setTagCompound(tag);
		return item;
	}

	public static ItemStack assignNullID(ItemStack item) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger(NBT_TELEPORTER_ID, 0);
		item.setTagCompound(tag);
		return item;
	}

	public void onUpdate(ItemStack item, World world, Entity entity, int slot, boolean held) {
		NBTTagCompound tag = item.getTagCompound();
		if (tag == null) return;

		if (tag.hasKey(NBT_TELEPORTER_ID) && tag.getInteger(NBT_TELEPORTER_ID) == 0) {
			tag.removeTag(NBT_TELEPORTER_ID);
			if (tag.hasNoTags()) item.setTagCompound(null);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		list.add((new ItemStack(item, 1, 0)));
	}

	public boolean onItemUse(ItemStack item, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		NBTTagCompound tag = item.getTagCompound();
		if (tag == null || tag.getInteger(NBT_TELEPORTER_ID) == 0) {
			if (world.isRemote) {
				player.addChatComponentMessage(new ChatComponentTranslation("frame.teleport.no_id.0"));
				player.addChatComponentMessage(new ChatComponentTranslation("frame.teleport.no_id.1"));
			}
			return false;
		}
		return super.onItemUse(item, player, world, x, y, z, side, hitX, hitY, hitZ);
	}

	@SuppressWarnings("unchecked")
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack item, EntityPlayer player, List list, boolean debug) {
		super.addInformation(item, player, list, debug);

		NBTTagCompound tagCompound = item.getTagCompound();
		if (tagCompound == null || !tagCompound.hasKey(NBT_TELEPORTER_ID)) {
			list.add(StatCollector.translateToLocal("frame.teleport.no_id.0"));
			list.add(StatCollector.translateToLocal("frame.teleport.no_id.1"));
			return;
		}

		int id = tagCompound.getInteger(NBT_TELEPORTER_ID);

		if (id == 0) return;

		if (id < 0) id = -id;

		id = id & (0xFFFFF);

		StringBuilder builder = new StringBuilder().append(StatCollector.translateToLocal("frame.teleport.id")).append(": ").append('"');

		String[] words = WordDictionary.getWords();

		while (id != 0) {
			builder.append(words[(int) (id % words.length)]);
			id /= words.length;
		}

		builder.append('"');

		list.add(builder.toString());
	}



}
