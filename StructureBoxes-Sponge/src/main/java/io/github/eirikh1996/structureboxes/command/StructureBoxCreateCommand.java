package io.github.eirikh1996.structureboxes.command;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.sponge.SpongeWorld;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.WorldEditHandler;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class StructureBoxCreateCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Must be player")));
            return CommandResult.empty();
        }
        if (!src.hasPermission("structureboxes.create")) {
            src.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - No permission")));
            return CommandResult.empty();
        }
        String arg = args.<String>getOne(Text.of()).get();
        final Player player = (Player) src;
        @NotNull final WorldEditHandler weHandler = StructureBoxes.getInstance().getWorldEditHandler();
        World world = player.getWorld();
        SpongeWorld spongeWorld = StructureBoxes.getInstance().getWorldEditPlugin().getWorld(world);
        @Nullable Clipboard clipboard = weHandler.loadClipboardFromSchematic(spongeWorld, arg);
        if (clipboard == null) {
            player.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - No schematic")));
            return CommandResult.empty();
        }
        if (Settings.MaxStructureSize > -1 && weHandler.getStructureSize(clipboard) > Settings.MaxStructureSize) {
            player.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Structure too large")));
            return CommandResult.empty();
        }

        final ItemStack structureBox = ItemStack.builder().fromBlockState(((BlockType) Settings.StructureBoxItem).getDefaultState()).build();
        structureBox.offer(Keys.DISPLAY_NAME, Text.of(Settings.StructureBoxLore));
        List<Text> lore = new ArrayList<>();
        lore.add(0, Text.of(Settings.StructureBoxPrefix + arg));
        for (int i = 0 ; i < Settings.StructureBoxInstruction.size() ; i++) {
            lore.add(i + 1, Text.of(Settings.StructureBoxInstruction.get(i)));
        }
        structureBox.offer(Keys.ITEM_LORE, lore);
        PlayerInventory pInv = (PlayerInventory) player.getInventory();
        if (!pInv.getMain().canFit(structureBox) && !pInv.getHotbar().canFit(structureBox)) {
            player.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Insufficient inventory space")));
            return CommandResult.empty();
        }
        pInv.offer(structureBox);
        player.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - New structure box created")));
        return CommandResult.success();
    }
}
