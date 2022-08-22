package io.github.eirikh1996.structureboxes.utils;

import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class StructureboxFlagV6 extends BooleanFlag<StructureboxFlagV6> {

    public static final StructureboxFlagV6 STRUCTUREBOX_FLAG_TRUE = new StructureboxFlagV6(true);
    public static final StructureboxFlagV6 STRUCTUREBOX_FLAG_FALSE = new StructureboxFlagV6(false);

    protected StructureboxFlagV6(boolean value) {
        super(value, StaticCaption.of("A flag that determines if non-members can place structure boxes in the plot"));
    }

    @Override
    protected StructureboxFlagV6 flagOf(@NotNull Boolean aBoolean) {
        return aBoolean ? STRUCTUREBOX_FLAG_TRUE : STRUCTUREBOX_FLAG_FALSE;
    }
}
