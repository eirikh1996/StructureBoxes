/*
    This file is part of Structure Boxes.

    Structure Boxes is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Structure Boxes is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Structure Boxes.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.eirikh1996.structureboxes.listener;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.sponge.SpongeAdapter;
import com.sk89q.worldedit.sponge.SpongeWorld;
import io.github.eirikh1996.structureboxes.*;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.RegionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class BlockListener {

    @Listener
    public void onBlockPlace(ChangeBlockEvent.All event, @Root ServerPlayer player) throws IOException {
        if (event.isCancelled()) {
            return;
        }
        final ItemStack itemInHand = player.itemInHand(HandTypes.MAIN_HAND);
        if (itemInHand == null || itemInHand.type() == ItemTypes.AIR.get()){
            return;
        }
        final Optional<List<Component>> optionalLore = itemInHand.get(Keys.LORE);
        if (!optionalLore.isPresent()){
            return;
        }
        final List<Component> lore = optionalLore.get();
        final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
        final String loreID = serializer.serialize(lore.get(0));
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
            player.sendMessage(serializer.deserialize(I18nSupport.getInternationalisedString("Place - No permission for this ID")));
            return;
        }
        final SpongeWorld world = (SpongeWorld) SpongeAdapter.adapt(player.world());
        final WorldEditHandler weHandler = StructureBoxes.getInstance().getWorldEditHandler();
        final Clipboard clipboard = weHandler.loadClipboardFromSchematic(world, schematicID);
        if (clipboard == null) {
            player.sendMessage(serializer.deserialize(I18nSupport.getInternationalisedString("Command - No schematic")));
            event.setCancelled(true);
            return;
        }
        BlockSnapshot snapshot = null;
        for (Transaction<BlockSnapshot> transaction : event.transactions()) {
            final BlockSnapshot finalReplacement = transaction.finalReplacement();
            if (!transaction.isValid() || !finalReplacement.state().type().equals(Settings.StructureBoxItem)) {
                continue;
            }
            snapshot = finalReplacement;
            break;
        }
        if (snapshot == null) {
            return;
        }

        final ServerLocation placed = snapshot.location().orElseThrow(RuntimeException::new);
        final Direction sDir = weHandler.getClipboardFacingFromOrigin(clipboard, SpongeAdapter.adapt(placed, Vector3d.ZERO));
        final Direction pDir = Direction.fromYaw((float) player.headRotation().get().y());
        final double angle = pDir.getAngle(sDir);
        boolean exemptFromRegionRestriction = false;
        if (!Settings.RestrictToRegionsExceptions.isEmpty()){
            for (String exception : Settings.RestrictToRegionsExceptions){
                if (exception == null){
                    continue;
                }
                if ((lore.get(0)).insertion().toLowerCase().contains(exception.toLowerCase())){
                    exemptFromRegionRestriction = true;
                    break;
                }

            }
        }
        if (Settings.RestrictToRegionsEnabled && !exemptFromRegionRestriction && !RegionUtils.isWithinRegion(placed) && !player.hasPermission("structureboxes.bypassregionrestriction")) {
            player.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Place - Must be within region")));
            event.setCancelled(true);
            return;
        }
        if (!weHandler.pasteClipboard(player.uniqueId(), schematicID, clipboard, angle, SpongeAdapter.adapt(placed, Vector3d.ZERO))) {
            event.setCancelled(true);
            return;
        }
        Sponge.server().scheduler().submit(Task.builder().execute(() -> {
            placed.setBlockType(BlockTypes.AIR.get());
        }).build());
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.All event, @Root Player player) {
        BlockSnapshot snapshot = null;
        for (Transaction<BlockSnapshot> transaction : event.transactions(Operations.BREAK.get()).toList()) {
            if (!transaction.isValid() || !transaction.finalReplacement().state().type().equals(Settings.StructureBoxItem)) {
                continue;
            }
            snapshot = transaction.finalReplacement();
        }
        if (snapshot == null || snapshot.location().isEmpty()) {
            return;
        }
        final Structure structure = StructureManager.getInstance().getStructureAt(SpongeAdapter.adapt(snapshot.location().get(), Vector3d.ZERO));
        if (structure == null) {
            return;
        }
        if (player == null) {
            return;
        }
        StructureManager.getInstance().removeStructure(structure);
        player.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Session - Expired due to block broken")));
    }
/*
    @Listener
    public void onBlockUpdate(NotifyNeighborBlockEvent event) {
        if (!(event.getSource() instanceof LocatableBlock)) {
            return;
        }
        LocatableBlock lb = (LocatableBlock) event.getSource();
        Structure structure = null;
        for (org.spongepowered.api.util.Direction dir : event.getNeighbors().keySet()) {
            Location<World> relative = lb.getLocation().getBlockRelative(dir);
            structure = StructureManager.getInstance().getStructureAt(MathUtils.spongeToSBLoc(relative));
            if (structure == null || !structure.isProcessing()) {
                continue;
            }
            break;
        }

        if (structure == null)
            structure = StructureManager.getInstance().getStructureAt(MathUtils.spongeToSBLoc(lb.getLocation()));
        if (structure == null || !structure.isProcessing())
            return;
        if (lb.getBlockState().getType().equals(BlockTypes.WALL_SIGN)) {
            StructureBoxes.getInstance().broadcast(event.getNeighbors().toString());
        }
        Set<org.spongepowered.api.util.Direction> directions = new HashSet<>(event.getNeighbors().keySet());
        for (org.spongepowered.api.util.Direction dir : directions) {
            event.getNeighbors().remove(dir);
        }



    }*/

}
