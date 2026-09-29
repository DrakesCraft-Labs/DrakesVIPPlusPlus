package com.drakescraft.vip.listener;

import com.drakescraft.vip.manager.BoosterManager;
import com.drakescraft.vip.manager.BoosterManager.BoostType;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

/**
 * Multiplica la experiencia VANILLA que gana el jugador segun el booster de XP
 * activo (global + extra VIP). El boost de skills de AuraSkills se maneja aparte
 * en {@link com.drakescraft.vip.hook.SkillHook}.
 */
public final class VipXpListener implements Listener {

    private final BoosterManager boosterManager;

    public VipXpListener(BoosterManager boosterManager) {
        this.boosterManager = boosterManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onExp(PlayerExpChangeEvent event) {
        double mult = boosterManager.effectiveMultiplier(event.getPlayer(), BoostType.XP);
        if (mult > 1.0 && event.getAmount() > 0) {
            event.setAmount((int) Math.round(event.getAmount() * mult));
        }
    }
}
