package com.drakescraft.vip.gui;

import com.drakescraft.vip.api.DrakesVipApi;
import com.drakescraft.vip.manager.BoosterManager;
import com.drakescraft.vip.manager.VipManager;
import com.drakescraft.vip.manager.VipToggleManager;
import com.drakescraft.vip.model.VipTier;
import com.drakescraft.vip.model.VipToggleType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Creador y renderizador de la GUI de configuración de habilidades VIP.
 */
public final class VipGui {

    public static final int SLOT_PROFILE = 4;
    public static final int SLOT_ABILITY = 10;
    public static final int SLOT_BUFFS = 11;
    public static final int SLOT_AURA = 12;
    public static final int SLOT_CATALOG = 13;
    public static final int SLOT_DOUBLE_JUMP = 14;
    public static final int SLOT_JOIN_BURST = 15;
    public static final int SLOT_KILL_FLOURISH = 16;
    public static final int SLOT_CLOSE = 22;

    // Catálogo 54 slots
    public static final int SLOT_CATALOG_HEADER = 4;
    public static final int SLOT_CATALOG_BAND_OLYMPIAN_BASE = 10;
    public static final int SLOT_CATALOG_BAND_OLYMPIAN_HIGH = 19;
    public static final int SLOT_CATALOG_BAND_TITAN = 28;
    public static final int SLOT_CATALOG_STORE = 48;
    public static final int SLOT_CATALOG_BACK = 49;
    public static final int SLOT_CATALOG_CLOSE = 50;

    public static final Map<Integer, VipTier> CATALOG_TIER_SLOTS = Map.ofEntries(
            Map.entry(11, VipTier.HERCULES),
            Map.entry(12, VipTier.HESTIA),
            Map.entry(13, VipTier.HERMES),
            Map.entry(14, VipTier.HEFESTO),
            Map.entry(15, VipTier.ARTEMISA),
            Map.entry(16, VipTier.AFRODITA),
            Map.entry(20, VipTier.ZEUS),
            Map.entry(21, VipTier.THOR),
            Map.entry(22, VipTier.ANUBIS),
            Map.entry(23, VipTier.POSEIDON),
            Map.entry(29, VipTier.TITAN_JAPETO),
            Map.entry(30, VipTier.TITAN_OCEANO),
            Map.entry(31, VipTier.TITAN_HIPERION),
            Map.entry(32, VipTier.TITAN_CRONOS),
            Map.entry(33, VipTier.TITAN_CAOS)
    );

    public static final Map<Integer, VipToggleType> TOGGLE_SLOTS = Map.of(
            SLOT_ABILITY, VipToggleType.ABILITY,
            SLOT_BUFFS, VipToggleType.PASSIVE_BUFFS,
            SLOT_AURA, VipToggleType.AURA,
            SLOT_DOUBLE_JUMP, VipToggleType.DOUBLE_JUMP,
            SLOT_JOIN_BURST, VipToggleType.JOIN_BURST,
            SLOT_KILL_FLOURISH, VipToggleType.KILL_FLOURISH
    );

    private final VipManager vipManager;
    private final BoosterManager boosterManager;
    private final VipToggleManager toggleManager;

    public VipGui(VipManager vipManager, BoosterManager boosterManager, VipToggleManager toggleManager) {
        this.vipManager = vipManager;
        this.boosterManager = boosterManager;
        this.toggleManager = toggleManager;
    }

    /**
     * Abre el menú interactivo para el jugador.
     */
    public void open(Player player) {
        VipMenuHolder holder = new VipMenuHolder(player);
        Component title = Component.text("⚡ ", NamedTextColor.GOLD)
                .append(Component.text("Configuración VIP++", NamedTextColor.WHITE, TextDecoration.BOLD))
                .append(Component.text(" ⚡", NamedTextColor.GOLD));

        Inventory inv = Bukkit.createInventory(holder, 27, title);
        holder.setInventory(inv);

        populate(player, inv);
        player.openInventory(inv);
    }

    /**
     * Actualiza los elementos del inventario en vivo sin cerrarlo.
     */
    public void populate(Player player, Inventory inv) {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, Component.text(" "));

        for (int i = 0; i < 27; i++) {
            if (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, border);
            } else {
                inv.setItem(i, filler);
            }
        }

        // Casilla 4: Perfil Divino
        inv.setItem(SLOT_PROFILE, createProfileItem(player));

        // Toggles
        VipTier tier = vipManager.getTier(player);
        for (Map.Entry<Integer, VipToggleType> entry : TOGGLE_SLOTS.entrySet()) {
            int slot = entry.getKey();
            VipToggleType type = entry.getValue();
            inv.setItem(slot, createToggleItem(player, tier, type));
        }

        // Casilla 13: Catálogo completo de todos los rangos VIP
        inv.setItem(SLOT_CATALOG, createCatalogButtonItem());

        // Casilla 22: Botón Cerrar
        ItemStack closeBtn = createItem(Material.BARRIER,
                Component.text("Cerrar Menú", NamedTextColor.RED, TextDecoration.BOLD),
                List.of(Component.text("Haz clic para volver al juego.", NamedTextColor.GRAY)));
        inv.setItem(SLOT_CLOSE, closeBtn);
    }

    private ItemStack createProfileItem(Player player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.displayName(Component.text("✦ Perfil Divino VIP ✦", NamedTextColor.GOLD, TextDecoration.BOLD));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Jugador: ", NamedTextColor.GRAY)
                    .append(Component.text(player.getName(), NamedTextColor.WHITE)));

            VipTier tier = vipManager.getTier(player);
            if (tier != null) {
                lore.add(Component.text("Rango: ", NamedTextColor.GRAY)
                        .append(Component.text(tier.name(), NamedTextColor.AQUA))
                        .append(Component.text(" (#" + tier.getHierarchy() + ")", NamedTextColor.DARK_AQUA)));
                lore.add(Component.text("Banda: ", NamedTextColor.GRAY)
                        .append(Component.text(tier.getBand().name(), NamedTextColor.LIGHT_PURPLE)));
                lore.add(Component.text("Poder Divino: ", NamedTextColor.GRAY)
                        .append(Component.text(getAbilitySummary(tier), NamedTextColor.YELLOW)));
                lore.add(Component.empty());
                lore.add(Component.text("Multiplicadores Actuales:", NamedTextColor.GOLD));
                lore.add(Component.text("  • Dinero: ", NamedTextColor.GRAY)
                        .append(Component.text("x" + fmt(DrakesVipApi.getMoneyMultiplier(player)), NamedTextColor.GREEN)));
                lore.add(Component.text("  • EXP: ", NamedTextColor.GRAY)
                        .append(Component.text("x" + fmt(DrakesVipApi.getXpMultiplier(player)), NamedTextColor.GREEN)));
                lore.add(Component.text("  • Skills: ", NamedTextColor.GRAY)
                        .append(Component.text("x" + fmt(DrakesVipApi.getSkillMultiplier(player)), NamedTextColor.GREEN)));
            } else {
                lore.add(Component.empty());
                lore.add(Component.text("Sin rango VIP activo.", NamedTextColor.RED));
                lore.add(Component.text("Adquiérelo en: ", NamedTextColor.GRAY)
                        .append(Component.text("web.drakescraft.cl", NamedTextColor.AQUA)));
            }
            lore.add(Component.empty());
            lore.add(Component.text("Configura tus habilidades y efectos abajo:", NamedTextColor.DARK_GRAY));

            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createToggleItem(Player player, VipTier tier, VipToggleType type) {
        boolean enabled = toggleManager.isEnabled(player, type);
        Material mat = enabled ? Material.LIME_DYE : Material.GRAY_DYE;

        NamedTextColor statusColor = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;
        String statusText = enabled ? "ACTIVADA" : "DESACTIVADA";

        Component name = Component.text(type.getTitle(), statusColor, TextDecoration.BOLD)
                .append(Component.text(" [" + statusText + "]", NamedTextColor.DARK_GRAY));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(type.getDescription(), NamedTextColor.GRAY));
        lore.add(Component.empty());

        if (tier == null) {
            lore.add(Component.text("⚠ Requiere rango VIP activo.", NamedTextColor.RED));
            lore.add(Component.text("Visita la tienda oficial en web.drakescraft.cl", NamedTextColor.YELLOW));
        } else {
            lore.add(Component.text("Estado: ", NamedTextColor.GRAY)
                    .append(Component.text(statusText, statusColor, TextDecoration.BOLD)));
            lore.add(Component.empty());
            if (enabled) {
                lore.add(Component.text("▶ Haz clic para ", NamedTextColor.YELLOW)
                        .append(Component.text("DESACTIVAR", NamedTextColor.RED, TextDecoration.BOLD))
                        .append(Component.text(".", NamedTextColor.YELLOW)));
            } else {
                lore.add(Component.text("▶ Haz clic para ", NamedTextColor.YELLOW)
                        .append(Component.text("ACTIVAR", NamedTextColor.GREEN, TextDecoration.BOLD))
                        .append(Component.text(".", NamedTextColor.YELLOW)));
            }
        }

        return createItem(mat, name, lore);
    }

    private ItemStack createItem(Material material, Component name) {
        return createItem(material, name, null);
    }

    private ItemStack createItem(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            if (lore != null) {
                meta.lore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private String getAbilitySummary(VipTier tier) {
        return switch (tier) {
            case HERCULES -> "Ground Slam (Onda Sísmica)";
            case HESTIA -> "Hearth (Llama Sagrada Purificadora)";
            case HERMES -> "Blink Dash (Impulso Sónico)";
            case HEFESTO -> "Forge Meteor (Lluvia Ígnea)";
            case ARTEMISA -> "Hunter's Volley (Flechas Espectrales)";
            case AFRODITA -> "Charm (Fascinación y Lentitud en Área)";
            case ZEUS -> "Lightning (Descarga y Rayos Divinos)";
            case THOR -> "Thunder Dash (Embestida Eléctrica)";
            case ANUBIS -> "Soul Drain (Extracción y Robo de Vida)";
            case POSEIDON -> "Marea (Onda de Expulsión Acuática)";
            case TITAN_JAPETO -> "Titan Forge (Poder y Fortaleza Titánica)";
            case TITAN_OCEANO -> "Vórtice Abisal (Atracción y Asfixia)";
            case TITAN_HIPERION -> "Solar Flare (Destello Abrasador)";
            case TITAN_CRONOS -> "Time Stop (Parada Temporal - Cooldown -50%)";
            case TITAN_CAOS -> "Cataclismo Primordial (Vacío y Rayos Supremos)";
        };
    }

    private String fmt(double d) {
        return d == Math.floor(d) ? String.valueOf((int) d) : String.format("%.2f", d);
    }

    private ItemStack createCatalogButtonItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("✦ Catálogo de Todos los Rangos VIP ✦", NamedTextColor.GOLD, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Explora las ventajas, corazones extra,", NamedTextColor.GRAY));
            lore.add(Component.text("habilidades míticas y multiplicadores de", NamedTextColor.GRAY));
            lore.add(Component.text("los 15 rangos VIP (Hércules a Titán Caos).", NamedTextColor.GRAY));
            lore.add(Component.empty());
            lore.add(Component.text("▶ Haz clic para ver el catálogo completo.", NamedTextColor.YELLOW));
            meta.lore(lore);
            meta.setEnchantmentGlintOverride(true);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Abre el catálogo interactivo de 54 casillas mostrando todos los 15 rangos VIP.
     */
    public void openCatalog(Player player) {
        VipCatalogHolder holder = new VipCatalogHolder(player);
        Component title = Component.text("⚡ ", NamedTextColor.GOLD)
                .append(Component.text("Catálogo Oficial de Rangos VIP", NamedTextColor.WHITE, TextDecoration.BOLD))
                .append(Component.text(" ⚡", NamedTextColor.GOLD));

        Inventory inv = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inv);

        populateCatalog(player, inv);
        player.openInventory(inv);
    }

    /**
     * Rellena el catálogo con todos los rangos, separadores y botones de navegación.
     */
    public void populateCatalog(Player player, Inventory inv) {
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, Component.text(" "));
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, border);
            } else {
                inv.setItem(i, filler);
            }
        }

        // Encabezado
        inv.setItem(SLOT_CATALOG_HEADER, createCatalogHeaderItem());

        // Indicadores de Bandas
        inv.setItem(SLOT_CATALOG_BAND_OLYMPIAN_BASE, createBandIndicator(VipTier.VipBand.OLYMPIAN_BASE));
        inv.setItem(SLOT_CATALOG_BAND_OLYMPIAN_HIGH, createBandIndicator(VipTier.VipBand.OLYMPIAN_HIGH));
        inv.setItem(SLOT_CATALOG_BAND_TITAN, createBandIndicator(VipTier.VipBand.TITAN));

        // Los 15 Tiers
        VipTier playerTier = vipManager.getTier(player);
        for (Map.Entry<Integer, VipTier> entry : CATALOG_TIER_SLOTS.entrySet()) {
            int slot = entry.getKey();
            VipTier tier = entry.getValue();
            boolean isCurrent = (playerTier == tier);
            inv.setItem(slot, createTierCatalogItem(tier, isCurrent));
        }

        // Navegación
        inv.setItem(SLOT_CATALOG_STORE, createItem(Material.EMERALD,
                Component.text("✦ Tienda Web Oficial ✦", NamedTextColor.GREEN, TextDecoration.BOLD),
                List.of(
                        Component.text("Adquiere o mejora tu rango VIP al instante:", NamedTextColor.GRAY),
                        Component.text("web.drakescraft.cl", NamedTextColor.AQUA, TextDecoration.UNDERLINED),
                        Component.text("Entregas automáticas y seguras vía Tebex.", NamedTextColor.YELLOW)
                )));

        inv.setItem(SLOT_CATALOG_BACK, createItem(Material.ARROW,
                Component.text("← Volver a Configuración VIP", NamedTextColor.YELLOW, TextDecoration.BOLD),
                List.of(Component.text("Haz clic para volver al menú de opciones VIP.", NamedTextColor.GRAY))));

        inv.setItem(SLOT_CATALOG_CLOSE, createItem(Material.BARRIER,
                Component.text("✕ Cerrar Menú", NamedTextColor.RED, TextDecoration.BOLD),
                List.of(Component.text("Haz clic para regresar al juego.", NamedTextColor.GRAY))));
    }

    private ItemStack createCatalogHeaderItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("✦ Catálogo de Rangos VIP & Mitológicos ✦", NamedTextColor.GOLD, TextDecoration.BOLD));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("DrakesCraft cuenta con 15 rangos organizados en 3 bandas.", NamedTextColor.GRAY));
            lore.add(Component.text("Cada rango otorga corazones extra, multiplicadores de fin de", NamedTextColor.GRAY));
            lore.add(Component.text("semana, habilidades de combate (Sneak+F) y cosméticos divinos.", NamedTextColor.GRAY));
            lore.add(Component.empty());
            lore.add(Component.text("✦ Tu rango actual se encuentra resaltado con brillo divino.", NamedTextColor.YELLOW));
            meta.lore(lore);
            meta.setEnchantmentGlintOverride(true);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBandIndicator(VipTier.VipBand band) {
        Material mat = Material.COPPER_BLOCK;
        NamedTextColor color = NamedTextColor.GOLD;
        String title = "✦ Banda Olímpica Base (#1 - #6) ✦";
        List<Component> lore = new ArrayList<>();

        switch (band) {
            case OLYMPIAN_BASE -> {
                mat = Material.COPPER_BLOCK;
                color = NamedTextColor.GOLD;
                title = "✦ Banda Olímpica Base (#1 - #6) ✦";
                lore.add(Component.text("Rangos: Hércules, Hestia, Hermes, Hefesto, Artemisa, Afrodita", NamedTextColor.GRAY));
                lore.add(Component.empty());
                lore.add(Component.text("• Vida Extra: ", NamedTextColor.GRAY)
                        .append(Component.text("+1 Corazón permanente (2 HP)", NamedTextColor.RED)));
                lore.add(Component.text("• Pasivas: ", NamedTextColor.GRAY)
                        .append(Component.text("Velocidad I + pasiva temática por dios", NamedTextColor.AQUA)));
                lore.add(Component.text("• Booster Fin de Semana: ", NamedTextColor.GRAY)
                        .append(Component.text("+25% extra (Total x2.25)", NamedTextColor.GREEN)));
                lore.add(Component.text("• Habilidad Activa (Sneak + F): ", NamedTextColor.GRAY)
                        .append(Component.text("Habilidades míticas básicas", NamedTextColor.YELLOW)));
            }
            case OLYMPIAN_HIGH -> {
                mat = Material.GOLD_BLOCK;
                color = NamedTextColor.AQUA;
                title = "✦ Banda Olímpica Alta (#7 - #10) ✦";
                lore.add(Component.text("Rangos: Zeus, Thor, Anubis, Poseidón", NamedTextColor.GRAY));
                lore.add(Component.empty());
                lore.add(Component.text("• Vida Extra: ", NamedTextColor.GRAY)
                        .append(Component.text("+3 Corazones permanentes (6 HP)", NamedTextColor.RED)));
                lore.add(Component.text("• Pasivas: ", NamedTextColor.GRAY)
                        .append(Component.text("Velocidad I + pasiva temática elemental", NamedTextColor.AQUA)));
                lore.add(Component.text("• Booster Fin de Semana: ", NamedTextColor.GRAY)
                        .append(Component.text("+50% extra (Total x2.50)", NamedTextColor.GREEN)));
                lore.add(Component.text("• Habilidad Activa (Sneak + F): ", NamedTextColor.GRAY)
                        .append(Component.text("Habilidades divinas de combate masivo", NamedTextColor.YELLOW)));
            }
            case TITAN -> {
                mat = Material.NETHERITE_BLOCK;
                color = NamedTextColor.LIGHT_PURPLE;
                title = "✦ Banda Titán Suprema (#11 - #15) ✦";
                lore.add(Component.text("Rangos: Jápeto, Océano, Hiperión, Cronos, Caos", NamedTextColor.GRAY));
                lore.add(Component.empty());
                lore.add(Component.text("• Vida Extra: ", NamedTextColor.GRAY)
                        .append(Component.text("+5 Corazones permanentes (10 HP)", NamedTextColor.RED)));
                lore.add(Component.text("• Pasivas: ", NamedTextColor.GRAY)
                        .append(Component.text("Velocidad II, Visión Nocturna I + pasiva titán", NamedTextColor.AQUA)));
                lore.add(Component.text("• Booster Fin de Semana: ", NamedTextColor.GRAY)
                        .append(Component.text("+100% extra (Total x3.00 / Doble)", NamedTextColor.GREEN)));
                lore.add(Component.text("• Habilidad Activa (Sneak + F): ", NamedTextColor.GRAY)
                        .append(Component.text("Poderes cósmicos y cataclismos", NamedTextColor.YELLOW)));
                lore.add(Component.text("• Especial Cronos & Caos: ", NamedTextColor.GOLD)
                        .append(Component.text("Enfriamiento reducido a la mitad (15s)", NamedTextColor.WHITE)));
            }
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(title, color, TextDecoration.BOLD));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createTierCatalogItem(VipTier tier, boolean isCurrent) {
        Material mat = getTierMaterial(tier);
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamedTextColor titleColor = switch (tier.getBand()) {
                case OLYMPIAN_BASE -> NamedTextColor.GOLD;
                case OLYMPIAN_HIGH -> NamedTextColor.AQUA;
                case TITAN -> NamedTextColor.LIGHT_PURPLE;
            };

            Component nameComp;
            if (isCurrent) {
                nameComp = Component.text("✦ [TU RANGO] ", NamedTextColor.GREEN, TextDecoration.BOLD)
                        .append(Component.text(getTierDisplayName(tier) + " (#" + tier.getHierarchy() + ") ✦", titleColor, TextDecoration.BOLD));
                meta.setEnchantmentGlintOverride(true);
            } else {
                nameComp = Component.text("✦ Rango " + getTierDisplayName(tier) + " (#" + tier.getHierarchy() + ") ✦", titleColor, TextDecoration.BOLD);
            }
            meta.displayName(nameComp);

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Banda: ", NamedTextColor.GRAY)
                    .append(Component.text(tier.getBand().name(), titleColor)));

            int hearts = switch (tier.getBand()) {
                case OLYMPIAN_BASE -> 1;
                case OLYMPIAN_HIGH -> 3;
                case TITAN -> 5;
            };
            lore.add(Component.text("Vida Extra: ", NamedTextColor.GRAY)
                    .append(Component.text("+" + hearts + " Corazones (" + (hearts * 2) + " HP permanentes)", NamedTextColor.RED)));

            lore.add(Component.text("Efectos Pasivos:", NamedTextColor.GOLD));
            for (String passive : getTierPassivesDisplay(tier)) {
                lore.add(Component.text("  • " + passive, NamedTextColor.WHITE));
            }

            String boosterStr = switch (tier.getBand()) {
                case OLYMPIAN_BASE -> "+25% extra (Total x2.25)";
                case OLYMPIAN_HIGH -> "+50% extra (Total x2.50)";
                case TITAN -> "+100% extra (Total x3.00 / Doble)";
            };
            lore.add(Component.text("Booster Fin de Semana: ", NamedTextColor.GOLD)
                    .append(Component.text(boosterStr, NamedTextColor.GREEN)));

            lore.add(Component.empty());
            lore.add(Component.text("Habilidad Mítica (Sneak + F):", NamedTextColor.YELLOW, TextDecoration.BOLD));
            lore.add(Component.text("  • " + getAbilitySummary(tier), NamedTextColor.YELLOW));
            lore.add(Component.text("    " + getAbilityDetailedDescription(tier), NamedTextColor.GRAY));
            int cd = (tier.getHierarchy() >= VipTier.TITAN_CRONOS.getHierarchy()) ? 15 : 30;
            lore.add(Component.text("  • Enfriamiento: ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(cd + "s", NamedTextColor.WHITE))
                    .append(tier.getHierarchy() >= VipTier.TITAN_CRONOS.getHierarchy()
                            ? Component.text(" (¡Reducción especial -50%!)", NamedTextColor.GOLD)
                            : Component.empty()));

            lore.add(Component.empty());
            lore.add(Component.text("Cosméticos y Beneficios:", NamedTextColor.GOLD));
            lore.add(Component.text("  • Aura de partículas personalizable (/vip)", NamedTextColor.GRAY));
            lore.add(Component.text("  • Ráfaga al entrar y al derrotar enemigos", NamedTextColor.GRAY));
            lore.add(Component.text("  • Acceso a /fly, /feed, /heal, kits y bóvedas", NamedTextColor.GRAY));

            lore.add(Component.empty());
            if (isCurrent) {
                lore.add(Component.text("✔ ¡ESTE ES TU RANGO ACTUAL ACTIVO!", NamedTextColor.GREEN, TextDecoration.BOLD));
            } else {
                lore.add(Component.text("▶ Disponible en: ", NamedTextColor.YELLOW)
                        .append(Component.text("web.drakescraft.cl", NamedTextColor.AQUA, TextDecoration.UNDERLINED)));
            }

            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private Material getTierMaterial(VipTier tier) {
        return switch (tier) {
            case HERCULES -> Material.IRON_SWORD;
            case HESTIA -> Material.CAMPFIRE;
            case HERMES -> Material.FEATHER;
            case HEFESTO -> Material.ANVIL;
            case ARTEMISA -> Material.BOW;
            case AFRODITA -> Material.POPPY;
            case ZEUS -> Material.LIGHTNING_ROD;
            case THOR -> Material.NETHERITE_AXE;
            case ANUBIS -> Material.WITHER_SKELETON_SKULL;
            case POSEIDON -> Material.TRIDENT;
            case TITAN_JAPETO -> Material.NETHERITE_INGOT;
            case TITAN_OCEANO -> Material.HEART_OF_THE_SEA;
            case TITAN_HIPERION -> Material.GLOWSTONE;
            case TITAN_CRONOS -> Material.CLOCK;
            case TITAN_CAOS -> Material.RESPAWN_ANCHOR;
        };
    }

    private String getTierDisplayName(VipTier tier) {
        return switch (tier) {
            case HERCULES -> "Hércules";
            case HESTIA -> "Hestia";
            case HERMES -> "Hermes";
            case HEFESTO -> "Hefesto";
            case ARTEMISA -> "Artemisa";
            case AFRODITA -> "Afrodita";
            case ZEUS -> "Zeus";
            case THOR -> "Thor";
            case ANUBIS -> "Anubis";
            case POSEIDON -> "Poseidón";
            case TITAN_JAPETO -> "Titán Jápeto";
            case TITAN_OCEANO -> "Titán Océano";
            case TITAN_HIPERION -> "Titán Hiperión";
            case TITAN_CRONOS -> "Titán Cronos";
            case TITAN_CAOS -> "Titán Caos";
        };
    }

    private List<String> getTierPassivesDisplay(VipTier tier) {
        return switch (tier) {
            case HERCULES -> List.of("Velocidad I");
            case HESTIA -> List.of("Resistencia al Fuego I", "Velocidad I");
            case HERMES -> List.of("Velocidad II (Doble velocidad)");
            case HEFESTO -> List.of("Resistencia al Fuego I", "Prisa Minera I", "Velocidad I");
            case ARTEMISA -> List.of("Visión Nocturna I", "Suerte I", "Velocidad I");
            case AFRODITA -> List.of("Velocidad I");
            case ZEUS -> List.of("Velocidad I");
            case THOR -> List.of("Velocidad I");
            case ANUBIS -> List.of("Velocidad I");
            case POSEIDON -> List.of("Respiración Acuática I", "Gracia de Delfín I", "Velocidad I");
            case TITAN_JAPETO -> List.of("Prisa Minera II", "Velocidad II", "Visión Nocturna I");
            case TITAN_OCEANO -> List.of("Respiración Acuática I", "Poder de Conducto I", "Gracia de Delfín I", "Velocidad II", "Visión Nocturna I");
            case TITAN_HIPERION -> List.of("Resistencia al Fuego I", "Velocidad II", "Visión Nocturna I");
            case TITAN_CRONOS -> List.of("Prisa Minera I", "Velocidad II", "Visión Nocturna I");
            case TITAN_CAOS -> List.of("Resistencia al Fuego I", "Visión Nocturna I", "Respiración Acuática I", "Velocidad II");
        };
    }

    private String getAbilityDetailedDescription(VipTier tier) {
        return switch (tier) {
            case HERCULES -> "Golpea el suelo creando una onda expansiva que daña y eleva a los enemigos.";
            case HESTIA -> "Invoca un fuego sagrado protector que cura al invocador y extingue efectos negativos.";
            case HERMES -> "Propulsión supersónica instantánea en la dirección de la mirada con estela divina.";
            case HEFESTO -> "Lanza fragmentos de forja ardientes que calcinan a los objetivos frente al jugador.";
            case ARTEMISA -> "Dispara una salva de flechas de luz guiadas que persiguen a los enemigos hostiles.";
            case AFRODITA -> "Desprende un aura cautivadora que apacigua, debilita y ralentiza a las entidades.";
            case ZEUS -> "Descarga rayos celestiales sobre todos los enemigos en un radio de 9 bloques.";
            case THOR -> "Carga frontal electrizante con impacto devastador de truenos sobre los adversarios.";
            case ANUBIS -> "Roba la energía vital de las criaturas cercanas y la transfiere al jugador como curación.";
            case POSEIDON -> "Libera un torrente de olas que repele bruscamente y ahoga a las criaturas hostiles.";
            case TITAN_JAPETO -> "Otorga resistencia impenetrable y fuerza destructiva descomunal durante 10 segundos.";
            case TITAN_OCEANO -> "Vórtice acuático gravitacional que arrastra, asfixia y daña a las criaturas.";
            case TITAN_HIPERION -> "Emite un destello solar cegador que incinera instantáneamente a los agresores en área.";
            case TITAN_CRONOS -> "Detiene el flujo del tiempo inmovilizando completamente a los enemigos a tu alrededor.";
            case TITAN_CAOS -> "Desata el vacío primordial combinando rayos celestiales y ondas de choque extremas.";
        };
    }
}
