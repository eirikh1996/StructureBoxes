package io.github.eirikh1996.structureboxes.utils;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GriefPreventionUtils {
    public static boolean canBuild(Player player, Location location){
        return StructureBoxes.getInstance().getGriefPreventionPlugin().allowBuild(player, location) == null;
    }
}
