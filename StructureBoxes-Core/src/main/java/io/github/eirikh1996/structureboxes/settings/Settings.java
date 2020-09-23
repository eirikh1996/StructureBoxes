package io.github.eirikh1996.structureboxes.settings;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Settings {
    public static String locale;
    public static boolean FAWE, IsLegacy, UsePS5, Debug;
    public static Object StructureBoxItem;
    public static String StructureBoxLore;
    public static String StructureBoxPrefix, PluginPrefix;
    public static List<String> StructureBoxInstruction = new ArrayList<>();
    public static List<String> AlternativePrefixes = new ArrayList<>();
    public static boolean RestrictToRegionsEnabled, RequirePermissionPerStructureBox, IncrementalBlockPlacement;
    public static List<String> RestrictToRegionsExceptions = new ArrayList<>();
    public static Set blocksToIgnore = new HashSet<>();
    public static int MaxStructureSize;
    public static long MaxSessionTime;
    public static int IncrementalPlacementDelay;

    public static boolean Metrics;
    public static long PlaceCooldownTime;
    public static boolean CheckFreeSpace;
    public static boolean RestrictToRegionsEntireStructure;
}
