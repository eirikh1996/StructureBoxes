package io.github.eirikh1996.structureboxes.utils;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.world.World;
import org.bukkit.Location;

import java.util.UUID;

public class WorldEditLocation {
    private final World world;
    private final UUID worldID;
    private final int x;
    private final int y;
    private final int z;

    public WorldEditLocation(Location bukkitLoc) {
        world = new BukkitWorld(bukkitLoc.getWorld());
        worldID = bukkitLoc.getWorld().getUID();
        x = bukkitLoc.getBlockX();
        y = bukkitLoc.getBlockY();
        z = bukkitLoc.getBlockZ();
    }

    public World getWorld() {
        return world;
    }

    public UUID getWorldID() {
        return worldID;
    }

    public io.github.eirikh1996.structureboxes.utils.Location toSBloc() {
        return new io.github.eirikh1996.structureboxes.utils.Location(getWorld().getName(), getX(), getY(), getZ());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}
