package io.github.eirikh1996.structureboxes.listener;

import io.github.eirikh1996.structureboxes.settings.Settings;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.List;

public class BlockListener {

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event, @Root Player player){
        if (!player.getItemInHand(HandTypes.MAIN_HAND).isPresent()){
            return;
        }
        final ItemStack itemInHand = player.getItemInHand(HandTypes.MAIN_HAND).get();
        if (!itemInHand.get(Keys.ITEM_LORE).isPresent()){
            return;
        }
        final List<Text> lore = itemInHand.get(Keys.ITEM_LORE).get();
        if (!lore.get(0).toString().startsWith(Settings.StructureBoxPrefix)){
            return;
        }

    }
}
