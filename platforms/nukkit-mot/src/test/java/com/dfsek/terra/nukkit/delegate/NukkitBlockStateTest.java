package com.dfsek.terra.nukkit.delegate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import com.dfsek.terra.nukkit.JeBlockState;
import com.dfsek.terra.nukkit.config.MyConfig;

import static org.junit.jupiter.api.Assertions.assertSame;


class NukkitBlockStateTest {

    @BeforeEach
    void setUp() {
        MyConfig.BLOCK_REPLACEMENTS_ENABLED = true;
        NukkitBlockState.BLOCK_REPLACEMENTS = new HashMap<>();
        NukkitBlockState.BLOCK_IDENTIFIER_REPLACEMENTS = new HashMap<>();
    }

    @AfterEach
    void tearDown() {
        NukkitBlockState.BLOCK_REPLACEMENTS = new HashMap<>();
        NukkitBlockState.BLOCK_IDENTIFIER_REPLACEMENTS = new HashMap<>();
    }

    @Test
    void replacementForFallbackAirStateDoesNotReplaceRealAir() {
        NukkitBlockState fallbackAirState = state(0, 0, "minecraft:cactus_flower");
        NukkitBlockState realAir = state(0, 0, "minecraft:air");
        NukkitBlockState replacement = state(31, 1, "minecraft:short_grass");

        NukkitBlockState.BLOCK_REPLACEMENTS.put(fallbackAirState, replacement);

        assertSame(replacement, NukkitBlockState.resolve(fallbackAirState));
        assertSame(realAir, NukkitBlockState.resolve(realAir));
    }

    @Test
    void replacementUsesMetadataAsPartOfTheKey() {
        NukkitBlockState source = state(1, 0, "minecraft:stone");
        NukkitBlockState sameIdDifferentMetadata = state(1, 1, "minecraft:granite");
        NukkitBlockState replacement = state(4, 0, "minecraft:cobblestone");

        NukkitBlockState.BLOCK_REPLACEMENTS.put(source, replacement);

        assertSame(replacement, NukkitBlockState.resolve(source));
        assertSame(sameIdDifferentMetadata, NukkitBlockState.resolve(sameIdDifferentMetadata));
    }

    @Test
    void identifierReplacementMatchesEveryStateWithThatIdentifier() {
        NukkitBlockState leafLitterDefault = state(0, 0, "minecraft:leaf_litter[facing=north,segment_amount=1]");
        NukkitBlockState leafLitterVariant = state(0, 0, "minecraft:leaf_litter[facing=east,segment_amount=4]");
        NukkitBlockState replacement = state(0, 0, "minecraft:air");

        NukkitBlockState.BLOCK_IDENTIFIER_REPLACEMENTS.put("minecraft:leaf_litter", replacement);

        assertSame(replacement, NukkitBlockState.resolve(leafLitterDefault));
        assertSame(replacement, NukkitBlockState.resolve(leafLitterVariant));
    }

    @Test
    void exactReplacementWinsOverIdentifierReplacement() {
        NukkitBlockState leafLitterDefault = state(0, 0, "minecraft:leaf_litter[facing=north,segment_amount=1]");
        NukkitBlockState leafLitterVariant = state(0, 0, "minecraft:leaf_litter[facing=east,segment_amount=4]");
        NukkitBlockState wildcardReplacement = state(31, 1, "minecraft:short_grass");
        NukkitBlockState exactReplacement = state(0, 0, "minecraft:air");

        NukkitBlockState.BLOCK_IDENTIFIER_REPLACEMENTS.put("minecraft:leaf_litter", wildcardReplacement);
        NukkitBlockState.BLOCK_REPLACEMENTS.put(leafLitterDefault, exactReplacement);

        assertSame(exactReplacement, NukkitBlockState.resolve(leafLitterDefault));
        assertSame(wildcardReplacement, NukkitBlockState.resolve(leafLitterVariant));
    }

    @Test
    void identifierReplacementForFallbackAirStateDoesNotReplaceRealAir() {
        NukkitBlockState fallbackAirState = state(0, 0, "minecraft:cactus_flower");
        NukkitBlockState realAir = state(0, 0, "minecraft:air");
        NukkitBlockState replacement = state(31, 1, "minecraft:short_grass");

        NukkitBlockState.BLOCK_IDENTIFIER_REPLACEMENTS.put("minecraft:cactus_flower", replacement);

        assertSame(replacement, NukkitBlockState.resolve(fallbackAirState));
        assertSame(realAir, NukkitBlockState.resolve(realAir));
    }

    private static NukkitBlockState state(int blockId, int metadata, String javaState) {
        return new NukkitBlockState(blockId, metadata, JeBlockState.fromString(javaState));
    }
}
