package io.github.eirikh1996.structureboxes.utils;

import com.sk89q.worldedit.world.World;

import java.util.UUID;

public interface WorldEditLocation {
    World getWorld();
    UUID getWorldID();
    Location toSBloc();
    int getX();
    int getY();
    int getZ();
}
