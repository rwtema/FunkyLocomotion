package com.rwtema.funkylocomotion.fakes;

import com.rwtema.funkylocomotion.blocks.TileMovingClient;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.WorldEvent;

import java.util.HashMap;

@SideOnly(Side.CLIENT)
public class FakeWorldClient extends WorldClient {
    private static final HashMap<World, FakeWorldClient> cache = new HashMap<World, FakeWorldClient>();
    public double offset = 0;
    public ForgeDirection dir = ForgeDirection.UNKNOWN;
    final World world;
    final WorldClient worldClient;


    private FakeWorldClient(World world) {
        super(new NetHandlerPlayClient(Minecraft.getMinecraft(), null, new NetworkManager(true)), new WorldSettings(world.getWorldInfo()), world.provider.dimensionId, world.difficultySetting, world.theProfiler);
        this.world = world;
        this.worldClient = world instanceof WorldClient ? ((WorldClient) world) : null;
    }

    public static FakeWorldClient getFakeWorldWrapper(World world) {
        FakeWorldClient fakeWorldClient = cache.get(world);
        if (fakeWorldClient == null) {
            fakeWorldClient = new FakeWorldClient(world);
            cache.put(world, fakeWorldClient);
        }

        return fakeWorldClient;
    }

    public static void register(){
        MinecraftForge.EVENT_BUS.register(new FakeWorldManager());
    }

    @Override
    public boolean isBlockNormalCubeDefault(int p_147445_1_, int p_147445_2_, int p_147445_3_, boolean p_147445_4_) {
        Block block = this.getBlock(p_147445_1_, p_147445_2_, p_147445_3_);
        return block.isNormalCube(this, p_147445_1_, p_147445_2_, p_147445_3_);
    }

    @Override
    protected boolean chunkExists(int p_72916_1_, int p_72916_2_) {
        return world.blockExists(p_72916_1_ << 4, 100, p_72916_2_ << 4);
    }

    @Override
    public Chunk getChunkFromChunkCoords(int p_72964_1_, int p_72964_2_) {
        return world.getChunkFromChunkCoords(p_72964_1_, p_72964_2_);
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return null;
    }

    @Override
    protected int func_152379_p() {
        return Minecraft.getMinecraft().gameSettings.renderDistanceChunks;
    }

    @Override
    public Entity getEntityByID(int p_73045_1_) {
        return world.getEntityByID(p_73045_1_);
    }

    public TileMovingClient getTile(int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return (tile != null && tile.getClass() == TileMovingClient.class) ? (TileMovingClient) tile : null;
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        TileMovingClient tile = getTile(x, y, z);
        if (tile != null)
            return tile.block;

        return Blocks.air;
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        TileMovingClient tile = getTile(x, y, z);
        return tile == null ? null : tile.tile;
    }

    @Override
    public boolean setBlock(int p_147465_1_, int p_147465_2_, int p_147465_3_, Block p_147465_4_, int p_147465_5_, int p_147465_6_) {
        return false;
    }

    @Override
    public boolean updateLightByType(EnumSkyBlock p_147463_1_, int p_147463_2_, int p_147463_3_, int p_147463_4_) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getLightBrightnessForSkyBlocks(int p_72802_1_, int p_72802_2_, int p_72802_3_, int p_72802_4_) {
        return world.getLightBrightnessForSkyBlocks(p_72802_1_, p_72802_2_, p_72802_3_, p_72802_4_);
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        TileMovingClient tile = getTile(x, y, z);
        return tile == null ? 0 : tile.meta;
    }

    @Override
    public int isBlockProvidingPowerTo(int p_72879_1_, int p_72879_2_, int p_72879_3_, int p_72879_4_) {
        return world.isBlockProvidingPowerTo(p_72879_1_, p_72879_2_, p_72879_3_, p_72879_4_);
    }

    @Override
    public boolean isAirBlock(int x, int y, int z) {
        TileMovingClient tile = getTile(x, y, z);
        return tile == null || tile.block == Blocks.air;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BiomeGenBase getBiomeGenForCoords(int p_72807_1_, int p_72807_2_) {
        return world.getBiomeGenForCoords(p_72807_1_, p_72807_2_);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getHeight() {
        return world.getHeight();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean extendedLevelsInChunkCache() {
        return world.extendedLevelsInChunkCache();
    }

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
        TileMovingClient tile = getTile(x, y, z);
        return tile != null && tile.block.isSideSolid(this, x, y, z, side);
    }

    @Override
    public boolean spawnEntityInWorld(Entity p_72838_1_) {
        return false;
    }

    @Override
    public void spawnParticle(String type, double x, double y, double z, double r, double g, double b) {
        world.spawnParticle(type, x + offset * dir.offsetX, y + offset * dir.offsetY, z + offset * dir.offsetZ, r, g, b);
    }

    @Override
    public void tick() {

    }

    @Override
    public CrashReportCategory addWorldInfoToCrashReport(CrashReport p_72914_1_) {
        CrashReportCategory crashReportCategory = world.addWorldInfoToCrashReport(p_72914_1_);
        crashReportCategory.addCrashSection("Fake World", "This world is a fake wrapper used by Funky Locomotion");
        return crashReportCategory;
    }

    @Override
    public void doVoidFogParticles(int p_73029_1_, int p_73029_2_, int p_73029_3_) {
        if (worldClient != null) worldClient.doVoidFogParticles(p_73029_1_, p_73029_2_, p_73029_3_);
    }

    @Override
    public void makeFireworks(double p_92088_1_, double p_92088_3_, double p_92088_5_, double p_92088_7_, double p_92088_9_, double p_92088_11_, NBTTagCompound p_92088_13_) {
        if (worldClient != null)
            worldClient.makeFireworks(p_92088_1_, p_92088_3_, p_92088_5_, p_92088_7_, p_92088_9_, p_92088_11_, p_92088_13_);
    }

    @Override
    public boolean func_147492_c(int p_147492_1_, int p_147492_2_, int p_147492_3_, Block p_147492_4_, int p_147492_5_) {
        return false;
    }

    @Override
    public void removeEntity(Entity p_72900_1_) {

    }

    @Override
    public void addEntityToWorld(int p_73027_1_, Entity p_73027_2_) {

    }

    @Override
    public Entity removeEntityFromWorld(int p_73028_1_) {
        return null;
    }

    @Override
    public void playSound(double p_72980_1_, double p_72980_3_, double p_72980_5_, String p_72980_7_, float p_72980_8_, float p_72980_9_, boolean p_72980_10_) {
        if (worldClient != null)
            worldClient.playSound(p_72980_1_, p_72980_3_, p_72980_5_, p_72980_7_, p_72980_8_, p_72980_9_, p_72980_10_);
    }

    @Override
    public void removeAllEntities() {

    }

    @Override
    public void doPreChunk(int p_73025_1_, int p_73025_2_, boolean p_73025_3_) {

    }

    @Override
    protected void func_147456_g() {

    }

    @Override
    public void sendQuittingDisconnectingPacket() {
        world.sendQuittingDisconnectingPacket();
    }

    @SideOnly(Side.CLIENT)
    public static class FakeWorldManager {

        @SubscribeEvent
        public void onDimensionUnload(WorldEvent.Unload event){
            cache.remove(event.world);

            MinecraftServer server = MinecraftServer.getServer();
            if (server != null && !server.isServerRunning())
            {
                cache.clear();
            }
        }


    }
}
