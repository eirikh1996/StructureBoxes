package io.github.eirikh1996.structureboxes.utils;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.world.World;
import org.bukkit.Location;

public class IWorldEditLocation implements WorldEditLocation {
    private final World world;
    private final int x;
    private final int y;
    private final int z;
    public IWorldEditLocation(Location bukkitLoc){
        world = new BukkitWorld(bukkitLoc.getWorld());
        x = bukkitLoc.getBlockX();
        y = bukkitLoc.getBlockY();
        z = bukkitLoc.getBlockZ();
    }
    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }
}
