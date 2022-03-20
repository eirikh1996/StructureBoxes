package io.github.eirikh1996.structureboxes.utils;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class Vector implements Comparable<Vector>, Serializable, Cloneable {
    private final int x, y, z;

    public Vector(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
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


    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector)){
            return false;
        }
        final Vector other = (Vector) obj;
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public int compareTo(@NotNull Vector o) {
        return (o.x - x) + (o.y - y) + (o.z - z);
    }

    @Override
    protected Object clone() {
        return new Vector(x, y, z);
    }
}
