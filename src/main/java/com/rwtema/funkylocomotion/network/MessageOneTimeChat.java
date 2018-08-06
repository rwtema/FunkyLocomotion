package com.rwtema.funkylocomotion.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

public class MessageOneTimeChat implements IMessage {
	ITextComponent component;
	int id;

	public MessageOneTimeChat() {
	}

	public MessageOneTimeChat(ITextComponent component, int id) {
		this.component = component;
		this.id = id;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		try {
			component = (new PacketBuffer(buf)).readTextComponent();
			id = buf.readInt();
		} catch (IOException e) {
		    throw new RuntimeException(e);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		(new PacketBuffer(buf)).writeTextComponent(component);
		buf.writeInt(id);
	}

	@SideOnly(Side.CLIENT)
	private void handlePacket(MessageContext ctx) {
		component = net.minecraftforge.event.ForgeEventFactory.onClientChat(ChatType.CHAT, component);
		if (component == null) return;
		Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(component, id);
	}

	public static class Handler implements IMessageHandler<MessageOneTimeChat, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(final MessageOneTimeChat message, final MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> message.handlePacket(ctx));
			return null;
		}
	}

}
