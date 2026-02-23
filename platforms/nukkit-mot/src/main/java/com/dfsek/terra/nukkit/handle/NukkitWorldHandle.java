package com.dfsek.terra.nukkit.handle;

import org.jetbrains.annotations.NotNull;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.entity.EntityType;
import com.dfsek.terra.api.handle.WorldHandle;
import com.dfsek.terra.nukkit.JeBlockState;
import com.dfsek.terra.nukkit.Mapping;
import com.dfsek.terra.nukkit.Mapping.NukkitBlockData;
import com.dfsek.terra.nukkit.delegate.NukkitBlockState;


public class NukkitWorldHandle implements WorldHandle {

    @Override
    public @NotNull BlockState createBlockState(@NotNull String data) {
        JeBlockState jeBlockState = JeBlockState.fromString(data);
        NukkitBlockData beData = Mapping.blockStateJeToBe(jeBlockState);
        return new NukkitBlockState(beData.blockId(), beData.metadata(), jeBlockState);
    }

    @Override
    public @NotNull BlockState air() {
        return NukkitBlockState.AIR;
    }

    @Override
    public @NotNull EntityType getEntity(@NotNull String id) {
        return new EntityType() {
            private final Object fakeEntityType = new Object();

            @Override
            public Object getHandle() {
                return fakeEntityType;
            }
        };
    }
}
