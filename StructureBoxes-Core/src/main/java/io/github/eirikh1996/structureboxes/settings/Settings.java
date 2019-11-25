package io.github.eirikh1996.structureboxes.settings;


import java.util.ArrayList;
import java.util.List;

public class Settings {
    public static String locale;
    public static boolean IsLegacy;
    public static boolean Debug;
    public static Object StructureBoxItem;
    public static String StructureBoxLore;
    public static String StructureBoxPrefix;
    public static String StructureBoxInstruction;
    public static List<String> AlternativePrefixes;
    public static boolean RestrictToRegionsEnabled;
    public static boolean RequirePermissionPerStructureBox;
    public static ArrayList<String> RestrictToRegionsExceptions = new ArrayList<>();
    public static ArrayList blocksToIgnore = new ArrayList();
    public static int MaxStructureSize;
    public static long MaxSessionTime;

    public static boolean Metrics;
    public static long PlaceCooldownTime;
    public static boolean CheckFreeSpace;
    public static boolean RestrictToRegionsEntireStructure;
}
