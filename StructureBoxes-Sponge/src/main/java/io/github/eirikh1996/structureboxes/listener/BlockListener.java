package io.github.eirikh1996.structureboxes.listener;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.sponge.SpongeWorld;
import io.github.eirikh1996.structureboxes.Direction;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.WorldEditHandler;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.IWorldEditLocation;
import io.github.eirikh1996.structureboxes.utils.MathUtils;
import io.github.eirikh1996.structureboxes.utils.RegionUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class BlockListener {

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event, @Root Player player){
        if (event.isCancelled()) {
            return;
        }
        if (!player.getItemInHand(HandTypes.MAIN_HAND).isPresent()){
            return;
        }
        final ItemStack itemInHand = player.getItemInHand(HandTypes.MAIN_HAND).get();
        if (!itemInHand.get(Keys.ITEM_LORE).isPresent()){
            return;
        }
        final List<Text> lore = itemInHand.get(Keys.ITEM_LORE).get();
        final String loreID = lore.get(0).toString();
        String schematicID = "";
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
        final WorldEditHandler weHandler = StructureBoxes.getInstance().getWorldEditHandler();
        final Clipboard clipboard = weHandler.loadClipboardFromSchematic(world, schematicID);
        if (clipboard == null) {
            player.sendMessage(Text.of(I18nSupport.getInternationalisedString("Command - No schematic")));
            event.setCancelled(true);
            return;
        }
        BlockSnapshot snapshot = null;
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (!transaction.isValid() || !transaction.getFinal().getExtendedState().getType().equals(Settings.StructureBoxItem)) {
                continue;
            }
            snapshot = transaction.getDefault();
        }
        if (snapshot == null) {
            return;
        }

        final Location<World> placed = snapshot.getLocation().get();
        final Direction sDir = weHandler.getClipboardFacingFromOrigin(clipboard, MathUtils.spongeToSBLoc(placed));
        final Direction pDir = Direction.fromYaw((float) player.getHeadRotation().getY());
        final double angle = pDir.getAngle(sDir);
        boolean exemptFromRegionRestriction = false;
        if (!Settings.RestrictToRegionsExceptions.isEmpty()){
            for (String exception : Settings.RestrictToRegionsExceptions){
                if (exception == null){
                    continue;
                }
                if (lore.get(0).toPlain().toLowerCase().contains(exception.toLowerCase())){
                    exemptFromRegionRestriction = true;
                    break;
                }

            }
        }
        if (Settings.RestrictToRegionsEnabled && !exemptFromRegionRestriction && !RegionUtils.isWithinRegion(placed) && !player.hasPermission("structureboxes.bypassregionrestriction")) {
            player.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Place - Must be within region")));
            event.setCancelled(true);
            return;
        }
        if (weHandler.pasteClipboard(player.getUniqueId(), schematicID, clipboard, angle, new IWorldEditLocation(placed))) {
            return;
        }
        Runnable runnable = () -> {
            placed.setBlockType(BlockTypes.AIR);
        };
        StructureBoxes.getInstance().scheduleSyncTask(runnable);
    }
}
