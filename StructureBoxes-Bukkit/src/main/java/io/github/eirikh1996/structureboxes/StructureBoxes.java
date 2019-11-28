package io.github.eirikh1996.structureboxes;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import com.massivecraft.factions.Factions;
import com.palmergames.bukkit.towny.Towny;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import io.github.eirikh1996.structureboxes.commands.StructureBoxCommand;
import io.github.eirikh1996.structureboxes.listener.BlockListener;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.*;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.zombie_striker.landclaiming.LandClaiming;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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
    private boolean plotSquaredInstalled = false;
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
    }

    @Override
    public void onEnable() {
        String packageName = getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf(".") + 1);
        Settings.IsLegacy = Integer.parseInt(version.split("_")[1]) <= 12;
        final String[] LOCALES = {"en", "no", "it"};
        for (String locale : LOCALES){
            final File langFile = new File(getDataFolder().getAbsolutePath() + "/localisation/lang_" + locale + ".properties");
            if (langFile.exists()){
                continue;
            }
            saveResource("localisation/lang_" + locale + ".properties", false);
        }


        if (Settings.IsLegacy){
            saveLegacyConfig();
        } else {
            saveDefaultConfig();
        }
        Settings.locale = getConfig().getString("Locale", "en");
        Settings.Metrics = getConfig().getBoolean("Metrics", true);
        Settings.PlaceCooldownTime = getConfig().getLong("Place Cooldown Time", 10);
        Settings.StructureBoxItem = Material.getMaterial(getConfig().getString("Structure Box Item").toUpperCase());
        Settings.StructureBoxLore = getConfig().getString("Structure Box Display Name");
        Object object = getConfig().get("Structure Box Instruction Message");
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
        if (!I18nSupport.initialize(getDataFolder())){
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

        String weVersion = worldEditPlugin.getDescription().getVersion();
        int index = weVersion.indexOf(".");

        int versionNumber = Integer.parseInt(weVersion.substring(0, index));
        final Map data;
        try {
            File weConfig = new File(getWorldEditPlugin().getDataFolder(), "config.yml");
            Yaml yaml = new Yaml();
            data = yaml.load(new FileInputStream(weConfig));
        } catch (IOException e){
            getLogger().severe(I18nSupport.getInternationalisedString("Startup - Error reading WE config"));
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        File schemDir = new File(getWorldEditPlugin().getDataFolder(), (String) ((Map) data.get("saving")).get("dir"));

        //Check if there is a compatible version of WorldEdit
        try {
            final Class weHandler = Class.forName("io.github.eirikh1996.structureboxes.compat.we" + versionNumber + ".IWorldEditHandler");
            if (WorldEditHandler.class.isAssignableFrom(weHandler)){
                worldEditHandler = (WorldEditHandler) weHandler.getConstructor(File.class, SBMain.class).newInstance(schemDir , this);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            getLogger().severe(I18nSupport.getInternationalisedString("Startup - Unsupported WorldEdit"));
            getLogger().severe(String.format(I18nSupport.getInternationalisedString("Startup - Requires WorldEdit 6.0.0 or 7.0.0"), weVersion));
            getLogger().severe(I18nSupport.getInternationalisedString("Startup - Will be disabled"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Plugin wg = getServer().getPluginManager().getPlugin("WorldGuard");
        //Check for WorldGuard
        if (wg instanceof WorldGuardPlugin){
            getLogger().info(I18nSupport.getInternationalisedString("Startup - WorldGuard detected"));
            worldGuardPlugin = (WorldGuardPlugin) wg;
        }
        Plugin f = getServer().getPluginManager().getPlugin("Factions");
        //Check for Factions
        if (f instanceof Factions){
            getLogger().info(I18nSupport.getInternationalisedString("Startup - Factions detected"));
            factionsPlugin = (Factions) f;
        }
        //Check for RedProtect
        Plugin rp = getServer().getPluginManager().getPlugin("RedProtect");
        if (rp instanceof RedProtect){
            getLogger().info(I18nSupport.getInternationalisedString("Startup - RedProtect detected"));
            redProtectPlugin = (RedProtect) rp;
        }
        //Check for GriefPrevention
        Plugin gp = getServer().getPluginManager().getPlugin("GriefPrevention");
        if (gp instanceof GriefPrevention){
            getLogger().info(I18nSupport.getInternationalisedString("Startup - GriefPrevention detected"));
            griefPreventionPlugin = (GriefPrevention) gp;
        }
        //Check for PlotSquared
        Plugin ps = getServer().getPluginManager().getPlugin("PlotSquared");
        if (Settings.IsLegacy ? PlotSquaredUtils.isPlotSquared(ps) : PlotSquared4Utils.isPlotSquared(ps)){
            getLogger().info(I18nSupport.getInternationalisedString("Startup - PlotSquared detected"));
            if (Settings.IsLegacy) {
                PlotSquaredUtils.initialize();
            } else {
                PlotSquared4Utils.initialize();
            }
            plotSquaredInstalled = true;
        }
        //Check for landClaiming
        Plugin lp = getServer().getPluginManager().getPlugin("LandClaiming");
        if (lp instanceof LandClaiming){
            getLogger().info(I18nSupport.getInternationalisedString("Startup - LandClaiming detected"));
            landClaimingPlugin = (LandClaiming) lp;
        }
        //Check for Towny
        Plugin tp = getServer().getPluginManager().getPlugin("Towny");
        if (tp instanceof Towny){
            getLogger().info(I18nSupport.getInternationalisedString("Startup - Towny detected"));
            townyPlugin = (Towny) tp;
        }
        //If no compatible protection plugin is found, disable region restriction if it is on
        if (Settings.RestrictToRegionsEnabled && worldGuardPlugin == null && factionsPlugin == null && griefPreventionPlugin == null && redProtectPlugin == null && !plotSquaredInstalled && landClaimingPlugin == null){
            getLogger().warning(I18nSupport.getInternationalisedString("Startup - Restrict to regions no compatible protection plugin"));
            Settings.RestrictToRegionsEnabled = false;
            getLogger().info(I18nSupport.getInternationalisedString("Startup - Restrict to regions set to false"));
        }
        if (Settings.Metrics) {
            metrics = new Metrics(this);
        }

        this.getCommand("structurebox").setExecutor(new StructureBoxCommand());

        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(UpdateChecker.getInstance(), this);
        if (startup){
            getServer().getScheduler().runTaskTimerAsynchronously(this, StructureManager.getInstance(), 0, 20);
            UpdateChecker.getInstance().runTaskTimerAsynchronously(this, 0, 10000);
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

    @Override
    public Platform getPlatform() {
        return Platform.BUKKIT;
    }

    @Override
    public boolean isFreeSpace(UUID playerID, String schematicName, Collection<Location> locations) {
        final HashMap<Location, Object> originalBlocks = new HashMap<>();
        @NotNull final Player p = getServer().getPlayer(playerID);
        for (Location location : locations){
            World world = getServer().getWorld(location.getWorld());
            org.bukkit.Location bukkitLoc = new org.bukkit.Location(world, location.getX(), location.getY(), location.getZ());
            if (Settings.Debug) {
                world.spawnParticle(Particle.VILLAGER_ANGRY, bukkitLoc, 1);
            }
            Material test = bukkitLoc.getBlock().getType();
            originalBlocks.put(location, test);

            if ((getRedProtectPlugin() != null && !RedProtectUtils.canBuild(p, bukkitLoc))){
                p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "RedProtect"));
                return false;
            }
            if (getGriefPreventionPlugin() != null && GriefPreventionUtils.canBuild(p, bukkitLoc)){
                p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "GriefPrevention"));
                return false;
            }
            if (getFactionsPlugin() != null && (Settings.IsLegacy ? !FactionsUtils.allowBuild(p, bukkitLoc) : !Factions3Utils.allowBuild(p, bukkitLoc))){
                p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "Factions"));
                return false;
            }
            if (getWorldGuardPlugin() != null && !WorldGuardUtils.allowBuild(p, bukkitLoc)){
                p.sendMessage(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Place - Forbidden Region"), "WorldGuard"));
                return false;
            }
            if (isPlotSquaredInstalled() && !(Settings.IsLegacy ? PlotSquaredUtils.canBuild(p, bukkitLoc) : PlotSquared4Utils.canBuild(p, bukkitLoc))){
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
    public void clearInterior(Collection<Location> interior) {
                for (Location location : interior){
                    org.bukkit.Location bukkitLoc = MathUtils.sb2BukkitLoc(location);
                    //ignore air blocks
                    if (bukkitLoc.getBlock().getType().name().endsWith("AIR")){
                        continue;
                    }
                    bukkitLoc.getBlock().setType(Material.AIR);
                }


    }

    @Override
    public void addItemToPlayerInventory(UUID id, Object item) {
        Bukkit.getPlayer(id).getInventory().addItem((ItemStack) item);
    }

    @Override
    public void scheduleSyncTask(final Runnable runnable) {
        getServer().getScheduler().runTask(this, runnable);
    }

    @Override
    public void scheduleAsyncTask(final Runnable runnable) {
        getServer().getScheduler().runTaskAsynchronously(this, runnable);
    }

    @Override
    public void broadcast(String s) {
        getServer().broadcastMessage(s);
    }

}
