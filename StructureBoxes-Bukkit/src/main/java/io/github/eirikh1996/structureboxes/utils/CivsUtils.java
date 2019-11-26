package io.github.eirikh1996.structureboxes.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

public class CivsUtils {
    public static boolean allowBuild(Player player, Location location){
        Region region = RegionManager.getInstance().getRegionAt(location);
        region.getEffects();
        return true;
    }
}
