package io.github.eirikh1996.structureboxes.processing.validators;

import com.sk89q.worldedit.util.Location;
import io.github.eirikh1996.structureboxes.processing.MonadicPredicate;
import io.github.eirikh1996.structureboxes.processing.Result;
import org.jetbrains.annotations.NotNull;

public class WithinRegionValidator implements MonadicPredicate<Location> {


    @Override
    public @NotNull Result validate(@NotNull Location location) {
        return null;
    }
}
