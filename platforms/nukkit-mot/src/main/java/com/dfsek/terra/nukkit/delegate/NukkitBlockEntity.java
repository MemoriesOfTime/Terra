package com.dfsek.terra.nukkit.delegate;

import cn.nukkit.block.Block;
import com.dfsek.seismic.type.vector.Vector3;

import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.nukkit.JeBlockState;
import com.dfsek.terra.nukkit.Mapping;


public record NukkitBlockEntity(
    cn.nukkit.blockentity.BlockEntity nukkitBlockEntity
) implements com.dfsek.terra.api.block.entity.BlockEntity {

    @Override
    public boolean update(boolean applyPhysics) {
        return false;
    }

    @Override
    public Vector3 getPosition() {
        return Vector3.of(nukkitBlockEntity.getFloorX(), nukkitBlockEntity.getFloorY(), nukkitBlockEntity.getFloorZ());
    }

    @Override
    public int getX() {
        return nukkitBlockEntity.getFloorX();
    }

    @Override
    public int getY() {
        return nukkitBlockEntity.getFloorY();
    }

    @Override
    public int getZ() {
        return nukkitBlockEntity.getFloorZ();
    }

    @Override
    public BlockState getBlockState() {
        Block block = nukkitBlockEntity.getBlock();
        int fullId = (block.getId() << Block.DATA_BITS) | block.getDamage();
        JeBlockState je = Mapping.blockFullIdToJe(fullId);
        if(je == null) {
            je = JeBlockState.fromString("minecraft:air");
        }
        return new NukkitBlockState(block.getId(), block.getDamage(), je);
    }

    @Override
    public Object getHandle() {
        return nukkitBlockEntity;
    }
}
