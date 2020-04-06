package io.github.eirikh1996.structureboxes.command;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

public class StructureBoxCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
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
