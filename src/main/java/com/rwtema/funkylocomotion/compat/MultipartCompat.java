package com.rwtema.funkylocomotion.compat;

import org.apache.commons.lang3.Validate;

import com.rwtema.funkylocomotion.LogHelper;
import com.rwtema.funkylocomotion.api.FunkyCapabilities;
import com.rwtema.funkylocomotion.api.FunkyRegistry;
import com.rwtema.funkylocomotion.api.IMoveFactory;
import com.rwtema.funkylocomotion.blocks.BlockStickyFrame;
import com.rwtema.funkylocomotion.blocks.FLBlocks;
import com.rwtema.funkylocomotion.factory.DefaultMoveFactory;
import com.rwtema.funkylocomotion.helper.BlockHelper;

import codechicken.microblock.Microblock;
import codechicken.multipart.BlockMultipart;
import codechicken.multipart.PacketScheduler;
import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileCache;
import codechicken.multipart.TileMultipart;
import codechicken.multipart.handler.MultipartMod;
import cofh.core.render.IBlockAppearance;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@ModCompat(modid = "forgemultipartcbe")
public class MultipartCompat extends CompatHandler {

	@Override
	public void init() {
		// Add a proxy for checking stickiness on multipart blocks
		Validate.notNull(FunkyRegistry.INSTANCE).registerProxy(BlockMultipart.class, FunkyCapabilities.STICKY_BLOCK, (world, pos, side) -> {
			TileEntity tile = world.getTileEntity(pos);
			// Make sure a multipart tile entity exists in this tile
			if (tile != null && tile instanceof TileMultipart) {
				// Check if the tile has a multipart on the side being checked for stickiness
				TileMultipart multipart = (TileMultipart) tile;
				TMultiPart sidePart = multipart.partMap(side.ordinal());
				if (sidePart != null)
				{
					// Check if the part on the side being checked is a microblock
					if (sidePart instanceof Microblock)
					{
						// Check if the microblock has the same material as the frame block
						Microblock sideMicro = (Microblock) sidePart;
						if (sideMicro.getIMaterial().getMaterialID().startsWith(FLBlocks.FRAMES[0].getRegistryName().toString()))
						{
							return true;
						}
					}
				}
			}
			return false;
		});
		// Add a move factory for multipart blocks
		Validate.notNull(FunkyRegistry.INSTANCE).registerMoveFactoryBlockClass(BlockMultipart.class, new IMoveFactory() {
			
			private DefaultMoveFactory defaultFactory = new DefaultMoveFactory();
			
			@Override
			public boolean recreateBlock(World world, BlockPos pos, NBTTagCompound tag) {
				// Get the multipart block
				Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(MultipartMod.modID(), "multipart_block"));
				
				// Set the block at the given position to the multipart block
				Chunk chunk = world.getChunkFromBlockCoords(pos);
				BlockHelper.silentSetBlock(chunk, pos, block, 0);
				// Extract the multiparts from the NBT data
				TileMultipart multipart = TileMultipart.createFromNBT(tag, world);
				// Add all of the multiparts back into the world (simple adding the created tile entity does not work)
				for (TMultiPart p : multipart.jPartList())
					TileMultipart.addPart(world, pos, p);
				
				return true;
			}
			
			@Override
			public NBTTagCompound destroyBlock(World world, BlockPos pos) {
				// Create a blank NBT
				NBTTagCompound tag = new NBTTagCompound();
				// Check if a multipart tile entity exists in this position
				TileEntity te = world.getTileEntity(pos);				
				if (te != null && te instanceof TileMultipart)
				{
					// Write the mutlipart's data into the NBT
					TileMultipart multipart = (TileMultipart) te;
					multipart.writeToNBT(tag);
				}
				// Add the default data to the NBT to ensure recreateBlock gets called
				tag.merge(defaultFactory.destroyBlock(world, pos));
				return tag;
			}
		});

	}

}
