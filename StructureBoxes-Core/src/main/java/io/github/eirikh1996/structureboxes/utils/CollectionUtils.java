package io.github.eirikh1996.structureboxes.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class CollectionUtils {
    public static ArrayList<Location> boundingBox(@NotNull final Location min, @NotNull final Location max){
        ArrayList<Location> box = new ArrayList<>();
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

    public static ArrayList<Location> filter(ArrayList<Location> locations, ArrayList<Location> filter){
        final ArrayList<Location> filtered = new ArrayList<>();
        for (final Location loc : locations){
            if (filter.contains(loc)){
                continue;
            }
            filtered.add(loc);
        }
        return filtered;
    }

    @Contract(pure = true)
    public static synchronized ArrayList<Location> exterior(ArrayList<Location> structure){
        final ArrayList<Location> exterior = new ArrayList<>();
        final Location min = Location.min(structure);
        final Location max = Location.max(structure);
        final ArrayList<Location> invertedStructure = filter(boundingBox(min, max), structure);
        for (int y = min.getY(); y <= max.getY() ; y++){
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
        }
        return exterior;
    }

    public static synchronized ArrayList<Location> invert(ArrayList<Location> structure){
        final Location min = Location.min(structure);
        final Location max = Location.max(structure);
        return filter(boundingBox(min, max), structure);
    }
}
