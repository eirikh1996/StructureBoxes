package io.github.eirikh1996.structureboxes.utils;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RedProtectUtils {
    public static boolean canBuild(Player player, Location location){
        return StructureBoxes.getInstance().getRedProtectPlugin().getAPI().getRegion(location).canBuild(player);
    }

    public static boolean withinRegion(Location location) {
        return StructureBoxes.getInstance().getRedProtectPlugin().getAPI().getRegion(location) != null;
    }
}
