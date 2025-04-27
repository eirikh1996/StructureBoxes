package io.github.eirikh1996.structureboxes.command;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.sponge.SpongeWorld;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.WorldEditHandler;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class StructureBoxCreateCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandContext args) {
        if (!(args.cause().root() instanceof Player)) {
            src.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Must be player")));
            return CommandResult.empty();
        }
        if (!src.hasPermission("structureboxes.create")) {
            src.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - No permission")));
            return CommandResult.empty();
        }
        String arg = args.<String>getOne(Component.text()).get();
        final Player player = (Player) src;
        @NotNull final WorldEditHandler weHandler = StructureBoxes.getInstance().getWorldEditHandler();
        World world = player.getWorld();
        SpongeWorld spongeWorld = StructureBoxes.getInstance().getWorldEditPlugin().getWorld(world);
        @Nullable Clipboard clipboard = weHandler.loadClipboardFromSchematic(spongeWorld, arg);
        if (clipboard == null) {
            player.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - No schematic")));
            return CommandResult.empty();
        }
        if (Settings.MaxStructureSize > -1 && weHandler.getStructureSize(clipboard) > Settings.MaxStructureSize) {
            player.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Structure too large")));
            return CommandResult.empty();
        }

        final ItemStack structureBox = ItemStack.builder().fromBlockState(((BlockType) Settings.StructureBoxItem).getDefaultState()).build();
        structureBox.offer(Keys.DISPLAY_NAME, Component.text(Settings.StructureBoxLore));
        List<Text> lore = new ArrayList<>();
        lore.add(0, Component.text(Settings.StructureBoxPrefix + arg));
        for (int i = 0 ; i < Settings.StructureBoxInstruction.size() ; i++) {
            lore.add(Component.text(Settings.StructureBoxInstruction.get(i)));
        }
        structureBox.offer(Keys.ITEM_LORE, lore);
        PlayerInventory pInv = (PlayerInventory) player.getInventory();
        if (!pInv.getMain().canFit(structureBox) && !pInv.getHotbar().canFit(structureBox)) {
            player.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Insufficient inventory space")));
            return CommandResult.empty();
        }
        pInv.offer(structureBox);
        player.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - New structure box created")));
        return CommandResult.success();
    }
}
