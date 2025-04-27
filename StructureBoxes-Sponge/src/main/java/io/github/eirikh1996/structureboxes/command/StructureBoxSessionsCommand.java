/*
    This file is part of Structure Boxes.

    Structure Boxes is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Structure Boxes is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Structure Boxes.  If not, see <https://www.gnu.org/licenses/>.
 */


package io.github.eirikh1996.structureboxes.command;

import io.github.eirikh1996.structureboxes.Structure;
import io.github.eirikh1996.structureboxes.StructureManager;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.TopicPaginator;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class StructureBoxSessionsCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandContext args) {
        Optional<String> arg = args.one(Parameter.key("player|-a", String.class));
        Optional<Integer> pageNo = args.one(Parameter.key("page", Integer.class));
        if (!(args.cause().root() instanceof Player)) {
            return CommandResult.error(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Sessions - Must supply player")));
        }
        ServerPlayer p = (ServerPlayer) args.cause().root();
        Optional<ServerPlayer> target = Sponge.server().player(arg.orElse(""));
        Player sessionOwner = target.orElse(p);
        int page = pageNo.orElse(0);
        if (!p.equals(sessionOwner) && !p.hasPermission("structurebox.sessions.others")) {
            return CommandResult.error(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Sessions - No permission to view others")));

        } else if (arg.isPresent() && arg.get().equalsIgnoreCase("-a") && !p.hasPermission("structurebox.sessions.all")) {
            return CommandResult.error(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Sessions - No permission to view all")));

        }




        if (arg.isPresent() && !target.isPresent()) {
            return CommandResult.error(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Sessions - Invalid player name")));

        }
        Collection<Structure> sessions;
        String title = "";
        if (arg.isPresent() && arg.get().equalsIgnoreCase("-a")) {
            sessions = StructureManager.getInstance().getStructures();
            title += "All ";
        } else {
            sessions = StructureManager.getInstance().getSessions(sessionOwner.uniqueId());
            title += sessionOwner.name() + "'s ";
        }
        title += " sessions";
        if (sessions.isEmpty()) {
            return CommandResult.error(Component.text(COMMAND_PREFIX + (p.equals(sessionOwner) ? I18nSupport.getInternationalisedString("Sessions - You") : sessionOwner.name()) +I18nSupport.getInternationalisedString("Command - Sessions - No sessions for") ));
        }
        final TopicPaginator paginator = new TopicPaginator(title);
        for (Structure structure : sessions) {
            paginator.addLine((title.startsWith("All") ? Sponge.server().player(structure.getOwner()).get().name()+ " " : "") + structure.getSchematicName() + ": " + (Settings.MaxSessionTime - (System.currentTimeMillis() - structure.getPlacementTime())/1000) + " seconds left");
        }
        if (!paginator.isInBounds(page)) {
            return CommandResult.error(Component.text(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Pagination - Invalid page") + page));
        }
        for (String line : paginator.getPage(page)) {
            p.sendMessage(Component.text(line));
        }
        return CommandResult.success();
    }
}
