package com.rwtema.funkylocomotion.network;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.blocks.TileMovingClient;
import com.rwtema.funkylocomotion.fakes.FakeWorldClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.ref.WeakReference;

public class MessageClearTile implements IMessage {
	int x, y, z;

	public MessageClearTile() {

	}


	public MessageClearTile(BlockPos pos) {
		this(pos.getX(), pos.getY(), pos.getZ());
	}

	public MessageClearTile(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
	}

	@SideOnly(Side.CLIENT)
	public void handlePacket(MessageContext ctx) {
		World clientWorld = FunkyLocomotion.proxy.getClientWorld();
		BlockPos pos = new BlockPos(x, y, z);

		IBlockState state = clientWorld.getBlockState(pos);
		Block b = state.getBlock();
		if (b == Blocks.AIR || b == FunkyLocomotion.moving)
			return;

		TileEntity tile = clientWorld.getTileEntity(pos);

		if (tile == null)
			return;

		clientWorld.loadedTileEntityList.remove(tile);
		Chunk chunk = clientWorld.getChunkFromBlockCoords(pos);
		chunk.getTileEntityMap().remove(pos);
		tile.invalidate();

		if (!FakeWorldClient.isValid(clientWorld)) return;
		TileMovingClient.cachedTiles.put(pos.toImmutable(), new WeakReference<>(tile));
	}


	public static class Handler implements IMessageHandler<MessageClearTile, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(final MessageClearTile message, final MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> message.handlePacket(ctx));
			return null;
		}
	}
}
