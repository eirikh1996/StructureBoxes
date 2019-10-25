package io.github.eirikh1996.structureboxes.utils;

import io.github.eirikh1996.structureboxes.settings.Settings;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class BlockUtils {
    public static boolean isFragile(Material type){
        return type.name().endsWith("SIGN") ||
                type.name().equals("SIGN_POST") ||
                type.name().endsWith("BUTTON") ||
                type.name().endsWith("TORCH") ||
                type == Material.LADDER;
    }
}
