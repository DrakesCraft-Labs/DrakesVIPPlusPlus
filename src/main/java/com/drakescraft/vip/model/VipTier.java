package com.drakescraft.vip.model;

import java.util.Locale;
import javax.annotation.Nullable;

/**
 * Los 15 rangos VIP de DrakesCraft, en el mismo orden de jerarquia que
 * Odysseia/purchases.yml (hercules=1 .. titancaos=15). Cada rango pertenece a
 * una de tres bandas que definen que perks activos recibe. El grupo LuckPerms
 * (entregado por la compra Tebex) es la unica fuente de verdad: este plugin solo
 * LEE ese grupo, nunca lo otorga.
 */
public enum VipTier {

    HERCULES("hercules", 1, VipBand.OLYMPIAN_BASE),
    HESTIA("hestia", 2, VipBand.OLYMPIAN_BASE),
    HERMES("hermes", 3, VipBand.OLYMPIAN_BASE),
    HEFESTO("hefesto", 4, VipBand.OLYMPIAN_BASE),
    ARTEMISA("artemisa", 5, VipBand.OLYMPIAN_BASE),
    AFRODITA("afrodita", 6, VipBand.OLYMPIAN_BASE),
    ZEUS("zeus", 7, VipBand.OLYMPIAN_HIGH),
    THOR("thor", 8, VipBand.OLYMPIAN_HIGH),
    ANUBIS("anubis", 9, VipBand.OLYMPIAN_HIGH),
    POSEIDON("poseidon", 10, VipBand.OLYMPIAN_HIGH),
    TITAN_JAPETO("titanjapeto", 11, VipBand.TITAN),
    TITAN_OCEANO("titanoceanus", 12, VipBand.TITAN),
    TITAN_HIPERION("titanhiperion", 13, VipBand.TITAN),
    TITAN_CRONOS("titancronos", 14, VipBand.TITAN),
    TITAN_CAOS("titancaos", 15, VipBand.TITAN);

    private final String group;
    private final int hierarchy;
    private final VipBand band;

    VipTier(String group, int hierarchy, VipBand band) {
        this.group = group;
        this.hierarchy = hierarchy;
        this.band = band;
    }

    /** Nombre del grupo LuckPerms tal cual lo entrega Odysseia. */
    public String getGroup() {
        return group;
    }

    /** Jerarquia 1..15 (mayor = mejor). */
    public int getHierarchy() {
        return hierarchy;
    }

    public VipBand getBand() {
        return band;
    }

    /**
     * Resuelve el tier a partir de un nombre de grupo LuckPerms.
     *
     * @return el tier, o {@code null} si el grupo no es un rango VIP.
     */
    @Nullable
    public static VipTier fromGroup(@Nullable String groupName) {
        if (groupName == null) {
            return null;
        }
        String normalized = groupName.toLowerCase(Locale.ROOT).trim();
        if (normalized.startsWith("group.")) {
            normalized = normalized.substring("group.".length());
        }
        for (VipTier tier : values()) {
            if (tier.group.equals(normalized)) {
                return tier;
            }
        }
        if ("titanoceano".equals(normalized)) {
            return TITAN_OCEANO;
        }
        return null;
    }

    /** Bandas de perks. */
    public enum VipBand {
        OLYMPIAN_BASE,
        OLYMPIAN_HIGH,
        TITAN
    }
}
