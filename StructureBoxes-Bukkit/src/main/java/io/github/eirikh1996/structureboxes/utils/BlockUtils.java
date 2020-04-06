package io.github.eirikh1996.structureboxes.utils;

import io.github.eirikh1996.structureboxes.settings.Settings;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

public class BlockUtils {
    public static boolean isFragile(Block block){
        Material type = block.getType();
        BlockState state = block.getState();
        return type.name().endsWith("_BED") ||
                state instanceof Sign ||
                type.name().endsWith("DOOR") ||
                type.name().endsWith("BUTTON") ||
                type.name().endsWith("_PLATE") ||
                type == Material.REDSTONE_WIRE ||
                type.name().endsWith("TORCH") ||
                type == Material.TRIPWIRE ||
                type == Material.TRIPWIRE_HOOK ||
                type == Material.LADDER ||
                type == Material.LEVER ||
                type == Material.DAYLIGHT_DETECTOR ||
                type == Material.CACTUS ||
                type == Material.COCOA ||
                (Settings.IsLegacy ?
                        (type == Material.getMaterial("BED_BLOCK") ||
                                type == Material.getMaterial("CROPS") ||
                                type == Material.getMaterial("PISTON_EXTENSION") ||
                                type == Material.getMaterial("SIGN_POST") ||
                                type == Material.getMaterial("WOOD_DOOR") ||
                                type == Material.getMaterial("WALL_SIGN") ||
                                type == Material.getMaterial("IRON_DOOR_BLOCK") ||
                                type == Material.getMaterial("REDSTONE_TORCH_OFF") ||
                                type == Material.getMaterial("REDSTONE_TORCH_ON") ||
                                type == Material.getMaterial("DIODE_BLOCK_OFF") ||
                                type == Material.getMaterial("DIODE_BLOCK_ON") ||
                                type == Material.getMaterial("TRAP_DOOR")  ||
                                type == Material.getMaterial("REDSTONE_COMPARATOR_OFF") ||
                                type == Material.getMaterial("REDSTONE_COMPARATOR_ON")  ||
                                type == Material.getMaterial("CARPET") ||
                                type == Material.getMaterial("DAYLIGHT_DETECTOR_INVERTED") ||
                                type == Material.POTATO ||
                                type == Material.CARROT)
                        :
                        (type == Material.REPEATER ||
                                type == Material.COMPARATOR ||
                                type == Material.CARROTS ||
                                type == Material.POTATOES)
                );
    }
}
