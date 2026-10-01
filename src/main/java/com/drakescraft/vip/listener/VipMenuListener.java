package com.drakescraft.vip.listener;

import com.drakescraft.vip.gui.VipCatalogHolder;
import com.drakescraft.vip.gui.VipGui;
import com.drakescraft.vip.gui.VipMenuHolder;
import com.drakescraft.vip.manager.VipManager;
import com.drakescraft.vip.manager.VipToggleManager;
import com.drakescraft.vip.model.VipTier;
import com.drakescraft.vip.model.VipToggleType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Escucha las interacciones con el menú VIP y el catálogo de rangos, alternando perks
 * de forma segura y cancelando cualquier evento de movimiento para evitar duplicaciones.
 */
public final class VipMenuListener implements Listener {

    private final VipManager vipManager;
    private final VipToggleManager toggleManager;
    private final VipGui vipGui;

    public VipMenuListener(VipManager vipManager, VipToggleManager toggleManager, VipGui vipGui) {
        this.vipManager = vipManager;
        this.toggleManager = toggleManager;
        this.vipGui = vipGui;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof VipMenuHolder) && !(holder instanceof VipCatalogHolder)) {
            return;
        }

        // Blindaje absoluto: jamás permitir mover, tomar o cambiar ítems en ninguna GUI VIP
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();

        // 1. Manejo del Catálogo de Todos los Rangos
        if (holder instanceof VipCatalogHolder) {
            if (slot == VipGui.SLOT_CATALOG_CLOSE) {
                player.closeInventory();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
                return;
            }

            if (slot == VipGui.SLOT_CATALOG_BACK) {
                vipGui.open(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
                return;
            }

            if (slot == VipGui.SLOT_CATALOG_STORE) {
                player.closeInventory();
                player.sendMessage(Component.text("✦ Adquiere o mejora tu rango VIP al instante en: ", NamedTextColor.GOLD)
                        .append(Component.text("https://web.drakescraft.cl", NamedTextColor.AQUA, TextDecoration.UNDERLINED)));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f);
                return;
            }

            if (VipGui.CATALOG_TIER_SLOTS.containsKey(slot)) {
                VipTier clickedTier = VipGui.CATALOG_TIER_SLOTS.get(slot);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.3f);
                player.sendActionBar(Component.text("✦ Rango: ", NamedTextColor.GOLD)
                        .append(Component.text(clickedTier.name(), NamedTextColor.AQUA, TextDecoration.BOLD))
                        .append(Component.text(" (#" + clickedTier.getHierarchy() + ") - Banda " + clickedTier.getBand().name(), NamedTextColor.YELLOW)));
            }
            return;
        }

        // 2. Manejo del Menú de Configuración de Toggles
        if (slot == VipGui.SLOT_CLOSE) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
            return;
        }

        if (slot == VipGui.SLOT_CATALOG) {
            vipGui.openCatalog(player);
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.8f, 1.0f);
            return;
        }

        if (VipGui.TOGGLE_SLOTS.containsKey(slot)) {
            VipTier tier = vipManager.getTier(player);
            if (tier == null) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
                player.sendActionBar(Component.text("⚠ Requiere un rango VIP activo para alternar este perk.", NamedTextColor.RED));
                return;
            }

            VipToggleType type = VipGui.TOGGLE_SLOTS.get(slot);
            boolean newState = toggleManager.toggle(player, type);

            float pitch = newState ? 1.3f : 0.8f;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, pitch);

            NamedTextColor col = newState ? NamedTextColor.GREEN : NamedTextColor.RED;
            String stateStr = newState ? "ACTIVADA" : "DESACTIVADA";

            player.sendActionBar(Component.text("✦ " + type.getTitle() + ": ", NamedTextColor.GOLD)
                    .append(Component.text(stateStr, col, TextDecoration.BOLD))
                    .append(Component.text(" ✦", NamedTextColor.GOLD)));

            // Actualiza visualmente el menú sin cerrarlo
            vipGui.populate(player, event.getInventory());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof VipMenuHolder || holder instanceof VipCatalogHolder) {
            event.setCancelled(true);
        }
    }
}
