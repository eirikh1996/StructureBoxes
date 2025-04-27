package io.github.eirikh1996.structureboxes.command;

import com.sk89q.worldedit.sponge.SpongeAdapter;
import com.sk89q.worldedit.util.Location;
import io.github.eirikh1996.structureboxes.Structure;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.StructureManager;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.carrier.CarrierBlockEntity;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.*;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class StructureBoxUndoCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandContext args){
        final long start = System.currentTimeMillis();
        if (!(args.cause().root() instanceof Player)){
            args.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Must Be Player")));
            return CommandResult.success();
        }
        final ServerPlayer p = (ServerPlayer) args.cause().root();
        if (!p.hasPermission("structureboxes.undo")){
            p.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - No permission")));
            return CommandResult.success();
        }
        Structure structure = StructureManager.getInstance().getLatestStructure(p.uniqueId());
        if (structure == null){
            p.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - latest session expired")));
            return CommandResult.success();
        }
        String schematicName = structure.getSchematicName();
        Map<Location, Object> locationMaterialHashMap = structure.getOriginalBlocks();
        if (locationMaterialHashMap == null) {
            p.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - latest session expired")));
            return CommandResult.success();
        }
        StructureBoxes.getInstance().clearStructure(structure);
        ItemStack structureBox = ItemStack.builder().fromBlockState(((BlockType) Settings.StructureBoxItem).defaultState()).build();
        List<Component> lore = new ArrayList<>();
        structureBox.offer(Keys.DISPLAY_NAME, Component.text(Settings.StructureBoxLore));
        lore.add(Component.text(Settings.StructureBoxPrefix + schematicName));
        for (String instruction : Settings.StructureBoxInstruction) {
            final Component text = Component.text(instruction);
            if (lore.contains(text)) {
                continue;
            }
            lore.add(text);
        }
        structureBox.offer(Keys.LORE, lore);
        p.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Successful undo")));
        PlayerInventory pInv = p.inventory();
        if (!structure.isProcessing()) {
            structure.setProcessing(true);
        }

        //if inventory is full, drop item on the ground
        if (!pInv.primary().canFit(structureBox)){
            p.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Inventory - No space")));
            final Entity item = p.world().createEntity(EntityTypes.ITEM, p.position());
            item.offer(Keys.ITEM_STACK_SNAPSHOT, structureBox.asImmutable());
            p.world().spawnEntity(item);
            return CommandResult.success();
        }
        p.inventory().offer(structureBox);

        if (Settings.Debug){
            final long end = System.currentTimeMillis();
            Sponge.server().broadcastAudience().sendMessage(Component.text("Undo took (ms): " + (end - start)));
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
                ServerLocation spongeLoc = SpongeAdapter.adapt(location);
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
