package io.github.eirikh1996.structureboxes.utils;

import com.arckenver.nations.DataHandler;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class NationsUtils {
    public static boolean allowedToBuild(Player player, Location<World> loc) {
        return DataHandler.getPerm("build", player.getUniqueId(), loc);
    }

    public static boolean withinRegion(Location<World> loc) {
        return DataHandler.getNation(loc) != null;
    }
}
