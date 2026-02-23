package com.dfsek.terra.nukkit.delegate;

import cn.nukkit.item.enchantment.Enchantment;

import com.dfsek.terra.api.inventory.ItemStack;


public record NukkitEnchantment(Enchantment nukkitEnchantment) implements com.dfsek.terra.api.inventory.item.Enchantment {
    @Override
    public boolean canEnchantItem(ItemStack itemStack) {
        cn.nukkit.item.Item nukkitItem = (cn.nukkit.item.Item) itemStack.getHandle();
        return nukkitEnchantment.canEnchant(nukkitItem);
    }

    @Override
    public boolean conflictsWith(com.dfsek.terra.api.inventory.item.Enchantment other) {
        Enchantment otherNukkit = ((NukkitEnchantment) other).nukkitEnchantment();
        return !nukkitEnchantment.isCompatibleWith(otherNukkit);
    }

    @Override
    public String getID() {
        return nukkitEnchantment.getName();
    }

    @Override
    public int getMaxLevel() {
        return nukkitEnchantment.getMaxLevel();
    }

    @Override
    public Object getHandle() {
        return nukkitEnchantment;
    }
}
