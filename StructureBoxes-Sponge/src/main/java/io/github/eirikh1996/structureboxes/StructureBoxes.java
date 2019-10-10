package io.github.eirikh1996.structureboxes;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;

import javax.inject.Inject;
import java.util.logging.Logger;

@Plugin(id = "structureboxes", name = "Structure Boxes", version = "1.0")
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

    public static synchronized StructureBoxes getInstance() {
        return instance;
    }

}
