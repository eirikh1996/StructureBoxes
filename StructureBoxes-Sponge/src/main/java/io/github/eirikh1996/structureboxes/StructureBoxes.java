package io.github.eirikh1996.structureboxes;

import br.net.fabiozumbi12.RedProtect.Sponge.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.intellectualcrafters.plot.IPlotMain;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.sk89q.worldedit.sponge.SpongeWorldEdit;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.eirikh1996.structureboxes.command.StructureBoxCommand;
import io.github.eirikh1996.structureboxes.command.StructureBoxCreateCommand;
import io.github.eirikh1996.structureboxes.command.StructureBoxReloadCommand;
import io.github.eirikh1996.structureboxes.command.StructureBoxUndoCommand;
import io.github.eirikh1996.structureboxes.compat.we6.IWorldEditHandler;
import io.github.eirikh1996.structureboxes.listener.BlockListener;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.eirikh1996.structureboxes.utils.MathUtils;
import io.github.eirikh1996.structureboxes.utils.RegionUtils;
import me.ryanhamshire.griefprevention.GriefPrevention;
import me.ryanhamshire.griefprevention.api.claim.Claim;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.bstats.sponge.Metrics2;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.ConfigManager;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
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
import java.util.*;
import java.util.logging.Logger;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;


@Plugin(id = "structureboxes",
        name = "StructureBoxes",
        description = "A plugin that adds placable blocks that turn into pre-made structures",
        version = "1.0",
        authors = {"eirikh1996"},
        dependencies = {
                @Dependency(id = "worldedit"),
                @Dependency(id = "redprotect", optional = true),
                @Dependency(id = "griefprevention", optional = true),
                @Dependency(id = "plotsquared", optional = true)})
public class StructureBoxes implements SBMain {

    private static StructureBoxes instance;

    @Inject private Logger logger;
    @Inject private Game game;
    @Inject @DefaultConfig(sharedRoot = false) private Path defaultConfig;
    @Inject @ConfigDir(sharedRoot = false) private Path configDir;

    private ConfigurationLoader<CommentedConfigurationNode> loader;

    public Path getConfigDir() {
        return configDir;
    }

    private SpongeWorldEdit worldEditPlugin;
    @Inject private PluginManager pluginManager;
    @Inject private PluginContainer plugin;
    @Inject private ConfigManager configManager;
    private WorldEditHandler worldEditHandler;
    @NotNull private Optional<RedProtect> redProtectPlugin = Optional.empty();
    @NotNull private Optional<GriefPrevention> griefPreventionPlugin = Optional.empty();
    @NotNull private Optional<EagleFactionsPlugin> eagleFactionsPlugin = Optional.empty();
    @NotNull private Optional<IPlotMain> plotSquaredPlugin = Optional.empty();

    private Task.Builder taskBuilder = Task.builder();
    private boolean plotSquaredInstalled = false;
    @Inject private Metrics2 metrics;

    private ConsoleSource console = Sponge.getServer().getConsole();

    @Listener
    public void onGameLoaded(GameLoadCompleteEvent event) {
        instance = this;

        try {
            final Optional<Asset> config = plugin.getAsset("structureboxes.conf");
            assert config.isPresent();
            config.get().copyToFile(defaultConfig, false, true);
            readConfig();
            loadLocales();
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
        I18nSupport.initialize(getConfigDir().toFile());
        //Create command
        CommandSpec createCommand = CommandSpec.builder()
                .permission("structureboxes.create")
                .arguments(GenericArguments.string(Text.of()))
                .executor(new StructureBoxCreateCommand())
                .build();

        //undo command
        CommandSpec undoCommand = CommandSpec.builder()
                .executor(new StructureBoxUndoCommand())
                .permission("structureboxes.undo")
                .build();


        //reload command
        CommandSpec reloadCommand = CommandSpec.builder()
                .executor(new StructureBoxReloadCommand())
                .permission("structureboxes.reload")
                .build();

        CommandSpec structureBoxCommand = CommandSpec.builder()
                .executor(new StructureBoxCommand())
                .child(createCommand, "create", "cr", "c")
                .child(undoCommand, "undo", "u" , "ud")
                .child(reloadCommand, "reload", "r", "rl")
                .build();
        Sponge.getCommandManager().register(plugin, structureBoxCommand, "structurebox", "sbox", "sb");


    }



    @SuppressWarnings("unchecked")
    @Listener
    public void onServerStarting(GameStartingServerEvent event) {

        worldEditPlugin = (SpongeWorldEdit) pluginManager.getPlugin("worldedit").get().getInstance().get();

        //Check for RedProtect
        Optional<PluginContainer> redprotect = pluginManager.getPlugin("redprotect");
        if (redprotect.isPresent() && redprotect.get().getInstance().isPresent()){
            console.sendMessage(Text.of(I18nSupport.getInternationalisedString("Startup - RedProtect detected")));
            redProtectPlugin = (Optional<RedProtect>) redprotect.get().getInstance();
        }
        //Check for GriefPrevention
        Optional<PluginContainer> griefprevention = pluginManager.getPlugin("griefprevention");
        if (griefprevention.isPresent() && griefprevention.get().getInstance().isPresent()) {
            console.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Startup - GriefPrevention detected")));
            griefPreventionPlugin = (Optional<GriefPrevention>) griefprevention.get().getInstance();
        }
        //Check for EagleFactions
        Optional<PluginContainer> eagleFactions = pluginManager.getPlugin("eaglefactions");
        if (eagleFactions.isPresent() && eagleFactions.get().getInstance().isPresent()) {
            console.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Startup - EagleFactions detected")));
            eagleFactionsPlugin = (Optional<EagleFactionsPlugin>) eagleFactions.get().getInstance();
        }
        //Check for PlotSquared
        Optional<PluginContainer> plotsquared = pluginManager.getPlugin("plotsquared");
        if (plotsquared.isPresent() && plotsquared.get().getInstance().isPresent()) {
            console.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Startup - PlotSquared detected")));
            plotSquaredPlugin = (Optional<IPlotMain>) plotsquared.get().getInstance();
        }
        //Now read WorldEdit config
        final Path weDir = Paths.get(configDir.getParent().toString(), "worldedit");
        final Path weConfig = Paths.get(weDir.toString(), "worldedit.conf");
        final ConfigurationLoader<CommentedConfigurationNode> weLoader = HoconConfigurationLoader.builder().setPath(weConfig).build();
        try {
            final String schematicDir = weLoader.load().getNode("saving").getNode("dir").getString();
            worldEditHandler = new IWorldEditHandler(new File(weDir.toFile(), schematicDir), this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Sponge.getMetricsConfigManager().areMetricsEnabled(plugin)) {

        }

        //Register listener
        Sponge.getEventManager().registerListeners(this, new BlockListener());
    }

    public WorldEditHandler getWorldEditHandler() {
        return worldEditHandler;
    }

    @Override
    public boolean structureWithinRegion(UUID playerID, String schematicID, Collection<Location> locations) {
        if (!Settings.RestrictToRegionsEntireStructure && Sponge.getServer().getPlayer(playerID).get().hasPermission("structureboxes.bypassregionrestriction")) {
            return true;
        }
        for (Location location : locations) {
            if (RegionUtils.isWithinRegion(MathUtils.sbToSpongeLoc(location))) {
                continue;
            }
            return false;
        }
        return true;
    }

    public Platform getPlatform() {
        return Platform.SPONGE;
    }

    public boolean isFreeSpace(UUID playerID, String schematicName, Collection<Location> locations) {
        final HashMap<Location, Object> originalBlocks = new HashMap<>();
        Player p = Sponge.getServer().getPlayer(playerID).get();
        for (Location loc : locations){
            org.spongepowered.api.world.Location<World> spongeLoc = MathUtils.sbToSpongeLoc(loc);
            originalBlocks.put(loc, spongeLoc.getBlockType());
            if (!spongeLoc.getBlockType().equals(BlockTypes.AIR) && !Settings.blocksToIgnore.contains(spongeLoc.getBlockType())){
                p.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Place - No free space")));
                return false;
            }
            if (redProtectPlugin.isPresent()) {
                RedProtectAPI api = redProtectPlugin.get().getAPI();
                Region region = api.getRegion(spongeLoc);
                if (region == null) {
                    continue;
                } else if (region.canBuild(p)) {
                    continue;
                }
                p.sendMessage(Text.of(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "RedProtect")));
                return false;
            }
            if (griefPreventionPlugin.isPresent()) {
                final Claim claim = GriefPrevention.getApi().getClaimManager(p.getWorld()).getClaimAt(p.getLocation());
                if (claim == null) {
                    continue;
                } else if (claim.isTrusted(p.getUniqueId())) {
                    continue;
                }
                p.sendMessage(Text.of(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "GriefPrevention")));
                return false;
            }
            if (plotSquaredPlugin.isPresent()) {
                final PS ps = PS.get();
                final com.intellectualcrafters.plot.object.Location psLoc = new com.intellectualcrafters.plot.object.Location(p.getWorld().getName(), p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
                final PlotArea pArea = ps.getApplicablePlotArea(psLoc);
                if (pArea == null) {
                    continue;
                }
                Plot plot = pArea.getPlot(psLoc);
                if (plot == null) {
                    continue;
                }
                if (plot.isOwner(p.getUniqueId()) || plot.isAdded(p.getUniqueId())) {
                    continue;
                }
                p.sendMessage(Text.of(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "PlotSquared")));
                return false;
            }
            if (eagleFactionsPlugin.isPresent()) {
                final Optional<Faction> optionalFaction = eagleFactionsPlugin.get().getFactionLogic().getFactionByChunk(p.getWorld().getUniqueId(), p.getLocation().getChunkPosition());
                if (!optionalFaction.isPresent()) {
                    continue;
                }
                Faction faction = optionalFaction.get();
                if (faction.containsPlayer(p.getUniqueId())) {
                    continue;
                }
                p.sendMessage(Text.of(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "EagleFactions")));
                return false;
            }

        }
        StructureManager.getInstance().addStructureByPlayer(playerID, schematicName, originalBlocks);
        return true;
    }

    public void sendMessageToPlayer(UUID recipient, String message) {
        Sponge.getServer().getPlayer(recipient).get().sendMessage(Text.of(message));
    }

    public Logger getLogger() {
        return logger;
    }

    public SpongeWorldEdit getWorldEditPlugin() {
        return worldEditPlugin;
    }

    public Optional<GriefPrevention> getGriefPreventionPlugin() {
        return griefPreventionPlugin;
    }

    public Optional<RedProtect> getRedProtectPlugin() {
        return redProtectPlugin;
    }

    public Optional<EagleFactionsPlugin> getEagleFactionsPlugin() {
        return eagleFactionsPlugin;
    }

    public void clearInterior(Collection<Location> interior) {
        for (Location loc : interior){
            MathUtils.sbToSpongeLoc(loc).setBlockType(BlockTypes.AIR);
        }
    }

    @Override
    public void scheduleSyncTask(Runnable runnable) {
        taskBuilder.execute(runnable).submit(this);
    }

    @Override
    public void scheduleAsyncTask(Runnable runnable) {
        taskBuilder.async().execute(runnable).submit(this);
    }

    @Override
    public void broadcast(String s) {
        Sponge.getServer().getBroadcastChannel().send(Text.of(s));
    }

    public static synchronized StructureBoxes getInstance() {
        return instance;
    }

    public void readConfig() throws ObjectMappingException, IOException {
        loader = HoconConfigurationLoader.builder().setPath(defaultConfig).build();
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
        Settings.AlternativePrefixes.addAll(node.getNode("Alternative Prefixes").getList(TypeToken.of(String.class), Collections.emptyList()));
        Settings.StructureBoxInstruction.addAll(node.getNode("Structure Box Instruction Message").getList(TypeToken.of(String.class), Collections.emptyList()));
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

    }

    public void loadLocales() throws IOException {
        final String[] LOCALES = {"en", "no", "it"};
        for (String locale : LOCALES){
            if (new File(configDir.toString() + "/localisation/lang_" + locale + ".properties").exists()){
                continue;
            }

            final Optional<Asset> asset = plugin.getAsset("localisation/lang_" + locale + ".properties");
            asset.get().copyToDirectory(Paths.get(configDir.toString(), "localisation"), false, true);

        }


    }

}
