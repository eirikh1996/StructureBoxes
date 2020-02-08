package io.github.eirikh1996.structureboxes.listener;

import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.event.EventFactionsChunksChange;
import io.github.eirikh1996.structureboxes.settings.Settings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class FactionsListener implements Listener {

    @EventHandler
    public void onChunkChange(EventFactionsChunksChange event) {
        final MPlayer mp = event.getMPlayer();
        final Player player = mp.getPlayer();
        ItemStack itemInHand = player.getItemInHand();
        if (!itemInHand.hasItemMeta()) {
            return;
        }
        ItemMeta meta = itemInHand.getItemMeta();
        if (!meta.hasLore()) {
            return;
        }
        final List<String> lore = meta.getLore();
        if (!lore.get(0).startsWith(Settings.StructureBoxPrefix)) {
            return;
        }


    }
}
