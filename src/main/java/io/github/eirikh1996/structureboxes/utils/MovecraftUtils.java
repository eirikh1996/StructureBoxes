package io.github.eirikh1996.structureboxes.utils;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;

public class MovecraftUtils {
    public static Location movecraftToSBloc(World w, MovecraftLocation ml) {
        return new Location(w.getName(), ml.getX(), ml.getY(), ml.getZ());
    }
}
