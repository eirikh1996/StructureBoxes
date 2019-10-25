package io.github.eirikh1996.structureboxes.utils;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;

public class MathUtils {
    public static double calculateRequiredRotation(BlockFace originalFace, float yaw){
        double angle = yaw > 0 ? yaw : 360f + yaw;
        if (angle > 315f && angle <=45f){
            angle = 0f;
        } else if (angle > 45f && angle <=135f){
            angle = 90f;
        } else if (angle > 135f && angle <=225f){
            angle = 180f;
        } else if (angle > 225f && angle <=315f){
            angle = 270f;
        }
        switch (originalFace){
            case NORTH:
                angle -= 0f;
                break;
            case SOUTH:
                angle -= 180f;
                break;
            case EAST:
                angle -= 90f;
                break;
            case WEST:
                angle -= 270f;
                break;
        }
        return angle;
    }

    public static Location bukkit2SBLoc(org.bukkit.Location bukkitLoc){
        return new Location(bukkitLoc.getWorld().getUID(), bukkitLoc.getBlockX(), bukkitLoc.getBlockY(), bukkitLoc.getBlockZ());
    }

    public static org.bukkit.Location sb2BukkitLoc(Location location){
        return new org.bukkit.Location(Bukkit.getWorld(location.getWorld()), location.getX(), location.getY(), location.getZ());
    }
}
