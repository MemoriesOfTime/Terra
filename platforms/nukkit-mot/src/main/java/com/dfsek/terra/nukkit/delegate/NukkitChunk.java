package com.dfsek.terra.nukkit.delegate;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.DimensionData;
import cn.nukkit.level.format.FullChunk;
import org.jetbrains.annotations.NotNull;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.world.ServerWorld;
import com.dfsek.terra.api.world.chunk.Chunk;
import com.dfsek.terra.nukkit.JeBlockState;
import com.dfsek.terra.nukkit.Mapping;


public record NukkitChunk(ServerWorld world, FullChunk nukkitChunk,
                           DimensionData dimensionData) implements Chunk {

    @Override
    public void setBlock(int x, int y, int z, BlockState data, boolean physics) {
        if(x < 0 || x > 15 || z < 0 || z > 15 ||
           y < dimensionData.getMinHeight() || y > dimensionData.getMaxHeight()) {
            return;
        }

        NukkitBlockState nukkitBlockState = (NukkitBlockState) data;
        nukkitChunk.setBlockAtLayer(x, y, z, 0, nukkitBlockState.blockId(), nukkitBlockState.metadata());
        if(nukkitBlockState.containsWater()) {
            nukkitChunk.setBlockAtLayer(x, y, z, 1, BlockID.STILL_WATER, 0);
        }
    }

    @Override
    public @NotNull BlockState getBlock(int x, int y, int z) {
        int fullId = nukkitChunk.getFullBlock(x, y, z);
        int blockId = fullId >> Block.DATA_BITS;
        int meta = fullId & Block.DATA_MASK;
        JeBlockState je = Mapping.blockFullIdToJe(fullId);
        if(je == null) {
            je = JeBlockState.fromString("minecraft:air");
        }
        return new NukkitBlockState(blockId, meta, je);
    }

    @Override
    public int getX() {
        return nukkitChunk.getX();
    }

    @Override
    public int getZ() {
        return nukkitChunk.getZ();
    }

    @Override
    public ServerWorld getWorld() {
        return world;
    }

    @Override
    public Object getHandle() {
        return nukkitChunk;
    }
}
