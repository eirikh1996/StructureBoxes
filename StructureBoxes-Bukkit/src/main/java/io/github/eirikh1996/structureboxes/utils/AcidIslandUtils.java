package io.github.eirikh1996.structureboxes.utils;

import com.wasteofplastic.acidisland.ASkyBlockAPI;
import com.wasteofplastic.acidisland.Island;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AcidIslandUtils {
    public static boolean canBuild(Player player, Location location) {
        final Island island = ASkyBlockAPI.getInstance().getIslandAt(location);
        return island != null && (island.getOwner().equals(player.getUniqueId()) || island.getMembers().contains(player.getUniqueId()));
    }

    public static boolean isWithinRegion(Location location) {
        return ASkyBlockAPI.getInstance().getIslandAt(location) != null;
    }
}
