package io.github.eirikh1996.structureboxes.utils;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import org.spongepowered.api.world.server.ServerLocation;

public class RegionUtils {
    public static boolean isWithinRegion(ServerLocation loc) {
        boolean eagleFactions = false;
        final StructureBoxes sb = StructureBoxes.getInstance();
        if (sb.getEagleFactionsPlugin().isPresent()) {
            eagleFactions = sb.getEagleFactionsPlugin().get().getFactionLogic().getFactionByChunk(loc.world().uniqueId(), loc.chunkPosition()).isPresent();
        }
        return eagleFactions ;
    }
}
