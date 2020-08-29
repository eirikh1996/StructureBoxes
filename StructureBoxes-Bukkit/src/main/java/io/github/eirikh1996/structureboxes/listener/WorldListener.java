package io.github.eirikh1996.structureboxes.listener;


import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.PlotSquared4Utils;
import io.github.eirikh1996.structureboxes.utils.PlotSquared5Utils;
import io.github.eirikh1996.structureboxes.utils.PlotSquaredUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;

public class WorldListener implements Listener {

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        final ChunkGenerator generator = event.getWorld().getGenerator();
        if (!(generator instanceof GeneratorWrapper || generator instanceof com.github.intellectualsites.plotsquared.plot.generator.GeneratorWrapper || generator instanceof com.plotsquared.core.generator.GeneratorWrapper)) {
            return;
        }
        if (Settings.IsLegacy) {
            PlotSquaredUtils.initialize();
        } else if (Settings.UsePS5) {
            PlotSquared5Utils.initialize();
        } else {
            PlotSquared4Utils.initialize();
        }
    }
}
