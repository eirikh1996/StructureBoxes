package io.github.eirikh1996.structureboxes.utils;


import com.intellectualcrafters.plot.IPlotMain;
import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.flag.BooleanFlag;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.Plot;
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

public class PlotSquaredUtils {
    private static Map<String, Object> worlds;
    private static IPlotMain ps;
    public static final Flag STRUCTUREBOX_FLAG = new BooleanFlag("structurebox");
    private PlotSquaredUtils() {

    }
    public static void initialize(){
        ps = (IPlotMain) Bukkit.getServer().getPluginManager().getPlugin("PlotSquared");
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
        worlds = (Map<String, Object>) data.getOrDefault("worlds", Collections.emptyMap());
    }

    public static boolean canBuild(Player player, org.bukkit.Location location){

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

    public static void registerFlag() {
        new PlotAPI().addFlag(STRUCTUREBOX_FLAG);
    }

    public static boolean canPlaceStructureBox(Location loc) {
        final PlotAPI plotAPI = new PlotAPI();
        Plot plot = plotAPI.getPlot(loc);
        return (boolean) plot.getFlag(STRUCTUREBOX_FLAG).get();
    }

    private static class PlotSquaredWorldsConfigException extends RuntimeException {
        public PlotSquaredWorldsConfigException(String message, Throwable cause){
            super(message, cause);
        }
    }
}
