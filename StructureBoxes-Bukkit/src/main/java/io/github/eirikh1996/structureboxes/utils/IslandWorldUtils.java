package io.github.eirikh1996.structureboxes.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.islandworld.api.IslandWorldApi;
@Deprecated(
        forRemoval = true
)
public class IslandWorldUtils {
    public static boolean canBuild(Player player, org.bukkit.Location location) {
        return IslandWorldApi.canBuildOnLocation(player, location, true);
    }

    public static boolean withinRegion(Location location) {
        return IslandWorldApi.getIsland(location) != null;
    }
}
