package io.github.eirikh1996.structureboxes.utils;

import com.sk89q.worldedit.world.World;

public interface WorldEditLocation {
    World getWorld();
    int getX();
    int getY();
    int getZ();
}
