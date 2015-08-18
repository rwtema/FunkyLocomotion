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
	protected TileEntity loadTile(BlockPos pos, NBTTagCompound tag, Chunk chunk) {
		TileEntity tileEntity = super.loadTile(pos, tag, chunk);
		if(tileEntity instanceof INode){
			INode node = (INode) tileEntity;
			node.setNodeModifier(NodeModifier.FADING);
		}

		return tileEntity;
	}
}
