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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class StructureBoxSessionsCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<String> arg = args.getOne("player|-a");
        Optional<Integer> pageNo = args.getOne("page");
        if (!arg.isPresent() && !(src instanceof Player)) {
            src.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Sessions - Must supply player")));
            return CommandResult.empty();
        }
        Player p = (Player) src;
        Optional<Player> target = Sponge.getServer().getPlayer(arg.orElse(""));
        Player sessionOwner = target.orElse(p);
        int page = pageNo.orElse(0);
        if (!p.equals(sessionOwner) && !p.hasPermission("structurebox.sessions.others")) {
            p.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Sessions - No permission to view others")));
            return CommandResult.empty();
        } else if (arg.isPresent() && arg.get().equalsIgnoreCase("-a") && !p.hasPermission("structurebox.sessions.all")) {
            p.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Sessions - No permission to view all")));
            return CommandResult.empty();
        }




        if (arg.isPresent() && !target.isPresent()) {
            p.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Command - Sessions - Invalid player name")));
            return CommandResult.empty();
        }
        Collection<Structure> sessions;
        String title = "";
        if (arg.isPresent() && arg.get().equalsIgnoreCase("-a")) {
            sessions = StructureManager.getInstance().getStructures();
            title += "All ";
        } else {
            sessions = StructureManager.getInstance().getSessions(sessionOwner.getUniqueId());
            title += sessionOwner.getName() + "'s ";
        }
        title += " sessions";
        if (sessions.isEmpty()) {
            p.sendMessage(Text.of(COMMAND_PREFIX + (p.equals(sessionOwner) ? I18nSupport.getInternationalisedString("Sessions - You") : sessionOwner.getName()) +I18nSupport.getInternationalisedString("Command - Sessions - No sessions for") ));
            return CommandResult.empty();
        }
        final TopicPaginator paginator = new TopicPaginator(title);
        for (Structure structure : sessions) {
            paginator.addLine((title.startsWith("All") ? Sponge.getServer().getPlayer(structure.getOwner()).get().getName()+ " " : "") + structure.getSchematicName() + ": " + (Settings.MaxSessionTime - (System.currentTimeMillis() - structure.getPlacementTime())/1000) + " seconds left");
        }
        if (!paginator.isInBounds(page)) {
            p.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Pagination - Invalid page") + page));
            return CommandResult.empty();
        }
        for (String line : paginator.getPage(page)) {
            p.sendMessage(Text.of(line));
        }
        return CommandResult.success();
    }
    private static class SessionsCommandElement extends CommandElement {

        protected SessionsCommandElement(@Nullable Text key) {
            super(key);
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            return null;
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            return null;
        }
    }
}
