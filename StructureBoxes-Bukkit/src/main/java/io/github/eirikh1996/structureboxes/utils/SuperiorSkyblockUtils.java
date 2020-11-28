package io.github.eirikh1996.structureboxes.utils;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SuperiorSkyblockUtils {
    public static boolean canBuild(Player player, Location location) {
        final Island island = SuperiorSkyblockAPI.getIslandAt(location);
        return island != null && (island.hasPermission(SuperiorSkyblockAPI.getPlayer(player), IslandPrivilege.getByName("build")));
    }

    public static boolean isWithinRegion(Location location) {
        return SuperiorSkyblockAPI.getIslandAt(location) != null;
    }

}
