package com.drakescraft.vip.ability;

import com.drakescraft.vip.manager.VipManager;
import com.drakescraft.vip.model.VipTier;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Habilidad activa divina exclusiva VIP (bandas OLYMPIAN_HIGH y TITAN). Se dispara
 * al agacharse (sneak) + intercambiar mano (tecla F). Cada dios tiene un efecto
 * distinto para no copiar a DrakesRankup. Con cooldown y desactivable por mundo/
 * modalidad (PvP/clasico) via config {@code abilities.disabled-worlds}.
 */
public final class VipAbilityListener implements Listener {

    private final Plugin plugin;
    private final VipManager vipManager;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public VipAbilityListener(Plugin plugin, VipManager vipManager) {
        this.plugin = plugin;
        this.vipManager = vipManager;
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }
        VipTier tier = vipManager.getTier(player);
        if (tier == null || tier.getBand() == VipTier.VipBand.OLYMPIAN_BASE) {
            return; // solo bandas altas tienen habilidad activa
        }
        if (isDisabledWorld(player)) {
            return;
        }

        long cooldownMs = plugin.getConfig().getLong("abilities.cooldown-seconds", 30) * 1000L;
        // Cronos reduce cooldowns a la mitad.
        if (tier == VipTier.TITAN_CRONOS || tier.getHierarchy() >= VipTier.TITAN_CRONOS.getHierarchy()) {
            cooldownMs /= 2;
        }
        long now = System.currentTimeMillis();
        Long until = cooldowns.get(player.getUniqueId());
        if (until != null && now < until) {
            long left = (until - now) / 1000 + 1;
            player.sendActionBar(Component.text("Habilidad en enfriamiento: " + left + "s", NamedTextColor.GRAY));
            return;
        }

        event.setCancelled(true);
        cooldowns.put(player.getUniqueId(), now + cooldownMs);
        cast(player, tier);
    }

    private void cast(Player player, VipTier tier) {
        switch (tier) {
            case ZEUS -> lightning(player);
            case THOR -> dash(player);
            case ANUBIS -> lifesteal(player);
            case POSEIDON -> shockwave(player, 2.0);
            // Titanes: onda + rayo combinados (god-tier)
            default -> {
                shockwave(player, 3.0);
                lightning(player);
            }
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 1.4f);
    }

    private void lightning(Player player) {
        Location target = player.getTargetBlockExact(30) != null
                ? player.getTargetBlockExact(30).getLocation().add(0.5, 1, 0.5)
                : player.getLocation();
        player.getWorld().strikeLightningEffect(target);
        for (LivingEntity e : nearby(target, 4.0)) {
            if (!e.equals(player)) {
                e.damage(8.0, player);
            }
        }
    }

    private void dash(Player player) {
        Vector dir = player.getLocation().getDirection().normalize().multiply(1.8).setY(0.6);
        player.setVelocity(dir);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, 0.3, 0.3, 0.3, 0.02);
    }

    private void lifesteal(Player player) {
        double healed = 0;
        for (LivingEntity e : nearby(player.getLocation(), 5.0)) {
            if (!e.equals(player)) {
                e.damage(5.0, player);
                healed += 2.0;
            }
        }
        if (healed > 0) {
            double max = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
            player.setHealth(Math.min(max, player.getHealth() + healed));
        }
        player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, player.getLocation().add(0, 1, 0), 15);
    }

    private void shockwave(Player player, double power) {
        for (LivingEntity e : nearby(player.getLocation(), 5.0)) {
            if (!e.equals(player)) {
                Vector push = e.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(power).setY(0.5);
                e.setVelocity(push);
                e.damage(4.0, player);
            }
        }
        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 3);
    }

    private List<LivingEntity> nearby(Location loc, double r) {
        return loc.getWorld().getNearbyEntities(loc, r, r, r).stream()
                .filter(en -> en instanceof LivingEntity)
                .map(en -> (LivingEntity) en)
                .toList();
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
