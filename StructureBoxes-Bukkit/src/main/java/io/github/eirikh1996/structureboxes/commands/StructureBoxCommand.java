package io.github.eirikh1996.structureboxes.commands;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import io.github.eirikh1996.structureboxes.Structure;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.StructureManager;
import io.github.eirikh1996.structureboxes.WorldEditHandler;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.TopicPaginator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
            PluginDescriptionFile desc = StructureBoxes.getInstance().getDescription();
            commandSender.sendMessage("§5 ==================[§6StructureBoxes§5]==================");
            commandSender.sendMessage("§6 Author: " + String.join(",", desc.getAuthors()));
            commandSender.sendMessage("§6 Version: v" + desc.getVersion());
            commandSender.sendMessage("§6 /sb create <schematic ID> [-m] - Creates new structure box");
            commandSender.sendMessage("§6 If using FAWE, -m will move schematic to global directory");
            commandSender.sendMessage("§6 /sb undo - Undoes the last undo session");
            commandSender.sendMessage("§6 /sb reload - Reloads the plugin");
            commandSender.sendMessage("§6 /sb sessions [player|-a|you] [page] - Shows active sessions");
            commandSender.sendMessage("§5 ========================================================");
            return true;
        }
        if (strings[0].equalsIgnoreCase("create")){
            String schematicName;
            try {
                schematicName = strings[1];
            } catch (ArrayIndexOutOfBoundsException e){
                schematicName = "";
            }
            boolean moveSchem = false;
            int expiry = -1;
            if (strings.length > 2) {
                for (int i = 2 ; i < strings.length ; i++) {
                    String arg = strings[i];
                    if (arg.equalsIgnoreCase("-m"))
                        moveSchem = true;
                    if (arg.equalsIgnoreCase("-e")) {
                        try {
                            expiry = Integer.parseInt(strings[i + 1]);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            commandSender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Invalid argument"));
                            return true;
                        } catch (NumberFormatException e) {
                            commandSender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Not a number"));
                            return true;
                        }
                    }
                }
            }

            return createStructureBox(commandSender, schematicName, moveSchem, expiry);
        } else if (strings[0].equalsIgnoreCase("undo")){
            return undoCommand(commandSender);
        } else if (strings[0].equalsIgnoreCase("reload")){
            return reloadCommand(commandSender);
        } else if (strings[0].equalsIgnoreCase("sessions")) {
            return sessionsCommand(commandSender, strings);
        }
        return false;
    }



    private boolean createStructureBox(CommandSender sender, @NotNull String schematicName, boolean moveSchem, int expiry){
        if (!(sender instanceof Player)){

            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("structureboxes.create")){
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - No permission"));
            return true;
        }
        File schematicFile = new File(StructureBoxes.getInstance().getWorldEditPlugin().getDataFolder().getAbsolutePath() + "/" + schematicDir + "/" + schematicName + ".schematic");

        if (!schematicFile.exists()){
            schematicFile = new File(StructureBoxes.getInstance().getWorldEditPlugin().getDataFolder().getAbsolutePath() + "/" + schematicDir + "/" + schematicName + ".schem");
        }
        File playerSchematicFile = new File(StructureBoxes.getInstance().getWorldEditPlugin().getDataFolder().getAbsolutePath() + "/" + schematicDir + "/" + player.getUniqueId() + "/" + schematicName + ".schematic");

        if (!playerSchematicFile.exists()) {
            playerSchematicFile = new File(StructureBoxes.getInstance().getWorldEditPlugin().getDataFolder().getAbsolutePath() + "/" + schematicDir + "/" + player.getUniqueId() + "/" + schematicName + ".schem");
        }
        boolean noSchematic = !schematicFile.exists() && !playerSchematicFile.exists();
        String[] foundRandomSchematics = null;
        if (schematicName.endsWith("_#"))  {
            final String start = schematicName.replace("_#", "");
            final File schemDir = new File(StructureBoxes.getInstance().getWorldEditPlugin().getDataFolder().getAbsolutePath() + "/" + schematicDir);
            final String[] foundFiles = schemDir.list(
                    (file, name) -> (name.endsWith(".schematic") || name.endsWith(".schem")) &&
                            name.startsWith(start) && isInteger(name.replace(start + "_", "").replace(".schematic", "").replace(".schem", "")));
            noSchematic = foundFiles == null || foundFiles.length == 0;
            foundRandomSchematics = foundFiles;
        }
        if (noSchematic){
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - No schematic"));
            return true;
        }
        if (moveSchem) {
            if (!player.hasPermission("structurebox.moveschematic")) {
                player.sendMessage(I18nSupport.getInternationalisedString("Command - No permission to move"));
                return true;
            }
            final File globalSchemDir = new File(StructureBoxes.getInstance().getWorldEditPlugin().getDataFolder().getAbsolutePath() + "/" + schematicDir);
            playerSchematicFile.renameTo(new File(globalSchemDir, playerSchematicFile.getName()));
        } else if (!schematicFile.exists() && (foundRandomSchematics == null || foundRandomSchematics.length == 0)){
            schematicName = player.getUniqueId() + "/" + schematicName;
        }
        Clipboard clipboard = StructureBoxes.getInstance().getWorldEditHandler().loadClipboardFromSchematic(new BukkitWorld(((Player) sender).getWorld()), schematicName);
        if (Settings.MaxStructureSize > -1) {
            if (foundRandomSchematics != null) {
                for (String name : foundRandomSchematics) {
                    clipboard = StructureBoxes.getInstance().getWorldEditHandler().loadClipboardFromSchematic(new BukkitWorld(((Player) sender).getWorld()), name.replace(".schematic", "").replace(".schem", ""));
                    if (StructureBoxes.getInstance().getWorldEditHandler().getStructureSize(clipboard) > Settings.MaxStructureSize) {
                        sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Structure too large") + " " + Settings.MaxStructureSize);
                        return true;
                    }
                }
            }
            else if (StructureBoxes.getInstance().getWorldEditHandler().getStructureSize(clipboard) > Settings.MaxStructureSize) {
                sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Structure too large") + " " + Settings.MaxStructureSize);
                return true;
            }

        }
        ItemStack structureBox = new ItemStack((Material) Settings.StructureBoxItem);
        List<String> lore = new ArrayList<>();
        ItemMeta meta = structureBox.getItemMeta();
        meta.setDisplayName(Settings.StructureBoxLore);
        lore.add(Settings.StructureBoxPrefix + schematicName);
        if (expiry > -1) {
            lore.add("Expires after: " + expiry);
        }
        lore.addAll(Settings.StructureBoxInstruction);
        meta.setLore(lore);
        structureBox.setItemMeta(meta);
        if (!player.getInventory().addItem(structureBox).isEmpty()) {
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Insufficient inventory space"));
            return true;
        }
        player.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - New structure box created"));
        return true;
    }

    private boolean undoCommand(CommandSender sender){
        final long start = System.currentTimeMillis();
        if (!(sender instanceof Player)){
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Must Be Player"));
            return true;
        }
        final Player p = (Player) sender;
        if (!p.hasPermission("structureboxes.undo")){
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - No permission"));
            return true;
        }
        final WorldEditHandler weHandler =  StructureBoxes.getInstance().getWorldEditHandler();
        Structure structure = StructureManager.getInstance().getLatestStructure(p.getUniqueId());
        if (Settings.IncrementalPlacement && structure.getIncrementalPlacementTask() != null) {
            structure.getIncrementalPlacementTask().cancel();
        }
        if (structure == null){
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - latest session expired"));
            return true;
        }
        if (!structure.isProcessing()) {
            structure.setProcessing(true);
        }
        String schematicName = structure.getSchematicName();




        ItemStack structureBox = new ItemStack((Material) Settings.StructureBoxItem);
        List<String> lore = new ArrayList<>();
        ItemMeta meta = structureBox.getItemMeta();
        meta.setDisplayName(Settings.StructureBoxLore);
            lore.add(Settings.StructureBoxPrefix + schematicName);
        if (!lore.containsAll(Settings.StructureBoxInstruction))
            lore.addAll(Settings.StructureBoxInstruction);
        meta.setLore(lore);
        structureBox.setItemMeta(meta);
        p.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Successful undo"));
        if (!p.getInventory().addItem(structureBox).isEmpty()){
            p.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Inventory - No space"));
            p.getWorld().dropItem(p.getLocation(), structureBox);
        }


        if (Settings.Debug){
            final long end = System.currentTimeMillis();
            Bukkit.broadcastMessage("Undo took (ms): " + (end - start));
        }

        return true;


    }


    private boolean reloadCommand(CommandSender sender){
        if (!sender.hasPermission("structureboxes.reload")){
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - No permission"));
            return true;
        }
        StructureBoxes.getInstance().readConfig();
        sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Reload successful"));
        return true;
    }

    private boolean sessionsCommand(CommandSender sender, String[] args) {
        if (args.length == 1 && !(sender instanceof Player)) {
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Sessions - Must supply player"));
            return true;
        }
        Player p = (Player) sender;
        int page;
        try {
            page = Integer.parseInt(args[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            page = 1;
        } catch (NumberFormatException e) {
            p.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Usage") + " /structurebox sessions [page] [player|-a|you]");
            return true;
        }
        OfflinePlayer sessionOwner = args.length == 3 && !args[2].equalsIgnoreCase("-a") ? Bukkit.getOfflinePlayer(args[2]) : p;
        if (!p.equals(sessionOwner) && !p.hasPermission("structurebox.sessions.others")) {
            p.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Sessions - No permission to view others"));
            return true;
        } else if (args.length == 3 && args[2].equalsIgnoreCase("-a") && !p.hasPermission("structurebox.sessions.all")) {
            p.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Sessions - No permission to view all"));
            return true;
        }
        if (sessionOwner == null) {
            p.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Sessions - Invalid player name"));
            return true;
        }
        Collection<Structure> sessions;
        String title = "";
        if (args.length == 3 && args[2].equalsIgnoreCase("-a")) {
            sessions = StructureManager.getInstance().getStructures();
            title += "All ";
        } else {
            sessions = StructureManager.getInstance().getSessions(sessionOwner.getUniqueId());
            title += sessionOwner.getName() + "'s ";
        }
        title += " sessions";
        if (sessions.isEmpty()) {
            p.sendMessage(COMMAND_PREFIX + (p.equals(sessionOwner) ? I18nSupport.getInternationalisedString("Sessions - You") : sessionOwner.getName()) +I18nSupport.getInternationalisedString("Command - Sessions - No sessions for") );
            return true;
        }
        final TopicPaginator paginator = new TopicPaginator(title);
        for (Structure structure : sessions) {
            paginator.addLine((title.startsWith("All") ? Bukkit.getOfflinePlayer(structure.getOwner()).getName()+ " " : "") + structure.getSchematicName() + ": " + (Settings.MaxSessionTime - (System.currentTimeMillis() - structure.getPlacementTime())/1000) + " seconds left");
        }
        if (!paginator.isInBounds(page)) {
            p.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Pagination - Invalid page") + page);
            return true;
        }
        for (String line : paginator.getPage(page)) {
            p.sendMessage(line);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> subCmds = new ArrayList<>();
        if (strings.length <= 1) {
            if (commandSender.hasPermission("structureboxes.create")) {
                subCmds.add("create");
            }
            if (commandSender.hasPermission("structureboxes.undo")) {
                subCmds.add("undo");
            }
            if (commandSender.hasPermission("structureboxes.reload")) {
                subCmds.add("reload");
            }
            if (commandSender.hasPermission("structureboxes.sessions")) {
                subCmds.add("sessions");
            }
        } else if (strings[0].equalsIgnoreCase("create") && strings.length < 3){
            File schemFolder = new File(StructureBoxes.getInstance().getWorldEditPlugin().getDataFolder().getAbsolutePath() + "/" + schematicDir);

            if (!schemFolder.exists() || !(commandSender instanceof Player)){
                return Collections.emptyList();
            }
            Player p = (Player) commandSender;
            File playerFolder = new File(schemFolder, p.getUniqueId().toString());
            for (String schem : schemFolder.list()){
                if (!schem.endsWith(".schematic") && !schem.endsWith(".schem")){
                    continue;
                }
                final String schemName = schem.replace(".schematic", "").replace(".schem", "");
                final String end = schemName.substring(schemName.lastIndexOf("_") + 1);
                if (isInteger(end)) {
                    final String start = schemName.replace(end, "");
                    final String[] foundFiles = schemFolder.list( (dir, name) -> (name.endsWith(".schematic") || name.endsWith(".schem")) && name.startsWith(start) && isInteger(name.replace(start, "").replace(".schematic", "").replace(".schem", "")));
                    final String entry = start + "#";
                    if (foundFiles.length > 1 && !subCmds.contains(entry))
                        subCmds.add(entry);
                }

                subCmds.add(schemName);
            }
            if (playerFolder.exists()) {
                for (String schem : playerFolder.list()) {
                    if (!schem.endsWith(".schematic") && !schem.endsWith(".schem")){
                        continue;
                    }
                    subCmds.add(schem.replace(".schematic", "").replace(".schem", ""));
                }
            }
        }
        List<String> completions = new ArrayList<>();
        for (String arg : subCmds){
            if (!arg.startsWith(strings[strings.length - 1])){
                continue;
            }
            completions.add(arg);
        }
        return completions;
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
