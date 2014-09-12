package com.rwtema.funkylocomotion.network;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.blocks.TileMovingClient;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import framesapi.BlockPos;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class MessageClearTile implements IMessage {
    int x, y, z;

    public MessageClearTile() {

    }


    public MessageClearTile(BlockPos pos) {
        this(pos.x, pos.y, pos.z);
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

        Block b = clientWorld.getBlock(x, y, z);
        if (b == FunkyLocomotion.moving)
            return;

        TileEntity tile = clientWorld.getTileEntity(x, y, z);

        if (tile == null)
            return;

        clientWorld.loadedTileEntityList.remove(tile);
        Chunk chunk = clientWorld.getChunkFromBlockCoords(x, z);
        chunk.chunkTileEntityMap.remove(new ChunkCoordinates(x & 15, y, z & 15));

        TileMovingClient.cachedTiles.put(new ChunkCoordinates(x, y, z), tile);
    }


    public static class Handler implements IMessageHandler<MessageClearTile, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageClearTile message, MessageContext ctx) {
            message.handlePacket(ctx);
            return null;
        }
    }
}
