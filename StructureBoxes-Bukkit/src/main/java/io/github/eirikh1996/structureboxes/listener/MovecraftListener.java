package io.github.eirikh1996.structureboxes.listener;

import io.github.eirikh1996.structureboxes.Structure;
import io.github.eirikh1996.structureboxes.StructureManager;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.utils.MovecraftUtils;
import io.github.eirikh1996.structureboxes.utils.ChatUtils;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.libs.net.kyori.adventure.text.Component;
import net.countercraft.movecraft.libs.net.kyori.adventure.text.TextComponent;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MovecraftListener implements Listener {

    @EventHandler
    public void onCraftDetect(CraftDetectEvent event) {
        if (event.getCraft().getAudience() == null) {
            return;
        }
        Structure structure;
        HitBox hitbox = event.getCraft().getHitBox();
        for (MovecraftLocation ml : hitbox) {
            structure = StructureManager.getInstance().getStructureAt(MovecraftUtils.movecraftToSBloc(event.getCraft().getW(), ml));
            if (structure != null) {
                event.getCraft().getAudience().sendMessage(Component.empty().content(ChatUtils.COMMAND_PREFIX + I18nSupport.getInternationalisedString("Movecraft - Session will expire")));
                break;
            }
        }
    }

    @EventHandler
    public void onCraftTranslate(CraftTranslateEvent event) {
        if (event.getCraft().getAudience() == null) {
            return;
        }
        Structure structure;
        HitBox oldHitbox = event.getOldHitBox();
        for (MovecraftLocation ml : oldHitbox) {
            structure = StructureManager.getInstance().getStructureAt(MovecraftUtils.movecraftToSBloc(event.getCraft().getW(), ml));
            if (structure != null) {
                event.getCraft().getAudience().sendMessage(Component.empty().content(ChatUtils.COMMAND_PREFIX + I18nSupport.getInternationalisedString("Movecraft - Removed due to motion")));
                StructureManager.getInstance().removeStructure(structure);
                break;
            }
        }
    }

    @EventHandler
    public void onCraftRotate(CraftRotateEvent event) {
        if (event.getCraft().getAudience() == null) {
            return;
        }
        Structure structure;
        HitBox oldHitbox = event.getOldHitBox();
        for (MovecraftLocation ml : oldHitbox) {
            structure = StructureManager.getInstance().getStructureAt(MovecraftUtils.movecraftToSBloc(event.getCraft().getW(), ml));
            if (structure != null) {
                event.getCraft().getAudience().sendMessage(Component.empty().content(ChatUtils.COMMAND_PREFIX + I18nSupport.getInternationalisedString("Movecraft - Removed due to motion")));
                StructureManager.getInstance().removeStructure(structure);
                break;
            }
        }
    }

    @EventHandler
    public void onSink(CraftSinkEvent event) {
        if (event.getCraft().getAudience() == null) {
            return;
        }
        Structure structure;
        HitBox hitbox = event.getCraft().getHitBox();
        for (MovecraftLocation ml : hitbox) {
            structure = StructureManager.getInstance().getStructureAt(MovecraftUtils.movecraftToSBloc(event.getCraft().getW(), ml));
            if (structure != null) {
                event.getCraft().getAudience().sendMessage(Component.empty().content(ChatUtils.COMMAND_PREFIX + I18nSupport.getInternationalisedString("Movecraft - Removed due to motion")));
                StructureManager.getInstance().removeStructure(structure);
                break;
            }
        }
    }
}
