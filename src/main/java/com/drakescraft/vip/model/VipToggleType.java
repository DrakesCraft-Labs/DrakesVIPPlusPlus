package com.drakescraft.vip.model;

import org.bukkit.Material;

/**
 * Tipos de perks y habilidades VIP configurables por el jugador.
 */
public enum VipToggleType {

    ABILITY(
            "vip_toggle_ability",
            "Habilidad Divina Activa",
            Material.BLAZE_POWDER,
            "Poder divino único de tu Dios activado con Sneak + F.",
            true
    ),
    PASSIVE_BUFFS(
            "vip_toggle_buffs",
            "Buffs Pasivos y Vida",
            Material.GOLDEN_APPLE,
            "Corazones extra permanentes y efectos de poción ambientales.",
            true
    ),
    AURA(
            "vip_toggle_aura",
            "Aura Cosmética de Partículas",
            Material.NETHER_STAR,
            "Aura visual de partículas en órbita alrededor de tu personaje.",
            true
    ),
    DOUBLE_JUMP(
            "vip_toggle_double_jump",
            "Doble Salto Cosmético",
            Material.FEATHER,
            "Salto cósmico impulsado con rastro de partículas al saltar en el aire.",
            false
    ),
    JOIN_BURST(
            "vip_toggle_join_burst",
            "Ráfaga de Entrada",
            Material.FIREWORK_ROCKET,
            "Efecto de fuegos artificiales y sonido al entrar al servidor.",
            true
    ),
    KILL_FLOURISH(
            "vip_toggle_kill_flourish",
            "Fogonazo de Bajas",
            Material.DIAMOND_SWORD,
            "Destello de partículas de impacto crítico al eliminar a un enemigo.",
            true
    );

    private final String keyName;
    private final String title;
    private final Material icon;
    private final String description;
    private final boolean defaultEnabled;

    VipToggleType(String keyName, String title, Material icon, String description, boolean defaultEnabled) {
        this.keyName = keyName;
        this.title = title;
        this.icon = icon;
        this.description = description;
        this.defaultEnabled = defaultEnabled;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getTitle() {
        return title;
    }

    public Material getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }
}
