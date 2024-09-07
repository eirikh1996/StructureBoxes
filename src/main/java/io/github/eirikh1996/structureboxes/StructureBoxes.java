package io.github.eirikh1996.structureboxes;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.listener.EventAbstractionListener;
import io.github.eirikh1996.structureboxes.commands.StructureBoxCommand;
import io.github.eirikh1996.structureboxes.listener.BlockListener;
import io.github.eirikh1996.structureboxes.listener.InventoryListener;
import io.github.eirikh1996.structureboxes.listener.MovecraftListener;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.region.*;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.*;
import net.countercraft.movecraft.Movecraft;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class StructureBoxes extends JavaPlugin {
    private static StructureBoxes instance;
    private WorldGuardPlugin worldGuardPlugin;
    private WorldEditPlugin worldEditPlugin;
    private WorldEditHandler worldEditHandler;

    private boolean startup = true;

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

        if (!I18nSupport.initialize(getDataFolder()))
            return;

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
        try {
            Class.forName("com.boydti.fawe.bukkit.FaweBukkit");
            Settings.FAWE = true;
        }
        catch (ClassNotFoundException e) {
            Settings.FAWE = false;
        }

        String schemDirName = null;
        try {
            // Try loading from config by file
            File weConfig = new File(worldEditPlugin.getDataFolder(), (Settings.FAWE ? "worldedit-" : "") + "config" + ".yml");
            Map<?, ?> data = new Yaml().load(new FileInputStream(weConfig));
            data = (Map<?, ?>) data.get("saving");
            schemDirName = (String) data.get("dir");
        } catch (Exception e){
            e.printStackTrace();
            try {
                // Try loading from API
                schemDirName = worldEditPlugin.getLocalConfiguration().saveDir;
            } catch (Exception e2){
                e2.printStackTrace();
                try {
                    // Try loading from config by plugin
                    Map<?, ?> data = (Map<?, ?>) worldEditPlugin.getConfig().get("saving");
                    schemDirName = (String) data.get("dir");
                } catch (Exception e3){
                    e3.printStackTrace();
                }
            }
        }

        if (schemDirName == null) {
            getLogger().severe(I18nSupport.getInternationalisedString("Startup - Error reading WE config"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        File schemDir = new File(worldEditPlugin.getDataFolder(), schemDirName);
        worldEditHandler = new WorldEditHandler(schemDir, this);

        boolean foundRegionProvider = false;
        // Check for WorldGuard
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

        //Check for Movecraft
        Plugin movecraft = getServer().getPluginManager().getPlugin("Movecraft");
        if (movecraft instanceof Movecraft) {
            getLogger().info(I18nSupport.getInternationalisedString("Startup - Movecraft detected"));
            getServer().getPluginManager().registerEvents(new MovecraftListener(), this);
            foundRegionProvider = true;
        }
        //If no compatible protection plugin is found, disable region restriction if it is on
        if (Settings.RestrictToRegionsEnabled && !foundRegionProvider){
            getLogger().warning(I18nSupport.getInternationalisedString("Startup - Restrict to regions no compatible protection plugin"));
            Settings.RestrictToRegionsEnabled = false;
            getLogger().info(I18nSupport.getInternationalisedString("Startup - Restrict to regions set to false"));
        }

        getCommand("structurebox").setExecutor(new StructureBoxCommand());

        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        StructureManager.getInstance().setSbMain(this);
        if (startup){
            getServer().getScheduler().runTaskTimerAsynchronously(this, StructureManager.getInstance(), 0, 20);
            startup = false;
        }
    }

    @Override
    public void onDisable(){
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

    public WorldEditHandler getWorldEditHandler() {
        return worldEditHandler;
    }

    public boolean structureWithinRegion(UUID playerID, String schematicID, Collection<Location> locations) {
        boolean withinRegion = true;
        for (Location loc : locations){
            if (RegionUtils.isWithinRegion(MathUtils.sb2BukkitLoc(loc))){
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

    public void clearStructure(Structure structure) {
        final Player sender = Bukkit.getPlayer(structure.getOwner());
        Map<Location, Object> locationMaterialHashMap = structure.getOriginalBlocks();
        if (locationMaterialHashMap == null) {
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - latest session expired"));
            return;
        }
        final Deque<Location> locations = structure.getLocationsToRemove();
        for (Location loc : locations) {
            org.bukkit.Location bukkitLoc = MathUtils.sb2BukkitLoc(loc);
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
                    org.bukkit.Location bukkitLoc = MathUtils.sb2BukkitLoc(poll);
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

    public boolean isFreeSpace(UUID playerID, String schematicName, Collection<Location> locations) {
        final HashMap<Location, Object> originalBlocks = new HashMap<>();
        @NotNull final Player p = getServer().getPlayer(playerID);
        assert p != null;
        for (Location location : locations){
            World world = getServer().getWorld(location.getWorld());
            org.bukkit.Location bukkitLoc = new org.bukkit.Location(world, location.getX(), location.getY(), location.getZ());
            if (Settings.Debug) {
                world.spawnParticle(Particle.ANGRY_VILLAGER, bukkitLoc, 1);
            }
            Material test = bukkitLoc.getBlock().getType();
            originalBlocks.put(location, test);

            if (getWorldGuardPlugin() != null && !WorldGuardUtils.allowBuild(p, bukkitLoc)){
                p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "WorldGuard"));
                return false;
            }
            if (test.name().endsWith("AIR") || Settings.blocksToIgnore.contains(test)){
                continue;
            }
            if (!Settings.CheckFreeSpace){
                continue;
            }
            p.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Place - No free space") );
            return false;
        }
        StructureManager.getInstance().addStructureByPlayer(playerID, schematicName, originalBlocks);
        return true;
    }

    public void sendMessageToPlayer(UUID recipient, String message) {
        Bukkit.getPlayer(recipient).sendMessage(I18nSupport.getInternationalisedString(message));
    }

    public void clearInterior(Collection<Location> interior) {
        for (Location location : interior){
            org.bukkit.Location bukkitLoc = MathUtils.sb2BukkitLoc(location);
            //ignore air blocks
            if (bukkitLoc.getBlock().getType().name().endsWith("AIR")){
                continue;
            }
            bukkitLoc.getBlock().setType(Material.AIR, false);
        }
    }

    public void scheduleSyncTask(final Runnable runnable) {
        getServer().getScheduler().runTask(this, runnable);
    }

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
        List<?> materials = freeSpace.getList("Blocks to ignore");
        for (Object obj : materials) {
            Material type;
            if (obj instanceof String) {
                String str = (String) obj;
                type = Material.getMaterial(str.toUpperCase());
            }
            else
                continue;

            if (type == null)
                continue;
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
}
