package io.github.eirikh1996.structureboxes.command;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

public class StructureBoxCommand implements CommandExecutor {

    public static
    //Create command
    CommandSpec createCommand = CommandSpec.builder()
            .permission("structureboxes.create")
            .arguments(GenericArguments.string(Text.of()), GenericArguments.flags().flag("m").buildWith(GenericArguments.none()), GenericArguments.flags().flag("e").buildWith(GenericArguments.integer(Text.EMPTY)))
            .executor(new StructureBoxCreateCommand())
            .build();

    //undo command
    CommandSpec undoCommand = CommandSpec.builder()
            .executor(new StructureBoxUndoCommand())
            .permission("structureboxes.undo")
            .build();


    //reload command
    CommandSpec reloadCommand = CommandSpec.builder()
            .executor(new StructureBoxReloadCommand())
            .permission("structureboxes.reload")
            .build();

    //sessions command
    CommandSpec sessionsCommand = CommandSpec.builder()
            .executor(new StructureBoxSessionsCommand())
            .arguments(
                    GenericArguments.optional(GenericArguments.string(Text.of("player|-a"))),
                    GenericArguments.optional(GenericArguments.integer(Text.of("page"))))
            .build();

    CommandSpec structureBoxCommand = CommandSpec.builder()
            .executor(new StructureBoxCommand())
            .child(createCommand, "create", "cr", "c")
            .child(undoCommand, "undo", "u" , "ud")
            .child(reloadCommand, "reload", "r", "rl")
            .child(sessionsCommand, "sessions", "s")
            .build();
        Sponge.commandManager().register(plugin, structureBoxCommand, "structurebox", "sbox", "sb");


    @Override
    public CommandResult execute(CommandContext args) throws CommandException {
        PluginContainer plugin = StructureBoxes.getInstance().getPlugin();
        src.sendMessage(Text.of("§5 ==================[§6StructureBoxes§5]=================="));
        src.sendMessage(Text.of("§6 Author: " + String.join(",", plugin.getAuthors())));
        src.sendMessage(Text.of("§6 Version: v" + plugin.getVersion()));
        src.sendMessage(Text.of("§6 /sb create <schematic ID> [-m] - Creates new structure box"));
        src.sendMessage(Text.of("§6 If using FAWE, -m will move schematic to global directory"));
        src.sendMessage(Text.of("§6 /sb undo - Undoes the last undo session"));
        src.sendMessage(Text.of("§6 /sb reload - Reloads the plugin"));
        src.sendMessage(Text.of("§6 /sb sessions [player|-a|you] [page] - Shows active sessions"));
        src.sendMessage(Text.of("§5 ========================================================"));
        return CommandResult.empty();
    }
}
