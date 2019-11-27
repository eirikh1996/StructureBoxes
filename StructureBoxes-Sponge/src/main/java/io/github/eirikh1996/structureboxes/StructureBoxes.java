package io.github.eirikh1996.structureboxes;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.sk89q.worldedit.sponge.SpongeWorldEdit;
import io.github.eirikh1996.structureboxes.compat.we6.IWorldEditHandler;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.eirikh1996.structureboxes.utils.MathUtils;
import me.ryanhamshire.griefprevention.GriefPrevention;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.bstats.sponge.Metrics2;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
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

    @Inject private Logger logger;
    @Inject @DefaultConfig(sharedRoot = false) private Path configDir;
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    public Path getConfigDir() {
        return configDir;
    }

    @Inject private SpongeWorldEdit worldEditPlugin;
    @Inject private PluginManager pluginManager;
    @Inject private PluginContainer plugin;
    private WorldEditHandler worldEditHandler;
    private Optional<RedProtect> redProtectPlugin;
    private Optional<GriefPrevention> griefPreventionPlugin;

    private Task.Builder taskBuilder = Task.builder();
    private boolean plotSquaredInstalled = false;
    @Inject private Metrics2 metrics;

    @Listener
    public void onGameLoaded(GameLoadCompleteEvent event) throws IOException {
        instance = this;


        final String[] LOCALES = {"en", "no", "it"};
        for (String locale : LOCALES){
            plugin.getAsset("localisation/lang_" + locale + ".properties").get().copyToDirectory(configDir, false, true);
        }
        plugin.getAsset("structureboxes.conf").get().copyToFile(configDir, false, true);
        loader = HoconConfigurationLoader.builder().setPath(getConfigDir()).build();

        final ConfigurationNode node = loader.load();
        Settings.locale = node.getNode("Locale").getString("en");
        Settings.Metrics = node.getNode("Metrics").getBoolean(false);
        //Read free space
        final ConfigurationNode freeSpaceNode = node.getNode("Free space");
        Settings.CheckFreeSpace = freeSpaceNode.getNode("Enabled").getBoolean(true);
        try {
            Settings.blocksToIgnore.addAll(freeSpaceNode.getNode("Blocks to ignore").getList(TypeToken.of(BlockType.class), Collections.emptyList()));
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }

        //Read restrict to regions section
        final ConfigurationNode restrictToRegionsNode = node.getNode("Restrict to regions");
        Settings.RestrictToRegionsEnabled = restrictToRegionsNode.getNode("Enabled").getBoolean(false);
        Settings.RestrictToRegionsEntireStructure = restrictToRegionsNode.getNode("Entire structure").getBoolean(false);
        try {
            Settings.RestrictToRegionsExceptions = restrictToRegionsNode.getNode("Exceptions").getList(TypeToken.of(String.class), Collections.emptyList());
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onServerAboutToStart(GameAboutToStartServerEvent event) {
        I18nSupport.initialize(getConfigDir().toFile());
    }


    @Listener
    public void onServerStarting(GameStartingServerEvent event) {


        Optional<PluginContainer> redprotect = pluginManager.getPlugin("redprotect");
        if (redprotect.isPresent() && redprotect.get().getInstance().isPresent()){
            logger.info("RedProtect found");
            redProtectPlugin = (Optional<RedProtect>) redprotect.get().getInstance();
        }
        Optional<PluginContainer> griefprevention = pluginManager.getPlugin("griefprevention");
        if (griefprevention.isPresent() && griefprevention.get().getInstance().isPresent())
            griefPreventionPlugin = (Optional<GriefPrevention>) griefprevention.get().getInstance();

    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) throws IOException {
        final ConfigurationLoader<CommentedConfigurationNode> weLoader = HoconConfigurationLoader.builder().setPath(worldEditPlugin.getWorkingDir().toPath()).build();
        ConfigurationNode weNode = weLoader.load();
        File schemDir = new File(worldEditPlugin.getWorkingDir(), weNode.getNode("saving").getNode("dir").getString());
        worldEditHandler = new IWorldEditHandler(schemDir, this);

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
        taskBuilder.execute(runnable);
    }

    @Override
    public void scheduleAsyncTask(Runnable runnable) {
        taskBuilder.async().execute(runnable);
    }

    @Override
    public void broadcast(String s) {

    }

    public static synchronized StructureBoxes getInstance() {
        return instance;
    }

}
