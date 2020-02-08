package io.github.eirikh1996.structureboxes;

import io.github.eirikh1996.structureboxes.utils.Location;
import net.fabricmc.api.ModInitializer;

import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

public class StructureBoxes implements ModInitializer, SBMain {
    public void onInitialize() {

    }

    public WorldEditHandler getWorldEditHandler() {
        return null;
    }

    public boolean structureWithinRegion(UUID playerID, String schematicID, Collection<Location> locations) {
        return false;
    }

    public Platform getPlatform() {
        return Platform.FABRIC;
    }

    public boolean isFreeSpace(UUID playerID, String schematicName, Collection<Location> locations) {
        return false;
    }

    public void sendMessageToPlayer(UUID recipient, String message) {

    }

    public Logger getLogger() {
        return null;
    }

    public void clearInterior(Collection<Location> interior) {

    }

    public void scheduleSyncTask(Runnable runnable) {

    }

    public void scheduleAsyncTask(Runnable runnable) {

    }

    public void broadcast(String s) {

    }
}
