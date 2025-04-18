package io.github.eirikh1996.structureboxes.command;

import com.sk89q.worldedit.util.Location;
import io.github.eirikh1996.structureboxes.Structure;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.StructureManager;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.MathUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.carrier.CarrierBlockEntity;
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
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.server.ServerLocation;

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
        Structure structure = StructureManager.getInstance().getLatestStructure(p.getUniqueId());
        if (structure == null){
            src.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - latest session expired")));
            return CommandResult.success();
        }
        String schematicName = structure.getSchematicName();
        Map<Location, Object> locationMaterialHashMap = structure.getOriginalBlocks();
        if (locationMaterialHashMap == null) {
            src.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - latest session expired")));
            return CommandResult.success();
        }
        StructureBoxes.getInstance().clearStructure(structure);
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
        if (!structure.isProcessing()) {
            structure.setProcessing(true);
        }


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

    public static class StructureUndoTask implements Runnable {
        private final Queue<Location> locationQueue;
        private final Map<Location, Object> locationMaterialHashMap;
        private final HashSet<Location> structure;

        public StructureUndoTask(Queue<Location> locationQueue, Map<Location, Object> locationMaterialHashMap) {
            this.locationQueue = locationQueue;
            this.locationMaterialHashMap = locationMaterialHashMap;
            this.structure = new HashSet<>(locationQueue);
        }

        @Override
        public void run() {
            int queueSize = Math.min(locationQueue.size(), Settings.IncrementalPlacement ? Settings.IncrementalPlacementBlocksPerTick : 30000);
            for (int i = 1 ; i <= queueSize ; i++){
                final Location location = locationQueue.poll();
                if (location == null)
                    break;
                final BlockType origType = (BlockType) locationMaterialHashMap.get(location);
                ServerLocation spongeLoc = MathUtils.sbToSpongeLoc(location);
                Optional<? extends BlockEntity> tileHolder = spongeLoc.blockEntity();
                if (tileHolder.isPresent() && tileHolder.get() instanceof CarrierBlockEntity){
                    CarrierBlockEntity carrier = (CarrierBlockEntity) tileHolder.get();
                    carrier.inventory().clear();
                }
                spongeLoc.setBlockType(origType, BlockChangeFlags.NONE);

            }
            if (locationQueue.isEmpty()){

                StructureManager.getInstance().removeStructure(StructureManager.getInstance().getCorrespondingStructure(structure));

            }
        }
    }
}
