package io.github.eirikh1996.structureboxes.utils;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RegionUtils {

    public static boolean isWithinRegion(Location location){
        boolean worldguard = false;
        StructureBoxes structureBoxes = StructureBoxes.getInstance();
        if (structureBoxes.getWorldGuardPlugin() != null){
            worldguard = WorldGuardUtils.insideRegion(location);
        }

        return worldguard;
    }

    public static boolean canPlaceStructure(Player player, Location loc) {
        boolean worldguard = false;
        final StructureBoxes sb = StructureBoxes.getInstance();
        if (sb.getWorldGuardPlugin() != null) {
            worldguard = WorldGuardUtils.canPlaceStructureBox(player, loc);
        }
        return worldguard;
    }

}
