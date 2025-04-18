package io.github.eirikh1996.structureboxes.utils;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import me.ryanhamshire.griefprevention.GriefPrevention;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.server.ServerLocation;

public class RegionUtils {
    public static boolean isWithinRegion(L) {
        boolean eagleFactions = false;
        final StructureBoxes sb = StructureBoxes.getInstance();
        if (sb.getEagleFactionsPlugin().isPresent()) {
            eagleFactions = sb.getEagleFactionsPlugin().get().getFactionLogic().getFactionByChunk(loc.world().uniqueId(), loc.chunkPosition()).isPresent();
        }
        return eagleFactions ;
    }
}
