package io.github.eirikh1996.structureboxes.settings;

import io.github.eirikh1996.structureboxes.Platform;
import org.bukkit.Material;

import java.util.ArrayList;

public class Settings {
    public static String locale;
    public static Platform platform;
    public static Material StructureBoxItem;
    public static String StructureBoxLore;
    public static boolean RestrictToRegionsEnabled;
    public static boolean RequirePermissionPerStructureBox;
    public static ArrayList<String> RestrictToRegionsExceptions = new ArrayList<String>();
    public static ArrayList blocksToIgnore = new ArrayList();

}
