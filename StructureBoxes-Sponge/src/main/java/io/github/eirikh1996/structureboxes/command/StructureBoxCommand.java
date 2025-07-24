package io.github.eirikh1996.structureboxes.command;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StructureBoxCommand implements CommandExecutor {
    private final PluginContainer plugin;

    public StructureBoxCommand(final PluginContainer plugin) {
        this.plugin = plugin;

    }


    @Override
    public CommandResult execute(CommandContext args) throws CommandException {
        final Audience audience = args.cause().audience();
        PluginContainer plugin = StructureBoxes.getInstance().getPlugin();
        final List<String> contributors = new ArrayList<>();
        plugin.metadata().contributors().forEach((c) -> contributors.add(c.name()));
        audience.sendMessage(Component.text("§5 ==================[§6StructureBoxes§5]=================="));
        audience.sendMessage(Component.text("§6 Author: " + String.join(",", contributors)));
        audience.sendMessage(Component.text("§6 Version: v" + plugin.metadata().version()));
        audience.sendMessage(Component.text("§6 /sb create <schematic ID> [-m] - Creates new structure box"));
        audience.sendMessage(Component.text("§6 If using FAWE, -m will move schematic to global directory"));
        audience.sendMessage(Component.text("§6 /sb undo - Undoes the last undo session"));
        audience.sendMessage(Component.text("§6 /sb reload - Reloads the plugin"));
        audience.sendMessage(Component.text("§6 /sb sessions [player|-a|you] [page] - Shows active sessions"));
        audience.sendMessage(Component.text("§5 ========================================================"));
        return CommandResult.success();
    }
}
