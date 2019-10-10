package io.github.eirikh1996.structureboxes.compat.we6;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.WorldEditLocation;
import org.bukkit.Location;

import java.util.HashSet;

public class BukkitUtils {
    public static boolean isFreeSpace(Clipboard clipboard, WorldEditLocation to){

        HashSet<Location> structureLocs = new HashSet();
        int minX = clipboard.getMinimumPoint().getBlockX();
        int maxX = clipboard.getMaximumPoint().getBlockX();
        int minY = clipboard.getMinimumPoint().getBlockY();
        int maxY = clipboard.getMaximumPoint().getBlockY();
        int minZ = clipboard.getMinimumPoint().getBlockZ();
        int maxZ = clipboard.getMaximumPoint().getBlockZ();
        Vector distance = new Vector(to.getX(), to.getY(), to.getZ()).subtract(clipboard.getOrigin());
        for (int x = minX ; x <= maxX ; x++){
            for (int y = minY ; y <= maxY ; y++){
                for (int z = minZ ; z <= maxZ ; z++){
                    BaseBlock baseBlock = clipboard.getBlock(new Vector(x, y, z));
                    if (baseBlock.getType() == 0){
                        continue;
                    }
                    structureLocs.add(new Location(((BukkitWorld) to.getWorld()).getWorld(), x + distance.getBlockX(), y + distance.getBlockY(), z + distance.getBlockZ()));
                }
            }
        }
        for (Location loc : structureLocs){
            if (loc.getBlock().getType().name().endsWith("AIR") || !Settings.blocksToIgnore.contains(loc.getBlock().getType())){
                continue;
            }
            return false;
        }
        return true;
    }
}
