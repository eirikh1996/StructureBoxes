package io.github.eirikh1996.structureboxes.command;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import java.io.IOException;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class StructureBoxReloadCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!src.hasPermission("structureboxes.reload")) {
            src.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - No permission")));
            return CommandResult.empty();
        }

        try {
            StructureBoxes.getInstance().loadLocales();
            StructureBoxes.getInstance().readConfig();
            I18nSupport.initialize(StructureBoxes.getInstance().getConfigDir().toFile());
        } catch (ObjectMappingException | IOException e) {
            e.printStackTrace();
            return CommandResult.empty();
        }
        src.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Reload successful")));
        return CommandResult.success();
    }
}
