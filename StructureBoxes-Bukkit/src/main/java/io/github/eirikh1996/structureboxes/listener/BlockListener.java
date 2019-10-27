package io.github.eirikh1996.structureboxes.listener;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.StructureManager;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.Direction;
import io.github.eirikh1996.structureboxes.utils.IWorldEditLocation;
import io.github.eirikh1996.structureboxes.utils.MathUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

import static io.github.eirikh1996.structureboxes.utils.RegionUtils.isWithinRegion;
import static java.lang.Math.abs;

public class BlockListener implements Listener {


    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event){
        if (event.isCancelled()){
            return;
        }
        if (!event.getBlockPlaced().getType().equals(Settings.StructureBoxItem) &&
        !event.getItemInHand().getItemMeta().hasLore()){
            return;
        }
        if (!event.getItemInHand().getItemMeta().getDisplayName().equals(Settings.StructureBoxLore) &&
                !Settings.AlternativeDisplayNames.contains(ChatColor.stripColor(event.getItemInHand().getItemMeta().getDisplayName()))){
            return;
        }
        List<String> lore = event.getItemInHand().getItemMeta().getLore();
        String schematicID = ChatColor.stripColor(lore.get(0));
        if (!schematicID.startsWith(ChatColor.stripColor(Settings.StructureBoxPrefix))){
            boolean hasAlternativePrefix = false;
            for (String prefix : Settings.AlternativePrefixes){
                if (!schematicID.startsWith(prefix)){
                    continue;
                }
                hasAlternativePrefix = true;
                schematicID = schematicID.replace(prefix, "");
                break;
            }
            if (!hasAlternativePrefix){
                return;
            }
        } else {
            schematicID = schematicID.replace(ChatColor.stripColor(Settings.StructureBoxPrefix), "");
        }
        if (Settings.RequirePermissionPerStructureBox && !event.getPlayer().hasPermission("structureboxes.place." + schematicID)){
            event.getPlayer().sendMessage(String.format(I18nSupport.getInternationalisedString("Place - No permission for this ID"), schematicID));
            return;
        }
        Clipboard clipboard = StructureBoxes.getInstance().getWorldEditHandler().loadClipboardFromSchematic(new BukkitWorld(event.getBlockPlaced().getWorld()), schematicID);
        if (clipboard == null){
            return;
        }
        final Location placed = event.getBlockPlaced().getLocation();
        Direction clipboardDir = StructureBoxes.getInstance().getWorldEditHandler().getClipboardFacingFromOrigin(clipboard, MathUtils.bukkit2SBLoc(placed));
        Direction playerDir = Direction.fromYaw(event.getPlayer().getLocation().getYaw());
        int angle = playerDir.getAngle(clipboardDir);
        final Location loc = event.getBlockPlaced().getLocation();
        boolean exemptFromRegionRestriction = false;
        if (!Settings.RestrictToRegionsExceptions.isEmpty()){
            for (String exception : Settings.RestrictToRegionsExceptions){
                if (!lore.get(0).contains(exception)){
                    continue;
                }
                exemptFromRegionRestriction = true;
                break;
            }
        }
        if (Settings.RestrictToRegionsEnabled && !isWithinRegion(placed) && !exemptFromRegionRestriction && !event.getPlayer().hasPermission("structureboxes.bypassregionrestriction")){
            I18nSupport.getInternationalisedString("Place - Must be within region");
            return;
        }
        if (Settings.Debug){
            Bukkit.broadcastMessage("Player direction: " + playerDir.name() + " Structure direction: " + clipboardDir.name());
        }
        final String schemID = schematicID;

                if (!StructureBoxes.getInstance().getWorldEditHandler().pasteClipboard(event.getPlayer().getUniqueId(), schemID, clipboard, angle, new IWorldEditLocation(placed))){

                    event.setCancelled(true);
                    return;
                }

                final UUID id = event.getPlayer().getUniqueId();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        StructureManager.getInstance().removeStructure(StructureBoxes.getInstance().getWorldEditHandler().getStructureByPlayer(id));
                        loc.getBlock().setType(Material.AIR);
                    }
                }.runTask(StructureBoxes.getInstance());





    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPhysics(BlockPhysicsEvent event){
        Block b = event.getBlock();
        io.github.eirikh1996.structureboxes.utils.Location structureLoc = new io.github.eirikh1996.structureboxes.utils.Location(b.getWorld().getUID(), b.getX(), b.getY(), b.getZ());
        if (!StructureManager.getInstance().isPartOfStructure(structureLoc)){
            return;
        }
        event.setCancelled(true);
    }

    private boolean requireWallSupport(Material type){
        return type.name().endsWith("WALL_SIGN") ||
                type.name().endsWith("TORCH") ||
                type == Material.LADDER ||
                type.name().equalsIgnoreCase("BED_BLOCK") ||
                type.name().endsWith("BED");
    }


}
