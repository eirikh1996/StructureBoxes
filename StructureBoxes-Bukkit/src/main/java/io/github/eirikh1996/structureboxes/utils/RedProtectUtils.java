package io.github.eirikh1996.structureboxes.utils;

import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RedProtectUtils {
    public static boolean canBuild(Player player, Location location){
        final Region region = StructureBoxes.getInstance().getRedProtectPlugin().getAPI().getRegion(location);
        if (region == null) {
            return true;
        }
        return region.canBuild(player);
    }

    public static boolean withinRegion(Location location) {
        return StructureBoxes.getInstance().getRedProtectPlugin().getAPI().getRegion(location) != null;
    }

    public static boolean canPlaceStructureBox(Location location) {
        final Region region = StructureBoxes.getInstance().getRedProtectPlugin().getAPI().getRegion(location);
        if (region == null) {
            return true;
        }
        return region.getFlagBool("structurebox");
    }
}
