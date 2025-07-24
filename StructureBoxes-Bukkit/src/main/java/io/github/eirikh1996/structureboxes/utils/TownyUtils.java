package io.github.eirikh1996.structureboxes.utils;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import org.bukkit.Location;
import org.bukkit.entity.Player;
@Deprecated(
        forRemoval = true
)
public class TownyUtils {
    public static boolean canBuild(Player player, Location location){
        return PlayerCacheUtil.getCachePermission(player, location, location.getBlock().getType(), TownyPermission.ActionType.BUILD);
    }

    public static boolean insideTownBlock(Location location){
        return TownyAPI.getInstance().getTownBlock(location) != null;
    }
}
