package io.github.eirikh1996.structureboxes.utils;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import org.Bukkitters.SkyBlock.Main;
import org.Bukkitters.SkyBlock.Utils.Files.SkyBlocks;
import org.Bukkitters.SkyBlock.Utils.IChunkGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
@Deprecated(
        forRemoval = true
)
public class SkyBlockUtils {

    private static SkyBlocks sb = new SkyBlocks();
    public static boolean canBuild(Player player, org.bukkit.Location location) {
        if (!(location.getWorld().getGenerator() instanceof IChunkGenerator))
            return true;
        final Main sbPlugin = StructureBoxes.getInstance().getSkyBlockPlugin();
        return sbPlugin.getConfig().getBoolean("allow-build-on-other-skyblock") ||
                sbPlugin.hasSkyBlock(player.getUniqueId()) && sb.distanceKept(player.getUniqueId(), location) ||
                sbPlugin.hasNetherSkyBlock(player.getUniqueId()) && sb.distanceKeptNether(player.getUniqueId(), location);
    }

    public static boolean withinRegion(Location location) {
        if (!(location.getWorld().getGenerator() instanceof IChunkGenerator))
            return false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!sb.distanceKept(player.getUniqueId(), location) && !sb.distanceKeptNether(player.getUniqueId(), location))
                continue;
            return true;
        }
        return false;
    }
}
