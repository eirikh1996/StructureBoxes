package io.github.eirikh1996.structureboxes.commands;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.settings.Settings;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
            return true;
        }
        if (strings[0].equalsIgnoreCase("create")){
            return createStructureBox(commandSender, strings[1]);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        ArrayList<String> subCmds = new ArrayList<>();
        if (commandSender.hasPermission("structureboxes.create")){
            subCmds.add("create");
        }
        if (strings.length == 0){
            return subCmds;
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
            sender.sendMessage("Error: You must specify a schematic id");
            return true;
        }
        String schemPath = StructureBoxes.getInstance().getWorldEditPlugin().getDataFolder().getAbsolutePath() + "/" + schematicDir + "/" + schematicName + ".schematic";
        File schematicFile = new File(schemPath);
        if (!schematicFile.exists()){
            sender.sendMessage("Error: No such schematic exists");
            return true;
        }
        int emptySlot = player.getInventory().firstEmpty();
        if (emptySlot < 0){
            return true;
        }
        ItemStack structureBox = new ItemStack(Settings.StructureBoxItem);
        List<String> lore = new ArrayList<>();
        ItemMeta meta = structureBox.getItemMeta();
        meta.setDisplayName(Settings.StructureBoxLore);
        lore.add(ChatColor.AQUA + schematicName);
        lore.add(ChatColor.AQUA + "Place structure box in a free space to spawn a structure");
        meta.setLore(lore);
        structureBox.setItemMeta(meta);
        player.getInventory().addItem(structureBox);
        return true;
    }
}
