package io.github.eirikh1996.structureboxes.utils;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import me.zombie_striker.landclaiming.claimedobjects.ClaimedBlock;
import me.zombie_striker.landclaiming.claimedobjects.ClaimedLand;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
@Deprecated(
        forRemoval = true
)
public class LandClaimingUtils {
    private static final List<ClaimedBlock> claimedBlocks = StructureBoxes.getInstance().getLandClaimingPlugin().claimedBlock;
    private static final List<ClaimedLand> claimedLands = StructureBoxes.getInstance().getLandClaimingPlugin().claimedLand;
    public static boolean canBuild(Player player, Location location){

        for (ClaimedLand claimedLand : claimedLands){
            if (insideClaimedLand(claimedLand, location)){
                return claimedLand.getOwner().equals(player.getUniqueId()) || claimedLand.getGuests().contains(player.getUniqueId());
            }

        }
        return true;
    }
    public static boolean insideClaimedLand(Location location){
        for (ClaimedLand claimedLand : claimedLands){
            if (insideClaimedLand(claimedLand, location)){
                return true;
            }

        }
        return false;
    }

    private static boolean insideClaimedLand(ClaimedLand claimedLand, Location location){
        final int minX = claimedLand.getMinLoc().getBlockX();
        final int minY = claimedLand.getMinLoc().getBlockY();
        final int minZ = claimedLand.getMinLoc().getBlockZ();
        final int maxX = claimedLand.getMaxLoc().getBlockX();
        final int maxY = claimedLand.getMaxLoc().getBlockY();
        final int maxZ = claimedLand.getMaxLoc().getBlockZ();
        return (location.getBlockX() >= minX && location.getBlockX() <= maxX) && (location.getBlockY() >= minY && location.getBlockY() <= maxY) && (location.getBlockZ() >= minZ && location.getBlockZ() <= maxZ);
    }
}
