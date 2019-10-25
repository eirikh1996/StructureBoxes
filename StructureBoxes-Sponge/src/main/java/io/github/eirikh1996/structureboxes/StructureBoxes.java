package io.github.eirikh1996.structureboxes;

import io.github.eirikh1996.structureboxes.utils.Location;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Plugin(id = "structureboxes",
        name = "Structure Boxes",
        version = "1.0",
        authors = {"eirikh1996"},
        dependencies = {@Dependency(id = "worldedit"),
                @Dependency(id = "redprotect", optional = true),
                @Dependency(id = "worldguard", optional = true)})
public class StructureBoxes implements SBMain {

    private static StructureBoxes instance;

    @Inject private Logger logger;

    @Listener
    public void onServerStarting(GameStartingServerEvent event){
        instance = this;

    }

    @Listener
    public void onServerStart(GameStartedServerEvent event){

    }

    public WorldEditHandler getWorldEditHandler() {
        return null;
    }

    public Platform getPlatform() {
        return Platform.SPONGE;
    }

    public boolean isFreeSpace(UUID playerID, String schematicName, List<Location> locations) {
        for (Location loc : locations){

        }
        return false;
    }

    public void sendMessageToPlayer(UUID recipient, String message) {

    }

    public Logger getLogger() {
        return logger;
    }

    public static synchronized StructureBoxes getInstance() {
        return instance;
    }

}
