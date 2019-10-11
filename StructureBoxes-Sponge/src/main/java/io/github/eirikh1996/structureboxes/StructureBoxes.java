package io.github.eirikh1996.structureboxes;

import io.github.eirikh1996.structureboxes.utils.Location;
import org.bukkit.World;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.logging.Logger;

@Plugin(id = "structureboxes",
        name = "Structure Boxes",
        version = "1.0",
        authors = {"eirikh1996"},
        dependencies = {@Dependency(id = "worldedit")})
public class StructureBoxes implements SBMain {

    private static StructureBoxes instance;

    @Inject
    private Logger logger;

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

    public boolean isFreeSpace(ArrayList<Location> locations) {
        for (Location loc : locations){

        }
        return false;
    }

    public static synchronized StructureBoxes getInstance() {
        return instance;
    }

}
