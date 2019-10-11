package io.github.eirikh1996.structureboxes.utils;

import java.util.UUID;

public final class Location {
    private final UUID world;
    private final int x;
    private final int y;
    private final int z;

    public Location(UUID world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public UUID getWorld() {
        return world;
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
