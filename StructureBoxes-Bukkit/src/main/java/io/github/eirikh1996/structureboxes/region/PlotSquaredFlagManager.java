package io.github.eirikh1996.structureboxes.region;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.plotsquared.bukkit.listeners.PlayerEvents;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.RegionUtils;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

public class PlotSquaredFlagManager implements EventExecutor {
    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event) throws EventException {
        if (!(listener instanceof PlayerEvents && event instanceof BlockPlaceEvent)) {
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
        PlotAPI api = new PlotAPI();
        final Block placed = pe.getBlockPlaced();
        if (!RegionUtils.canPlaceStructure(pe.getPlayer(), placed.getLocation()))
            return;
        pe.setCancelled(false);
    }
}
