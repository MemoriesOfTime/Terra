package com.dfsek.terra.nukkit.delegate;

import cn.nukkit.block.Block;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.dfsek.terra.api.block.BlockType;
import com.dfsek.terra.api.block.state.properties.Property;
import com.dfsek.terra.nukkit.JeBlockState;


import static com.dfsek.terra.nukkit.config.MyConfig.BLOCK_REPLACEMENTS_ENABLED;


public final class NukkitBlockState implements com.dfsek.terra.api.block.state.BlockState {

    // JE blocks that always exist in water but have no 'waterlogged' property
    private static final Set<String> IMPLICIT_WATER_BLOCKS = Set.of(
        "minecraft:seagrass",
        "minecraft:tall_seagrass",
        "minecraft:kelp",
        "minecraft:kelp_plant",
        "minecraft:bubble_column"
    );

    public static final NukkitBlockState AIR = new NukkitBlockState(0, 0, JeBlockState.fromString("minecraft:air"));
    public static Map<NukkitBlockState, NukkitBlockState> BLOCK_REPLACEMENTS = new ConcurrentHashMap<>();

    /**
     * 解析块状态：如果其 blockId 在BLOCK_REPLACEMENTS，返回映射后的替换。
     */
    public static NukkitBlockState resolve(NukkitBlockState state) {
        if(BLOCK_REPLACEMENTS_ENABLED) {
            NukkitBlockState replacement = BLOCK_REPLACEMENTS.get(state);
            return replacement == null ? state : replacement;
        }
        return state;
    }

    private final int blockId;
    private final int metadata;
    private final JeBlockState jeBlockState;
    private final boolean containsWater;

    public NukkitBlockState(int blockId, int metadata, JeBlockState jeBlockState) {
        this.blockId = blockId;
        this.metadata = metadata;
        this.jeBlockState = jeBlockState;
        this.containsWater = "true".equals(jeBlockState.getPropertyValue("waterlogged"))
                             || IMPLICIT_WATER_BLOCKS.contains(jeBlockState.getIdentifier());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NukkitBlockState that)) return false;
        return blockId == that.blockId && metadata == that.metadata;
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockId, metadata);
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
