package io.github.eirikh1996.structureboxes.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemManager {
    private final Map<UUID, Object> playerItemMap = new HashMap<>();
    private static ItemManager instance;

    private ItemManager(){}

    public void addItem(UUID id, Object item){
        playerItemMap.put(id, item);
    }

    public Object getItem(UUID id){
        return playerItemMap.remove(id);
    }

    public static synchronized ItemManager getInstance(){
        if (instance == null){
            instance = new ItemManager();
        }
        return instance;
    }
}
