package io.github.eirikh1996.structureboxes;

import com.sk89q.worldedit.util.Location;
import io.github.eirikh1996.structureboxes.processing.RegionPredicate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Common interface for the main classes of all editions of Structure Boxes
 */
public interface SBMain {
    WorldEditHandler getWorldEditHandler();
    @Deprecated boolean structureWithinRegion(UUID playerID, String schematicID, Collection<Location> locations);
    @Deprecated Platform getPlatform();
    @Deprecated void clearStructure(Structure structure);
    @Deprecated boolean isFreeSpace(UUID playerID, String schematicName, Collection<Location> locations);
    void sendMessageToPlayer(UUID recipient, String message);
    void logMessage(Level level, String message);
    void clearInterior(Collection<Location> interior);

    default void removeItems(UUID world, Structure structure) {

    }

    default void placeSupportBlocks(Map<Location, Object> supportBlocks) {

    }

    void clearInterior(Collection<? extends Location> interior);

    void scheduleSyncTask(final Runnable runnable);
    void scheduleSyncTaskLater(final Runnable runnable, long delay);
    void scheduleAsyncTask(final Runnable runnable);
    void broadcast(String s);
}
