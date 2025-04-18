package io.github.eirikh1996.structureboxes.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;

public class MovecraftUtils {
    public static Set<Location> structureFromHitbox(World w, HitBox hitBox) {
        Set<Location> structure = new HashSet<>();
        for (MovecraftLocation ml : hitBox) {
            structure.add(movecraftToWEloc(w, ml));
        }
        return structure;
    }

    public static Location movecraftToWEloc(World w, MovecraftLocation ml) {
        return BukkitAdapter.adapt(ml.toBukkit(w));
    }
}
