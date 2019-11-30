package io.github.eirikh1996.structureboxes.listener;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.sponge.SpongeWorld;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
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
        final String loreID = lore.get(0).toString();
        String schematicID = null;
        if (!loreID.startsWith(Settings.StructureBoxPrefix)){
            boolean hasAlternativePrefix = false;
            for (String alternativePrefix : Settings.AlternativePrefixes) {
                if (!loreID.startsWith(alternativePrefix)) {
                    continue;
                }
                schematicID = loreID.replace(alternativePrefix, "");
                hasAlternativePrefix = true;
            }
            if (!hasAlternativePrefix) {
                return;
            }

        } else {
             schematicID = loreID.replace(Settings.StructureBoxPrefix, "");
        }

        if (Settings.RequirePermissionPerStructureBox && !player.hasPermission("structureboxes.place." + schematicID)) {
            player.sendMessage(Text.of(I18nSupport.getInternationalisedString("Place - No permission for this ID")));
            return;
        }
        final SpongeWorld world = StructureBoxes.getInstance().getWorldEditPlugin().getWorld(player.getWorld());
        final Clipboard clipboard = StructureBoxes.getInstance().getWorldEditHandler().loadClipboardFromSchematic(world, schematicID);




    }
}
