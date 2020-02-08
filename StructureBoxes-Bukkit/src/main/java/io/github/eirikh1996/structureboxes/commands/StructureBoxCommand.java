package io.github.eirikh1996.structureboxes.commands;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.StructureManager;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.eirikh1996.structureboxes.utils.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
            final String[] page = new String[4];
            page[0] = "§5==================[§6StructureBoxes§5]==================";
            page[1] = "§6Author: " + String.join(",", desc.getAuthors());
            page[2] = "§6/sb create <schematic ID> - Creates new structure box";
            page[3] = "§6/sb undo - Undoes the last undo session";
            page[4] = "§6/sb reload - Reloads the plugin";
            page[5] = "§5========================================================";
            commandSender.sendMessage(page);
            return true;
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
        } else if (strings[0].equalsIgnoreCase("reload")){
            return reloadCommand(commandSender);
        }
        return false;
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
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - No permission"));
            return true;
        }
        File schematicFile = new File(StructureBoxes.getInstance().getWorldEditPlugin().getDataFolder().getAbsolutePath() + "/" + schematicDir + "/" + schematicName + ".schematic");

        if (!schematicFile.exists()){
            schematicFile = new File(StructureBoxes.getInstance().getWorldEditPlugin().getDataFolder().getAbsolutePath() + "/" + schematicDir + "/" + schematicName + ".schem");
        }
        if (!schematicFile.exists()){
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - No schematic"));
            return true;
        }
        Clipboard clipboard = StructureBoxes.getInstance().getWorldEditHandler().loadClipboardFromSchematic(new BukkitWorld(((Player) sender).getWorld()), schematicName);
        if (Settings.MaxStructureSize > -1 && StructureBoxes.getInstance().getWorldEditHandler().getStructureSize(clipboard) > Settings.MaxStructureSize) {
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Structure too large") + " " + Settings.MaxStructureSize);
            return true;
        }
        int emptySlot = player.getInventory().firstEmpty();
        if (emptySlot < 0){
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Insufficient inventory space"));
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
        player.getInventory().addItem(structureBox);
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
        AbstractMap.SimpleImmutableEntry<String, HashMap<Location, Object>> stringStructurePair = StructureManager.getInstance().getLatestStructure(p.getUniqueId());
        if (stringStructurePair == null){
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - latest session expired"));
            return true;
        }
        String schematicName = stringStructurePair.getKey();
        HashMap<Location, Object> locationMaterialHashMap = stringStructurePair.getValue();
        if (locationMaterialHashMap == null) {
            sender.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - latest session expired"));
            return true;
        }
        final HashSet<Location> structure = new HashSet<>(locationMaterialHashMap.keySet());
        final List<Collection<Location>> sections = new ArrayList<>();
        for (int i = 0 ; i <= structure.size() / 30000; i++){
            sections.add(new HashSet<>());
        }
        int index = 0;
        int count = 0;
        for (Location location : structure){
            sections.get(index).add(location);
            count++;
            if (count >= 30000){
                index++;
                count = 0;
            }

        }
        StructureManager.getInstance().addStructure(structure);

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
        boolean fullInventory = p.getInventory().firstEmpty() == -1;;
        Collection<? extends ItemStack> foundBoxes = p.getInventory().all(structureBox).values();
        if (!foundBoxes.isEmpty()){
            for (ItemStack box : foundBoxes){
                if (box.getAmount() == structureBox.getMaxStackSize()){
                    continue;
                }
                fullInventory = false;
            }
        }


        if (fullInventory){
            p.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Inventory - No space"));
            p.getWorld().dropItem(p.getLocation(), structureBox);
            return true;
        }
        p.getInventory().addItem(structureBox);

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
        } else if (strings[0].equalsIgnoreCase("create")){
            File schemFolder = new File(StructureBoxes.getInstance().getWorldEditPlugin().getDataFolder().getAbsolutePath() + "/" + schematicDir);
            if (!schemFolder.exists()){
                return Collections.emptyList();
            }
            for (String schem : schemFolder.list()){
                if (!schem.endsWith(".schematic") && !schem.endsWith(".schem")){
                    continue;
                }
                subCmds.add(schem.replace(".schematic", "").replace(".schem", ""));
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
