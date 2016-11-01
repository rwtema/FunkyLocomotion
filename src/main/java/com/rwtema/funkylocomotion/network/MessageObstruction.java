package com.rwtema.funkylocomotion.network;

import com.rwtema.funkylocomotion.particles.ParticleObstruction;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

	public MessageObstruction(BlockPos pos, EnumFacing dir) {
		this(pos.getX(), pos.getY(), pos.getZ(), ((byte) (dir == null ? 6 : dir.ordinal())));
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
		public IMessage onMessage(final MessageObstruction message, final MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> message.handlePacket(ctx));
			return null;
		}
	}


}
