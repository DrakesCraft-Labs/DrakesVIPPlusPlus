package com.drakescraft.vip.command;

import com.drakescraft.vip.DrakesVipPlugin;
import com.drakescraft.vip.api.DrakesVipApi;
import com.drakescraft.vip.gui.VipGui;
import com.drakescraft.vip.model.VipTier;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

/**
 * {@code /vip}: abre el menú GUI interactivo para configurar habilidades y efectos.
 * {@code /vip info}: muestra el resumen textual de rango, banda y boosters.
 * {@code /vip reload} (permiso staff) recarga config/tiers/boosters en caliente.
 */
public final class VipCommand implements CommandExecutor {

    private final DrakesVipPlugin plugin;
    private final VipGui vipGui;

    public VipCommand(DrakesVipPlugin plugin, VipGui vipGui) {
        this.plugin = plugin;
        this.vipGui = vipGui;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command,
                             @Nonnull String label, @Nonnull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("drakesvip.admin")) {
                sender.sendMessage(Component.text("Sin permiso.", NamedTextColor.RED));
                return true;
            }
            plugin.reloadAll();
            sender.sendMessage(Component.text("DrakesVIP++ recargado.", NamedTextColor.GREEN));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Solo jugadores. Usa /vip reload desde consola.", NamedTextColor.RED));
            return true;
        }

        if (args.length > 0) {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "all", "tiers", "catalogo", "catalog", "rangos", "lista" -> {
                    vipGui.openCatalog(player);
                    return true;
                }
                case "info" -> {
                    showInfo(player);
                    return true;
                }
            }
        }

        // Por defecto abre el menú interactivo
        vipGui.open(player);
        return true;
    }

    private void showInfo(Player player) {
        VipTier tier = DrakesVipApi.getTier(player);
        player.sendMessage(Component.text("━━━━━━ DrakesVIP++ ━━━━━━", NamedTextColor.GOLD));
        if (tier == null) {
            player.sendMessage(Component.text("No tienes rango VIP activo.", NamedTextColor.GRAY));
            player.sendMessage(Component.text("Consíguelo en la tienda y potencia tu juego: web.drakescraft.cl", NamedTextColor.YELLOW));
        } else {
            player.sendMessage(Component.text("Rango: ", NamedTextColor.GRAY)
                    .append(Component.text(tier.name() + " (#" + tier.getHierarchy() + ")", NamedTextColor.AQUA)));
            player.sendMessage(Component.text("Banda: ", NamedTextColor.GRAY)
                    .append(Component.text(tier.getBand().name(), NamedTextColor.LIGHT_PURPLE)));
        }
        player.sendMessage(Component.text("Booster dinero: x" + fmt(DrakesVipApi.getMoneyMultiplier(player)), NamedTextColor.GREEN));
        player.sendMessage(Component.text("Booster XP: x" + fmt(DrakesVipApi.getXpMultiplier(player)), NamedTextColor.GREEN));
        player.sendMessage(Component.text("Booster skills: x" + fmt(DrakesVipApi.getSkillMultiplier(player)), NamedTextColor.GREEN));

        Component menuLink = Component.text("[Abrir Configuración VIP]", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.runCommand("/vip"));
        Component catalogLink = Component.text(" [Ver Catálogo de Todos los Rangos]", NamedTextColor.GOLD, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.runCommand("/vip catalogo"));
        player.sendMessage(menuLink.append(catalogLink));
    }

    private String fmt(double d) {
        return d == Math.floor(d) ? String.valueOf((int) d) : String.format("%.2f", d);
    }
}
