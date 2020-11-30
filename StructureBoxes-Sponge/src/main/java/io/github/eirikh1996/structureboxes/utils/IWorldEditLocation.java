package io.github.eirikh1996.structureboxes.utils;

import com.sk89q.worldedit.world.World;
import io.github.eirikh1996.structureboxes.StructureBoxes;

import java.util.UUID;

public class IWorldEditLocation implements WorldEditLocation {
    private final World world;
    private final UUID worldID;
    private final int x, y, z;
    public IWorldEditLocation(org.spongepowered.api.world.Location<org.spongepowered.api.world.World> spongeLoc) {
        world = StructureBoxes.getInstance().getWorldEditPlugin().getWorld(spongeLoc.getExtent());
        worldID = spongeLoc.getExtent().getUniqueId();
        x = spongeLoc.getBlockX();
        y = spongeLoc.getBlockY();
        z = spongeLoc.getBlockZ();
    }
    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public UUID getWorldID() {
        return worldID;
    }

    @Override
    public Location toSBloc() {
        return new Location(world.getName(), x, y, z);
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
