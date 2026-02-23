package com.dfsek.terra.nukkit.delegate;

import cn.nukkit.block.Block;

import com.dfsek.terra.api.block.BlockType;
import com.dfsek.terra.api.block.state.properties.Property;
import com.dfsek.terra.nukkit.JeBlockState;
import com.dfsek.terra.nukkit.Mapping;


public final class NukkitBlockState implements com.dfsek.terra.api.block.state.BlockState {

    public static final NukkitBlockState AIR = new NukkitBlockState(0, 0, JeBlockState.fromString("minecraft:air"));

    private final int blockId;
    private final int metadata;
    private final JeBlockState jeBlockState;
    private final boolean containsWater;

    public NukkitBlockState(int blockId, int metadata, JeBlockState jeBlockState) {
        this.blockId = blockId;
        this.metadata = metadata;
        this.jeBlockState = jeBlockState;
        this.containsWater = "true".equals(jeBlockState.getPropertyValue("waterlogged"));
    }

    @Override
    public boolean matches(com.dfsek.terra.api.block.state.BlockState o) {
        NukkitBlockState other = (NukkitBlockState) o;
        return other.blockId == this.blockId && other.containsWater == this.containsWater;
    }

    @Override
    public <T extends Comparable<T>> boolean has(Property<T> property) {
        return false;
    }

    @Override
    public <T extends Comparable<T>> T get(Property<T> property) {
        return null;
    }

    @Override
    public <T extends Comparable<T>> com.dfsek.terra.api.block.state.BlockState set(Property<T> property, T value) {
        return null;
    }

    @Override
    public BlockType getBlockType() {
        return new NukkitBlockType(blockId);
    }

    @Override
    public String getAsString(boolean properties) {
        return jeBlockState.toString(properties);
    }

    @Override
    public boolean isAir() {
        return blockId == 0;
    }

    @Override
    public Object getHandle() {
        return Block.get(blockId, metadata);
    }

    public int blockId() {
        return blockId;
    }

    public int metadata() {
        return metadata;
    }

    public boolean containsWater() {
        return containsWater;
    }

    public JeBlockState jeBlockState() {
        return jeBlockState;
    }
}
