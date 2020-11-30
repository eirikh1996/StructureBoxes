package io.github.eirikh1996.structureboxes.utils;

import com.github.intellectualsites.plotsquared.api.PlotAPI;
import com.github.intellectualsites.plotsquared.plot.IPlotMain;
import com.github.intellectualsites.plotsquared.plot.flag.BooleanFlag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlotSquared4Utils {
    private static Map<String, Object> worlds;
    public static BooleanFlag StructureBoxFlag;

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
            throw new PlotSquaredWorldsConfigException("Something went wrong when loading PlotSquared worlds file", e);
        }
        worlds = data == null ? new HashMap<>() : (Map<String, Object>) data.getOrDefault("worlds", Collections.emptyMap());
        StructureBoxFlag = new BooleanFlag("structurebox");
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
        return plot.isAdded(player.getUniqueId()) || plot.getFlag(StructureBoxFlag, false);
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

    public static void registerFlag() {
        new PlotAPI().addFlag(StructureBoxFlag);
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
        if (plot == null) {
            return true;
        }
        return plot.getFlag(StructureBoxFlag).get();
    }

    private static com.github.intellectualsites.plotsquared.plot.object.Location bukkitToPSLoc(Location location){
        return new com.github.intellectualsites.plotsquared.plot.object.Location(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private static class PlotSquaredWorldsConfigException extends RuntimeException {
        public PlotSquaredWorldsConfigException(String message, Throwable cause){
            super(message, cause);
        }
    }
}
