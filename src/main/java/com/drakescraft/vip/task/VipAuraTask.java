package com.drakescraft.vip.task;

import com.drakescraft.vip.manager.VipManager;
import com.drakescraft.vip.model.VipTier;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Aura cosmetica exclusiva VIP: una corona sutil de particulas alrededor del
 * jugador, con color e intensidad segun su banda. Ligera (un task cada 10 ticks,
 * como AuraTask de DrakesRankup) y respeta el toggle de cosmeticos.
 */
public final class VipAuraTask extends BukkitRunnable {

    private final Plugin plugin;
    private final VipManager vipManager;

    public VipAuraTask(Plugin plugin, VipManager vipManager) {
        this.plugin = plugin;
        this.vipManager = vipManager;
    }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("cosmetics.aura-enabled", true)) {
            return;
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            VipTier tier = vipManager.getTier(player);
            if (tier == null) {
                continue;
            }
            if (!player.hasPermission("drakesvip.cosmetic.aura")) {
                continue; // permite un toggle externo si se quiere
            }
            spawnAura(player, tier);
        }
    }

    private void spawnAura(Player player, VipTier tier) {
        Color color = switch (tier.getBand()) {
            case OLYMPIAN_BASE -> Color.fromRGB(120, 200, 255);
            case OLYMPIAN_HIGH -> Color.fromRGB(255, 215, 0);
            case TITAN -> Color.fromRGB(170, 80, 255);
        };
        int points = switch (tier.getBand()) {
            case OLYMPIAN_BASE -> 4;
            case OLYMPIAN_HIGH -> 6;
            case TITAN -> 10;
        };
        Particle.DustOptions dust = new Particle.DustOptions(color, 1.0f);
        double radius = 0.6;
        double y = 0.1;
        double baseAngle = (System.currentTimeMillis() % 3600) / 3600.0 * Math.PI * 2;
        for (int i = 0; i < points; i++) {
            double angle = baseAngle + (Math.PI * 2 * i / points);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            player.getWorld().spawnParticle(Particle.DUST,
                    player.getLocation().add(x, y, z), 1, 0, 0, 0, 0, dust);
        }
    }
}
