package io.github.eirikh1996.structureboxes.command;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;

import java.io.IOException;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class StructureBoxReloadCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandContext args){
        if (!args.hasPermission("structureboxes.reload")) {
            args.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - No permission")));
            return CommandResult.success();
        }

        try {
            StructureBoxes.getInstance().loadLocales();
            StructureBoxes.getInstance().readConfig();
            I18nSupport.initialize(StructureBoxes.getInstance().getConfigDir().toFile(), StructureBoxes.getInstance());
        } catch (IOException e) {
            e.printStackTrace();
            return CommandResult.success();
        }
        args.sendMessage(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Reload successful")));
        return CommandResult.success();
    }
}
