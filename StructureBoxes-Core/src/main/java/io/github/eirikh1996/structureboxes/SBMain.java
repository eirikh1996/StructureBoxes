package io.github.eirikh1996.structureboxes;

import io.github.eirikh1996.structureboxes.utils.Location;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public interface SBMain {
    WorldEditHandler getWorldEditHandler();
    boolean structureWithinRegion(UUID playerID, String schematicID, Collection<Location> locations);
    Platform getPlatform();
    void clearStructure(Structure structure);
    boolean isFreeSpace(UUID playerID, String schematicName, Collection<Location> locations);
    void sendMessageToPlayer(UUID recipient, String message);
    void logMessage(Level level, String message);
    void clearInterior(Collection<Location> interior);

    default void removeItems(UUID world, Structure structure) {

    }

    default void placeSupportBlocks(Map<Location, Object> supportBlocks) {

    }

    void scheduleSyncTask(final Runnable runnable);
    void scheduleSyncTaskLater(final Runnable runnable, long delay);
    void scheduleAsyncTask(final Runnable runnable);
    void broadcast(String s);
}
