package com.drakescraft.vip.task;

import com.drakescraft.vip.manager.VipManager;
import com.drakescraft.vip.model.VipTier;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Aura cosmetica exclusiva VIP con estilo por BANDA:
 *  - OLYMPIAN_BASE: halo giratorio azul a los pies.
 *  - OLYMPIAN_HIGH: doble orbita dorada (una alta, otra baja) + destellos.
 *  - TITAN: tormenta purpura ascendente en helice + chispas.
 * Ligera (task cada 10 ticks, como AuraTask de DrakesRankup) y respeta el toggle.
 */
public final class VipAuraTask extends BukkitRunnable {

    private final Plugin plugin;
    private final VipManager vipManager;
    private double phase;

    public VipAuraTask(Plugin plugin, VipManager vipManager) {
        this.plugin = plugin;
        this.vipManager = vipManager;
    }

    @Override
    public void run() {
        if (!plugin.getConfig().getBoolean("cosmetics.aura-enabled", true)) {
            return;
        }
        phase += Math.PI / 8;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            VipTier tier = vipManager.getTier(player);
            if (tier == null || !player.hasPermission("drakesvip.cosmetic.aura")) {
                continue;
            }
            switch (tier.getBand()) {
                case OLYMPIAN_BASE -> halo(player, Color.fromRGB(120, 200, 255));
                case OLYMPIAN_HIGH -> doubleOrbit(player, Color.fromRGB(255, 215, 0));
                case TITAN -> storm(player, Color.fromRGB(170, 80, 255));
            }
        }
    }

    private void halo(Player player, Color color) {
        Particle.DustOptions dust = new Particle.DustOptions(color, 1.0f);
        ring(player.getLocation(), 0.6, 0.1, 4, dust);
    }

    private void doubleOrbit(Player player, Color color) {
        Particle.DustOptions dust = new Particle.DustOptions(color, 1.1f);
        ring(player.getLocation(), 0.7, 0.1, 6, dust);
        ring(player.getLocation(), 0.45, 1.9, 6, dust);
        if (Math.random() < 0.25) {
            player.getWorld().spawnParticle(Particle.END_ROD,
                    player.getLocation().add(0, 1, 0), 1, 0.3, 0.5, 0.3, 0.01);
        }
    }

    private void storm(Player player, Color color) {
        Particle.DustOptions dust = new Particle.DustOptions(color, 1.3f);
        // Helice ascendente
        for (int i = 0; i < 3; i++) {
            double y = ((phase * 0.15 + i * 0.7) % 2.4);
            double angle = phase + i * (Math.PI * 2 / 3);
            double x = Math.cos(angle) * 0.55;
            double z = Math.sin(angle) * 0.55;
            player.getWorld().spawnParticle(Particle.DUST,
                    player.getLocation().add(x, y, z), 1, 0, 0, 0, 0, dust);
        }
        if (Math.random() < 0.35) {
            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                    player.getLocation().add(0, 2.2, 0), 2, 0.3, 0.1, 0.3, 0.02);
        }
    }

    private void ring(Location center, double radius, double y, int points, Particle.DustOptions dust) {
        for (int i = 0; i < points; i++) {
            double angle = phase + (Math.PI * 2 * i / points);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            center.getWorld().spawnParticle(Particle.DUST,
                    center.clone().add(x, y, z), 1, 0, 0, 0, 0, dust);
        }
    }
}
