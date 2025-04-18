package io.github.eirikh1996.structureboxes.utils;

import com.massivecraft.factions.*;
import com.massivecraft.factions.perms.PermissibleActions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

public class FactionsUUIDUtils {

    public static boolean canBuild(Player player, Location location) {
        final FLocation fLoc = new FLocation(location);
        final Faction faction = Board.getInstance().getFactionAt(fLoc);
        final FPlayer fp = FPlayers.getInstance().getByPlayer(player);
        return fp.isAdminBypassing() ||
                FactionsPlugin.getInstance().conf().factions().protection().getPlayersWhoBypassAllProtection().contains(fp.getName()) ||
                faction.hasAccess(fp, PermissibleActions.BUILD, fLoc) ||
                faction.playerHasOwnershipRights(fp, fLoc) ||
                (FactionsPlugin.getInstance().getWorldguard() != null && FactionsPlugin.getInstance().getWorldguard().playerCanBuild(player, location));
    }

    public static boolean isWithinRegion(Location location) {
        final FLocation fLoc = new FLocation(location);
        return Board.getInstance().getFactionAt(fLoc) != Factions.getInstance().getWilderness();
    }

    public static void structureBoxPermission() {

    }

    public static boolean isFactionsUUID(Plugin plugin) {
        return plugin instanceof FactionsPlugin;
    }

    private class FactionsUUIDIntegrationException extends Exception {
        public FactionsUUIDIntegrationException (String message) {
            super(message);
        }

        public FactionsUUIDIntegrationException (String message, Throwable cause) {
            super(message, cause);
        }
    }
}
