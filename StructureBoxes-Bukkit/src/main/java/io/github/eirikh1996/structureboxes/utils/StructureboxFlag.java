package io.github.eirikh1996.structureboxes.utils;

import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class StructureboxFlag extends BooleanFlag<StructureboxFlag> {

    public static final StructureboxFlag STRUCTUREBOX_FLAG_TRUE = new StructureboxFlag(true);
    public static final StructureboxFlag STRUCTUREBOX_FLAG_FALSE = new StructureboxFlag(false);

    protected StructureboxFlag(boolean value) {
        super(value, StaticCaption.of("A flag that determines if non-members can place structure boxes in the plot"));
    }

    protected StructureboxFlag() {
        this(false);
    }

    public static void register() {
        GlobalFlagContainer.getInstance().addFlag(new StructureboxFlag());
    }

    @Override
    protected StructureboxFlag flagOf(@NotNull Boolean aBoolean) {
        return aBoolean ? STRUCTUREBOX_FLAG_TRUE : STRUCTUREBOX_FLAG_FALSE;
    }
}
