package io.github.eirikh1996.structureboxes.commands;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.StructureManager;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.BlockUtils;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.eirikh1996.structureboxes.utils.MathUtils;
import javafx.util.Pair;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class StructureBoxCommand implements TabExecutor {
    private final String schematicDir;
    public StructureBoxCommand(){
        schematicDir = StructureBoxes.getInstance().getWorldEditPlugin().getConfig().getConfigurationSection("saving").getString("dir");
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!command.getName().equalsIgnoreCase("structurebox")){
            return false;
        }
        if (strings.length == 0){
            return false;
        }
        if (strings[0].equalsIgnoreCase("create")){
            String schematicName;
            try {
                schematicName = strings[1];
            } catch (ArrayIndexOutOfBoundsException e){
                schematicName = "";
            }
            return createStructureBox(commandSender, schematicName);
        } else if (strings[0].equalsIgnoreCase("undo")){
            return undoCommand(commandSender);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        ArrayList<String> subCmds = new ArrayList<>();
        if (strings.length == 0){
            return subCmds;
        }
        else if (strings.length <= 1) {
            if (commandSender.hasPermission("structureboxes.create")) {
                subCmds.add("create");
            }
            if (commandSender.hasPermission("structureboxes.undo")) {
                subCmds.add("undo");
            }
        }

        else if (strings[0].equalsIgnoreCase("create")){
            File schematicDir = new File(StructureBoxes.getInstance().getWorldEditPlugin().getDataFolder().getAbsolutePath() + "/" + this.schematicDir);
            if (!schematicDir.exists()){
                return Collections.emptyList();
            }
            for (File schem : schematicDir.listFiles()){
                if (schem == null){
                    continue;
                } else if (schem.getName().endsWith(".schematic")){
                    subCmds.add(schem.getName().replace(".schematic", ""));
                } else if (schem.getName().endsWith(".schem")){
                    subCmds.add(schem.getName().replace(".schem", ""));
                }
            }
        }
        ArrayList<String> completions = new ArrayList<>();
        for (String subCmd : subCmds){
            if (!subCmd.startsWith(strings[0])){
                continue;
            }
            completions.add(subCmd);
        }
        return completions;
    }

    private boolean createStructureBox(CommandSender sender, String schematicName){
        if (!(sender instanceof Player)){

            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("structureboxes.create")){
            return true;
        }
        if (schematicName == null){
            sender.sendMessage(I18nSupport.getInternationalisedString("Command - No permission"));
            return true;
        }
        File schematicFile = new File(StructureBoxes.getInstance().getWorldEditPlugin().getDataFolder().getAbsolutePath() + "/" + schematicDir + "/" + schematicName + ".schematic");

        if (!schematicFile.exists()){
            schematicFile = new File(StructureBoxes.getInstance().getWorldEditPlugin().getDataFolder().getAbsolutePath() + "/" + schematicDir + "/" + schematicName + ".schem");
        }
        if (!schematicFile.exists()){
            sender.sendMessage(I18nSupport.getInternationalisedString("Command - No schematic"));
            return true;
        }
        Clipboard clipboard = StructureBoxes.getInstance().getWorldEditHandler().loadClipboardFromSchematic(new BukkitWorld(((Player) sender).getWorld()), schematicName);
        if (StructureBoxes.getInstance().getWorldEditHandler().getStructureSize(clipboard) > Settings.MaxStructureSize) {
            sender.sendMessage(I18nSupport.getInternationalisedString("Command - Structure too large"));
            return true;
        }
        int emptySlot = player.getInventory().firstEmpty();
        if (emptySlot < 0){
            return true;
        }
        ItemStack structureBox = new ItemStack((Material) Settings.StructureBoxItem);
        List<String> lore = new ArrayList<>();
        ItemMeta meta = structureBox.getItemMeta();
        meta.setDisplayName(Settings.StructureBoxLore);
        lore.add(Settings.StructureBoxPrefix + schematicName);
        lore.add(ChatColor.AQUA + "Place structure box in a free space to spawn a structure");
        meta.setLore(lore);
        structureBox.setItemMeta(meta);
        player.getInventory().addItem(structureBox);
        player.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - New structure box created"));
        return true;
    }

    private boolean undoCommand(CommandSender sender){
        if (!(sender instanceof Player)){
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Must Be Player"));
            return true;
        }
        final Player p = (Player) sender;
        if (!p.hasPermission("structureboxes.undo")){
            sender.sendMessage(I18nSupport.getInternationalisedString("Command - No permission"));
            return true;
        }
        Pair<String, HashMap<Location, Object>> stringStructurePair = StructureManager.getInstance().getLatestStructure(p.getUniqueId());
        String schematicName = stringStructurePair.getKey();
        HashMap<Location, Object> locationMaterialHashMap = stringStructurePair.getValue();
        if (locationMaterialHashMap == null) {
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - latest session expired"));
            return true;
        }
        final ArrayList<Location> structure = new ArrayList<>(locationMaterialHashMap.keySet());
        StructureManager.getInstance().addStructure(structure);
        new BukkitRunnable() {
            @Override
            public void run() {

                for (Location location : locationMaterialHashMap.keySet()){
                    final Material origType = (Material) locationMaterialHashMap.get(location);
                    Block b = MathUtils.sb2BukkitLoc(location).getBlock();
                    if (b.getState() instanceof InventoryHolder){
                        InventoryHolder holder = (InventoryHolder) b.getState();
                        holder.getInventory().clear();
                    }
                    b.setType(origType);
                }
                StructureManager.getInstance().removeStructure(structure);
            }
        }.runTask(StructureBoxes.getInstance());

        ItemStack structureBox = new ItemStack((Material) Settings.StructureBoxItem);
        List<String> lore = new ArrayList<>();
        ItemMeta meta = structureBox.getItemMeta();
        meta.setDisplayName(Settings.StructureBoxLore);
        lore.add(Settings.StructureBoxPrefix + schematicName);
        lore.add(ChatColor.AQUA + "Place structure box in a free space to spawn a structure");
        meta.setLore(lore);
        structureBox.setItemMeta(meta);
        p.getInventory().addItem(structureBox);
        p.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Successful undo"));
        return true;


    }
}
