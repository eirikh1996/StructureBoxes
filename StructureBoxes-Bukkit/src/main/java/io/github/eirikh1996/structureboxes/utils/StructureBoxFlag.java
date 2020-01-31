package io.github.eirikh1996.structureboxes.utils;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroup;

import javax.annotation.Nullable;

public class StructureBoxFlag extends Flag {
    protected StructureBoxFlag(String name, @Nullable RegionGroup defaultGroup) {
        super(name, defaultGroup);
    }

    @Override
    public Object parseInput(FlagContext flagContext) throws InvalidFlagFormat {
        return null;
    }

    @Override
    public Object unmarshal(@Nullable Object o) {
        return null;
    }

    @Override
    public Object marshal(Object o) {
        return null;
    }
}
