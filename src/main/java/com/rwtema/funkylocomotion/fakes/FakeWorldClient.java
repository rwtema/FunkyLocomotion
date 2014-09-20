package com.rwtema.funkylocomotion.fakes;

import com.rwtema.funkylocomotion.blocks.TileMovingClient;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.WeakHashMap;

@SideOnly(Side.CLIENT)
public class FakeWorldClient extends World {
    private static WeakHashMap<World, FakeWorldClient> cache = new WeakHashMap<World, FakeWorldClient>();
    public double offset = 0;
    public ForgeDirection dir = ForgeDirection.UNKNOWN;
    World world;

    private FakeWorldClient(World worldClient) {
        super(worldClient.getSaveHandler(),
                worldClient.getWorldInfo().getWorldName(),
                worldClient.provider,
                new WorldSettings(worldClient.getWorldInfo()),
                worldClient.theProfiler);
        this.world = worldClient;
        this.isRemote = true;
    }

    public static FakeWorldClient getFakeWorldWrapper(World world) {
        FakeWorldClient fakeWorld = cache.get(world);
        if (fakeWorld == null) {
            fakeWorld = new FakeWorldClient(world);
            cache.put(world, fakeWorld);
        }

        return fakeWorld;
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
        //return world.isBlockNormalCubeDefault(x, y, z, false) ? Blocks.stone : Blocks.air;

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
}
