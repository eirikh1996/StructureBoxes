package io.github.eirikh1996.structureboxes.region;

import com.massivecraft.factions.engine.EnginePermBuild;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.FactionsUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

public class RegionFlagManager implements EventExecutor {
    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event) {
        if (!(listener instanceof EnginePermBuild) || !(event instanceof BlockPlaceEvent)) {
            return;
        }
        Bukkit.broadcastMessage(String.valueOf(listener));
        BlockPlaceEvent placeEvent = (BlockPlaceEvent) event;
        if (!placeEvent.isCancelled()) {
            return;
        }
        Bukkit.broadcastMessage("Test");
        if (!FactionsUtils.canPlaceStructureBox(placeEvent.getBlockPlaced().getLocation())) {
            return;
        }

        if (!placeEvent.getBlockPlaced().getType().equals(Settings.StructureBoxItem)) {
            return;
        }
        final ItemStack placedItem = placeEvent.getItemInHand();
        if (!placedItem.hasItemMeta())
            return;
        final ItemMeta meta = placedItem.getItemMeta();
        assert meta != null;
        if (!meta.hasLore()) {
            return;
        }
        assert meta.getLore() != null;
        String schematicID = ChatColor.stripColor(meta.getLore().get(0));
        if (!schematicID.startsWith(ChatColor.stripColor(Settings.StructureBoxPrefix))){
            boolean hasAlternativePrefix = false;
            for (String prefix : Settings.AlternativePrefixes){
                if (!schematicID.startsWith(prefix)){
                    continue;
                }
                hasAlternativePrefix = true;
                break;
            }
            if (!hasAlternativePrefix){
                return;
            }
        }
        placeEvent.setCancelled(false);
    }
}
