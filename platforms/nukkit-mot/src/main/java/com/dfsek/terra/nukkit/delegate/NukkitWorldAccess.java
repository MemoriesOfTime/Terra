package com.dfsek.terra.nukkit.delegate;

import cn.nukkit.block.BlockID;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.DimensionData;
import cn.nukkit.level.format.FullChunk;


public final class NukkitWorldAccess {

    private static final int BASE_LAYER = 0;
    private static final int WATER_LAYER = 1;

    private NukkitWorldAccess() {
    }

    public static int terraMaxHeight(DimensionData dimensionData) {
        return dimensionData.getMaxHeight() + 1;
    }

    public static boolean isOutsideBuildHeight(DimensionData dimensionData, int y) {
        return y < dimensionData.getMinHeight() || y > dimensionData.getMaxHeight();
    }

    static void setBlockState(FullChunk chunk, int x, int y, int z, NukkitBlockState state) {
        if(chunk == null) return;

        chunk.setBlockAtLayer(x, y, z, BASE_LAYER, state.blockId(), state.metadata());
        if(state.containsWater()) {
            chunk.setBlockAtLayer(x, y, z, WATER_LAYER, BlockID.STILL_WATER, 0);
        } else if(chunk.getBlockId(x, y, z, WATER_LAYER) != BlockID.AIR ||
                  chunk.getBlockData(x, y, z, WATER_LAYER) != 0) {
            chunk.setBlockAtLayer(x, y, z, WATER_LAYER, BlockID.AIR, 0);
        }
    }

    static void setBlockState(ChunkManager chunkManager, int x, int y, int z, NukkitBlockState state) {
        if(chunkManager == null) return;

        FullChunk chunk = chunkManager.getChunk(x >> 4, z >> 4);
        setBlockState(chunk, x & 15, y, z & 15, state);
    }
}
