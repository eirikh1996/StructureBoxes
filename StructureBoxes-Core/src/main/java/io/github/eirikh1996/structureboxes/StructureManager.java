package io.github.eirikh1996.structureboxes;

import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.Location;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StructureManager implements Iterable<Structure>, Runnable {
    private final Set<Structure> structures = new HashSet<>();
    private Map<UUID, Collection<Location>> structurePlayerMap = new HashMap<>();
    private final Map<UUID,  LinkedList<AbstractMap.SimpleImmutableEntry<Long,AbstractMap.SimpleImmutableEntry<String, HashMap<Location, Object>>>>> playerTimeStructureMap = new HashMap<>();
    private SBMain sbMain;
    private StructureManager() {}

    public boolean isPartOfStructure(Location location){
        for (Structure structure : structures) {
            if (!structure.getStructure().contains(location)) {
                continue;
            }
            return true;
        }
        return false;
    }

    public Structure getCorrespondingStructure(Collection<Location> locations) {
        for (Structure structure : structures) {
            if (!structure.getStructure().containsAll(locations)) {
                continue;
            }
            return structure;
        }
        return null;
    }
    private void processRemovalOfSavedStructures(){
        if (structures.isEmpty()){
            return;
        }
        final Iterator<Structure> iter = structures.iterator();
        while (iter.hasNext()) {
            final Structure structure = iter.next();
            long expiry = structure.getExpiry() > -1 ? structure.getExpiry() : Settings.MaxSessionTime;
            if (structure.getPlacementTime() <= -1 || (System.currentTimeMillis() - structure.getPlacementTime()) / 1000 < expiry) {
                continue;
            }
            if (structure.getExpiry() > -1) {
                sbMain.clearStructure(structure);
            }
            iter.remove();
        }

    }

    public Structure getLatestStructure(UUID playerID){
        Structure ret = null;
        for (Structure structure : structures) {
            if (!structure.getOwner().equals(playerID)) {
                continue;
            }
            if (ret != null && ret.getPlacementTime() > structure.getPlacementTime()) {
                continue;
            }
            ret = structure;
        }
        return ret;
    }
    public void addStructureByPlayer(UUID id, String schematicName, HashMap<Location, Object> structure){
        structures.add(new Structure(schematicName, structure, id));

    }

    public Set<Structure> getSessions(UUID playerID) {
        Set<Structure> playerStructures = new HashSet<>();
        for (Structure structure : structures) {
            if (!structure.getOwner().equals(playerID)) {
                continue;
            }
            playerStructures.add(structure);
        }
        return playerStructures;
    }


    public Set<Structure> getStructures() {
        return structures;
    }

    public Structure getStructureAt(Location loc) {
        for (Structure structure : structures) {
            if (!structure.getStructure().contains(loc)) {
                continue;
            }
            return structure;
        }
        return null;
    }

    public void addStructure(Structure structure){
        structures.add(structure);
    }

    public void removeStructure(Structure structure){
        structures.remove(structure);
    }

    @NotNull
    @Override
    public Iterator<Structure> iterator() {
        return Collections.unmodifiableCollection(structures).iterator();
    }

    public static synchronized StructureManager getInstance(){
        return StructureManagerHolder.instance;
    }

    public Collection<Location> getStructureByPlayer(UUID id){
        return structurePlayerMap.get(id);
    }

    public void addStructureByPlayer(UUID id, Collection<Location> structure){
        structurePlayerMap.put(id, structure);
    }

    @Override
    public void run() {
        processRemovalOfSavedStructures();
    }

    public SBMain getSbMain() {
        return sbMain;
    }

    public void setSbMain(SBMain sbMain) {
        this.sbMain = sbMain;
    }

    private static class StructureManagerHolder{
        static StructureManager instance = new StructureManager();
    }
}
