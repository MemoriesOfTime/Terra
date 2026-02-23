package com.dfsek.terra.nukkit.delegate;

import com.dfsek.terra.api.world.biome.PlatformBiome;


public record NukkitBiome(int biomeId) implements PlatformBiome {
    @Override
    public Object getHandle() {
        return biomeId;
    }
}
