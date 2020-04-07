package io.github.eirikh1996.structureboxes.utils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

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
    public Location add(int x, int y, int z){
        return new Location(getWorld(), getX() + x, getY() + y, getZ() + z);
    }

    public Location add(Location loc){
        return new Location(loc.getWorld(), getX() + loc.getX(), getY() + loc.getY(), getZ() + loc.getZ());
    }

    public Location add(Vector loc){
        return add(loc.getX(), loc.getY(), loc.getZ());
    }

    public Location subtract(Location loc){
        return new Location(loc.getWorld(), getX() - loc.getX(), getY() - loc.getY(), getZ() - loc.getZ());
    }

    public Location subtract(Vector loc){
        return subtract(loc.getX(), loc.getY(), loc.getZ());
    }

    public Location subtract(int x, int y, int z) {
        return new Location(getWorld(), getX() - x, getY() - y, getZ() - z);
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

    public Location rotate(final double theta, final Location centre){
        final int xRot = (int) (centre.getX() + cos(theta) * (getX() - centre.getX()) - sin(theta) * (getZ() - centre.getZ()));
        final int zRot = (int) (centre.getZ() + sin(theta) * (getX() - centre.getX()) + cos(theta) * (getZ() - centre.getZ()));
        return new Location(getWorld(), xRot, getY(), zRot);
    }

    public static Location min(ArrayList<Location> structure){
        int x = Integer.MAX_VALUE;
        int y = Integer.MAX_VALUE;
        int z = Integer.MAX_VALUE;
        UUID wID = null;
        for (Location loc : structure){
            wID = loc.getWorld();
            if (loc.getX() < x){
                x = loc.getX();
            }
            if (loc.getY() < y){
                y = loc.getY();
            }
            if (loc.getZ() < z){
                z = loc.getZ();
            }
        }
        return new Location(wID, x, y, z);
    }

    public static Location max(ArrayList<Location> structure) {
        int x = Integer.MIN_VALUE;
        int y = Integer.MIN_VALUE;
        int z = Integer.MIN_VALUE;
        UUID wID = null;
        for (Location loc : structure) {
            wID = loc.getWorld();
            if (loc.getX() > x) {
                x = loc.getX();
            }
            if (loc.getY() > y) {
                y = loc.getY();
            }
            if (loc.getZ() > z) {
                z = loc.getZ();
            }
        }
        return new Location(wID, x, y, z);
    }
    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Location)){
            return false;
        }
        Location other = (Location) obj;
        return getWorld() == other.getWorld() &&
                getX() == other.getX() &&
                getY() == other.getY() &&
                getZ() == other.getZ();
    }


    @Override
    public String toString() {
        return String.format("{world ID: %s, x: %d, y: %d, z: %d}", getWorld().toString(), getX(), getY(), getZ());
    }
}
