package io.github.eirikh1996.structureboxes.utils;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class MathUtils {
    public static Location<World> sbToSpongeLoc(io.github.eirikh1996.structureboxes.utils.Location sbLoc){
        return new Location<>(Sponge.getServer().getWorld(sbLoc.getWorld()).get(), sbLoc.getX(), sbLoc.getY(), sbLoc.getZ());
    }

    public static io.github.eirikh1996.structureboxes.utils.Location spongeToSBLoc(Location<World> spongeLoc){
        return new io.github.eirikh1996.structureboxes.utils.Location(spongeLoc.getExtent().getUniqueId(), spongeLoc.getBlockX(), spongeLoc.getBlockY(), spongeLoc.getBlockZ());
    }
}
