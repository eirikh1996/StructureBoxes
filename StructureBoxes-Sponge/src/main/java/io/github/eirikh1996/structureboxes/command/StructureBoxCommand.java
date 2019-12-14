package io.github.eirikh1996.structureboxes.command;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.util.Optional;

public class StructureBoxCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional createArg = args.getOne("create");
        if (createArg.isPresent()) {
            return createCommand(src, createArg);
        }
        return CommandResult.empty();
    }

    private CommandResult createCommand(CommandSource src, Optional arg) {
        return null;
    }
}
