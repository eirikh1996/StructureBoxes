package io.github.eirikh1996.structureboxes.listener;

import io.github.eirikh1996.structureboxes.Structure;
import io.github.eirikh1996.structureboxes.StructureManager;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.utils.MovecraftUtils;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.utils.HitBox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class MovecraftListener implements Listener {

    @EventHandler
    public void onCraftDetect(CraftDetectEvent event) {
        if (event.getCraft().getNotificationPlayer() == null) {
            return;
        }
        Structure structure;
        HitBox hitbox;
        try {
            Method getNewHitBox = Craft.class.getDeclaredMethod("getHitBox");
            hitbox = (HitBox) getNewHitBox.invoke(event.getCraft());
        } catch (Exception e) {
            return;
        }
        for (MovecraftLocation ml : hitbox) {
            structure = StructureManager.getInstance().getStructureAt(MovecraftUtils.movecraftToSBloc(event.getCraft().getW(), ml));
            if (structure != null) {
                event.getCraft().getNotificationPlayer().sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Movecraft - Session will expire"));
                break;
            }
        }
    }

    @EventHandler
    public void onCraftTranslate(CraftTranslateEvent event) {
        if (event.getCraft().getNotificationPlayer() == null) {
            return;
        }
        Structure structure;
        HitBox oldHitbox;
        try {
            Method getNewHitBox = CraftTranslateEvent.class.getDeclaredMethod("getOldHitBox");
            oldHitbox = (HitBox) getNewHitBox.invoke(event);
        } catch (Exception e) {
            return;
        }
        for (MovecraftLocation ml : oldHitbox) {
            structure = StructureManager.getInstance().getStructureAt(MovecraftUtils.movecraftToSBloc(event.getCraft().getW(), ml));
            if (structure != null) {
                event.getCraft().getNotificationPlayer().sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Movecraft - Removed due to motion"));
                StructureManager.getInstance().removeStructure(structure);
                break;
            }
        }
    }

    @EventHandler
    public void onCraftRotate(CraftRotateEvent event) {
        if (event.getCraft().getNotificationPlayer() == null) {
            return;
        }
        Structure structure;
        HitBox oldHitbox;
        try {
            Method getNewHitBox = CraftRotateEvent.class.getDeclaredMethod("getOldHitBox");
            oldHitbox = (HitBox) getNewHitBox.invoke(event);
        } catch (Exception e) {
            return;
        }
        for (MovecraftLocation ml : oldHitbox) {
            structure = StructureManager.getInstance().getStructureAt(MovecraftUtils.movecraftToSBloc(event.getCraft().getW(), ml));
            if (structure != null) {
                event.getCraft().getNotificationPlayer().sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Movecraft - Removed due to motion"));
                StructureManager.getInstance().removeStructure(structure);
                break;
            }
        }
    }
}
