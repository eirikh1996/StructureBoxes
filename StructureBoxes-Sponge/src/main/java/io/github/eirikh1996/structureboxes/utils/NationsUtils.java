package io.github.eirikh1996.structureboxes.utils;

import com.arckenver.nations.DataHandler;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.server.ServerLocation;

public class NationsUtils {
    public static boolean allowedToBuild(Player player, ServerLocation loc) {
        return DataHandler.getPerm("build", player.uniqueId(), loc);
    }

    public static boolean withinRegion(Location<World> loc) {
        return DataHandler.getNation(loc) != null;
    }
}
