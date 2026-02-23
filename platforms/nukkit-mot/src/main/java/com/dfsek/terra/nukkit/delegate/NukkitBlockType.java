package com.dfsek.terra.nukkit.delegate;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.nukkit.Mapping;
import com.dfsek.terra.nukkit.JeBlockState;


public record NukkitBlockType(int blockId) implements com.dfsek.terra.api.block.BlockType {
    @Override
    public BlockState getDefaultState() {
        int fullId = blockId << Block.DATA_BITS;
        JeBlockState je = Mapping.blockFullIdToJe(fullId);
        if(je == null) {
            je = JeBlockState.fromString("minecraft:air");
        }
        return new NukkitBlockState(blockId, 0, je);
    }

    @Override
    public boolean isSolid() {
        return Block.solid != null && blockId < Block.solid.length && Block.solid[blockId];
    }

    @Override
    public boolean isWater() {
        return blockId == BlockID.WATER || blockId == BlockID.STILL_WATER;
    }

    @Override
    public Object getHandle() {
        return blockId;
    }
}
