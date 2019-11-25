package io.github.eirikh1996.structureboxes;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import com.google.inject.Inject;
import com.sk89q.worldedit.sponge.SpongeWorldEdit;
import io.github.eirikh1996.structureboxes.compat.we7.IWorldEditHandler;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.eirikh1996.structureboxes.utils.MathUtils;
import me.ryanhamshire.griefprevention.GriefPrevention;
import org.bstats.sponge.Metrics;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.world.World;

import java.nio.file.Path;
import java.util.Collection;
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
                @Dependency(id = "griefprevention", optional = true),
                @Dependency(id = "plotsquared", optional = true)})
public class StructureBoxes implements SBMain {

    private static StructureBoxes instance;

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path configDir;

    public Path getConfigDir() {
        return configDir;
    }

    @Inject private SpongeWorldEdit worldEditPlugin;
    WorldEditHandler worldEditHandler;
    @Inject(optional = true) private RedProtect redProtectPlugin;
    @Inject(optional = true) private GriefPrevention griefPreventionPlugin;
    //private SessionTask sessionTask;
    private boolean plotSquaredInstalled = false;
    private Metrics metrics;

    @Listener
    public void onServerStarting(GameStartingServerEvent event){
        instance = this;

    }

    @Listener
    public void onServerStart(GameStartedServerEvent event){
        if (redProtectPlugin != null){

        }
        worldEditPlugin.getWorkingDir();
        worldEditHandler = new IWorldEditHandler(worldEditPlugin.getWorkingDir(), this);
    }

    public WorldEditHandler getWorldEditHandler() {
        return null;
    }

    @Override
    public boolean structureWithinRegion(UUID playerID, String schematicID, Collection<Location> locations) {
        return false;
    }

    public Platform getPlatform() {
        return Platform.SPONGE;
    }

    public boolean isFreeSpace(UUID playerID, String schematicName, Collection<Location> locations) {
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

    public void clearInterior(Collection<Location> interior) {
        for (Location loc : interior){

        }
    }

    @Override
    public void addItemToPlayerInventory(UUID id, Object item) {

    }

    @Override
    public void scheduleSyncTask(Runnable runnable) {

    }

    @Override
    public void scheduleAsyncTask(Runnable runnable) {

    }

    @Override
    public void broadcast(String s) {

    }

    public static synchronized StructureBoxes getInstance() {
        return instance;
    }

}
