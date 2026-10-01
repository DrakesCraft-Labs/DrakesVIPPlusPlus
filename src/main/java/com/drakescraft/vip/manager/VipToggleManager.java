package com.drakescraft.vip.manager;

import com.drakescraft.vip.model.VipTier;
import com.drakescraft.vip.model.VipToggleType;

import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.EnumMap;
import java.util.Map;

/**
 * Gestiona las preferencias de alternancia (toggles) por jugador,
 * persistidas de forma atómica en el PersistentDataContainer nativo de Paper.
 */
public final class VipToggleManager {

    private final Plugin plugin;
    private final Map<VipToggleType, NamespacedKey> keys = new EnumMap<>(VipToggleType.class);

    private VipManager vipManager;
    private VipBuffManager buffManager;

    public VipToggleManager(Plugin plugin) {
        this.plugin = plugin;
        for (VipToggleType type : VipToggleType.values()) {
            keys.put(type, new NamespacedKey(plugin, type.getKeyName()));
        }
    }

    public void setManagers(VipManager vipManager, VipBuffManager buffManager) {
        this.vipManager = vipManager;
        this.buffManager = buffManager;
    }

    /**
     * Consulta si un perk específico está activado para el jugador.
     */
    public boolean isEnabled(Player player, VipToggleType type) {
        if (player == null) {
            return type.isDefaultEnabled();
        }
        NamespacedKey key = keys.get(type);
        if (key == null) {
            return type.isDefaultEnabled();
        }
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        Byte val = pdc.get(key, PersistentDataType.BYTE);
        if (val == null) {
            return type.isDefaultEnabled();
        }
        return val == (byte) 1;
    }

    /**
     * Establece el estado de un perk para el jugador.
     */
    public void setEnabled(Player player, VipToggleType type, boolean enabled) {
        if (player == null) {
            return;
        }
        NamespacedKey key = keys.get(type);
        if (key == null) {
            return;
        }
        player.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) (enabled ? 1 : 0));
        onToggleChanged(player, type, enabled);
    }

    /**
     * Invierte el estado actual del perk y retorna el nuevo estado.
     */
    public boolean toggle(Player player, VipToggleType type) {
        boolean next = !isEnabled(player, type);
        setEnabled(player, type, next);
        return next;
    }

    private void onToggleChanged(Player player, VipToggleType type, boolean enabled) {
        if (type == VipToggleType.PASSIVE_BUFFS && buffManager != null) {
            if (!enabled) {
                buffManager.clear(player);
            } else if (vipManager != null) {
                VipTier tier = vipManager.getTier(player);
                buffManager.apply(player, tier);
            }
        } else if (type == VipToggleType.DOUBLE_JUMP) {
            if (!enabled && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                // Si no tiene permiso de vuelo general, deshabilitamos el flight que activaba el doble salto
                if (!player.hasPermission("essentials.fly") && !player.hasPermission("cmi.command.fly")) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
            }
        }
    }
}
