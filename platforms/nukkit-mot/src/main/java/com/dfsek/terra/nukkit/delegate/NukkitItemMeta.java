package com.dfsek.terra.nukkit.delegate;

import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;

import java.util.HashMap;
import java.util.Map;

import com.dfsek.terra.api.inventory.item.ItemMeta;


public record NukkitItemMeta(Item nukkitItem) implements ItemMeta {
    @Override
    public void addEnchantment(com.dfsek.terra.api.inventory.item.Enchantment enchantment, int level) {
        Enchantment nukkitEnchantment = ((NukkitEnchantment) enchantment).nukkitEnchantment();
        nukkitItem.addEnchantment(nukkitEnchantment.setLevel(level));
    }

    @Override
    public Map<com.dfsek.terra.api.inventory.item.Enchantment, Integer> getEnchantments() {
        Map<com.dfsek.terra.api.inventory.item.Enchantment, Integer> results = new HashMap<>();
        for(Enchantment enchantment : nukkitItem.getEnchantments()) {
            results.put(new NukkitEnchantment(enchantment), enchantment.getLevel());
        }
        return results;
    }

    @Override
    public Object getHandle() {
        return nukkitItem;
    }
}
