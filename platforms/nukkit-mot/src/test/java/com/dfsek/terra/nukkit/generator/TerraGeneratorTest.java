package com.dfsek.terra.nukkit.generator;

import cn.nukkit.level.Level;
import cn.nukkit.level.generator.Generator;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;


class TerraGeneratorTest {

    @Test
    void jsonPresetSelectsNetherDimensionAndGeneratorType() {
        assertDimensionSelection(
            Map.of("preset", "{\"pack\":\"TARTARUS\"}"),
            Level.DIMENSION_NETHER,
            Generator.TYPE_NETHER
        );
    }

    @Test
    void jsonPresetSelectsEndDimensionAndGeneratorType() {
        assertDimensionSelection(
            Map.of("preset", "{\"pack\":\"REIMAGEND\"}"),
            Level.DIMENSION_THE_END,
            Generator.TYPE_THE_END
        );
    }

    @Test
    void directPresetSelectsNetherDimensionAndGeneratorType() {
        assertDimensionSelection(
            Map.of("preset", "TARTARUS"),
            Level.DIMENSION_NETHER,
            Generator.TYPE_NETHER
        );
    }

    @Test
    void legacyPackOptionSelectsNetherDimensionAndGeneratorType() {
        assertDimensionSelection(
            Map.of("pack", "TARTARUS"),
            Level.DIMENSION_NETHER,
            Generator.TYPE_NETHER
        );
    }

    @Test
    void unknownPresetDefaultsToOverworldDimensionAndGeneratorType() {
        assertDimensionSelection(
            Map.of("preset", "UNKNOWN"),
            Level.DIMENSION_OVERWORLD,
            Generator.TYPE_INFINITE
        );
    }

    private void assertDimensionSelection(Map<String, Object> options, int dimension, int generatorType) {
        TerraGenerator generator = new TerraGenerator(options);

        assertAll(
            () -> assertEquals(dimension, generator.getDimension()),
            () -> assertEquals(generatorType, generator.getId())
        );
    }
}
