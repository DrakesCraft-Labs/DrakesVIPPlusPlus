package com.drakescraft.vip.manager;

import com.drakescraft.vip.model.VipTier;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.annotation.Nullable;

/**
 * Resuelve el tier VIP de un jugador leyendo sus grupos heredados de LuckPerms.
 * Es de solo lectura sobre LuckPerms: la entrega la sigue haciendo Odysseia (Tebex).
 * Cachea el resultado por UUID y se refresca en join / cambio de grupo.
 */
public final class VipManager {

    private final Logger logger;
    private final Map<UUID, VipTier> cache = new ConcurrentHashMap<>();
    private LuckPerms luckPerms;

    public VipManager(Logger logger) {
        this.logger = logger;
    }

    /** Engancha LuckPerms; devuelve false si no esta disponible. */
    public boolean hook() {
        try {
            this.luckPerms = LuckPermsProvider.get();
            return true;
        } catch (IllegalStateException | NoClassDefFoundError ex) {
            logger.severe("LuckPerms no disponible; DrakesVIP++ no puede resolver rangos.");
            return false;
        }
    }

    /**
     * Recalcula y cachea el tier del jugador leyendo sus grupos LuckPerms.
     * Gana la jerarquia mas alta entre los grupos VIP que posea.
     */
    @Nullable
    public VipTier refresh(Player player) {
        if (luckPerms == null) {
            return null;
        }
        VipTier best = null;
        try {
            User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
            for (Group group : user.getInheritedGroups(user.getQueryOptions())) {
                VipTier tier = VipTier.fromGroup(group.getName());
                if (tier != null && (best == null || tier.getHierarchy() > best.getHierarchy())) {
                    best = tier;
                }
            }
        } catch (Exception ex) {
            logger.warning("No se pudo resolver el tier VIP de " + player.getName() + ": " + ex.getMessage());
        }
        if (best != null) {
            cache.put(player.getUniqueId(), best);
        } else {
            cache.remove(player.getUniqueId());
        }
        return best;
    }

    /** Tier cacheado (sin recalcular). Null si no es VIP. */
    @Nullable
    public VipTier getTier(UUID uuid) {
        return cache.get(uuid);
    }

    @Nullable
    public VipTier getTier(Player player) {
        return cache.get(player.getUniqueId());
    }

    public boolean isVip(Player player) {
        return cache.containsKey(player.getUniqueId());
    }

    public void clear(UUID uuid) {
        cache.remove(uuid);
    }
}
