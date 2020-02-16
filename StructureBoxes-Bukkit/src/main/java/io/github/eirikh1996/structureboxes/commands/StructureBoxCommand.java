package io.github.eirikh1996.structureboxes.commands;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import io.github.eirikh1996.structureboxes.Structure;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.StructureManager;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.eirikh1996.structureboxes.utils.MathUtils;
import io.github.eirikh1996.structureboxes.utils.TopicPaginator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

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
            boolean moveSchem;
            try {
                moveSchem = strings[2].equalsIgnoreCase("-m");
            } catch (ArrayIndexOutOfBoundsException e) {
                moveSchem = false;
            }
            return createStructureBox(commandSender, schematicName, moveSchem);
        } else if (strings[0].equalsIgnoreCase("undo")){
            return undoCommand(commandSender);
        } else if (strings[0].equalsIgnoreCase("reload")){
            return reloadCommand(commandSender);
        } else if (strings[0].equalsIgnoreCase("sessions")) {
            return sessionsCommand(commandSender, strings);
        }
        return false;
    }



    private boolean createStructureBox(CommandSender sender, String schematicName, boolean moveSchem){
        if (!(sender instanceof Player)){

            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("structureboxes.create")){
            return true;
        }
        if (schematicName == null){
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
        if (!schematicFile.exists() && !playerSchematicFile.exists()){
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
        } else if (!schematicFile.exists()){
            schematicName = player.getUniqueId() + "/" + schematicName;
        }
        Clipboard clipboard = StructureBoxes.getInstance().getWorldEditHandler().loadClipboardFromSchematic(new BukkitWorld(((Player) sender).getWorld()), schematicName);
        if (Settings.MaxStructureSize > -1 && StructureBoxes.getInstance().getWorldEditHandler().getStructureSize(clipboard) > Settings.MaxStructureSize) {
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Structure too large") + " " + Settings.MaxStructureSize);
            return true;
        }
        ItemStack structureBox = new ItemStack((Material) Settings.StructureBoxItem);
        List<String> lore = new ArrayList<>();
        ItemMeta meta = structureBox.getItemMeta();
        meta.setDisplayName(Settings.StructureBoxLore);
        lore.add(Settings.StructureBoxPrefix + schematicName);
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
        Structure structure = StructureManager.getInstance().getLatestStructure(p.getUniqueId());
        if (structure == null){
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - latest session expired"));
            return true;
        }
        if (!structure.isProcessing()) {
            structure.setProcessing(true);
        }
        String schematicName = structure.getSchematicName();
        Map<Location, Object> locationMaterialHashMap = structure.getOriginalBlocks();
        if (locationMaterialHashMap == null) {
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - latest session expired"));
            return true;
        }
        final HashSet<Location> structureLocs = new HashSet<>(locationMaterialHashMap.keySet());
        final List<Collection<Location>> sections = new ArrayList<>();
        for (int i = 0 ; i <= structureLocs.size() / 30000; i++){
            sections.add(new HashSet<>());
        }
        int index = 0;
        int count = 0;
        for (Location location : structureLocs){
            sections.get(index).add(location);
            count++;
            if (count >= 30000){
                index++;
                count = 0;
            }

        }

        final Queue<Collection<Location>> locationQueue = new LinkedList<>(sections);

            new BukkitRunnable() {
                @Override
                public void run() {
                    Collection<Location> poll = locationQueue.poll();
                    if (poll == null){
                        return;
                    }
                    for (Location location : poll){
                        final Material origType = (Material) locationMaterialHashMap.get(location);
                        Block b = MathUtils.sb2BukkitLoc(location).getBlock();
                        if (b.getState() instanceof InventoryHolder){
                            InventoryHolder holder = (InventoryHolder) b.getState();
                            holder.getInventory().clear();
                        }
                        b.setType(origType);

                    }
                    if (locationQueue.isEmpty()){
                        StructureManager.getInstance().removeStructure(structure);
                        if (structure.isProcessing()) {
                            structure.setProcessing(false);
                        }
                        cancel();
                    }
                }

            }.runTaskTimer(StructureBoxes.getInstance(), 0, 3);



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
        @NotNull HashMap<Integer, ItemStack> notFitting = p.getInventory().addItem(structureBox);
        if (!notFitting.isEmpty()){
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
                subCmds.add(schem.replace(".schematic", "").replace(".schem", ""));
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
}
