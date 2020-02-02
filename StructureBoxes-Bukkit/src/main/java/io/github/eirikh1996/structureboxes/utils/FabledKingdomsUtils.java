package io.github.eirikh1996.structureboxes.utils;

import com.songoda.kingdoms.constants.land.Land;
import com.songoda.kingdoms.constants.land.SimpleChunkLocation;
import com.songoda.kingdoms.manager.game.GameManagement;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FabledKingdomsUtils {
    public static boolean canBuild(Player player, Location location) {
        final Chunk chunk = location.getChunk();
        final Land land = GameManagement.getLandManager().getOrLoadLand(new SimpleChunkLocation(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
        return GameManagement.getKingdomManager().getKingdomList().get(land.getOwnerUUID()).getMembersList().contains(player.getUniqueId());
    }

    public static boolean isWithinRegion(Location location) {
        final Chunk chunk = location.getChunk();
        final Land land = GameManagement.getLandManager().getOrLoadLand(new SimpleChunkLocation(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
        return land.getOwnerUUID() != null;
    }
}
