package com.rwtema.funkylocomotion.network;

import com.rwtema.funkylocomotion.particles.ParticleObstruction;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import framesapi.BlockPos;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.util.ForgeDirection;

public class MessageObstruction implements IMessage {
    int x;
    int y;
    int z;
    byte side;

    public MessageObstruction() {

    }

    public MessageObstruction(int x, int y, int z, byte side) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.side = side;
    }

    public MessageObstruction(BlockPos pos, ForgeDirection dir) {
        this(pos.x, pos.y, pos.z, ((byte) dir.ordinal()));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        side = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(side);
    }

    @SideOnly(Side.CLIENT)
    private void handlePacket(MessageContext ctx) {
        for (int i = 0; i < 10; i++)
            Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleObstruction(Minecraft.getMinecraft().theWorld, x, y, z, side));

    }

    public static class Handler implements IMessageHandler<MessageObstruction, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageObstruction message, MessageContext ctx) {
            message.handlePacket(ctx);
            return null;
        }
    }


}
