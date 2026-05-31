package com.dfsek.terra.nukkit.delegate;

import cn.nukkit.block.Block;
import cn.nukkit.level.DimensionData;
import cn.nukkit.level.format.FullChunk;
import org.jetbrains.annotations.NotNull;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.world.chunk.generation.ProtoChunk;
import com.dfsek.terra.nukkit.JeBlockState;
import com.dfsek.terra.nukkit.Mapping;


public record NukkitProtoChunk(FullChunk nukkitChunk, DimensionData dimensionData) implements ProtoChunk {

    @Override
    public int getMaxHeight() {
        return NukkitWorldAccess.terraMaxHeight(dimensionData);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull BlockState blockState) {
        if(x < 0 || x > 15 || z < 0 || z > 15 ||
           NukkitWorldAccess.isOutsideBuildHeight(dimensionData, y)) {
            return;
        }

        NukkitBlockState nukkitBlockState = NukkitBlockState.resolve((NukkitBlockState) blockState);
        NukkitWorldAccess.setBlockState(nukkitChunk, x, y, z, nukkitBlockState);
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
    public Object getHandle() {
        return nukkitChunk;
    }
}
