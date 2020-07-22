package io.github.eirikh1996.structureboxes.region;

import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.StructureBoxFlag;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

public class PlotSquared5FlagManager implements EventExecutor {

    Class<?> playerEventsClass;
    public PlotSquared5FlagManager() {
        try {
            playerEventsClass = Class.forName("com.plotsquared.bukkit.listeners.BlockEventListener");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event) throws EventException {
        if (!(listener.getClass().isAssignableFrom(playerEventsClass) && event instanceof BlockPlaceEvent)) {
            return;
        }
        final BlockPlaceEvent pe = (BlockPlaceEvent) event;
        if (!pe.isCancelled()) {
            return;
        }
        final ItemStack inHand = pe.getItemInHand();
        if (!inHand.getType().equals(Settings.StructureBoxItem))
            return;
        if (!inHand.hasItemMeta()) {
            return;
        }
        final ItemMeta meta = inHand.getItemMeta();
        if (!Settings.StructureBoxLore.equals(meta.getDisplayName())) {
            return;
        }
        final Block placed = pe.getBlockPlaced();
        final Plot plot = Plot.getPlot(new Location(placed.getWorld().getName(), placed.getX(), placed.getY(), placed.getZ()));
        if (plot == null) {
            return;
        }
        final boolean value = plot.getFlag(StructureBoxFlag.STRUCTUREBOX_FLAG_FALSE);
        if (!value) {
            return;
        }
        pe.setCancelled(false);
    }
}
