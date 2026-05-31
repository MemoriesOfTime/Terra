package com.dfsek.terra.nukkit.delegate;

import cn.nukkit.block.Block;
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
import com.dfsek.terra.api.world.chunk.Chunk;
import com.dfsek.terra.api.world.chunk.generation.ChunkGenerator;
import com.dfsek.terra.nukkit.JeBlockState;
import com.dfsek.terra.nukkit.Mapping;
import com.dfsek.terra.nukkit.generator.TerraGenerator;


public class NukkitServerWorld implements ServerWorld {

    private final TerraGenerator generator;
    private final ChunkManager chunkManager;
    private final DimensionData dimensionData;

    public NukkitServerWorld(TerraGenerator generator, ChunkManager chunkManager, DimensionData dimensionData) {
        this.generator = generator;
        this.chunkManager = chunkManager;
        this.dimensionData = dimensionData;
    }

    @Override
    public Chunk getChunkAt(int x, int z) {
        BaseFullChunk chunk = chunkManager.getChunk(x, z);
        return new NukkitChunk(this, chunk, dimensionData);
    }

    @Override
    public void setBlockState(int x, int y, int z, BlockState data, boolean physics) {
        if(NukkitWorldAccess.isOutsideBuildHeight(dimensionData, y)) {
            return;
        }

        NukkitBlockState nukkitBlockState = NukkitBlockState.resolve((NukkitBlockState) data);
        NukkitWorldAccess.setBlockState(chunkManager, x, y, z, nukkitBlockState);
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
        return new NukkitFakeEntity(Vector3.of(x, y, z), this);
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
        return generator.getTerraGenerator();
    }

    @Override
    public BiomeProvider getBiomeProvider() {
        return generator.getTerraBiomeProvider();
    }

    @Override
    public ConfigPack getPack() {
        return generator.getConfigPack();
    }

    @Override
    public long getSeed() {
        return chunkManager.getSeed();
    }

    @Override
    public int getMaxHeight() {
        return NukkitWorldAccess.terraMaxHeight(dimensionData);
    }

    @Override
    public int getMinHeight() {
        return dimensionData.getMinHeight();
    }

    @Override
    public Object getHandle() {
        return chunkManager;
    }

    public DimensionData getDimensionData() {
        return dimensionData;
    }
}
