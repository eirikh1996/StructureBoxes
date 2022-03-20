package io.github.eirikh1996.structureboxes.utils;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;

public class MathUtils {
    public static Location bukkit2SBLoc(org.bukkit.Location bukkitLoc){
        return new Location(bukkitLoc.getWorld().getName(), bukkitLoc.getBlockX(), bukkitLoc.getBlockY(), bukkitLoc.getBlockZ());
    }

    public static org.bukkit.Location sb2BukkitLoc(Location location){
        return new org.bukkit.Location(Bukkit.getWorld(location.getWorld()), location.getX(), location.getY(), location.getZ());
    }
}
