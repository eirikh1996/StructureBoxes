package io.github.eirikh1996.structureboxes.processing.validators;

import com.sk89q.worldedit.util.Location;
import io.github.eirikh1996.structureboxes.processing.RegionPredicate;
import io.github.eirikh1996.structureboxes.processing.Result;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RegionValidatorManager implements Iterable<RegionPredicate>{
    private final Map<String, RegionPredicate> REGISTERED_REGION_VALIDATORS = new HashMap<>();
    private static RegionValidatorManager INSTANCE;

    private RegionValidatorManager() {

    }

    public static RegionValidatorManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RegionValidatorManager();
        }
        return INSTANCE;
    }

    public boolean registerRegionValidator(RegionPredicate validator, String pluginID) {
        return REGISTERED_REGION_VALIDATORS.put(pluginID, validator) == null;
    }

    public Collection<RegionPredicate> getRegisteredValidators() {
        return REGISTERED_REGION_VALIDATORS.values();
    }

    @Override
    public @NotNull Iterator<RegionPredicate> iterator() {
        return getRegisteredValidators().iterator();
    }
}
