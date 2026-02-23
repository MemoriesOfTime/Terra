package com.dfsek.terra.nukkit.delegate;

import cn.nukkit.item.Item;

import com.dfsek.terra.api.inventory.item.ItemMeta;


public record NukkitItemStack(Item nukkitItem) implements com.dfsek.terra.api.inventory.ItemStack {
    @Override
    public int getAmount() {
        return nukkitItem.getCount();
    }

    @Override
    public void setAmount(int i) {
        nukkitItem.setCount(i);
    }

    @Override
    public com.dfsek.terra.api.inventory.Item getType() {
        return new NukkitItemType(nukkitItem.getId(), nukkitItem.getDamage());
    }

    @Override
    public ItemMeta getItemMeta() {
        return new NukkitItemMeta(nukkitItem);
    }

    @Override
    public void setItemMeta(ItemMeta meta) {
        Item sourceItem = ((NukkitItemMeta) meta).nukkitItem();
        nukkitItem.clearNamedTag();
        if(sourceItem.hasCompoundTag()) {
            nukkitItem.setNamedTag(sourceItem.getNamedTag().clone());
        }
    }

    @Override
    public Object getHandle() {
        return nukkitItem;
    }
}
