package io.github.eirikh1996.structureboxes.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class CollectionUtils {
    public static Collection<Location> boundingBox(@NotNull final Location min, @NotNull final Location max){
        Collection<Location> box = new HashSet<>();
        for (int x = min.getX(); x <= max.getX() ; x++){
            for (int y = min.getY(); y <= max.getY() ; y++){
                for (int z = min.getZ(); z <= max.getZ() ; z++){
                    box.add(new Location(min.getWorld(), x, y, z));
                }
            }
        }
        return box;
    }

    public static ArrayList<Location> rotateLocs(@NotNull ArrayList<Location> structure, double theta, @NotNull Location center){
        ArrayList<Location> rotated = new ArrayList<>();
        for (Location loc : structure){
            rotated.add(loc.rotate(theta, center));
        }
        return rotated;
    }

    public static <E> Collection<E> filter(Collection<E> collection, Collection<E> filter){
        final HashSet<E> filtered = new HashSet<>();
        for (final E e : collection){
            if (filter.contains(e)){
                continue;
            }
            filtered.add(e);
        }
        return filtered;
    }

    @Contract(pure = true)
    public static Collection<Location> exterior(Location min, Location max, Collection<Location> invertedStructure, Collection<Location> structure){
        final ArrayList<Location> exterior = new ArrayList<>();

        /*for (int y = min.getY(); y <= max.getY() ; y++){
            for (int x = min.getX() ; x <= max.getX() ; x++){
                Location minTest = new Location(min.getWorld(), x, y, min.getZ());
                if (!invertedStructure.contains(minTest)){
                    continue;
                }
                while (invertedStructure.contains(minTest.add(0, 0, 1))){
                    exterior.add(minTest);
                    minTest = minTest.add(0, 0, 1);
                }
                Location maxTest = new Location(max.getWorld(), x, y, max.getZ());
                if (!invertedStructure.contains(maxTest)){
                    continue;
                }
                while (invertedStructure.contains(maxTest.add(0, 0, -1))){
                    if (exterior.contains(maxTest)){
                        break;
                    }
                    exterior.add(maxTest);
                    maxTest = maxTest.add(0, 0, -1);
                }
            }
        }
        for (int z = min.getZ(); z <= max.getZ() ; z++) {
            for (int x = min.getX(); x <= max.getX(); x++) {
                Location minTest = new Location(min.getWorld(), x, min.getY(), z);
                while (invertedStructure.contains(minTest.add(0, 1, 0))){
                    if (exterior.contains(minTest)){
                        break;
                    }
                    exterior.add(minTest);
                    minTest = minTest.add(0, 0, 1);
                }
            }
        }
        for (int z = min.getZ(); z <= max.getZ() ; z++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                Location minTest = new Location(min.getWorld(), min.getX(), y, z);
                if (!invertedStructure.contains(minTest)){
                    continue;
                }
                while (invertedStructure.contains(minTest.add(1, 0, 0))){
                    if (exterior.contains(minTest)){
                        break;
                    }
                    exterior.add(minTest);
                    minTest = minTest.add(0, 0, 1);
                }
                Location maxTest = new Location(min.getWorld(), max.getX(), y, z);
                if (!invertedStructure.contains(maxTest)){
                    continue;
                }
                while (invertedStructure.contains(maxTest.add(-1, 0, 0))){
                    if (exterior.contains(maxTest)){
                        break;
                    }
                    exterior.add(maxTest);
                    maxTest = maxTest.add(0, 0, -1);
                }
            }
        }*/
        return exterior;
    }

    public static Collection<Location> neighbors(Collection<Location> structure, Location node){
        final Collection<Location> ret = new HashSet<>();
        for (Vector shift : SHIFTS){
            Location test = node.add(shift);
            if (!structure.contains(test)){
                continue;
            }
            ret.add(test);
        }
        return ret;
    }


    private static final Vector[] SHIFTS = {
            new Vector(0, 1, 0),
            new Vector(1, 0, 0),
            new Vector(-1, 0, 0),
            new Vector(0, 0, 1),
            new Vector(0, 0, -1)
    };
}
