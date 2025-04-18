package io.github.eirikh1996.structureboxes;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.engine.EnginePermBuild;
import com.massivecraft.factions.entity.MFlag;
import com.palmergames.bukkit.towny.Towny;
import com.plotsquared.bukkit.BukkitPlatform;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.listener.EventAbstractionListener;
import com.wasteofplastic.askyblock.ASkyBlock;
import io.github.eirikh1996.structureboxes.commands.StructureBoxCommand;
import io.github.eirikh1996.structureboxes.listener.BlockListener;
import io.github.eirikh1996.structureboxes.listener.InventoryListener;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.region.FactionsFlagManager;
import io.github.eirikh1996.structureboxes.region.RedProtectFlagManager;
import io.github.eirikh1996.structureboxes.region.WorldGuardFlagManager;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.ASkyBlockUtils;
import io.github.eirikh1996.structureboxes.utils.AcidIslandUtils;
import io.github.eirikh1996.structureboxes.utils.BentoBoxUtils;
import io.github.eirikh1996.structureboxes.utils.CivsUtils;
import io.github.eirikh1996.structureboxes.utils.FactionsUUIDUtils;
import io.github.eirikh1996.structureboxes.utils.FactionsUtils;
import io.github.eirikh1996.structureboxes.utils.GriefPreventionUtils;
import io.github.eirikh1996.structureboxes.utils.IslandWorldUtils;
import io.github.eirikh1996.structureboxes.utils.LandClaimingUtils;
import io.github.eirikh1996.structureboxes.utils.LandsUtils;
import io.github.eirikh1996.structureboxes.utils.MathUtils;
import io.github.eirikh1996.structureboxes.utils.PlotSquaredUtils;
import io.github.eirikh1996.structureboxes.utils.RedProtectUtils;
import io.github.eirikh1996.structureboxes.utils.RegionUtils;
import io.github.eirikh1996.structureboxes.utils.SkyBlockUtils;
import io.github.eirikh1996.structureboxes.utils.StructureboxFlag;
import io.github.eirikh1996.structureboxes.utils.SuperiorSkyblockUtils;
import io.github.eirikh1996.structureboxes.utils.TownyUtils;
import io.github.eirikh1996.structureboxes.utils.UpdateChecker;
import io.github.eirikh1996.structureboxes.utils.WorldGuardUtils;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.zombie_striker.landclaiming.LandClaiming;
import net.countercraft.movecraft.Movecraft;
import org.Bukkitters.SkyBlock.Main;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.redcastlemedia.multitallented.civs.Civs;
import org.yaml.snakeyaml.Yaml;
import pl.islandworld.IslandWorld;
import world.bentobox.bentobox.BentoBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class StructureBoxes extends JavaPlugin implements SBMain {
    private static StructureBoxes instance;
    private WorldGuardPlugin worldGuardPlugin;
    private WorldEditPlugin worldEditPlugin;
    private WorldEditHandler worldEditHandler;
    private Factions factionsPlugin;
    private RedProtect redProtectPlugin;
    private GriefPrevention griefPreventionPlugin;
    private LandClaiming landClaimingPlugin;
    private Towny townyPlugin;

    private BukkitPlatform plotSquaredPlugin;

    //Skyblock plugins
    private IridiumSkyblock iridiumSkyblockPlugin;
    private IslandWorld islandWorldPlugin;
    private Main skyBlockPlugin;
    private BentoBox bentoBoxPlugin;
    private SuperiorSkyblock superiorSkyblockPlugin;
    private ASkyBlock aSkyBlockPlugin;
    private com.wasteofplastic.acidisland.ASkyBlock acidIslandPlugin;

    private boolean plotSquaredInstalled = false;
    private boolean factionsUUIDInstalled = false;
    private Civs civsPlugin;
    private Plugin landsPlugin;
    private Movecraft movecraftPlugin;
    private Metrics metrics;
    private boolean startup = true;

    private static Method GET_MATERIAL;

    static {
        try {
            GET_MATERIAL = Material.class.getDeclaredMethod("getMaterial", int.class);
        } catch (NoSuchMethodException e) {
            GET_MATERIAL = null;
        }
    }

    @Override
    public void onLoad() {
        instance = this;
        Plugin wg = getServer().getPluginManager().getPlugin("WorldGuard");
        //Check for WorldGuard
        if (wg instanceof WorldGuardPlugin){
            worldGuardPlugin = (WorldGuardPlugin) wg;
            WorldGuardUtils.registerFlag();
        }
    }

    @Override
    public void onEnable() {
        final String[] LOCALES = {"en", "no", "it", "zhcn"};
        for (String locale : LOCALES){
            final File langFile = new File(getDataFolder().getAbsolutePath() + "/localisation/lang_" + locale + ".properties");
            if (langFile.exists()){
                continue;
            }
            saveResource("localisation/lang_" + locale + ".properties", false);

        }


            saveDefaultConfig();
        readConfig();

        if (!I18nSupport.initialize(getDataFolder(), this)){
            return;
        }
        worldEditPlugin = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        //This plugin requires WorldEdit in order to load. Therefore, assert that WorldEdit is not null when this enables
        assert worldEditPlugin != null;
        //Disable this plugin if WorldEdit is disabled
        if (!worldEditPlugin.isEnabled()){
            getLogger().severe(I18nSupport.getInternationalisedString("Startup - WorldEdit is disabled"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        //if on 1.13 and up, Check if FAWE is installed
        if (!Settings.IsLegacy) {
            try {
                Class.forName("com.boydti.fawe.bukkit.FaweBukkit");
                Settings.FAWE = true;
            } catch (ClassNotFoundException e) {
                Settings.FAWE = false;
            }
        }

        String weVersion = worldEditPlugin.getDescription().getVersion();

        int versionNumber = Settings.IsLegacy ? 6 : 7;
        final Map data;
        try {
            File weConfig = new File(getWorldEditPlugin().getDataFolder(), "config" + (Settings.FAWE ? "-legacy" : "") + ".yml");
            Yaml yaml = new Yaml();
            data = yaml.load(new FileInputStream(weConfig));
        } catch (IOException e){
            getLogger().severe(I18nSupport.getInternationalisedString("Startup - Error reading WE config"));
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        File schemDir = new File(worldEditPlugin.getDataFolder(), (String) ((Map) data.get("saving")).get("dir"));

        //Check if there is a compatible version of WorldEdit
        worldEditHandler = new WorldEditHandler(schemDir, this);

        boolean foundRegionProvider = false;
        //Check for WorldGuard
        if (worldGuardPlugin != null){
            getLogger().info(I18nSupport.getInternationalisedString("Startup - WorldGuard detected"));
            foundRegionProvider = true;
            EventAbstractionListener listener = new EventAbstractionListener(worldGuardPlugin);
            HandlerList.unregisterAll(listener);
            listener.registerEvents();
            getServer().getPluginManager().registerEvent(
                    BlockPlaceEvent.class,
                    listener,
                    EventPriority.NORMAL,
                    new WorldGuardFlagManager(), this);
        }
        Plugin f = getServer().getPluginManager().getPlugin("Factions");
        try {
            Class.forName("com.massivecraft.factions.FactionsPlugin");
            factionsUUIDInstalled = true;
        } catch (ClassNotFoundException e) {
            factionsUUIDInstalled = false;
        }
        //Check for Factions
        if (!factionsUUIDInstalled && f instanceof Factions){
            getLogger().info(I18nSupport.getInternationalisedString("Startup - Factions detected"));
            factionsPlugin = (Factions) f;
            MFlag.getCreative(
                    16000,
                    "structurebox",
                    "structurebox",
                    "Can players place structure boxes in this faction's territory?",
                    "Players can place structure boxes",
                    "Players cannot place structure boxes",
                    false,
                    false,
                    true);
            foundRegionProvider = true;
            getServer().getPluginManager().registerEvent(
                    BlockPlaceEvent.class,
                    EnginePermBuild.get(),
                    EventPriority.NORMAL,
                    FactionsFlagManager.getInstance(),
                    this);
            getServer().getPluginManager().registerEvent(
                    PlayerInteractEvent.class,
                    EnginePermBuild.get(),
                    EventPriority.NORMAL,
                    FactionsFlagManager.getInstance(),
                    this);
        } else if (factionsUUIDInstalled && FactionsUUIDUtils.isFactionsUUID(f)) { //Check for FactionsUUID
            getLogger().info(I18nSupport.getInternationalisedString("Startup - Factions detected"));
            foundRegionProvider = true;
            factionsUUIDInstalled = true;
        } else {
            factionsUUIDInstalled = false;
        }
        //Check for RedProtect
        Plugin rp = getServer().getPluginManager().getPlugin("RedProtect");
        if (rp instanceof RedProtect){
            getLogger().info(I18nSupport.getInternationalisedString("Startup - RedProtect detected"));
            redProtectPlugin = (RedProtect) rp;
            foundRegionProvider = true;
            redProtectPlugin.getAPI().addFlag("structurebox", false, false);
            getServer().getPluginManager().registerEvent(
                    BlockPlaceEvent.class,
                    new br.net.fabiozumbi12.RedProtect.Bukkit.listeners.BlockListener(),
                    EventPriority.HIGH,
                    new RedProtectFlagManager(),
                    this
            );
        }
        //Check for GriefPrevention
        Plugin gp = getServer().getPluginManager().getPlugin("GriefPrevention");
        if (gp instanceof GriefPrevention){
            getLogger().info(I18nSupport.getInternationalisedString("Startup - GriefPrevention detected"));
            griefPreventionPlugin = (GriefPrevention) gp;
            foundRegionProvider = true;
        }
        //Check for PlotSquared
        Plugin ps = getServer().getPluginManager().getPlugin("PlotSquared");
        if (ps instanceof BukkitPlatform) {
                plotSquaredInstalled = true;
                foundRegionProvider = true;
                StructureboxFlag.register();
                getLogger().info(I18nSupport.getInternationalisedString("Startup - PlotSquared detected"));
        }

        //Check for landClaiming
        Plugin lp = getServer().getPluginManager().getPlugin("LandClaiming");
        if (lp instanceof LandClaiming){
            getLogger().info(I18nSupport.getInternationalisedString("Startup - LandClaiming detected"));
            landClaimingPlugin = (LandClaiming) lp;
            foundRegionProvider = true;
        }
        //Check for Towny
        Plugin tp = getServer().getPluginManager().getPlugin("Towny");
        if (tp instanceof Towny){
            getLogger().info(I18nSupport.getInternationalisedString("Startup - Towny detected"));
            townyPlugin = (Towny) tp;
            foundRegionProvider = true;
        }
        //Check for Civs
        Plugin cp = getServer().getPluginManager().getPlugin("Civs");
        if (cp instanceof Civs) {
            getLogger().info(I18nSupport.getInternationalisedString("Startup - Civs detected"));
            civsPlugin = (Civs) cp;
            foundRegionProvider = true;
        }
        //Check for Lands
        Plugin lands = getServer().getPluginManager().getPlugin("Lands");
        if (lands != null) {
            getLogger().info(I18nSupport.getInternationalisedString("Startup - Lands detected"));
            landsPlugin = lands;
            foundRegionProvider = true;
        }
        //Check for Movecraft
        Plugin movecraft = getServer().getPluginManager().getPlugin("Movecraft");
        if (movecraft instanceof Movecraft) {
            getLogger().info(I18nSupport.getInternationalisedString("Startup - Movecraft detected"));
            boolean movecraft8;
            try {
                Class.forName("net.countercraft.movecraft.craft.BaseCraft");
                movecraft8 = true;
            } catch (ClassNotFoundException e) {
                movecraft8 = false;
            }
            Class<?> clazz = null;
            try {
                clazz = Class.forName("io.github.eirikh1996.structureboxes.movecraft.v" + (movecraft8 ? "8" : "7") + ".MovecraftListener");
                if (Listener.class.isAssignableFrom(clazz)) {
                    getServer().getPluginManager().registerEvents((Listener) clazz.getDeclaredConstructor().newInstance(), this);
                }
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }


            movecraftPlugin = (Movecraft) movecraft;
            foundRegionProvider = true;
        }
        //Check for Iridium Skyblock
        Plugin iSkyBlock = getServer().getPluginManager().getPlugin("IridiumSkyblock");
        if (iSkyBlock instanceof IridiumSkyblock) {
            getLogger().info(I18nSupport.getInternationalisedString("Startup - IridiumSkyblock detected"));
            iridiumSkyblockPlugin = (IridiumSkyblock) iSkyBlock;
            foundRegionProvider = true;
        }
        //Check for Island World
        Plugin iw = getServer().getPluginManager().getPlugin("IslandWorld");
        if (iw instanceof IslandWorld) {
            getLogger().info(I18nSupport.getInternationalisedString("Startup - IslandWorld detected"));
            islandWorldPlugin = (IslandWorld) iw;
            foundRegionProvider = true;
        }
        //Check for SkyBlock
        Plugin skyblock= getServer().getPluginManager().getPlugin("SkyBlock");
        if (skyblock instanceof Main) {
            getLogger().info(I18nSupport.getInternationalisedString("Startup - SkyBlock detected"));
            skyBlockPlugin = (Main) skyblock;
            foundRegionProvider = true;
        }
        //Check for BentoBox
        Plugin bb= getServer().getPluginManager().getPlugin("BentoBox");
        if (bb instanceof BentoBox) {
            getLogger().info(I18nSupport.getInternationalisedString("Startup - BentoBox detected"));
            bentoBoxPlugin = (BentoBox) bb;
            foundRegionProvider = true;
        }
        Plugin ssb = getServer().getPluginManager().getPlugin("SuperiorSkyblock");
        if (ssb instanceof SuperiorSkyblock) {
            getLogger().info(I18nSupport.getInternationalisedString("Startup - SuperiorSkyblock detected"));
            superiorSkyblockPlugin = (SuperiorSkyblock) ssb;
            foundRegionProvider = true;
        }
        Plugin ai = getServer().getPluginManager().getPlugin("AcidIsland");
        if (ai instanceof com.wasteofplastic.acidisland.ASkyBlock) {
            getLogger().info(I18nSupport.getInternationalisedString("Startup - AcidIsland detected"));
            acidIslandPlugin = (com.wasteofplastic.acidisland.ASkyBlock) ai;
            foundRegionProvider = true;
        }
        Plugin asb = getServer().getPluginManager().getPlugin("ASkyBlock");
        if (asb instanceof ASkyBlock) {
            getLogger().info(I18nSupport.getInternationalisedString("Startup - ASkyBlock detected"));
            aSkyBlockPlugin = (ASkyBlock) asb;
            foundRegionProvider = true;
        }
        //If no compatible protection plugin is found, disable region restriction if it is on
        if (Settings.RestrictToRegionsEnabled && !foundRegionProvider){
            getLogger().warning(I18nSupport.getInternationalisedString("Startup - Restrict to regions no compatible protection plugin"));
            Settings.RestrictToRegionsEnabled = false;
            getLogger().info(I18nSupport.getInternationalisedString("Startup - Restrict to regions set to false"));
        }
        if (Settings.Metrics) {
            metrics = new Metrics(this);
            final boolean noRegionProvider = !foundRegionProvider;
            metrics.addCustomChart(new Metrics.AdvancedPie("region_providers", new Callable<>() {
                @Override
                public Map<String, Integer> call() {
                    Map<String, Integer> valueMap = new HashMap<>();
                    if (getFactionsPlugin() != null) {
                        valueMap.put("Factions", 1);
                    }
                    if (getTownyPlugin() != null) {
                        valueMap.put("Towny", 1);
                    }
                    if (getWorldGuardPlugin() != null) {
                        valueMap.put("WorldGuard", 1);
                    }
                    if (isPlotSquaredInstalled()) {
                        valueMap.put("PlotSquared", 1);
                    }
                    if (getRedProtectPlugin() != null) {
                        valueMap.put("RedProtect", 1);
                    }
                    if (getGriefPreventionPlugin() != null) {
                        valueMap.put("GriefPrevention", 1);
                    }
                    if (getLandClaimingPlugin() != null) {
                        valueMap.put("LandClaiming", 1);
                    }
                    if (getCivsPlugin() != null) {
                        valueMap.put("Civs", 1);
                    }
                    if (getLandsPlugin() != null) {
                        valueMap.put("Lands", 1);
                    }
                    if (noRegionProvider) {
                        valueMap.put("None", 1);
                    }
                    return valueMap;
                }
            }));
            metrics.addCustomChart(new Metrics.SimplePie("localisation", () -> Settings.locale));
        }

        this.getCommand("structurebox").setExecutor(new StructureBoxCommand());

        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(UpdateChecker.getInstance(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        StructureManager.getInstance().setSbMain(this);
        if (startup){
            getServer().getScheduler().runTaskTimerAsynchronously(this, StructureManager.getInstance(), 0, 20);
            UpdateChecker.getInstance().runTaskTimerAsynchronously(this, 120, 36000);
            startup = false;
        }

    }

    public static StructureBoxes getInstance(){
        return instance;
    }

    public WorldGuardPlugin getWorldGuardPlugin(){
        return worldGuardPlugin;
    }

    public WorldEditPlugin getWorldEditPlugin() {
        return worldEditPlugin;
    }

    public Factions getFactionsPlugin() {
        return factionsPlugin;
    }

    public RedProtect getRedProtectPlugin() {
        return redProtectPlugin;
    }

    public GriefPrevention getGriefPreventionPlugin() {
        return griefPreventionPlugin;
    }

    public LandClaiming getLandClaimingPlugin() {
        return landClaimingPlugin;
    }

    public Towny getTownyPlugin() {
        return townyPlugin;
    }

    public boolean isPlotSquaredInstalled() {
        return plotSquaredInstalled;
    }

    public Civs getCivsPlugin() {
        return civsPlugin;
    }

    public Plugin getLandsPlugin() {
        return landsPlugin;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    @Override
    public WorldEditHandler getWorldEditHandler() {
        return worldEditHandler;
    }

    @Override
    public boolean structureWithinRegion(UUID playerID, String schematicID, Collection<Location> locations) {
        boolean withinRegion = true;
        for (Location loc : locations){
            if (RegionUtils.isWithinRegion(BukkitAdapter.adapt(loc))){
                continue;
            }
            withinRegion = false;
        }
        boolean exempt = false;
        for (String exception : Settings.RestrictToRegionsExceptions){
            if (!schematicID.contains(exception)){
                continue;
            }
            exempt = true;
        }
        final Player player = getServer().getPlayer(playerID);
        if (Settings.Debug){
            Bukkit.broadcastMessage("Within region: " + withinRegion);
            Bukkit.broadcastMessage("Exempt: " + exempt);
            Bukkit.broadcastMessage("Can bypass: " + player.hasPermission("structureboxes.bypassregionrestriction"));
            Bukkit.broadcastMessage("Entire structure enabled: " + Settings.RestrictToRegionsEntireStructure);
        }
        if (!withinRegion && !exempt && Settings.RestrictToRegionsEntireStructure && !player.hasPermission("structureboxes.bypassregionrestriction")){
            player.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Place - Structure must be in region"));
            return false;
        }
        return true;
    }

    @Override
    public Platform getPlatform() {
        return Platform.BUKKIT;
    }

    @Override
    public void clearStructure(Structure structure) {
        final Player sender = Bukkit.getPlayer(structure.getOwner());
        Map<Location, Object> locationMaterialHashMap = structure.getOriginalBlocks();
        if (locationMaterialHashMap == null) {
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - latest session expired"));
            return;
        }
        final Deque<Location> locations = structure.getLocationsToRemove();
        for (Location loc : locations) {
            org.bukkit.Location bukkitLoc = BukkitAdapter.adapt(loc);
            if (!bukkitLoc.getBlock().getType().name().endsWith("_DOOR"))
                continue;
        }
        new BukkitRunnable() {
            final int queueSize = locations.size();
            final int blocksToProcess = Math.min(queueSize, Settings.IncrementalPlacement ? Settings.IncrementalPlacementBlocksPerTick : 30000);
            int blocksProcessed = 0;
            @Override
            public void run() {

                for (int i = 1 ; i <= blocksToProcess ; i++) {
                    Location poll = locations.pollLast();
                    if (poll == null)
                        break;
                    final Material origType = (Material) locationMaterialHashMap.get(poll);
                    org.bukkit.Location bukkitLoc = BukkitAdapter.adapt(poll);
                    Block b = bukkitLoc.getBlock();
                    if (b.getState() instanceof InventoryHolder){
                        InventoryHolder holder = (InventoryHolder) b.getState();
                        holder.getInventory().clear();
                    }
                    b.setType(origType, false);
                    blocksProcessed++;
                }
                if (Settings.IncrementalPlacement && blocksToProcess % blocksProcessed == 10) {
                    float percent = (((float) blocksProcessed / (float) blocksToProcess) * 100f);
                    sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Removal - Progress") + ": " + percent );
                }
                if (locations.isEmpty()){
                    StructureManager.getInstance().removeStructure(structure);
                    if (structure.isProcessing()) {
                        structure.setProcessing(false);
                    }
                    cancel();
                }
            }

        }.runTaskTimer(StructureBoxes.getInstance(), 0, Settings.IncrementalPlacement ? Settings.IncrementalPlacementDelay : 3);
    }

    @Override
    public boolean isFreeSpace(UUID playerID, String schematicName, Collection<Location> locations) {
        final HashMap<Location, Object> originalBlocks = new HashMap<>();
        @NotNull final Player p = getServer().getPlayer(playerID);
        assert p != null;
        for (Location location : locations){
            World world = BukkitAdapter.adapt((com.sk89q.worldedit.world.World) location.getExtent());
            org.bukkit.Location bukkitLoc = new org.bukkit.Location(world, location.getX(), location.getY(), location.getZ());
            if (Settings.Debug) {
                world.spawnParticle(Particle.ANGRY_VILLAGER, bukkitLoc, 1);
            }
            Material test = bukkitLoc.getBlock().getType();
            originalBlocks.put(location, test);


            if (test.name().endsWith("AIR") || Settings.blocksToIgnore.contains(test) || !Settings.CheckFreeSpace){
                if (RegionUtils.canPlaceStructure(p, bukkitLoc)) {
                    continue;
                }

                if ((getRedProtectPlugin() != null && !RedProtectUtils.canBuild(p, bukkitLoc))){
                    p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "RedProtect"));
                    return false;
                }
                if (getGriefPreventionPlugin() != null && GriefPreventionUtils.canBuild(p, bukkitLoc)){
                    p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "GriefPrevention"));
                    return false;
                }
                if (getFactionsPlugin() != null && (!FactionsUtils.allowBuild(p, bukkitLoc)) && !FactionsUtils.canPlaceStructureBox(bukkitLoc)){
                    p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "Factions"));
                    return false;
                }
                if (isFactionsUUIDInstalled() && !FactionsUUIDUtils.canBuild(p, bukkitLoc)) {
                    p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "Factions"));
                    return false;
                }
                if (getWorldGuardPlugin() != null && !WorldGuardUtils.allowBuild(p, bukkitLoc)){
                    p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "WorldGuard"));
                    return false;
                }
                if (isPlotSquaredInstalled() && PlotSquaredUtils.canBuild(p, bukkitLoc)){
                    p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "PlotSquared"));
                    return false;
                }
                if (getTownyPlugin() != null && !TownyUtils.canBuild(p, bukkitLoc)){
                    p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "Towny"));
                    return false;
                }
                if (getLandClaimingPlugin() != null && !LandClaimingUtils.canBuild(p, bukkitLoc)){
                    p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "LandClaiming"));
                    return false;
                }
                if (getCivsPlugin() != null && !CivsUtils.allowBuild(p, bukkitLoc)) {
                    p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "Civs"));
                    return false;
                }
                if (getLandsPlugin() != null && !LandsUtils.canBuild(p, bukkitLoc)) {
                    p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "Lands"));
                    return false;
                }
                if (getSuperiorSkyblockPlugin() != null && !SuperiorSkyblockUtils.canBuild(p, bukkitLoc)) {
                    p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "SuperiorSkyblock"));
                    return false;
                }
                if (getSkyBlockPlugin() != null && !SkyBlockUtils.canBuild(p, bukkitLoc)) {
                    p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "SkyBlock"));
                    return false;
                }
                if (getIslandWorldPlugin() != null && !IslandWorldUtils.canBuild(p, bukkitLoc)) {
                    p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "IslandWorld"));
                    return false;
                }
                if (getBentoBoxPlugin() != null && !BentoBoxUtils.canBuild(p, bukkitLoc)) {
                    p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "BentoBox"));
                    return false;
                }
                if (getAcidIslandPlugin() != null && !AcidIslandUtils.canBuild(p, bukkitLoc)) {
                    p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "AcidIsland"));
                    return false;
                }
                if (getaSkyBlockPlugin() != null && !ASkyBlockUtils.canBuild(p, bukkitLoc)) {
                    p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "ASkyBlock"));
                    return false;
                }
                continue;
            }
            p.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Place - No free space") );
            return false;
        }
        StructureManager.getInstance().addStructureByPlayer(playerID, schematicName, originalBlocks);
        return true;
    }

    private void saveLegacyConfig(){

        File configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists())
            return;
        saveResource("config_legacy.yml", false);
        File legacyConfigFile = new File(getDataFolder(), "config_legacy.yml");
        legacyConfigFile.renameTo(configFile);
    }

    @Override
    public void sendMessageToPlayer(UUID recipient, String message) {
        Bukkit.getPlayer(recipient).sendMessage(I18nSupport.getInternationalisedString(message));
    }

    @Override
    public void logMessage(Level level, String message) {
        getLogger().log(level, message);
    }

    @Override
    public void clearInterior(Collection<Location> interior) {
        for (Location location : interior){
            org.bukkit.Location bukkitLoc = BukkitAdapter.adapt(location);
            //ignore air blocks
            if (bukkitLoc.getBlock().getType().name().endsWith("AIR")){
                continue;
            }
            bukkitLoc.getBlock().setType(Material.AIR, false);
        }
    }

    @Override
    public void scheduleSyncTask(final Runnable runnable) {
        getServer().getScheduler().runTask(this, runnable);
    }

    @Override
    public void scheduleSyncTaskLater(Runnable runnable, long delay) {
        long ticks = (delay / 1000) * 20;
        ticks = Math.max(ticks, 1);
        getServer().getScheduler().runTaskLater(this, runnable, ticks);
    }

    @Override
    public void scheduleAsyncTask(final Runnable runnable) {
        getServer().getScheduler().runTaskAsynchronously(this, runnable);
    }

    @Override
    public void broadcast(String s) {
        getServer().broadcastMessage(s);
    }

    public void readConfig() {
        reloadConfig();
        Settings.locale = getConfig().getString("Locale", "en");
        Settings.Metrics = getConfig().getBoolean("Metrics", true);
        Settings.PlaceCooldownTime = getConfig().getLong("Place Cooldown Time", 10);
        Settings.PluginPrefix = getConfig().getString("Plugin prefix", "§5[§6StructureBoxes§5]§r");
        Settings.StructureBoxItem = Material.getMaterial(getConfig().getString("Structure Box Item").toUpperCase());
        Settings.StructureBoxLore = getConfig().getString("Structure Box Display Name");
        Object object = getConfig().get("Structure Box Instruction Message");
        Settings.StructureBoxInstruction.clear();
        if (object instanceof String){
            Settings.StructureBoxInstruction.add((String) object);
        } else if (object instanceof List) {
            List list = (List) object;
            for (Object i : list) {
                if (i == null)
                    continue;
                Settings.StructureBoxInstruction.add((String) i);
            }
        }
        Settings.StructureBoxPrefix = getConfig().getString("Structure Box Prefix");
        Settings.AlternativePrefixes = getConfig().getStringList("Alternative Prefixes");
        Settings.RequirePermissionPerStructureBox = getConfig().getBoolean("Require permission per structure box", false);
        ConfigurationSection restrictToRegions = getConfig().getConfigurationSection("Restrict to regions");
        Settings.RestrictToRegionsEnabled = restrictToRegions.getBoolean("Enabled", false);
        Settings.RestrictToRegionsEntireStructure = restrictToRegions.getBoolean("Entire structure", false);
        Settings.RestrictToRegionsExceptions.clear();
        List<String> exceptions = restrictToRegions.getStringList("Exceptions");
        if (!exceptions.isEmpty()){
            Settings.RestrictToRegionsExceptions.addAll(exceptions);
        }
        Settings.MaxSessionTime = getConfig().getLong("Max Session Time", 60);
        Settings.MaxStructureSize = getConfig().getInt("Max Structure Size", 10000);
        Settings.Debug = getConfig().getBoolean("Debug", false);
        ConfigurationSection freeSpace = getConfig().getConfigurationSection("Free space");
        List materials = freeSpace.getList("Blocks to ignore");
        for (Object obj : materials) {
            Material type = null;
            if (obj == null){
                continue;
            }
            else if (obj instanceof Integer) {
                int id = (int) obj;
                if (GET_MATERIAL == null){
                    throw new IllegalArgumentException("Numerical block IDs are not supported by this server version: " + getServer().getVersion());
                }
                try {
                    type = (Material) GET_MATERIAL.invoke(Material.class, id);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else if (obj instanceof String){
                String str = (String) obj;
                type = Material.getMaterial(str.toUpperCase());
            }
            if (type == null){
                continue;
            }
            Settings.blocksToIgnore.add(type);
        }
        Settings.CheckFreeSpace = freeSpace.getBoolean("Require free space", true);
        final ConfigurationSection incrementalPlacement = getConfig().getConfigurationSection("Incremental placement");
        if (incrementalPlacement != null) {
            Settings.IncrementalPlacement = incrementalPlacement.getBoolean("Enabled", false);
            Settings.IncrementalPlacementBlocksPerTick = incrementalPlacement.getInt("Blocks per tick", 1);
            Settings.IncrementalPlacementDelay = incrementalPlacement.getInt("Delay", 1);
        }

    }

    public Movecraft getMovecraftPlugin() {
        return movecraftPlugin;
    }

    public boolean isFactionsUUIDInstalled() {
        return factionsUUIDInstalled;
    }

    public IridiumSkyblock getIridiumSkyblockPlugin() {
        return iridiumSkyblockPlugin;
    }

    public IslandWorld getIslandWorldPlugin() {
        return islandWorldPlugin;
    }

    public Main getSkyBlockPlugin() {
        return skyBlockPlugin;
    }

    public BentoBox getBentoBoxPlugin() {
        return bentoBoxPlugin;
    }

    public SuperiorSkyblock getSuperiorSkyblockPlugin() {
        return superiorSkyblockPlugin;
    }

    public com.wasteofplastic.acidisland.ASkyBlock getAcidIslandPlugin() {
        return acidIslandPlugin;
    }

    public ASkyBlock getaSkyBlockPlugin() {
        return aSkyBlockPlugin;
    }
}
