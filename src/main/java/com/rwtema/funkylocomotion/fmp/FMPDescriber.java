package com.rwtema.funkylocomotion.fmp;

import codechicken.lib.data.MCDataOutputWrapper;
import codechicken.lib.packet.PacketCustom;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import framesapi.BlockPos;
import framesapi.IDescriptionProxy;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import scala.collection.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class FMPDescriber implements IDescriptionProxy {
    @Override
    public String getID() {
        return "FMP";
    }

    @Override
    public boolean canHandleTile(TileEntity tile) {
        return tile instanceof TileMultipart;
    }

    @Override
    public void addDescriptionToTags(NBTTagCompound descriptor, TileEntity tile) {
        TileMultipart t = (TileMultipart) tile;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        MCDataOutputWrapper s = new MCDataOutputWrapper(new DataOutputStream(bytes));
        s.writeByte(2);
        t.writeDesc(s);
        descriptor.setByteArray("Tile", bytes.toByteArray());
    }

    @Override
    public TileEntity recreateTileEntity(NetworkManager net, NBTTagCompound tag, Block block, int meta, BlockPos pos, World world) {
        if (!tag.hasKey("Tile", 7))
            return block.createTileEntity(world, meta);

        byte[] bytes = tag.getByteArray("Tile");

        PacketCustom packet = new PacketCustom(Unpooled.wrappedBuffer(bytes));
        int nparts = packet.readByte();
        ArrayList<TMultiPart> parts = new ArrayList<TMultiPart>();


        for (int i = 0; i < nparts; i++) {
            TMultiPart part = MultiPartRegistry.readPart(packet);
            part.readDesc(packet);
            parts.add(part);
        }

        if (parts.size() == 0)
            return block.createTileEntity(world, meta);

        TileEntity blanktile = block.createTileEntity(world, meta);

        scala.collection.Iterable<TMultiPart> parts1 = JavaConversions.collectionAsScalaIterable(parts);

//        TileMultipart tile = MultipartGenerator.generateCompositeTile(blanktile, parts1, true);
//        tile.loadParts(parts1);

        return block.createTileEntity(world, meta);
    }
}
