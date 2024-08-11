package io.github.eirikh1996.structureboxes.listener;

import io.github.eirikh1996.structureboxes.Structure;
import io.github.eirikh1996.structureboxes.StructureManager;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.utils.MovecraftUtils;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.PilotedCraft;
import net.countercraft.movecraft.events.*;
import net.countercraft.movecraft.processing.MovecraftWorld;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import org.bukkit.ChatColor;
import org.bukkit.Tag;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class MovecraftListener implements Listener {

    @EventHandler
    public void onCraftDetect(CraftDetectEvent event) {
        if (!(event.getCraft() instanceof PilotedCraft))
            return;

        Structure structure;
        HitBox hitbox = event.getCraft().getHitBox();
        for (MovecraftLocation ml : hitbox) {
            structure = StructureManager.getInstance().getStructureAt(MovecraftUtils.movecraftToSBloc(event.getCraft().getWorld(), ml));
            if (structure != null) {
                ((PilotedCraft) event.getCraft()).getPilot().sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Movecraft - Session will expire"));
                break;
            }
        }
    }

    @EventHandler
    public void onCraftPilot(@NotNull CraftPilotEvent e) {
        Craft craft = e.getCraft();
        MovecraftWorld world = e.getCraft().getMovecraftWorld();
        for (MovecraftLocation ml : craft.getHitBox()) {
            if (!Tag.SIGNS.isTagged(world.getMaterial(ml)))
                continue;

            BlockState state = world.getState(ml);
            if (!(state instanceof Sign sign))
                continue;

            if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("<<Private>>")) {
                sign.setLine(0, "[Private]");
                if (craft instanceof PilotedCraft pilotedCraft)
                    sign.setLine(1, pilotedCraft.getPilot().getName());
                sign.update();
            }
            else if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("<<More Users>>")) {
                sign.setLine(0, "[More Users]");
                sign.update();
            }
            else if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("<<Pilot>>")) {
                sign.setLine(0, "Pilot:");
                if (craft instanceof PilotedCraft pilotedCraft)
                    sign.setLine(1, pilotedCraft.getPilot().getName());
                sign.update();
            }
        }
    }

    @EventHandler
    public void onCraftTranslate(CraftTranslateEvent event) {
        if (!(event.getCraft() instanceof PilotedCraft))
            return;

        Structure structure;
        HitBox oldHitbox = event.getOldHitBox();
        for (MovecraftLocation ml : oldHitbox) {
            structure = StructureManager.getInstance().getStructureAt(MovecraftUtils.movecraftToSBloc(event.getCraft().getWorld(), ml));
            if (structure != null) {
                ((PilotedCraft) event.getCraft()).getPilot().sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Movecraft - Removed due to motion"));
                StructureManager.getInstance().removeStructure(structure);
                break;
            }
        }
    }

    @EventHandler
    public void onCraftRotate(CraftRotateEvent event) {
        if (!(event.getCraft() instanceof PilotedCraft))
            return;

        Structure structure;
        HitBox oldHitbox = event.getOldHitBox();
        for (MovecraftLocation ml : oldHitbox) {
            structure = StructureManager.getInstance().getStructureAt(MovecraftUtils.movecraftToSBloc(event.getCraft().getWorld(), ml));
            if (structure != null) {
                ((PilotedCraft) event.getCraft()).getPilot().sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Movecraft - Removed due to motion"));
                StructureManager.getInstance().removeStructure(structure);
                break;
            }
        }
    }

    @EventHandler
    public void onSink(CraftSinkEvent event) {
        if (!(event.getCraft() instanceof PilotedCraft))
            return;

        Structure structure;
        HitBox hitbox = event.getCraft().getHitBox();
        for (MovecraftLocation ml : hitbox) {
            structure = StructureManager.getInstance().getStructureAt(MovecraftUtils.movecraftToSBloc(event.getCraft().getWorld(), ml));
            if (structure != null) {
                ((PilotedCraft) event.getCraft()).getPilot().sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Movecraft - Removed due to motion"));
                StructureManager.getInstance().removeStructure(structure);
                break;
            }
        }
    }
}
