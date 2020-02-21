package io.github.eirikh1996.structureboxes;

import io.github.eirikh1996.structureboxes.utils.Location;

import java.util.Collection;
import java.util.UUID;

public interface SBMain {
    WorldEditHandler getWorldEditHandler();
    boolean structureWithinRegion(UUID playerID, String schematicID, Collection<Location> locations);
    Platform getPlatform();
    boolean isFreeSpace(UUID playerID, String schematicName, Collection<Location> locations);
    void sendMessageToPlayer(UUID recipient, String message);
    <T> T getLogger();
    void clearInterior(Collection<Location> interior);
    void scheduleSyncTask(final Runnable runnable);
    void scheduleAsyncTask(final Runnable runnable);
    void broadcast(String s);
}
