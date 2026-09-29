package com.drakescraft.vip.hook;

import com.drakescraft.vip.manager.BoosterManager;
import com.drakescraft.vip.manager.BoosterManager.BoostType;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aplica el boost de XP de skills apoyandose en los MULTIPLICADORES POR PERMISO de
 * AuraSkills ({@code auraskills.multiplier.<porcentaje>}), en vez de acoplarse a su
 * API interna. El permiso se concede con un {@link PermissionAttachment} transitorio
 * (se limpia solo al salir el jugador), asi que nunca toca LuckPerms ni persiste.
 *
 * <p>Multiplicador efectivo x2.0 -> +100% -> {@code auraskills.multiplier.100}.
 * Si AuraSkills no esta instalado, el permiso simplemente no lo lee nadie (no rompe).
 */
public final class SkillHook {

    private static final String PERM_PREFIX = "auraskills.multiplier.";

    private final Plugin plugin;
    private final BoosterManager boosterManager;
    private final Map<UUID, PermissionAttachment> attachments = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> currentPercent = new ConcurrentHashMap<>();
    private final boolean auraSkillsPresent;

    public SkillHook(Plugin plugin, BoosterManager boosterManager) {
        this.plugin = plugin;
        this.boosterManager = boosterManager;
        this.auraSkillsPresent = Bukkit.getPluginManager().getPlugin("AuraSkills") != null;
        if (auraSkillsPresent) {
            plugin.getLogger().info("AuraSkills detectado: el boost de skills usara multiplicadores por permiso.");
        } else {
            plugin.getLogger().info("AuraSkills no detectado: el boost de skills quedara latente hasta instalarlo.");
        }
    }

    public boolean isAuraSkillsPresent() {
        return auraSkillsPresent;
    }

    /** Ajusta el permiso-multiplicador del jugador segun el booster de SKILL activo. */
    public void refresh(Player player) {
        double mult = boosterManager.effectiveMultiplier(player, BoostType.SKILL);
        int percent = (int) Math.round((mult - 1.0) * 100.0);
        UUID id = player.getUniqueId();

        Integer prev = currentPercent.get(id);
        if (prev != null && prev == percent) {
            return; // sin cambios
        }

        // Quita el anterior si lo hubiera.
        removeAttachment(player);

        if (percent > 0) {
            PermissionAttachment att = player.addAttachment(plugin);
            att.setPermission(PERM_PREFIX + percent, true);
            attachments.put(id, att);
            currentPercent.put(id, percent);
        } else {
            currentPercent.put(id, 0);
        }
        player.recalculatePermissions();
    }

    /** Limpia el estado del jugador (al salir). */
    public void clear(Player player) {
        removeAttachment(player);
        currentPercent.remove(player.getUniqueId());
    }

    private void removeAttachment(Player player) {
        PermissionAttachment att = attachments.remove(player.getUniqueId());
        if (att != null) {
            try {
                player.removeAttachment(att);
            } catch (IllegalArgumentException ignored) {
                // el attachment ya no existe (relog); no pasa nada
            }
        }
    }

    /** Refresca a todos los conectados (llamado por el scheduler en cada transicion). */
    public void refreshAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            refresh(p);
        }
    }
}
