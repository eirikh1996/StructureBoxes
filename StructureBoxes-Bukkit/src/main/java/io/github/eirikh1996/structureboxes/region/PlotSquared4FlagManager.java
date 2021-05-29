package io.github.eirikh1996.structureboxes.region;

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

public class PlotSquared4FlagManager implements EventExecutor {

    Class<?> playerEventsClass;
    public PlotSquared4FlagManager() {
        try {
            playerEventsClass = Class.forName("com.github.intellectualsites.plotsquared.bukkit.listeners.PlayerEvents");
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
        if (!RegionUtils.canPlaceStructure(pe.getPlayer(), placed.getLocation()))
            return;
        pe.setCancelled(false);
    }
}
