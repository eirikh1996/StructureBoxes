package io.github.eirikh1996.structureboxes.region;

import com.sk89q.worldguard.bukkit.listener.EventAbstractionListener;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.WorldGuardUtils;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

public class WorldGuardFlagManager implements EventExecutor {

    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event) throws EventException {
        if (!(listener instanceof EventAbstractionListener && event instanceof BlockPlaceEvent)) {
            return;
        }
        BlockPlaceEvent placeEvent = (BlockPlaceEvent) event;
        if (!placeEvent.isCancelled()) {
            return;
        }
        final ItemStack inHand = placeEvent.getItemInHand();
        if (!inHand.getType().equals(Settings.StructureBoxItem) || !inHand.hasItemMeta()) {
            return;
        }
        final ItemMeta meta = inHand.getItemMeta();
        if (!meta.getDisplayName().equals(Settings.StructureBoxLore)) {
            return;
        }
        final Block placed = placeEvent.getBlockPlaced();
        if (WorldGuardUtils.getRegions(placed.getLocation()).size() == 0 || !WorldGuardUtils.canPlaceStructureBox(placeEvent.getPlayer(), placed.getLocation())) {
            return;
        }
        placeEvent.setCancelled(false);
    }
}
