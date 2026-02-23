package com.dfsek.terra.nukkit.delegate;

import cn.nukkit.item.Item;

import com.dfsek.terra.api.inventory.ItemStack;


public final class NukkitItemType implements com.dfsek.terra.api.inventory.Item {

    private final int itemId;
    private final int metadata;

    public NukkitItemType(int itemId, int metadata) {
        this.itemId = itemId;
        this.metadata = metadata;
    }

    @Override
    public ItemStack newItemStack(int amount) {
        return new NukkitItemStack(Item.get(itemId, metadata, amount));
    }

    @Override
    public double getMaxDurability() {
        return Item.get(itemId, metadata).getMaxDurability();
    }

    @Override
    public Object getHandle() {
        return Item.get(itemId, metadata);
    }
}
