package io.github.eirikh1996.structureboxes.utils;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.Island;
import com.iridium.iridiumskyblock.User;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class IridiumSkyblockUtils {

    public static boolean canBuild(Player player, Location location) {
        final Island island = IridiumSkyblock.getIslandManager().getIslandViaLocation(location);
        if (island == null) {
            return true;
        }
        final User user = User.getUser(player);
        return (island.getOwner().equals(user.player) || island.getMembers().contains(user.player)) && !island.isBanned(user);
    }

    public static boolean withinRegion(Location location) {
        return IridiumSkyblock.getIslandManager().getIslandViaLocation(location) != null;
    }
}
