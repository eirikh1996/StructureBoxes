package io.github.eirikh1996.structureboxes.region;

import com.massivecraft.factions.engine.EnginePermBuild;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.FactionsUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

public class FactionsFlagManager implements EventExecutor {
    private static FactionsFlagManager instance;

    private FactionsFlagManager() {}

    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event) {
        if (Settings.Debug) {
            Bukkit.broadcastMessage(listener.toString());
            Bukkit.broadcastMessage(event.toString());
        }
        if (!(listener instanceof EnginePermBuild)) {
            return;
        }
        if (!(event instanceof Cancellable) || !((Cancellable) event).isCancelled()) {
            return;
        }
        Cancellable can = (Cancellable) event;
        Block b;
        ItemStack handItem;
        boolean place = false;
        if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent placeEvent = (BlockPlaceEvent) event;
            b = placeEvent.getBlockPlaced();
            handItem = placeEvent.getItemInHand();
            place = true;
        } else if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;
            b = interactEvent.getClickedBlock();
            handItem = interactEvent.getItem();
        } else {
            return;
        }


        if (b == null || !FactionsUtils.canPlaceStructureBox(b.getLocation())) {
            return;
        }
        if (place ? !b.getType().equals(Settings.StructureBoxItem) : !handItem.getType().equals(Settings.StructureBoxItem)) {
            return;
        }
        if (!handItem.hasItemMeta())
            return;
        final ItemMeta meta = handItem.getItemMeta();
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
        can.setCancelled(false);
    }

    public static FactionsFlagManager getInstance() {
        if (instance == null)
            instance = new FactionsFlagManager();
        return instance;
    }
}
