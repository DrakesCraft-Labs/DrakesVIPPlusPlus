package com.drakescraft.vip.task;

import com.drakescraft.vip.hook.SkillHook;
import com.drakescraft.vip.manager.BoosterManager;
import com.drakescraft.vip.manager.BoosterManager.BoosterDef;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Comprueba periodicamente que boosters estan activos. En cada transicion
 * (apagado->encendido o viceversa) anuncia in-game y refresca los multiplicadores
 * de skills de todos los conectados. Auto-activado: sin comando manual.
 */
public final class BoosterScheduler extends BukkitRunnable {

    private final Plugin plugin;
    private final BoosterManager boosterManager;
    private final SkillHook skillHook;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<String, Boolean> lastState = new HashMap<>();

    public BoosterScheduler(Plugin plugin, BoosterManager boosterManager, SkillHook skillHook) {
        this.plugin = plugin;
        this.boosterManager = boosterManager;
        this.skillHook = skillHook;
    }

    @Override
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        boolean anyTransition = false;

        for (BoosterDef def : boosterManager.getDefinitions()) {
            boolean active = def.isActive(now);
            Boolean prev = lastState.put(def.id, active);
            if (prev != null && prev == active) {
                continue;
            }
            anyTransition = true;
            if (def.announce) {
                announce(def, active);
            }
        }

        if (anyTransition) {
            skillHook.refreshAll();
        }
    }

    private void announce(BoosterDef def, boolean active) {
        String affects = def.affects.toString().replace("[", "").replace("]", "");
        String msg = active
                ? "<gradient:#f9d423:#ff4e50><bold>BOOSTER x" + trim(def.global) + "</bold></gradient> "
                    + "<yellow>activado! (" + affects + ") <gray>Los VIP suben aun mas."
                : "<gray>El booster <white>" + def.id + "</white> ha terminado. ¡Gracias por jugar!";
        Component component = mm.deserialize(msg);
        Bukkit.getServer().sendMessage(component);
        plugin.getLogger().info("[Booster] " + def.id + " -> " + (active ? "ON" : "OFF"));
    }

    private String trim(double d) {
        if (d == Math.floor(d)) {
            return String.valueOf((int) d);
        }
        return String.valueOf(d);
    }
}
