package io.github.eirikh1996.structureboxes.command;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.sponge.SpongeAdapter;
import com.sk89q.worldedit.world.World;
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
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class StructureBoxCreateCommand implements CommandExecutor {


    @Override
    public CommandResult execute(CommandContext args) {
        if (!(args.cause().root() instanceof ServerPlayer player)) {
            args.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Must be player")));
            return CommandResult.success();
        }
        if (!args.hasPermission("structureboxes.create")) {
            args.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - No permission")));
            return CommandResult.success();
        }
        Optional<String> arg = args.one(Parameter.key("schematic", String.class));
        @NotNull final WorldEditHandler weHandler = StructureBoxes.getInstance().getWorldEditHandler();
        ServerWorld world = player.world();
        World spongeWorld = SpongeAdapter.adapt(world);
        @Nullable Clipboard clipboard = weHandler.loadClipboardFromSchematic(spongeWorld, arg.get());
        if (clipboard == null) {
            player.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - No schematic")));
            return CommandResult.success();
        }
        if (Settings.MaxStructureSize > -1 && weHandler.getStructureSize(clipboard) > Settings.MaxStructureSize) {
            player.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Structure too large")));
            return CommandResult.success();
        }

        final ItemStack structureBox = ItemStack.builder().fromBlockState(((BlockType) Settings.StructureBoxItem).defaultState()).build();
        structureBox.offer(Keys.DISPLAY_NAME, Component.text(Settings.StructureBoxLore));
        List<Component> lore = new ArrayList<>();
        lore.addFirst(Component.text(Settings.StructureBoxPrefix + arg));
        for (int i = 0 ; i < Settings.StructureBoxInstruction.size() ; i++) {
            lore.add(Component.text(Settings.StructureBoxInstruction.get(i)));
        }
        structureBox.offer(Keys.LORE, lore);
        PlayerInventory pInv = (PlayerInventory) player.inventory();
        if (!pInv.primary().canFit(structureBox) && !pInv.hotbar().canFit(structureBox)) {
            player.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Insufficient inventory space")));
            return CommandResult.success();
        }
        pInv.offer(structureBox);
        player.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - New structure box created")));
        return CommandResult.success();
    }
}
