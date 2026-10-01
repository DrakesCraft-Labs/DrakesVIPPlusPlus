package com.drakescraft.vip;

import com.drakescraft.vip.ability.VipAbilityListener;
import com.drakescraft.vip.api.DrakesVipApi;
import com.drakescraft.vip.command.VipCommand;
import com.drakescraft.vip.hook.SkillHook;
import com.drakescraft.vip.listener.VipConnectionListener;
import com.drakescraft.vip.listener.VipXpListener;
import com.drakescraft.vip.gui.VipGui;
import com.drakescraft.vip.gui.VipMenuHolder;
import com.drakescraft.vip.manager.BoosterManager;
import com.drakescraft.vip.manager.VipBuffManager;
import com.drakescraft.vip.manager.VipManager;
import com.drakescraft.vip.manager.VipToggleManager;
import com.drakescraft.vip.model.VipTier;
import com.drakescraft.vip.task.BoosterScheduler;
import com.drakescraft.vip.task.VipAuraTask;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * DrakesVIP++ — perks activos por rango VIP (15 tiers) + booster de fin de semana.
 * Standalone: solo LEE los grupos LuckPerms que entrega Odysseia (Tebex). No toca
 * la entrega de compras. Espeja la arquitectura de DrakesRankup.
 */
public final class DrakesVipPlugin extends JavaPlugin {

    private VipManager vipManager;
    private VipBuffManager buffManager;
    private BoosterManager boosterManager;
    private SkillHook skillHook;
    private VipToggleManager toggleManager;
    private VipGui vipGui;

    private BoosterScheduler boosterScheduler;
    private VipAuraTask auraTask;

    private FileConfiguration boostersConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfAbsent("tiers.yml");
        saveResourceIfAbsent("boosters.yml");
        loadBoostersConfig();

        this.vipManager = new VipManager(getLogger());
        if (!vipManager.hook()) {
            getLogger().severe("Deshabilitando DrakesVIP++: falta LuckPerms.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.buffManager = new VipBuffManager(this);
        this.toggleManager = new VipToggleManager(this);
        this.buffManager.setToggleManager(toggleManager);
        this.toggleManager.setManagers(vipManager, buffManager);

        this.boosterManager = new BoosterManager(getLogger(), vipManager);
        this.boosterManager.load(boostersConfig);
        this.skillHook = new SkillHook(this, boosterManager);
        this.vipGui = new VipGui(vipManager, boosterManager, toggleManager);

        DrakesVipApi.init(vipManager, boosterManager);

        // Listeners
        getServer().getPluginManager().registerEvents(
                new VipConnectionListener(this, vipManager, buffManager, skillHook), this);
        getServer().getPluginManager().registerEvents(new VipXpListener(boosterManager), this);
        getServer().getPluginManager().registerEvents(new VipAbilityListener(this, vipManager, toggleManager), this);
        getServer().getPluginManager().registerEvents(
                new com.drakescraft.vip.listener.VipEffectsListener(this, vipManager, toggleManager), this);
        getServer().getPluginManager().registerEvents(
                new com.drakescraft.vip.listener.VipMenuListener(vipManager, toggleManager, vipGui), this);

        // Comando
        if (getCommand("vip") != null) {
            getCommand("vip").setExecutor(new VipCommand(this, vipGui, vipManager));
        }

        // Suscribirse al EventBus de LuckPerms para actualizar rangos y buffs en tiempo real
        if (vipManager.getLuckPerms() != null) {
            vipManager.getLuckPerms().getEventBus().subscribe(this, net.luckperms.api.event.user.UserDataRecalculateEvent.class, event -> {
                Player player = getServer().getPlayer(event.getUser().getUniqueId());
                if (player != null && player.isOnline()) {
                    getServer().getScheduler().runTask(this, () -> {
                        if (player.isOnline()) {
                            VipTier tier = vipManager.refresh(player);
                            buffManager.apply(player, tier);
                            skillHook.refresh(player);
                        }
                    });
                }
            });
        }

        // Tasks
        long checkTicks = getConfig().getLong("booster.check-interval-ticks", 200L); // 10s
        this.boosterScheduler = new BoosterScheduler(this, boosterManager, skillHook);
        this.boosterScheduler.runTaskTimer(this, 100L, checkTicks);

        long auraTicks = getConfig().getLong("cosmetics.aura-interval-ticks", 10L);
        this.auraTask = new VipAuraTask(this, vipManager, toggleManager);
        this.auraTask.runTaskTimer(this, 40L, auraTicks);

        // Refresca a quienes ya estan conectados (reload en caliente)
        for (Player player : getServer().getOnlinePlayers()) {
            VipTier tier = vipManager.refresh(player);
            buffManager.apply(player, tier);
            skillHook.refresh(player);
        }

        getLogger().info("DrakesVIP++ habilitado: 15 tiers (desde Hércules) + booster de finde + GUI de alternancia.");
    }

    @Override
    public void onDisable() {
        for (Player player : getServer().getOnlinePlayers()) {
            if (buffManager != null) {
                buffManager.clear(player);
            }
            if (skillHook != null) {
                skillHook.clear(player);
            }
        }
    }

    /** Recarga config + tiers + boosters y re-aplica a los conectados. */
    public void reloadAll() {
        reloadConfig();
        loadBoostersConfig();
        boosterManager.load(boostersConfig);
        for (Player player : getServer().getOnlinePlayers()) {
            VipTier tier = vipManager.refresh(player);
            buffManager.apply(player, tier);
            skillHook.refresh(player);
        }
    }

    private void loadBoostersConfig() {
        File file = new File(getDataFolder(), "boosters.yml");
        this.boostersConfig = YamlConfiguration.loadConfiguration(file);
    }

    private void saveResourceIfAbsent(String name) {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            saveResource(name, false);
        }
    }
}
