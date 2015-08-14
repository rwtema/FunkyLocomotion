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
import net.minecraftforge.common.DimensionManager;
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

    public static boolean isValid(World world) {
        return world != null && world.provider != null && DimensionManager.isDimensionRegistered(world.provider.dimensionId);
    }

    public static FakeWorldClient getFakeWorldWrapper(World world) {
        FakeWorldClient fakeWorldClient = cache.get(world);
        if (fakeWorldClient == null) {
            fakeWorldClient = new FakeWorldClient(world);
            cache.put(world, fakeWorldClient);
        }

        return fakeWorldClient;
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new FakeWorldManager());
    }

    @Override
    public boolean isBlockNormalCubeDefault(int x, int y, int z, boolean defaultValue) {
        Block block = this.getBlock(x, y, z);
        return block.isNormalCube(this, x, y, z);
    }

    @Override
    protected boolean chunkExists(int x, int z) {
        return world.blockExists(x << 4, 100, z << 4);
    }

    @Override
    public Chunk getChunkFromChunkCoords(int x, int z) {
        return world.getChunkFromChunkCoords(x, z);
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
    public Entity getEntityByID(int id) {
        return world.getEntityByID(id);
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
    public boolean setBlock(int x, int y, int z, Block block, int meta, int flag) {
        return false;
    }

    @Override
    public boolean updateLightByType(EnumSkyBlock skyBlock, int x, int y, int z) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int minBrightness) {
        return world.getLightBrightnessForSkyBlocks(x, y, z, minBrightness);
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        TileMovingClient tile = getTile(x, y, z);
        return tile == null ? 0 : tile.meta;
    }

    @Override
    public int isBlockProvidingPowerTo(int x, int y, int z, int side) {
        return world.isBlockProvidingPowerTo(x, y, z, side);
    }

    @Override
    public boolean isAirBlock(int x, int y, int z) {
        TileMovingClient tile = getTile(x, y, z);
        return tile == null || tile.block == Blocks.air;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        return world.getBiomeGenForCoords(x, z);
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
    public boolean spawnEntityInWorld(Entity entity) {
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
    public CrashReportCategory addWorldInfoToCrashReport(CrashReport crash) {
        CrashReportCategory crashReportCategory = world.addWorldInfoToCrashReport(crash);
        crashReportCategory.addCrashSection("Fake World", "This world is a fake wrapper used by Funky Locomotion");
        return crashReportCategory;
    }

    @Override
    public void doVoidFogParticles(int x, int y, int z) {
        if (worldClient != null) worldClient.doVoidFogParticles(x, y, z);
    }

    @Override
    public void makeFireworks(double x, double y, double z, double vx, double vy, double vz, NBTTagCompound tag) {
        if (worldClient != null)
            worldClient.makeFireworks(x, y, z, vx, vy, vz, tag);
    }

    @Override
    public boolean func_147492_c(int x, int y, int z, Block block, int meta) {
        return false;
    }

    @Override
    public void removeEntity(Entity entity) {

    }

    @Override
    public void addEntityToWorld(int id, Entity entity) {

    }

    @Override
    public Entity removeEntityFromWorld(int entity) {
        return null;
    }

    @Override
    public void playSound(double x, double y, double z, String sound, float volume, float pitch, boolean positioned) {
        if (worldClient != null)
            worldClient.playSound(x, y, z, sound, volume, pitch, positioned);
    }

    @Override
    public void removeAllEntities() {

    }

    @Override
    public void doPreChunk(int x, int z, boolean load) {

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
        public void onDimensionUnload(WorldEvent.Unload event) {
            cache.remove(event.world);

            MinecraftServer server = MinecraftServer.getServer();
            if (server != null && !server.isServerRunning()) {
                cache.clear();
            }
        }


    }
}
