package io.github.eirikh1996.structureboxes;

import io.github.eirikh1996.structureboxes.utils.Location;

import java.util.*;

public class StructureManager implements Iterable<ArrayList<Location>> {
    private final Set<ArrayList<Location>> locationSets = new HashSet<>();
    private StructureManager() {}

    public boolean isPartOfStructure(Location location){
        for (ArrayList<Location> locationSet : locationSets){
            if (!locationSet.contains(location)){
                continue;
            }
            return true;
        }
        return false;
    }

    public boolean addStructure(ArrayList<Location> structure){
        return locationSets.add(structure);
    }

    @Override
    public Iterator<ArrayList<Location>> iterator() {
        return Collections.unmodifiableSet(locationSets).iterator();
    }

    public static synchronized StructureManager getInstance(){
        return StructureManagerHolder.instance;
    }

    private static class StructureManagerHolder{
        static StructureManager instance = new StructureManager();
    }
}
