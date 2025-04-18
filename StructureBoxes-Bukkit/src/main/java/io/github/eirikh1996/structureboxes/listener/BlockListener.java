package io.github.eirikh1996.structureboxes.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import io.github.eirikh1996.structureboxes.Direction;
import io.github.eirikh1996.structureboxes.Structure;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.StructureManager;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.ItemManager;
import io.github.eirikh1996.structureboxes.utils.MathUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;
import static io.github.eirikh1996.structureboxes.utils.RegionUtils.isWithinRegion;

public class BlockListener implements Listener {
    private final HashMap<UUID, Long> playerTimeMap = new HashMap<>();

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onBlockPlace(final BlockPlaceEvent event){
        if (event.isCancelled()) {
            return;
        }
        final UUID id = event.getPlayer().getUniqueId();
        if (!event.getBlockPlaced().getType().equals(Settings.StructureBoxItem) &&
        event.getItemInHand().getItemMeta() == null ||
        !event.getItemInHand().getItemMeta().hasLore()){
            return;
        }
        List<String> lore = event.getItemInHand().getItemMeta().getLore();
        assert lore != null;
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
        int expiry = -1;
        for (String entry : lore) {
            if (!entry.startsWith("Expires after:"))
                continue;
            expiry = Integer.parseInt(entry.split(":")[1].replace(" ", ""));
            break;
        }
        if (Settings.RequirePermissionPerStructureBox && !event.getPlayer().hasPermission("structureboxes.place." + schematicID)){
            event.getPlayer().sendMessage(String.format(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Place - No permission for this ID"), schematicID));
            return;
        }
        if (playerTimeMap.containsKey(id) && playerTimeMap.get(id) != null && (System.currentTimeMillis() - playerTimeMap.get(id)) < Settings.PlaceCooldownTime){
            event.getPlayer().sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Place - Cooldown"));
            return;
        }
        Clipboard clipboard = StructureBoxes.getInstance().getWorldEditHandler().loadClipboardFromSchematic(new BukkitWorld(event.getBlockPlaced().getWorld()), schematicID);
        if (clipboard == null && schematicID.endsWith("_#")){
            final String start = schematicID.replace("#", "");
            File schemDir = StructureBoxes.getInstance().getWorldEditHandler().getSchemDir();
            final String[] foundFiles = schemDir.list( (file, name) ->
                    (name.endsWith(".schematic") || name.endsWith(".schem")) &&
                    name.startsWith(start) &&
                            isInteger(name.replace(start, "").replace(".schematic", "").replace(".schem", ""))
            );
            if (foundFiles.length == 0)
                return;
            final Random random = new Random();
            String schemID = foundFiles[random.nextInt(foundFiles.length)].replace(".schematic", "").replace(".schem", "");
            clipboard = StructureBoxes.getInstance().getWorldEditHandler().loadClipboardFromSchematic(new BukkitWorld(event.getBlockPlaced().getWorld()), schemID);
        }
        if (clipboard == null){
            return;
        }
        final Location placed = event.getBlockPlaced().getLocation();
        Direction clipboardDir = StructureBoxes.getInstance().getWorldEditHandler().getClipboardFacingFromOrigin(clipboard, BukkitAdapter.adapt(placed));
        Direction playerDir = Direction.fromYaw(event.getPlayer().getLocation().getYaw());
        int angle = playerDir.getAngle(clipboardDir);
        final Location loc = event.getBlockPlaced().getLocation();
        boolean exemptFromRegionRestriction = false;
        if (!Settings.RestrictToRegionsExceptions.isEmpty()){
            for (String exception : Settings.RestrictToRegionsExceptions){
                if (exception == null){
                    continue;
                }
                if (ChatColor.stripColor(lore.get(0)).toLowerCase().contains(exception.toLowerCase())){
                    exemptFromRegionRestriction = true;
                    break;
                }

            }
        }
        if (Settings.Debug){
            Bukkit.broadcast(Component.text("Restrict to regions: " + Settings.RestrictToRegionsEnabled + " Outside region: " + !isWithinRegion(placed) + " Not Exempt: " + !exemptFromRegionRestriction + " unable to bypass : " + !event.getPlayer().hasPermission("structureboxes.bypassregionrestriction")));
        }

        if (Settings.RestrictToRegionsEnabled && !isWithinRegion(placed) && !exemptFromRegionRestriction && !event.getPlayer().hasPermission("structureboxes.bypassregionrestriction")){
            event.getPlayer().sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Place - Must be within region"));
            event.setCancelled(true);
            return;
        }
        ItemManager.getInstance().addItem(event.getPlayer().getUniqueId(), event.getItemInHand());
        if (Settings.Debug){
            Bukkit.broadcast(Component.text("Player direction: " + playerDir.name() + " Structure direction: " + clipboardDir.name()));
        }

        if (!StructureBoxes.getInstance().getWorldEditHandler().pasteClipboard(event.getPlayer().getUniqueId(), schematicID, clipboard, angle, BukkitAdapter.adapt(placed))) {
            event.setCancelled(true);
            return;
        }
        StructureManager.getInstance().getLatestStructure(event.getPlayer().getUniqueId()).setExpiry(expiry);



                new BukkitRunnable() {
                    @Override
                    public void run() {
                        loc.getBlock().setType(Material.AIR);
                    }
                }.runTask(StructureBoxes.getInstance());
        playerTimeMap.put(id, System.currentTimeMillis());




    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Set<Structure> sessions = StructureManager.getInstance().getStructures();
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        Iterator<Structure> iter = sessions.iterator();
        while (iter.hasNext()) {
            Structure next = iter.next();
            if (!next.getStructure().contains(BukkitAdapter.adapt(event.getBlock().getLocation()))){
                continue;
            }
            iter.remove();
            event.getPlayer().sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Session - Expired due to block broken"));
            break;
        }
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
