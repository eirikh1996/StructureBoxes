package io.github.eirikh1996.structureboxes.utils;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;

import java.util.HashSet;
import java.util.Set;

public class BlockUtils {
    private static final Set<BlockType> FRAGILE_BLOCKS = new HashSet<>();
    private static final Set<BlockType> ATTACHABLE_BLOCKS = new HashSet<>();
    
    static {
        FRAGILE_BLOCKS.add(BlockTypes.STANDING_SIGN);
        FRAGILE_BLOCKS.add(BlockTypes.WALL_SIGN);
        FRAGILE_BLOCKS.add(BlockTypes.REDSTONE_WIRE);
        FRAGILE_BLOCKS.add(BlockTypes.LADDER);
        FRAGILE_BLOCKS.add(BlockTypes.POWERED_REPEATER);
        FRAGILE_BLOCKS.add(BlockTypes.UNPOWERED_REPEATER);
        FRAGILE_BLOCKS.add(BlockTypes.POWERED_COMPARATOR);
        FRAGILE_BLOCKS.add(BlockTypes.UNPOWERED_COMPARATOR);
        FRAGILE_BLOCKS.add(BlockTypes.BED);
        FRAGILE_BLOCKS.add(BlockTypes.WHEAT);
        FRAGILE_BLOCKS.add(BlockTypes.POTATOES);
        FRAGILE_BLOCKS.add(BlockTypes.CARROTS);
        FRAGILE_BLOCKS.add(BlockTypes.BEETROOTS);
        FRAGILE_BLOCKS.add(BlockTypes.CACTUS);
        FRAGILE_BLOCKS.add(BlockTypes.CHORUS_FLOWER);
        FRAGILE_BLOCKS.add(BlockTypes.CHORUS_PLANT);
        FRAGILE_BLOCKS.add(BlockTypes.RED_FLOWER);
        FRAGILE_BLOCKS.add(BlockTypes.YELLOW_FLOWER);
        FRAGILE_BLOCKS.add(BlockTypes.DOUBLE_PLANT);
        FRAGILE_BLOCKS.add(BlockTypes.STONE_BUTTON);
        FRAGILE_BLOCKS.add(BlockTypes.WOODEN_BUTTON);
        FRAGILE_BLOCKS.add(BlockTypes.DARK_OAK_DOOR);
        FRAGILE_BLOCKS.add(BlockTypes.ACACIA_DOOR);
        FRAGILE_BLOCKS.add(BlockTypes.BIRCH_DOOR);
        FRAGILE_BLOCKS.add(BlockTypes.IRON_DOOR);
        FRAGILE_BLOCKS.add(BlockTypes.JUNGLE_DOOR);
        FRAGILE_BLOCKS.add(BlockTypes.SPRUCE_DOOR);
        FRAGILE_BLOCKS.add(BlockTypes.WOODEN_DOOR);
        FRAGILE_BLOCKS.add(BlockTypes.TRAPDOOR);
        FRAGILE_BLOCKS.add(BlockTypes.IRON_TRAPDOOR);
        FRAGILE_BLOCKS.add(BlockTypes.TRIPWIRE);
        FRAGILE_BLOCKS.add(BlockTypes.TRIPWIRE_HOOK);
        FRAGILE_BLOCKS.add(BlockTypes.CARPET);
        FRAGILE_BLOCKS.add(BlockTypes.TORCH);
        FRAGILE_BLOCKS.add(BlockTypes.WALL_BANNER);
        FRAGILE_BLOCKS.add(BlockTypes.STANDING_BANNER);
        FRAGILE_BLOCKS.add(BlockTypes.REDSTONE_TORCH);
        FRAGILE_BLOCKS.add(BlockTypes.UNLIT_REDSTONE_TORCH);
        FRAGILE_BLOCKS.add(BlockTypes.ACTIVATOR_RAIL);
        FRAGILE_BLOCKS.add(BlockTypes.DETECTOR_RAIL);
        FRAGILE_BLOCKS.add(BlockTypes.GOLDEN_RAIL);
        FRAGILE_BLOCKS.add(BlockTypes.RAIL);

        ATTACHABLE_BLOCKS.add(BlockTypes.WALL_BANNER);
        ATTACHABLE_BLOCKS.add(BlockTypes.WALL_SIGN);
        ATTACHABLE_BLOCKS.add(BlockTypes.LADDER);
        ATTACHABLE_BLOCKS.add(BlockTypes.LEVER);
        ATTACHABLE_BLOCKS.add(BlockTypes.TORCH);
        ATTACHABLE_BLOCKS.add(BlockTypes.REDSTONE_TORCH);
        ATTACHABLE_BLOCKS.add(BlockTypes.UNLIT_REDSTONE_TORCH);
    }


    public static boolean isFragile(BlockState state) {
        return FRAGILE_BLOCKS.contains(state.getType());
    }

    public static boolean attachable(BlockType type) {
        return ATTACHABLE_BLOCKS.contains(type);
    }
}
