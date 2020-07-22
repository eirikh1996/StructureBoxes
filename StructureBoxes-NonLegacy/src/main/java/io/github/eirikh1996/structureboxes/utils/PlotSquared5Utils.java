package io.github.eirikh1996.structureboxes.utils;

import com.plotsquared.core.IPlotMain;
import com.plotsquared.core.api.PlotAPI;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class PlotSquared5Utils {
    private static Map<String, Object> worlds;

    public static void initialize() {
        final IPlotMain ps = (IPlotMain) Bukkit.getServer().getPluginManager().getPlugin("PlotSquared");
        final File worldsFile = new File(ps.getDirectory(), "config/worlds.yml");
        if (!worldsFile.exists()) {
            return;
        }
        Yaml yaml = new Yaml();
        final Map data;
        try {
            data = yaml.load(new FileInputStream(worldsFile));
        } catch (FileNotFoundException e) {
            throw new PlotSquared5Utils.PlotSquaredWorldsConfigException("Something went wrong when loading PlotSquared worlds file", e);
        }
        worlds = (Map<String, Object>) data.getOrDefault("worlds", Collections.emptyMap());
    }

    public static void registerFlag() {
        GlobalFlagContainer.getInstance().addFlag(StructureBoxFlag.STRUCTUREBOX_FLAG_FALSE);
    }

    public static boolean canBuild(Player player, Location location){

        if (worlds == null || !worlds.containsKey(location.getWorld().getName())){
            return true;
        }
        final PlotAPI plotAPI = new PlotAPI();
        Set<PlotArea> plotAreas = plotAPI.getPlotAreas(location.getWorld().getName());
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
        return plot.isAdded(player.getUniqueId());
    }

    public static boolean withinPlot(Location location){
        if (worlds == null || !worlds.containsKey(location.getWorld().getName())){
            return false;
        }
        final PlotAPI plotAPI = new PlotAPI();
        Set<PlotArea> plotAreas = plotAPI.getPlotAreas(location.getWorld().getName());
        Plot plot = null;
        for (final PlotArea pArea : plotAreas){
            plot = pArea.getPlot(bukkitToPSLoc(location));
            if (plot != null){
                break;
            }
        }
        return plot != null;
    }

    public static boolean isPlotSquared(Plugin plugin){
        return plugin instanceof IPlotMain;
    }

    private static com.plotsquared.core.location.Location bukkitToPSLoc(Location location){
        return new com.plotsquared.core.location.Location(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private static class PlotSquaredWorldsConfigException extends RuntimeException {
        public PlotSquaredWorldsConfigException(String message, Throwable cause){
            super(message, cause);
        }
    }
}
