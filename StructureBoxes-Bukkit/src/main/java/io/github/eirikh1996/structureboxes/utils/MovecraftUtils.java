package io.github.eirikh1996.structureboxes.utils;

import io.github.eirikh1996.structureboxes.utils.Location;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;

public class MovecraftUtils {
    public static Set<Location> structureFromHitbox(World w, HitBox hitBox) {
        Set<Location> structure = new HashSet<>();
        for (MovecraftLocation ml : hitBox) {
            structure.add(movecraftToSBloc(w, ml));
        }
        return structure;
    }

    public static Location movecraftToSBloc(World w, MovecraftLocation ml) {
        return new Location(w.getName(), ml.getX(), ml.getY(), ml.getZ());
    }
}
