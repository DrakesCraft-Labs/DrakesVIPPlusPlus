package com.drakescraft.vip.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nonnull;

/**
 * Holder de inventario exclusivo para el menú GUI de DrakesVIP++.
 * Garantiza identificación unívoca y protección total contra sustracción de ítems.
 */
public final class VipMenuHolder implements InventoryHolder {

    private final Player player;
    private Inventory inventory;

    public VipMenuHolder(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    @Nonnull
    public Inventory getInventory() {
        return inventory;
    }
}
