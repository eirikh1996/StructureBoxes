package io.github.eirikh1996.structureboxes.listener;

import com.flowpowered.math.vector.Vector3i;
import io.github.eirikh1996.structureboxes.Structure;
import io.github.eirikh1996.structureboxes.StructureManager;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.pulverizer.movecraft.craft.Craft;
import io.github.pulverizer.movecraft.event.CraftDetectEvent;
import io.github.pulverizer.movecraft.event.CraftRotateEvent;
import io.github.pulverizer.movecraft.event.CraftTranslateEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.text.Text;

import java.util.Optional;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class MovecraftListener {

    @Listener
    public void onCraftTranslate(CraftTranslateEvent event) {
        final Craft craft = event.getCraft();
        for (Vector3i loc : event.getOldHitBox()) {
            final Structure structure = StructureManager.getInstance().getStructureAt(new Location(craft.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()));
            if (structure == null) {
                continue;
            }
            final Optional<Player> pilot = Sponge.getServer().getPlayer(craft.getCommander());
            if (pilot.isEmpty()) {
                return;
            }
            pilot.get().sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Movecraft - Removed due to motion")));
            StructureManager.getInstance().removeStructure(structure);
            break;
        }
    }

    @Listener
    public void onCraftRotate(CraftRotateEvent event) {
        final Craft craft = event.getCraft();
        for (Vector3i loc : event.getOldHitBox()) {
            final Structure structure = StructureManager.getInstance().getStructureAt(new Location(craft.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()));
            if (structure == null) {
                continue;
            }
            final Optional<Player> pilot = Sponge.getServer().getPlayer(craft.getCommander());
            if (!pilot.isPresent()) {
                return;
            }
            pilot.get().sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Movecraft - Removed due to motion")));
            StructureManager.getInstance().removeStructure(structure);
            break;
        }
    }

    @Listener
    public void onCraftDetect(CraftDetectEvent event) {
        final Craft craft = event.getCraft();
        for (Vector3i loc : craft.getHitBox()) {
            final Structure structure = StructureManager.getInstance().getStructureAt(new Location(craft.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()));
            if (structure == null) {
                continue;
            }
            final Optional<Player> pilot = Sponge.getServer().getPlayer(craft.getCommander());
            if (!pilot.isPresent()) {
                return;
            }
            pilot.get().sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Movecraft - Session will expire")));
            break;
        }
    }
}
