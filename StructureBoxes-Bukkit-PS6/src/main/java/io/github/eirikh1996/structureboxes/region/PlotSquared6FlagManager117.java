package io.github.eirikh1996.structureboxes.region;

import com.plotsquared.bukkit.listener.BlockEventListener117;
import io.github.eirikh1996.structureboxes.settings.Settings;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PlotSquared6FlagManager117 implements EventExecutor {

    Class<?> regionUtilsClass;
    Method canPlaceStructureMethod;
    public PlotSquared6FlagManager117() {
        try {
            regionUtilsClass = Class.forName("io.github.eirikh1996.structureboxes.utils.RegionUtils");
            canPlaceStructureMethod = regionUtilsClass.getMethod("canPlaceStructure", Player.class, Location.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event) throws EventException {
        if (!(listener instanceof BlockEventListener117 && event instanceof BlockPlaceEvent)) {
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
        boolean result;
        try {
            result = (boolean) canPlaceStructureMethod.invoke(null, pe.getPlayer(), placed.getLocation());
        } catch (IllegalAccessException | InvocationTargetException e) {
            return;
        }
        if (!result)
            return;
        pe.setCancelled(false);
    }
}
