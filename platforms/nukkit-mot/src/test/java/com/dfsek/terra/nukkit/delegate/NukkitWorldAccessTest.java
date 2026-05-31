package com.dfsek.terra.nukkit.delegate;

import cn.nukkit.block.BlockID;
import cn.nukkit.level.DimensionData;
import cn.nukkit.level.format.anvil.Anvil;
import cn.nukkit.level.format.anvil.Chunk;
import cn.nukkit.level.generator.PopChunkManager;
import org.junit.jupiter.api.Test;

import com.dfsek.terra.nukkit.JeBlockState;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;


class NukkitWorldAccessTest {

    @Test
    void terraMaxHeightIsExclusiveWhileNukkitWritesStillAllowTopY() {
        DimensionData dimension = new DimensionData(0, 0, 127);
        Chunk chunk = writableChunk();
        NukkitProtoChunk protoChunk = new NukkitProtoChunk(chunk, dimension);

        protoChunk.setBlock(0, 127, 0, state(BlockID.STONE, 0, "minecraft:stone"));
        protoChunk.setBlock(0, 128, 0, state(BlockID.COBBLESTONE, 0, "minecraft:cobblestone"));

        assertAll(
            () -> assertEquals(128, protoChunk.getMaxHeight()),
            () -> assertEquals(BlockID.STONE, chunk.getBlockId(0, 127, 0)),
            () -> assertEquals(BlockID.AIR, chunk.getBlockId(0, 128, 0))
        );
    }

    @Test
    void protoWorldWritesWaterLayerThroughPopChunkManager() {
        DimensionData dimension = new DimensionData(0, 0, 127);
        PopChunkManager manager = new PopChunkManager(1L, () -> dimension);
        Chunk chunk = writableChunk();
        manager.setChunk(0, 0, chunk);

        NukkitProtoWorld protoWorld = new NukkitProtoWorld(null, manager, 0, 0, dimension);
        protoWorld.setBlockState(3, 64, 5, state(BlockID.TALL_GRASS, 0, "minecraft:seagrass"), false);

        assertAll(
            () -> assertEquals(BlockID.TALL_GRASS, chunk.getBlockId(3, 64, 5, 0)),
            () -> assertEquals(BlockID.STILL_WATER, chunk.getBlockId(3, 64, 5, 1))
        );
    }

    @Test
    void serverWorldWritesWaterLayerThroughPopChunkManager() {
        DimensionData dimension = new DimensionData(0, 0, 127);
        PopChunkManager manager = new PopChunkManager(1L, () -> dimension);
        Chunk chunk = writableChunk();
        manager.setChunk(0, 0, chunk);

        NukkitServerWorld serverWorld = new NukkitServerWorld(null, manager, dimension);
        serverWorld.setBlockState(4, 64, 6, state(BlockID.TALL_GRASS, 0, "minecraft:seagrass"), false);

        assertAll(
            () -> assertEquals(BlockID.TALL_GRASS, chunk.getBlockId(4, 64, 6, 0)),
            () -> assertEquals(BlockID.STILL_WATER, chunk.getBlockId(4, 64, 6, 1))
        );
    }

    @Test
    void directChunkWritesClearStaleWaterLayer() {
        DimensionData dimension = new DimensionData(0, 0, 127);
        Chunk chunk = writableChunk();
        NukkitProtoChunk protoChunk = new NukkitProtoChunk(chunk, dimension);

        protoChunk.setBlock(1, 64, 1, state(BlockID.TALL_GRASS, 0, "minecraft:seagrass"));
        protoChunk.setBlock(1, 64, 1, state(BlockID.STONE, 0, "minecraft:stone"));

        assertAll(
            () -> assertEquals(BlockID.STONE, chunk.getBlockId(1, 64, 1, 0)),
            () -> assertEquals(BlockID.AIR, chunk.getBlockId(1, 64, 1, 1))
        );
    }

    private static NukkitBlockState state(int blockId, int metadata, String javaState) {
        return new NukkitBlockState(blockId, metadata, JeBlockState.fromString(javaState));
    }

    private static Chunk writableChunk() {
        return new Chunk(Anvil.class);
    }
}
