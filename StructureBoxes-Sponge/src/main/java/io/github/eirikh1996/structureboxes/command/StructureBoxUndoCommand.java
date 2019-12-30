package io.github.eirikh1996.structureboxes.command;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.StructureManager;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.eirikh1996.structureboxes.utils.MathUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.function.Consumer;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class StructureBoxUndoCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final long start = System.currentTimeMillis();
        if (!(src instanceof Player)){
            src.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Must Be Player")));
            return CommandResult.success();
        }
        final Player p = (Player) src;
        if (!p.hasPermission("structureboxes.undo")){
            src.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - No permission")));
            return CommandResult.success();
        }
        AbstractMap.SimpleImmutableEntry<String, HashMap<Location, Object>> stringStructurePair = StructureManager.getInstance().getLatestStructure(p.getUniqueId());
        if (stringStructurePair == null){
            src.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - latest session expired")));
            return CommandResult.success();
        }
        String schematicName = stringStructurePair.getKey();
        HashMap<Location, Object> locationMaterialHashMap = stringStructurePair.getValue();
        if (locationMaterialHashMap == null) {
            src.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - latest session expired")));
            return CommandResult.success();
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




        ItemStack structureBox = ItemStack.builder().fromBlockState(((BlockType) Settings.StructureBoxItem).getDefaultState()).build();
        List<Text> lore = new ArrayList<>();
        structureBox.offer(Keys.DISPLAY_NAME, Text.of(Settings.StructureBoxLore));
        lore.add(Text.of(Settings.StructureBoxPrefix + schematicName));
        for (String instruction : Settings.StructureBoxInstruction) {
            final Text text = Text.of(instruction);
            if (lore.contains(text)) {
                continue;
            }
            lore.add(text);
        }
        structureBox.offer(Keys.ITEM_LORE, lore);
        p.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Successful undo")));
        PlayerInventory pInv = (PlayerInventory) p.getInventory();
        Task.builder().execute(new StructureUndoTask(locationQueue, locationMaterialHashMap, structure)).submit(StructureBoxes.getInstance());


        if (!pInv.getMain().canFit(structureBox)){
            p.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Inventory - No space")));
            final Entity item = p.getWorld().createEntity(EntityTypes.ITEM, p.getPosition());
            item.offer(Keys.REPRESENTED_ITEM, structureBox.createSnapshot());
            p.getWorld().spawnEntity(item);
            return CommandResult.success();
        }
        p.getInventory().offer(structureBox);

        if (Settings.Debug){
            final long end = System.currentTimeMillis();
            Sponge.getServer().getBroadcastChannel().send(Text.of("Undo took (ms): " + (end - start)));
        }
        return CommandResult.success();
    }

    private static class StructureUndoTask implements Consumer<Task> {
        private final Queue<Collection<Location>> locationQueue;
        private final HashMap<Location, Object> locationMaterialHashMap;
        private final HashSet<Location> structure;

        private StructureUndoTask(Queue<Collection<Location>> locationQueue, HashMap<Location, Object> locationMaterialHashMap, HashSet<Location> structure) {
            this.locationQueue = locationQueue;
            this.locationMaterialHashMap = locationMaterialHashMap;
            this.structure = structure;
        }

        @Override
        public void accept(Task task) {
            Collection<Location> poll = locationQueue.poll();
            if (poll == null){
                return;
            }
            for (Location location : poll){
                final BlockType origType = (BlockType) locationMaterialHashMap.get(location);
                org.spongepowered.api.world.Location<World> spongeLoc = MathUtils.sbToSpongeLoc(location);
                Optional<TileEntity> tileHolder = spongeLoc.getTileEntity();
                if (tileHolder.isPresent() && tileHolder.get() instanceof TileEntityCarrier){
                    TileEntityCarrier carrier = (TileEntityCarrier) tileHolder.get();
                    carrier.getInventory().clear();
                }
                spongeLoc.setBlockType(origType);

            }
            if (locationQueue.isEmpty()){
                StructureManager.getInstance().removeStructure(structure);
                task.cancel();
            }
        }
    }
}
