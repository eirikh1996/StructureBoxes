package io.github.eirikh1996.structureboxes;

import com.sk89q.worldedit.sponge.SpongeWorldEdit;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.eirikh1996.structureboxes.utils.MathUtils;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.bstats.sponge.Metrics;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.world.World;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Plugin(id = "structureboxes",
        name = "StructureBoxes",
        version = "1.0",
        authors = {"eirikh1996"},
        dependencies = {
                @Dependency(id = "worldedit"),
                @Dependency(id = "redprotect", optional = true),
                @Dependency(id = "worldguard", optional = true),
                @Dependency(id = "griefprevention", optional = true)})
public class StructureBoxes implements SBMain {

    private static StructureBoxes instance;

    @Inject private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private @NonNull YAMLConfigurationLoader configManager = YAMLConfigurationLoader.builder().setPath(defaultConfig).build();

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path privateConfigDir;

    @Inject
    private SpongeWorldEdit worldEditPlugin;

    @Inject
    private WorldEditHandler worldEditHandler;
    //private RedProtect redProtectPlugin;
    //private GriefPrevention griefPreventionPlugin;
    //private SessionTask sessionTask;
    private boolean plotSquaredInstalled = false;
    private Metrics metrics;

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
        Player p = Sponge.getServer().getPlayer(playerID).get();
        for (Location loc : locations){
            org.spongepowered.api.world.Location<World> spongeLoc = MathUtils.sbToSpongeLoc(loc);
            if (spongeLoc.getBlockType().equals(BlockTypes.AIR) || Settings.blocksToIgnore.contains(spongeLoc.getBlockType())){
                continue;
            }
            return false;
        }
        return true;
    }

    public void sendMessageToPlayer(UUID recipient, String message) {

    }

    public Logger getLogger() {
        return logger;
    }

    public void clearInterior(ArrayList<Location> interior) {
        for (Location loc : interior){

        }
    }

    public static synchronized StructureBoxes getInstance() {
        return instance;
    }

}
