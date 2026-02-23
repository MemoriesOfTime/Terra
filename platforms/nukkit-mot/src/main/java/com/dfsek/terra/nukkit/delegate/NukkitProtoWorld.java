package com.dfsek.terra.nukkit.delegate;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.DimensionData;
import cn.nukkit.level.format.generic.BaseFullChunk;
import com.dfsek.seismic.type.vector.Vector3;

import com.dfsek.terra.api.block.entity.BlockEntity;
import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.entity.Entity;
import com.dfsek.terra.api.entity.EntityType;
import com.dfsek.terra.api.world.ServerWorld;
import com.dfsek.terra.api.world.biome.generation.BiomeProvider;
import com.dfsek.terra.api.world.chunk.generation.ChunkGenerator;
import com.dfsek.terra.api.world.chunk.generation.ProtoWorld;
import com.dfsek.terra.nukkit.JeBlockState;
import com.dfsek.terra.nukkit.Mapping;


public class NukkitProtoWorld implements ProtoWorld {

    private final NukkitServerWorld serverWorld;
    private final ChunkManager chunkManager;
    private final int centerChunkX;
    private final int centerChunkZ;
    private final DimensionData dimensionData;

    public NukkitProtoWorld(NukkitServerWorld serverWorld, ChunkManager chunkManager,
                            int centerChunkX, int centerChunkZ, DimensionData dimensionData) {
        this.serverWorld = serverWorld;
        this.chunkManager = chunkManager;
        this.centerChunkX = centerChunkX;
        this.centerChunkZ = centerChunkZ;
        this.dimensionData = dimensionData;
    }

    @Override
    public int centerChunkX() {
        return centerChunkX;
    }

    @Override
    public int centerChunkZ() {
        return centerChunkZ;
    }

    @Override
    public ServerWorld getWorld() {
        return serverWorld;
    }

    @Override
    public void setBlockState(int x, int y, int z, BlockState data, boolean physics) {
        if(y < dimensionData.getMinHeight() || y > dimensionData.getMaxHeight()) {
            return;
        }

        NukkitBlockState nukkitBlockState = (NukkitBlockState) data;
        chunkManager.setBlockAtLayer(x, y, z, 0, nukkitBlockState.blockId(), nukkitBlockState.metadata());
        if(nukkitBlockState.containsWater()) {
            chunkManager.setBlockAtLayer(x, y, z, 1, BlockID.STILL_WATER, 0);
        }
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        int blockId = chunkManager.getBlockIdAt(x, y, z);
        int meta = chunkManager.getBlockDataAt(x, y, z);
        int fullId = (blockId << Block.DATA_BITS) | meta;
        JeBlockState je = Mapping.blockFullIdToJe(fullId);
        if(je == null) {
            je = JeBlockState.fromString("minecraft:air");
        }
        return new NukkitBlockState(blockId, meta, je);
    }

    @Override
    public Entity spawnEntity(double x, double y, double z, EntityType entityType) {
        return new NukkitFakeEntity(Vector3.of(x, y, z), serverWorld);
    }

    @Override
    public BlockEntity getBlockEntity(int x, int y, int z) {
        BaseFullChunk chunk = chunkManager.getChunk(x >> 4, z >> 4);
        if(chunk == null) return null;
        cn.nukkit.blockentity.BlockEntity te = chunk.getTile(x & 15, y, z & 15);
        if(te == null) return null;
        return new NukkitBlockEntity(te);
    }

    @Override
    public ChunkGenerator getGenerator() {
        return serverWorld.getGenerator();
    }

    @Override
    public BiomeProvider getBiomeProvider() {
        return serverWorld.getBiomeProvider();
    }

    @Override
    public ConfigPack getPack() {
        return serverWorld.getPack();
    }

    @Override
    public long getSeed() {
        return serverWorld.getSeed();
    }

    @Override
    public int getMaxHeight() {
        return dimensionData.getMaxHeight();
    }

    @Override
    public int getMinHeight() {
        return dimensionData.getMinHeight();
    }

    @Override
    public Object getHandle() {
        return serverWorld;
    }
}
