package io.github.eirikh1996.structureboxes;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.sk89q.worldedit.sponge.SpongeWorldEdit;
import io.github.eirikh1996.structureboxes.command.StructureBoxCommand;
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
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.ConfigManager;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    @Inject @DefaultConfig(sharedRoot = false) private Path defaultConfig;
    @Inject @ConfigDir(sharedRoot = false) private Path configDir;
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    public Path getConfigDir() {
        return configDir;
    }

    @Inject private SpongeWorldEdit worldEditPlugin;
    @Inject private PluginManager pluginManager;
    @Inject private PluginContainer plugin;
    @Inject private ConfigManager configManager;
    private WorldEditHandler worldEditHandler;
    private Optional<RedProtect> redProtectPlugin;
    private Optional<GriefPrevention> griefPreventionPlugin;

    private Task.Builder taskBuilder = Task.builder();
    private boolean plotSquaredInstalled = false;
    @Inject private Metrics2 metrics;

    @Listener
    public void onGameLoaded(GameLoadCompleteEvent event) {
        instance = this;


        final String[] LOCALES = {"en", "no", "it"};
        for (String locale : LOCALES){
            if (new File(configDir.toString() + "/localisation/lang_" + locale + ".properties").exists()){
                continue;
            }
            try {
                final Optional<Asset> asset = plugin.getAsset("localisation/lang_" + locale + ".properties");
                logger.info("Locale " + locale + " present: " + asset.isPresent());
                asset.get().copyToDirectory(Paths.get(configDir.toString(), "localisation"), false, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            plugin.getAsset("structureboxes.conf").get().copyToFile(defaultConfig, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loader = HoconConfigurationLoader.builder().setPath(defaultConfig).build();


        try {
            final ConfigurationNode node = loader.load();
            //Read general config
            Settings.locale = node.getNode("Locale").getString("en");
            Settings.Metrics = node.getNode("Metrics").getBoolean(false);
            Settings.StructureBoxItem = node.getNode("Structure Box Item").getValue(TypeToken.of(BlockType.class), BlockTypes.CHEST);
            Settings.StructureBoxLore = node.getNode("Structure Box Display Name").getString("§6Structure Box");
            Settings.MaxStructureSize = node.getNode("Max Structure Size").getInt(10000);
            Settings.MaxSessionTime = node.getNode("Max Session Time").getInt(300);
            Settings.PlaceCooldownTime = node.getNode("Place Cooldown Time").getInt(30);
            Settings.StructureBoxPrefix = node.getNode("Structure Box Prefix").getString("§6Structure Box: ");
            Settings.AlternativePrefixes.addAll(node.getList(TypeToken.of(String.class), Collections.emptyList()));
            Settings.StructureBoxInstruction.addAll(node.getList(TypeToken.of(String.class), Collections.emptyList()));
            Settings.RequirePermissionPerStructureBox = node.getNode("Require permission per structure box").getBoolean(false);

            //Read restrict to regions section
            final ConfigurationNode restrictToRegionsNode = node.getNode("Restrict to regions");
            Settings.RestrictToRegionsEnabled = restrictToRegionsNode.getNode("Enabled").getBoolean(false);
            Settings.RestrictToRegionsEntireStructure = restrictToRegionsNode.getNode("Entire structure").getBoolean(false);
            Settings.RestrictToRegionsExceptions.addAll(restrictToRegionsNode.getNode("Exceptions").getList(TypeToken.of(String.class), Collections.emptyList()));

            //Read free space
            final ConfigurationNode freeSpaceNode = node.getNode("Free space");
            Settings.CheckFreeSpace = freeSpaceNode.getNode("Enabled").getBoolean(true);
            Settings.blocksToIgnore.addAll(freeSpaceNode.getNode("Blocks to ignore").getList(TypeToken.of(BlockType.class), Collections.emptyList()));

        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }

        CommandSpec structureBoxCommand = CommandSpec.builder()
                .executor(new StructureBoxCommand())
                .arguments(GenericArguments.firstParsing(GenericArguments.string(Text.of("create"))),
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("undo"))),
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("reload"))))
                .build();
        Sponge.getCommandManager().register(plugin, structureBoxCommand, "structurebox", "sbox", "sb");


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
    public void onServerStart(GameStartedServerEvent event)  {
        logger.info("Structure Boxes config path");
        logger.info(configDir.getParent().toString());
        logger.info(configDir.toString());

        /*final ConfigurationLoader<CommentedConfigurationNode> weLoader = configManager.getPluginConfig(worldEditPlugin).getConfig();
        ConfigurationNode weNode = weLoader.load();

        File schemDir = new File(worldEditPlugin.getWorkingDir(), weNode.getNode("saving").getNode("dir").getString());
        worldEditHandler = new IWorldEditHandler(schemDir, this);*/

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

    public SpongeWorldEdit getWorldEditPlugin() {
        return worldEditPlugin;
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

    private void readConfig() throws Exception {

    }

}
