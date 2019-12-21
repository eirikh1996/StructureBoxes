package io.github.eirikh1996.structureboxes.utils;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import me.ryanhamshire.griefprevention.GriefPrevention;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class RegionUtils {
    public static boolean isWithinRegion(Location<World> loc) {
        boolean redProtect = false;
        boolean griefPrevention = false;
        boolean eagleFactions = false;
        final StructureBoxes sb = StructureBoxes.getInstance();
        if (sb.getRedProtectPlugin().isPresent()) {
            redProtect = sb.getRedProtectPlugin().get().getAPI().getRegion(loc) != null;
        }
        if (sb.getGriefPreventionPlugin().isPresent()) {
            griefPrevention = GriefPrevention.getApi().getClaimManager(loc.getExtent()).getClaimAt(loc) != null;
        }
        if (sb.getEagleFactionsPlugin().isPresent()) {
            eagleFactions = sb.getEagleFactionsPlugin().get().getFactionLogic().getFactionByChunk(loc.getExtent().getUniqueId(), loc.getChunkPosition()).isPresent();
        }
        return redProtect || griefPrevention || eagleFactions;
    }
}
