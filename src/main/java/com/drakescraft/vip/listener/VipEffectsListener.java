package com.drakescraft.vip.listener;

import com.drakescraft.vip.manager.VipManager;
import com.drakescraft.vip.model.VipTier;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Efectos "de vida" del VIP: ráfaga de partículas+sonido al entrar, doble salto
 * cosmético con estela, y fogonazo al matar a un enemigo. Todo con toggles en
 * config ({@code movement.double-jump}, {@code effects.join-burst},
 * {@code effects.kill-flourish}) y respetando abilities.disabled-worlds.
 */
public final class VipEffectsListener implements Listener {

    private final Plugin plugin;
    private final VipManager vipManager;
    private final Set<UUID> jumpCooldown = ConcurrentHashMap.newKeySet();

    public VipEffectsListener(Plugin plugin, VipManager vipManager) {
        this.plugin = plugin;
        this.vipManager = vipManager;
    }

    // ---- Ráfaga de entrada ----
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("effects.join-burst", true)) {
            return;
        }
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            VipTier tier = vipManager.getTier(player);
            if (tier == null) {
                return;
            }
            Color c = bandColor(tier);
            Particle.DustOptions dust = new Particle.DustOptions(c, 2.0f);
            player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, 1, 0), 60, 0.6, 1.2, 0.6, dust);
            player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 1, 0), 40, 0.5, 1, 0.5, 0.1);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.6f, 1.4f);
        }, 30L);
    }

    // ---- Doble salto ----
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!plugin.getConfig().getBoolean("movement.double-jump", true)) {
            return;
        }
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        if (!vipManager.isVip(player) || isDisabledWorld(player)) {
            return;
        }
        // Re-habilita el "salto extra" al tocar suelo.
        if (player.isOnGround() && !player.getAllowFlight()) {
            player.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        if (!plugin.getConfig().getBoolean("movement.double-jump", true)) {
            return;
        }
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return; // vuelo real, no tocar
        }
        if (!event.isFlying() || !vipManager.isVip(player) || isDisabledWorld(player)) {
            return;
        }
        event.setCancelled(true);
        player.setAllowFlight(false);
        player.setFlying(false);

        Vector v = player.getLocation().getDirection().normalize().multiply(0.9);
        v.setY(0.85);
        player.setVelocity(v);

        VipTier tier = vipManager.getTier(player);
        Color c = tier != null ? bandColor(tier) : Color.AQUA;
        player.getWorld().spawnParticle(Particle.DUST, player.getLocation(), 25, 0.3, 0.1, 0.3,
                new Particle.DustOptions(c, 1.4f));
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15, 0.2, 0.1, 0.2, 0.02);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_JUMP, 1f, 1.2f);
    }

    // ---- Fogonazo al matar ----
    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (!plugin.getConfig().getBoolean("effects.kill-flourish", true)) {
            return;
        }
        Player killer = event.getEntity().getKiller();
        if (killer == null || !vipManager.isVip(killer)) {
            return;
        }
        var loc = event.getEntity().getLocation().add(0, 0.8, 0);
        VipTier tier = vipManager.getTier(killer);
        Color c = tier != null ? bandColor(tier) : Color.WHITE;
        loc.getWorld().spawnParticle(Particle.DUST, loc, 25, 0.4, 0.4, 0.4, new Particle.DustOptions(c, 1.5f));
        loc.getWorld().spawnParticle(Particle.ENCHANTED_HIT, loc, 20, 0.3, 0.3, 0.3, 0.2);
        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8f, 1.6f);
    }

    private Color bandColor(VipTier tier) {
        return switch (tier.getBand()) {
            case OLYMPIAN_BASE -> Color.fromRGB(120, 200, 255);
            case OLYMPIAN_HIGH -> Color.fromRGB(255, 215, 0);
            case TITAN -> Color.fromRGB(170, 80, 255);
        };
    }

    private boolean isDisabledWorld(Player player) {
        Set<String> disabled = new HashSet<>(plugin.getConfig().getStringList("abilities.disabled-worlds"));
        String world = player.getWorld().getName().toLowerCase();
        for (String d : disabled) {
            if (world.startsWith(d.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
