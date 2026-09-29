package com.drakescraft.vip.listener;

import com.drakescraft.vip.hook.SkillHook;
import com.drakescraft.vip.manager.VipBuffManager;
import com.drakescraft.vip.manager.VipManager;
import com.drakescraft.vip.model.VipTier;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/**
 * Refresca tier + buffs + multiplicador de skills al entrar, y limpia al salir.
 * El calculo de tier se hace 1 tick despues del join para que LuckPerms ya haya
 * cargado los grupos del usuario.
 */
public final class VipConnectionListener implements Listener {

    private final Plugin plugin;
    private final VipManager vipManager;
    private final VipBuffManager buffManager;
    private final SkillHook skillHook;

    public VipConnectionListener(Plugin plugin, VipManager vipManager,
                                 VipBuffManager buffManager, SkillHook skillHook) {
        this.plugin = plugin;
        this.vipManager = vipManager;
        this.buffManager = buffManager;
        this.skillHook = skillHook;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            VipTier tier = vipManager.refresh(player);
            buffManager.apply(player, tier);
            skillHook.refresh(player);
        }, 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        buffManager.clear(player);
        skillHook.clear(player);
        vipManager.clear(player.getUniqueId());
    }
}
