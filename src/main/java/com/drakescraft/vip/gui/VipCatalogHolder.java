package com.drakescraft.vip.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nonnull;

/**
 * Holder de inventario para el catálogo completo de todos los rangos VIP de DrakesVIP++.
 * Protege contra cualquier sustracción o manipulación de ítems.
 */
public final class VipCatalogHolder implements InventoryHolder {

    private final Player player;
    private Inventory inventory;

    public VipCatalogHolder(Player player) {
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
