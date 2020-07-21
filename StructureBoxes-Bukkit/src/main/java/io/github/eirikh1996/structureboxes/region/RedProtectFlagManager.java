package io.github.eirikh1996.structureboxes.region;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.listeners.BlockListener;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.settings.Settings;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

public class RedProtectFlagManager implements EventExecutor {
    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event) throws EventException {
        if (!(listener instanceof BlockListener && event instanceof BlockPlaceEvent)) {
            return;
        }
        final RedProtectAPI api = StructureBoxes.getInstance().getRedProtectPlugin().getAPI();
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
        final Region region = api.getRegion(pe.getBlockPlaced().getLocation());
        if (region == null || !region.getFlagBool("structurebox")) {
            return;
        }
        pe.setCancelled(false);
    }
}
