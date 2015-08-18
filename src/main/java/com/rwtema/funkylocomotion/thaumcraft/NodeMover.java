package com.rwtema.funkylocomotion.thaumcraft;

import com.rwtema.funkylocomotion.factory.DefaultMoveFactory;
import framesapi.BlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.nodes.NodeModifier;

public class NodeMover extends DefaultMoveFactory {
	@Override
	protected NBTTagCompound saveTile(BlockPos pos, Chunk chunk, NBTTagCompound tag) {
		TileEntity tile = chunk.getTileEntityUnsafe(pos.x & 15, pos.y, pos.z & 15);
		if (tile == null) return null;
		NBTTagCompound tagCompound = super.saveTile(pos, chunk, tag);
		tag.setInteger("OldDim", chunk.worldObj.provider.dimensionId);
		tag.setInteger("OldPosX", pos.x);
		tag.setInteger("OldPosY", pos.y);
		tag.setInteger("OldPosZ", pos.z);
		return tagCompound;
	}

	@Override
	protected TileEntity loadTile(BlockPos pos, NBTTagCompound tag, Chunk chunk) {
		TileEntity tileEntity = super.loadTile(pos, tag, chunk);
		if (tileEntity instanceof INode) {

			if (tag.getInteger("OldDim") == chunk.worldObj.provider.dimensionId) {
				if ((Math.abs(pos.x - tag.getInteger("OldPosX")) +
						Math.abs(pos.y - tag.getInteger("OldPosY")) +
						Math.abs(pos.z - tag.getInteger("OldPosZ"))) <= 1) {
					return tileEntity;
				}
			}

			INode node = (INode) tileEntity;
			NodeModifier nodeModifier = node.getNodeModifier();
			if(nodeModifier == NodeModifier.PALE){
				node.setNodeModifier(NodeModifier.FADING);
			} else if (nodeModifier != NodeModifier.FADING)
				node.setNodeModifier(NodeModifier.PALE);

		}

		return tileEntity;
	}
}
