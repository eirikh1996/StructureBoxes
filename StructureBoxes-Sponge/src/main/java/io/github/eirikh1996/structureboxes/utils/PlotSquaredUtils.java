package io.github.eirikh1996.structureboxes.utils;


import com.intellectualcrafters.plot.IPlotMain;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class PlotSquaredUtils {
    private static Map<String, Object> worlds;

    private PlotSquaredUtils() {

    }
    public static void initialize(){
        final IPlotMain ps = (IPlotMain) Sponge.getPluginManager().getPlugin("plotsquared").get();
        final File worldsFile = new File(ps.getDirectory(), "config/worlds.yml");
        if (!worldsFile.exists()) {
            return;
        }
        Yaml yaml = new Yaml();
        final Map data;
        try {
            data = (Map) yaml.load(new FileInputStream(worldsFile));
        } catch (FileNotFoundException e) {
            throw new PlotSquaredWorldsConfigException("Something went wrong when loading PlotSquared worlds file", e);
        }
        worlds = (Map<String, Object>) data.getOrDefault("worlds", Collections.emptyMap());
    }

    public static boolean canBuild(Player player, org.spongepowered.api.world.Location<World> location){

        if (worlds == null || !worlds.containsKey(location.getExtent().getName())){
            return true;
        }
        final PS ps = PS.get();
        final com.intellectualcrafters.plot.object.Location psLoc = new com.intellectualcrafters.plot.object.Location(location.getExtent().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        PlotArea pArea = ps.getApplicablePlotArea(psLoc);
        Plot plot = pArea.getPlot(psLoc);
        if (plot == null){
            return false;
        }
        return plot.isAdded(player.getUniqueId());
    }

    public static boolean withinPlot(org.spongepowered.api.world.Location<World> location){
        if (worlds == null || !worlds.containsKey(location.getExtent().getName())){
            return false;
        }
        final PS ps = PS.get();
        final com.intellectualcrafters.plot.object.Location psLoc = new com.intellectualcrafters.plot.object.Location(location.getExtent().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        PlotArea pArea = ps.getApplicablePlotArea(psLoc);
        Plot plot = pArea.getPlot(psLoc);
        return plot != null;
    }

    public static boolean isPlotSquared(Optional plugin){
        return plugin.isPresent() && plugin.get() instanceof IPlotMain;
    }


    private static class PlotSquaredWorldsConfigException extends RuntimeException {
        public PlotSquaredWorldsConfigException(String message, Throwable cause){
            super(message, cause);
        }
    }
}
