package io.github.eirikh1996.structureboxes.utils;

import java.util.Collection;
import java.util.HashSet;

public class CollectionUtils {
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
