package io.github.eirikh1996.structureboxes.processing;

import com.sk89q.worldedit.util.Location;
import org.jetbrains.annotations.NotNull;

public interface RegionPredicate<P> extends DyadicPredicate<P, Location> {

    @Override
    @NotNull Result validate(@NotNull P p, @NotNull Location location);

    @NotNull Result regionPresent(@NotNull Location location);
}
