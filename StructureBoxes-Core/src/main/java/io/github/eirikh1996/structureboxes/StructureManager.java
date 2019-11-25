package io.github.eirikh1996.structureboxes;

import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.Location;

import java.util.*;

import static java.lang.System.currentTimeMillis;

public class StructureManager implements Iterable<Collection<Location>>, Runnable {
    private final Set<Collection<Location>> locationSets = new HashSet<>();
    private Map<UUID, Collection<Location>> structurePlayerMap = new HashMap<>();
    private final Map<UUID,  LinkedList<AbstractMap.SimpleImmutableEntry<Long,AbstractMap.SimpleImmutableEntry<String, HashMap<Location, Object>>>>> playerTimeStructureMap = new HashMap<>();
    private StructureManager() {}

    public boolean isPartOfStructure(Location location){
        for (Collection<Location> locationSet : locationSets){
            if (!locationSet.contains(location)){
                continue;
            }
            return true;
        }
        return false;
    }
    private void processRemovalOfSavedStructures(){
        if (playerTimeStructureMap.isEmpty()){
            return;
        }
        for (UUID id : playerTimeStructureMap.keySet()){
            LinkedList<AbstractMap.SimpleImmutableEntry<Long, AbstractMap.SimpleImmutableEntry<String, HashMap<Location, Object>>>> pairLinkedList = playerTimeStructureMap.get(id);
            if (pairLinkedList == null || pairLinkedList.isEmpty()){
                return;
            }
            long timeStamp = pairLinkedList.getLast().getKey();
            if (currentTimeMillis() - timeStamp > Settings.MaxSessionTime * 1000){
                pairLinkedList.pollLast();
            }
        }

    }

    public AbstractMap.SimpleImmutableEntry<String, HashMap<Location, Object>> getLatestStructure(UUID playerID){
        LinkedList<AbstractMap.SimpleImmutableEntry<Long, AbstractMap.SimpleImmutableEntry<String, HashMap<Location, Object>>>> pairLinkedList = playerTimeStructureMap.get(playerID);
        if (pairLinkedList == null || pairLinkedList.isEmpty()){
            return null;
        }
        AbstractMap.SimpleImmutableEntry<Long, AbstractMap.SimpleImmutableEntry<String, HashMap<Location, Object>>> pair = pairLinkedList.pollFirst();
        return pair != null ? pair.getValue() : null;

    }
    public void addStructureByPlayer(UUID id, String schematicName, HashMap<Location, Object> structure){

        AbstractMap.SimpleImmutableEntry<Long, AbstractMap.SimpleImmutableEntry<String, HashMap<Location, Object>>> timePair = new AbstractMap.SimpleImmutableEntry<>(currentTimeMillis(), new AbstractMap.SimpleImmutableEntry<>(schematicName, structure));
        if (playerTimeStructureMap.containsKey(id)){
            playerTimeStructureMap.get(id).addFirst(timePair);
        } else {
            LinkedList<AbstractMap.SimpleImmutableEntry<Long, AbstractMap.SimpleImmutableEntry<String, HashMap<Location, Object>>>> pairLinkedList = new LinkedList<>();
            pairLinkedList.addFirst(timePair);
            playerTimeStructureMap.put(id, pairLinkedList);
        }

    }

    public void addStructure(Collection<Location> structure){
        locationSets.add(structure);
    }

    public void removeStructure(Collection<Location> structure){
        locationSets.remove(structure);
    }

    @Override
    public Iterator<Collection<Location>> iterator() {
        return Collections.unmodifiableCollection(locationSets).iterator();
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

    private static class StructureManagerHolder{
        static StructureManager instance = new StructureManager();
    }
}
