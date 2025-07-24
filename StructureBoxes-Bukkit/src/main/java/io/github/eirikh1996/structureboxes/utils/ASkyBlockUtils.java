package io.github.eirikh1996.structureboxes.utils;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;
import org.bukkit.Location;
import org.bukkit.entity.Player;
@Deprecated(forRemoval = true)
public class ASkyBlockUtils {
    public static boolean canBuild(Player player, Location location) {
        final Island island = ASkyBlockAPI.getInstance().getIslandAt(location);
        return island != null && (island.getOwner().equals(player.getUniqueId()) || island.getMembers().contains(player.getUniqueId()));
    }

    public static boolean isWithinRegion(Location location) {
        return ASkyBlockAPI.getInstance().getIslandAt(location) != null;
    }
}
