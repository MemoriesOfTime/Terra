package com.dfsek.terra.nukkit.handle;

import cn.nukkit.item.enchantment.Enchantment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dfsek.terra.api.handle.ItemHandle;
import com.dfsek.terra.api.inventory.Item;
import com.dfsek.terra.nukkit.Mapping;
import com.dfsek.terra.nukkit.Mapping.NukkitItemData;
import com.dfsek.terra.nukkit.delegate.NukkitEnchantment;
import com.dfsek.terra.nukkit.delegate.NukkitItemType;


public class NukkitItemHandle implements ItemHandle {

    private volatile Map<String, Enchantment> enchantmentCache;

    private Map<String, Enchantment> getEnchantmentCache() {
        if(enchantmentCache == null) {
            Map<String, Enchantment> cache = new HashMap<>();
            for(Enchantment enchantment : Enchantment.getEnchantments()) {
                if(enchantment != null) {
                    cache.put(enchantment.getName().toLowerCase(), enchantment);
                }
            }
            enchantmentCache = cache;
        }
        return enchantmentCache;
    }

    @Override
    public Item createItem(String data) {
        NukkitItemData itemData = Mapping.itemIdJeToBe(data);
        return new NukkitItemType(itemData.itemId(), itemData.metadata());
    }

    @Override
    public com.dfsek.terra.api.inventory.item.Enchantment getEnchantment(String id) {
        Enchantment enchantment = getEnchantmentCache().get(id.toLowerCase());
        return enchantment != null ? new NukkitEnchantment(enchantment) : null;
    }

    @Override
    public Set<com.dfsek.terra.api.inventory.item.Enchantment> getEnchantments() {
        Set<com.dfsek.terra.api.inventory.item.Enchantment> result = new HashSet<>();
        for(Enchantment enchantment : getEnchantmentCache().values()) {
            result.add(new NukkitEnchantment(enchantment));
        }
        return result;
    }
}
