package io.github.eirikh1996.structureboxes.processing.validators;

import com.sk89q.worldedit.util.Location;
import io.github.eirikh1996.structureboxes.processing.BukkitRegionPredicate;
import io.github.eirikh1996.structureboxes.processing.Result;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlotSquaredRegionValidator implements BukkitRegionPredicate {
    @Override
    public @NotNull Result validate(@NotNull Player player, @NotNull Location location) {
        return null;
    }

    @Override
    public @NotNull Result regionPresent(@NotNull Location location) {
        return null;
    }
}
