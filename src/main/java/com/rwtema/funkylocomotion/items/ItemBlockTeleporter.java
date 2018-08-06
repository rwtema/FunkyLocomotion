package com.rwtema.funkylocomotion.items;

import com.rwtema.funkylocomotion.rendering.WordDictionary;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ItemBlockTeleporter extends ItemBlock {

	public static final String NBT_TELEPORTER_ID = "TeleportID";
	public static Random rand = new Random();

	public ItemBlockTeleporter(Block block) {
		super(block);
		this.setMaxStackSize(2);
	}

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

	@Nonnull
	@Override
	public EnumActionResult onItemUse(@Nonnull EntityPlayer playerIn, World worldIn, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = playerIn.getHeldItem(hand);
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null || tag.getInteger(NBT_TELEPORTER_ID) == 0) {
			if (worldIn.isRemote) {
				playerIn.sendMessage(new TextComponentTranslation("frame.teleport.no_id.0"));
				playerIn.sendMessage(new TextComponentTranslation("frame.teleport.no_id.1"));
			}
			return EnumActionResult.FAIL;
		}
		return super.onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull ItemStack item, @Nullable World world, @Nonnull List<String> list, @Nonnull ITooltipFlag advanced) {
		super.addInformation(item, world, list, advanced);

		NBTTagCompound tagCompound = item.getTagCompound();
		if (tagCompound == null || !tagCompound.hasKey(NBT_TELEPORTER_ID)) {
			list.add(I18n.format("frame.teleport.no_id.0"));
			list.add(I18n.format("frame.teleport.no_id.1"));
			return;
		}

		int id = tagCompound.getInteger(NBT_TELEPORTER_ID);

		if (id == 0) return;

		if (id < 0) id = -id;

		id = id & (0xFFFFF);

		StringBuilder builder = new StringBuilder().append(I18n.format("frame.teleport.id")).append(": ").append('"');

		String[] words = WordDictionary.getWords();

		while (id != 0) {
			builder.append(words[id % words.length]);
			id /= words.length;
		}

		builder.append('"');

		list.add(builder.toString());
	}


}
