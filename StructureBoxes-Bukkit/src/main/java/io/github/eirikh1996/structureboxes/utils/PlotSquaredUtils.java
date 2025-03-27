package io.github.eirikh1996.structureboxes.utils;



import com.plotsquared.bukkit.generator.BukkitPlotGenerator;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.FlagContainer;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;

public class PlotSquaredUtils {
    private static Map<String, Object> worlds;
    public static final StructureboxFlag STRUCTUREBOX_FLAG = new StructureboxFlag();
    private PlotSquaredUtils() {

    }

    public static boolean canBuild(Player player, org.bukkit.Location location){

        if (!(location.getWorld().getGenerator() instanceof BukkitPlotGenerator)){
            return true;
        }

        final PlotAPI plotAPI = new PlotAPI();

        Plot plot = Plot.getPlot(bukkitToPSLoc(location));
        if (plot == null){
            return false;
        }
        return plot.isAdded(player.getUniqueId()) || plot.getFlag(STRUCTUREBOX_FLAG);
    }

    public static boolean withinPlot(Location location){
        if (worlds == null || !worlds.containsKey(location.getWorld().getName())){
            return false;
        }
        Plot plot = Plot.getPlot(bukkitToPSLoc(location));
        return plot != null;
    }

    private static com.plotsquared.core.location.Location bukkitToPSLoc(Location location){
        return com.plotsquared.core.location.Location.at(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static boolean canPlaceStructureBox(Location loc) {
        Plot plot = Plot.getPlot(bukkitToPSLoc(loc));
        return plot.getFlag(STRUCTUREBOX_FLAG).booleanValue();
    }

    private static class PlotSquaredWorldsConfigException extends RuntimeException {
        public PlotSquaredWorldsConfigException(String message, Throwable cause){
            super(message, cause);
        }
    }
}
