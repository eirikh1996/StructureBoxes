package io.github.eirikh1996.structureboxes.utils;


import com.intellectualcrafters.plot.IPlotMain;
import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Set;

public class PlotSquaredUtils {
    public static boolean canBuild(Player player, org.bukkit.Location location){
        final IPlotMain ps = (IPlotMain) Bukkit.getServer().getPluginManager().getPlugin("PlotSquared");
        final File worldsFile = new File(ps.getDirectory(), "config/worlds.yml");
        Yaml yaml = new Yaml();
        final Map data;
        try {
            data = yaml.load(new FileInputStream(worldsFile));
        } catch (FileNotFoundException e) {
            throw new PlotSquaredWorldsConfigException("Something went wrong when loading PlotSquared worlds file", e);
        }
        final Map<String, Object> worlds = (Map<String, Object>) data.get("worlds");
        if (worlds == null || !worlds.containsKey(location.getWorld().getName())){
            return true;
        }

        final PlotAPI plotAPI = new PlotAPI();

        Plot plot = plotAPI.getPlot(location);
        if (plot == null){
            return false;
        }
        return plot.isAdded(player.getUniqueId());
    }

    public static boolean withinPlot(org.bukkit.Location location){
        final IPlotMain ps = (IPlotMain) Bukkit.getServer().getPluginManager().getPlugin("PlotSquared");
        final File worldsFile = new File(ps.getDirectory(), "config/worlds.yml");
        Yaml yaml = new Yaml();
        final Map data;
        try {
            data = yaml.load(new FileInputStream(worldsFile));
        } catch (FileNotFoundException e) {
            throw new PlotSquaredWorldsConfigException("Something went wrong when loading PlotSquared worlds file", e);
        }
        final Map<String, Object> worlds = (Map<String, Object>) data.get("worlds");
        if (worlds == null || !worlds.containsKey(location.getWorld().getName())){
            return false;
        }
        final PlotAPI plotAPI = new PlotAPI();
        Plot plot = plotAPI.getPlot(location);
        return plot != null;
    }

    public static boolean isPlotSquared(Plugin plugin){
        return plugin instanceof IPlotMain;
    }

    private static com.intellectualcrafters.plot.object.Location bukkitToPSLoc(Location location){
        return new com.intellectualcrafters.plot.object.Location(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private static class PlotSquaredWorldsConfigException extends RuntimeException {
        public PlotSquaredWorldsConfigException(String message, Throwable cause){
            super(message, cause);
        }
    }
}
