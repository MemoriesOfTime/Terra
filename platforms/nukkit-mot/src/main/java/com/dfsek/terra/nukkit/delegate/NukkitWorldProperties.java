package com.dfsek.terra.nukkit.delegate;

import com.dfsek.terra.api.world.info.WorldProperties;


public class NukkitWorldProperties implements WorldProperties {

    private final Object fakeHandle = new Object();
    private final long seed;
    private final int minHeight;
    private final int maxHeight;

    public NukkitWorldProperties(long seed, int minHeight, int maxHeight) {
        this.seed = seed;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public int getMaxHeight() {
        return maxHeight;
    }

    @Override
    public int getMinHeight() {
        return minHeight;
    }

    @Override
    public Object getHandle() {
        return fakeHandle;
    }
}
