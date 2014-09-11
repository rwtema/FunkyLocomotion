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
import net.minecraftforge.common.util.ForgeDirection;

public class MessageMoveBlock implements IMessage {
    int x, y, z, dir;

    public MessageMoveBlock() {

    }

    public MessageMoveBlock(BlockPos pos, ForgeDirection dir) {
        this(pos, dir.ordinal());
    }

    public MessageMoveBlock(BlockPos pos, int dir) {
        this(pos.x, pos.y, pos.z, dir);
    }

    public MessageMoveBlock(int x, int y, int z, int dir) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        dir = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(dir);
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


    public static class Handler implements IMessageHandler<MessageMoveBlock, IMessage> {
        @Override
        public IMessage onMessage(MessageMoveBlock message, MessageContext ctx) {
            message.handlePacket(ctx);
            return null;
        }
    }
}
