package io.github.eirikh1996.structureboxes.utils;

import com.plotsquared.core.configuration.StaticCaption;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class StructureBoxFlag extends BooleanFlag<StructureBoxFlag> {

    public static final StructureBoxFlag STRUCTUREBOX_FLAG_TRUE = new StructureBoxFlag(true);
    public static final StructureBoxFlag STRUCTUREBOX_FLAG_FALSE = new StructureBoxFlag(false);

    protected StructureBoxFlag(boolean value) {
        super(value, new StaticCaption("A flag that determines if non-members can place structure boxes in the plot"));
    }

    @Override
    protected StructureBoxFlag flagOf(@NotNull Boolean aBoolean) {
        return aBoolean ? STRUCTUREBOX_FLAG_TRUE : STRUCTUREBOX_FLAG_FALSE;
    }
}
