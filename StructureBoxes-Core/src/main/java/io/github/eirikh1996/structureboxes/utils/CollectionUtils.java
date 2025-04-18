package io.github.eirikh1996.structureboxes.utils;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.sk89q.worldedit.util.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class CollectionUtils {
    public static Collection<Location> boundingBox(@NotNull final Location min, @NotNull final Location max){
        Collection<Location> box = new HashSet<>();
        for (int x = min.getBlockX(); x <= max.getBlockX() ; x++){
            for (int y = min.getBlockY(); y <= max.getBlockY() ; y++){
                for (int z = min.getBlockZ(); z <= max.getBlockZ() ; z++){
                    box.add(new Location(min.getExtent(), x, y, z));
                }
            }
        }
        return box;
    }

    public static ArrayList<Location> rotateLocs(@NotNull ArrayList<Location> structure, double theta, @NotNull Location center){
        ArrayList<Location> rotated = new ArrayList<>();
        for (Location loc : structure){
            rotated.add(rotate(loc, theta, center));
        }
        return rotated;
    }

    private static Location rotate(final Location toRotate, final double theta, final Location centre){
        final int xRot = (int) (centre.getX() + cos(theta) * (toRotate.getBlockX() - centre.getX()) - sin(theta) * (toRotate.getBlockZ() - centre.getZ()));
        final int zRot = (int) (centre.getZ() + sin(theta) * (toRotate.getBlockX() - centre.getX()) + cos(theta) * (toRotate.getBlockZ() - centre.getZ()));
        return new Location(centre.getExtent(), xRot, centre.getBlockY(), zRot);
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
        for (BlockVector3 shift : SHIFTS){
            Location test = new Location(node.getExtent(), node.toVector().toBlockPoint().add(shift).toVector3());
            if (!structure.contains(test)){
                continue;
            }
            ret.add(test);
        }
        return ret;
    }


    private static final BlockVector3[] SHIFTS = {
            new BlockVector3(0, 1, 0),
            new BlockVector3(1, 0, 0),
            new BlockVector3(-1, 0, 0),
            new BlockVector3(0, 0, 1),
            new BlockVector3(0, 0, -1)
    };
}
