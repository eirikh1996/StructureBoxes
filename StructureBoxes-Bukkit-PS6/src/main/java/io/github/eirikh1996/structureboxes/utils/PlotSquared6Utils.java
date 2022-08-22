package io.github.eirikh1996.structureboxes.utils;

import com.plotsquared.bukkit.BukkitPlatform;
import com.plotsquared.bukkit.generator.BukkitPlotGenerator;
import com.plotsquared.bukkit.listener.BlockEventListener;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlotSquared6Utils {
    private static BukkitPlatform platform;

    public static void initialize() {
        final BukkitPlatform ps = (BukkitPlatform) Bukkit.getServer().getPluginManager().getPlugin("PlotSquared");
        final File worldsFile = new File(ps.getDirectory(), "config/worlds.yml");
        if (!worldsFile.exists()) {
            return;
        }
        Yaml yaml = new Yaml();
        final Map data;
        try {
            data = yaml.load(new FileInputStream(worldsFile));
        } catch (FileNotFoundException e) {
            throw new PlotSquaredWorldsConfigException("Something went wrong when loading PlotSquared worlds file", e);
        }
    }

    public static void registerFlag() {
        GlobalFlagContainer.getInstance().addFlag(StructureboxFlagV6.STRUCTUREBOX_FLAG_FALSE);
    }

    public static boolean canPlaceStructureBox(Location loc) {
        final PlotAPI plotAPI = new PlotAPI();
        Set<PlotArea> plotAreas = plotAPI.getPlotAreas(loc.getWorld().getName());
        Plot plot = null;
        for (final PlotArea pArea : plotAreas){
            plot = pArea.getPlot(bukkitToPSLoc(loc));
            if (plot != null){
                break;
            }
        }
        return plot != null && plot.getFlag(StructureboxFlagV6.class);
    }

    public static boolean canBuild(Player player, Location location){
        final World plotWorld = location.getWorld();
        if (plotWorld == null || !(plotWorld.getGenerator() instanceof BukkitPlotGenerator)){
            return true;
        }
        final PlotAPI plotAPI = new PlotAPI();
        Set<PlotArea> plotAreas = plotAPI.getPlotAreas(plotWorld.getName());
        Plot plot = null;
        for (final PlotArea pArea : plotAreas){
            plot = pArea.getPlot(bukkitToPSLoc(location));
            if (plot != null){
                break;
            }
        }
        if (plot == null){
            return false;
        }
        return plot.isAdded(player.getUniqueId()) || plot.getFlag(StructureboxFlagV6.class);
    }

    public static boolean withinPlot(Location location){
        final World plotWorld = location.getWorld();
        if (plotWorld == null || !(plotWorld.getGenerator() instanceof BukkitPlotGenerator)){
            return true;
        }
        final PlotAPI plotAPI = new PlotAPI();
        Set<PlotArea> plotAreas = plotAPI.getPlotAreas(plotWorld.getName());
        Plot plot = null;
        for (final PlotArea pArea : plotAreas){
            plot = pArea.getPlot(bukkitToPSLoc(location));
            if (plot != null){
                break;
            }
        }
        return plot != null;
    }

    public static Listener getBlockListener() {
        return platform.injector().getInstance(BlockEventListener.class);
    }

    public static boolean isPlotSquared(Plugin plugin){
        boolean result = plugin instanceof BukkitPlatform;
        if (!result)
            return false;
        platform = (BukkitPlatform) plugin;
        return true;
    }

    private static com.plotsquared.core.location.Location bukkitToPSLoc(Location location){
        return com.plotsquared.core.location.Location.at(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private static class PlotSquaredWorldsConfigException extends RuntimeException {
        public PlotSquaredWorldsConfigException(String message, Throwable cause){
            super(message, cause);
        }
    }
}
