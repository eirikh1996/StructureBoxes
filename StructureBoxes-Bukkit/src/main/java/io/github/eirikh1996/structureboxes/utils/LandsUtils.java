package io.github.eirikh1996.structureboxes.utils;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LandsUtils {

    private static LandsIntegration landsInt = new LandsIntegration(StructureBoxes.getInstance());

    public static boolean canBuild(Player player, Location location) {
        if (!landsInt.isClaimed(location)) {
            return true;
        }
        final Land land = landsInt.getLand(location);
        return land != null && (land.getOwnerUID() == player.getUniqueId() || land.getTrustedPlayer(player.getUniqueId()) != null);
    }

    public static boolean isWithinRegion(Location location) {
        return landsInt.isClaimed(location);
    }
}
