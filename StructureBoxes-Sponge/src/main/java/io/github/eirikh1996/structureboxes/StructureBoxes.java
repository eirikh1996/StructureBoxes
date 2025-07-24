/*
    This file is part of Structure Boxes.

    Structure Boxes is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Structure Boxes is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Structure Boxes.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.eirikh1996.structureboxes;

import com.google.inject.Inject;


import com.sk89q.worldedit.sponge.SpongeAdapter;
import com.sk89q.worldedit.sponge.SpongeWorldEdit;
import com.sk89q.worldedit.util.Location;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;

import io.github.eirikh1996.structureboxes.utils.serializers.BlockTypeSerializer;
import io.leangen.geantyref.TypeToken;

import io.github.eirikh1996.structureboxes.command.*;
import io.github.eirikh1996.structureboxes.listener.BlockListener;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.RegionUtils;
import io.github.eirikh1996.structureboxes.utils.UpdateManager;

import io.github.pulverizer.movecraft.Movecraft;

import net.kyori.adventure.text.Component;

import org.apache.logging.log4j.Logger;

import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bstats.sponge.Metrics;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.ConfigManager;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.BlockChangeFlags;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;


@Plugin(value = "structureboxes")
public class StructureBoxes implements SBMain {

    private static StructureBoxes instance;

    @Inject private Logger logger;
    @Inject private Game game;
    @Inject @DefaultConfig(sharedRoot = false) private Path defaultConfig;
    @Inject @ConfigDir(sharedRoot = false) private Path configDir;

    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private String schematicDir;
    private Path weDir;

    public Path getConfigDir() {
        return configDir;
    }

    @NotNull private SpongeWorldEdit worldEditPlugin;
    @Inject private PluginManager pluginManager;
    @Inject private PluginContainer plugin;
    @Inject private ConfigManager configManager;
    private WorldEditHandler worldEditHandler;
    @NotNull private Optional<Movecraft> movecraftPlugin = Optional.empty();
    @NotNull private Optional<EagleFactionsPlugin> eagleFactionsPlugin = Optional.empty();

    private boolean plotSquaredInstalled = false;
    private Metrics metrics;
    @Inject private Metrics.Factory metricsFactory;

    private SystemSubject console;
    @Inject private PluginContainer container;


    @Listener
    public void onGameLoaded(LoadedGameEvent event) {
        instance = this;
        metrics = metricsFactory.make(6173);

        try {
            loadConfig();
            readConfig();
            loadLocales();
        } catch (IOException e) {
            e.printStackTrace();
        }
        I18nSupport.initialize(getConfigDir().toFile(), this);
        console = Sponge.systemSubject();

    }



    @SuppressWarnings("unchecked")
    @Listener
    public void onServerStarting(StartingEngineEvent<Server> event) {

        worldEditPlugin = (SpongeWorldEdit) pluginManager.plugin("worldedit").get().instance();
        boolean regionProviderFound = false;
        //Check for EagleFactions
        final Optional<PluginContainer> eagleFactions = pluginManager.plugin("eaglefactions");
        if (eagleFactions.isPresent() && eagleFactions.get().instance() instanceof EagleFactionsPlugin) {
            console.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Startup - EagleFactions detected")));
            regionProviderFound = true;
            eagleFactionsPlugin = (Optional<EagleFactionsPlugin>) eagleFactions.get().instance();
        }
        if ((Settings.RestrictToRegionsEnabled || Settings.RestrictToRegionsEntireStructure) && !regionProviderFound) {
            console.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Startup - Restrict to regions no compatible protection plugin")));
            Settings.RestrictToRegionsEnabled = false;
            Settings.RestrictToRegionsEntireStructure = false;
            console.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Startup - Restrict to regions set to false")));

        }
        //Now read WorldEdit config
        weDir = Paths.get(configDir.getParent().toString(), "worldedit");
        final Path weConfig = Paths.get(weDir.toString(), "worldedit.conf");
        final ConfigurationLoader<CommentedConfigurationNode> weLoader = HoconConfigurationLoader.builder().path(weConfig).build();
        try {
            schematicDir = weLoader.load().node("saving").node("dir").getString();
            worldEditHandler = new WorldEditHandler(new File(weDir.toFile(), schematicDir), this);
        } catch (IOException e) {
            logger.error(I18nSupport.getInternationalisedString("Startup - Error reading WE config"));
            e.printStackTrace();
        }

        Sponge.eventManager().registerListeners(container, new BlockListener());
        if (Sponge.metricsConfigManager().collectionState(container) != Tristate.TRUE && !Settings.Metrics) {
            return;
        }
        final boolean noRegionProvider = !regionProviderFound;
        metrics.addCustomChart(new AdvancedPie("region_providers", () -> {
            Map<String, Integer> valueMap = new HashMap<>();
            if (getEagleFactionsPlugin().isPresent()) {
                valueMap.put("EagleFactions", 1);
            }
            if (plotSquaredInstalled) {
                valueMap.put("PlotSquared", 1);
            }
            if (noRegionProvider) {
                valueMap.put("None", 1);
            }
            return valueMap;
        }));
        metrics.addCustomChart(new SimplePie("localisation", () -> Settings.locale));


        //Register listener




    }

    @Listener
    public void onRegisterCommand(RegisterCommandEvent<Command.Parameterized> event) {
        //Register commands
        //Create command
        Command.Parameterized createCommand = Command.builder()
                .permission("structureboxes.create")
                .addParameter(Parameter.string().key("schematic").completer((context, input) -> {
                    final List<CommandCompletion> completions = new ArrayList<>();
                    if (!(context.cause().root() instanceof ServerPlayer)) {
                        return completions;
                    }
                    final String[] files = instance.getWorldEditHandler().getSchemDir().list(((dir, name) -> name.endsWith(".schematic") || name.endsWith(".schem")));
                    if (files == null)
                        return completions;
                    for (String file : files) {
                        if (!input.isEmpty() && !file.startsWith(input))
                            continue;
                        completions.add(CommandCompletion.of(file.replace(".schematic", "").replace(".schem", "")));
                    }
                    return completions;
                }).build())
                .executor(new StructureBoxCreateCommand())
                .build();

        //undo command
        Command.Parameterized undoCommand = Command.builder()
                .executor(new StructureBoxUndoCommand())
                .permission("structureboxes.undo")
                .build();


        //reload command
        Command.Parameterized reloadCommand = Command.builder()
                .executor(new StructureBoxReloadCommand())
                .permission("structureboxes.reload")
                .build();

        //sessions command
        Command.Parameterized sessionsCommand = Command.builder()
                .addParameter(Parameter.integerNumber().key(Parameter.key("page", Integer.class)).build())
                .addParameter(Parameter.string().key(Parameter.key("player|-a", String.class))
                        .requiredPermission("structurebox.sessions.others")
                        .completer((context, input) -> {
                            final List<String> args = new ArrayList<>();
                            Sponge.server().onlinePlayers().forEach((p) -> args.add(p.name()));
                            args.add("-a");
                            final List<CommandCompletion> completions = new ArrayList<>();
                            args.forEach((arg) -> {
                                if (arg.toLowerCase().startsWith(input.toLowerCase())) {
                                    completions.add(CommandCompletion.of(arg));
                                }
                            });
                            return completions;
                        }).build())
                .executor(new StructureBoxSessionsCommand())
                .build();

        Command.Parameterized structureBoxCommand = Command.builder()
                .executor(new StructureBoxCommand(container))
                .addChild(createCommand, "create", "cr", "c")
                .addChild(undoCommand, "undo", "u" , "ud")
                .addChild(reloadCommand, "reload", "r", "rl")
                .addChild(sessionsCommand, "sessions", "s")
                .build();

        event.register(plugin, structureBoxCommand, "structurebox", "sbox", "sb");
    }

    @Listener
    public void onServerStarted(StartedEngineEvent<Server> event) {
        Sponge.asyncScheduler().executor(container).scheduleAtFixedRate(UpdateManager.getInstance(), 0, 1, TimeUnit.HOURS);
    }

    public WorldEditHandler getWorldEditHandler() {
        return worldEditHandler;
    }

    @Override
    public boolean structureWithinRegion(UUID playerID, String schematicID, Collection<Location> locations) {
        final ServerPlayer p = Sponge.server().player(playerID).get();
        if (!Settings.RestrictToRegionsEntireStructure || p.hasPermission("structureboxes.bypassregionrestriction")) {
            return true;
        }
        for (Location location : locations) {
            if (RegionUtils.isWithinRegion(SpongeAdapter.adapt(location))) {
                continue;
            }
            p.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Place - Structure must be in region")));
            return false;
        }
        return true;
    }

    public Platform getPlatform() {
        return Platform.SPONGE;
    }

    @Override
    public void clearStructure(Structure structure) {
        final Task.Builder taskBuilder = Task
                .builder()
                .execute(
                        new StructureBoxUndoCommand.StructureUndoTask(
                                structure.getLocationsToRemove(),
                                structure.getOriginalBlocks())
                );
        if (Settings.IncrementalPlacement) {
            taskBuilder.delay(Ticks.of(Settings.IncrementalPlacementDelay));
        }
        Sponge.server().scheduler().submit(taskBuilder.build());
    }

    public boolean isFreeSpace(UUID playerID, String schematicName, Collection<Location> locations) {
        final HashMap<Location, Object> originalBlocks = new HashMap<>();
        Player p = Sponge.server().player(playerID).get();
        for (Location loc : locations){
            ServerLocation spongeLoc = SpongeAdapter.adapt(loc);
            originalBlocks.put(loc, spongeLoc.blockType());
            if (!Settings.CheckFreeSpace){
                continue;
            }
            if (!spongeLoc.blockType().equals(BlockTypes.AIR.get()) && !Settings.blocksToIgnore.contains(spongeLoc.blockType())){
                p.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Place - No free space")));
                return false;
            }

        }
        StructureManager.getInstance().addStructureByPlayer(playerID, schematicName, originalBlocks);
        return true;
    }

    public void sendMessageToPlayer(UUID recipient, String message) {
        Sponge.server().player(recipient).get().sendMessage(Component.text(message));
    }

    @Override
    public void logMessage(Level level, String message) {
        switch (level.toString()) {
            case "SEVERE":
                logger.error(message);
                break;
            case "WARNING":
                logger.warn(message);
                break;
            case "INFO":
                logger.info(message);
                break;
        }
    }

    public Logger getLogger() {
        return logger;
    }

    @NotNull
    public SpongeWorldEdit getWorldEditPlugin() {
        return worldEditPlugin;
    }

    public void clearInterior(Collection<Location> interior) {
        for (Location loc : interior){
            SpongeAdapter.adapt(loc).setBlockType(BlockTypes.AIR.get(), BlockChangeFlags.NONE);
        }
    }

    @Override
    public void scheduleSyncTask(Runnable runnable) {
        Sponge.server().scheduler().submit(Task.builder().execute(runnable).build());
    }

    @Override
    public void scheduleSyncTaskLater(Runnable runnable, long delay) {
        Sponge.server().scheduler().executor(plugin).schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void scheduleAsyncTask(Runnable runnable) {
        Sponge.asyncScheduler().executor(plugin).submit(runnable);
    }

    @Override
    public void broadcast(String s) {
        Sponge.server().broadcastAudience().sendMessage(Component.text(s));
    }

    public static synchronized StructureBoxes getInstance() {
        return instance;
    }

    public void loadConfig() {
        final String CONFIG_FILENAME = "structureboxes.conf";
        final Path configPath = getConfigDir().resolve(CONFIG_FILENAME);
        if (configPath.toFile().exists()) {
            return;
        }
        logger.info("Main config missing. Creating one");
        final InputStream stream = plugin.openResource(CONFIG_FILENAME).orElseThrow(RuntimeException::new);
        try {
            Files.copy(stream, configPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void readConfig() throws IOException {
        loader = HoconConfigurationLoader
                .builder()
                .path(defaultConfig)
                .defaultOptions(
                        (objects) -> {
                            return objects.serializers(
                                    (builder ->
                                            builder.register(BlockType.class, new BlockTypeSerializer())
                                    )
                            );
                        }
                )
                .build();
        final ConfigurationNode node = loader.load();
        //Read general config
        Settings.locale = node.node("Locale").getString("en");
        Settings.Metrics = node.node("Metrics").getBoolean(false);
        Settings.StructureBoxItem = node.node("Structure Box Item").get(BlockType.class, BlockTypes.CHEST.get());
        Settings.StructureBoxLore = node.node("Structure Box Display Name").getString("§6Structure Box");
        Settings.MaxStructureSize = node.node("Max Structure Size").getInt(10000);
        Settings.MaxSessionTime = node.node("Max Session Time").getInt(300);
        Settings.PlaceCooldownTime = node.node("Place Cooldown Time").getInt(30);
        Settings.StructureBoxPrefix = node.node("Structure Box Prefix").getString("§6Structure Box: ");
        Settings.AlternativePrefixes.addAll(node.node("Alternative Prefixes").getList(TypeToken.get(String.class), Collections.emptyList()));
        Settings.StructureBoxInstruction.addAll(node.node("Structure Box Instruction Message").getList(TypeToken.get(String.class), Collections.emptyList()));
        Settings.RequirePermissionPerStructureBox = node.node("Require permission per structure box").getBoolean(false);

        //Read restrict to regions section
        final ConfigurationNode restrictToRegionsNode = node.node("Restrict to regions");
        Settings.RestrictToRegionsEnabled = restrictToRegionsNode.node("Enabled").getBoolean(false);
        Settings.RestrictToRegionsEntireStructure = restrictToRegionsNode.node("Entire structure").getBoolean(false);
        Settings.RestrictToRegionsExceptions.addAll(restrictToRegionsNode.node("Exceptions").getList(TypeToken.get(String.class), Collections.emptyList()));

        //Read free space
        final ConfigurationNode freeSpaceNode = node.node("Free space");
        Settings.CheckFreeSpace = freeSpaceNode.node("Enabled").getBoolean(true);
        Settings.blocksToIgnore.addAll(freeSpaceNode.node("Blocks to ignore").getList(TypeToken.get(BlockType.class), Collections.emptyList()));

    }

    public void loadLocales() throws IOException {
        final String[] LOCALES = {"en", "no", "it"};
        final String LOCALE_PATH = "localisation/lang_(lang).properties";
        final File localeDir = new File(getConfigDir().toFile(), "localisation");
        if (!localeDir.exists()) {
            localeDir.mkdirs();
        }
        for (String locale : LOCALES){
            final String pathName = LOCALE_PATH.replace("(lang)", locale);
            final Path target = getConfigDir().resolve(pathName);
            logger.info(target.toString());
            logger.info(pathName);
            if (target.toFile().exists())
                continue;
            final InputStream source = plugin.openResource(pathName).orElseThrow(IOException::new);
            logger.info(source);
            Files.copy(source, target);
        }


    }

    private void handleMetrics() {

    }

    public PluginContainer getPlugin() {
        return plugin;
    }

    public SystemSubject getConsole() {
        return console;
    }

    public String getSchematicDir() {
        return schematicDir;
    }

    public Path getWeDir() {
        return weDir;
    }

    @NotNull
    public Optional<Movecraft> getMovecraftPlugin() {
        return movecraftPlugin;
    }

    public @NotNull Optional<EagleFactionsPlugin> getEagleFactionsPlugin() {
        return eagleFactionsPlugin;
    }
}
