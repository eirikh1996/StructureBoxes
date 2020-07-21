package io.github.eirikh1996.structureboxes.utils;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.settings.Settings;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WorldGuardUtils {
    private static final Method CAN_BUILD = ReflectionUtils.getMethod(WorldGuardPlugin.class, "canBuild", Player.class, Location.class);
    private static final Method GET_REGION_MANAGER = ReflectionUtils.getMethod(WorldGuardPlugin.class,"getRegionManager", World.class);
    private static final Method GET_APPLICABLE_REGIONS = ReflectionUtils.getMethod(RegionManager.class, "getApplicableRegions", Location.class);

    public static StateFlag STRUCTUREBOX_FLAG = new StateFlag("structurebox", false);

    public static boolean allowBuild(Player player, Location location){
        if (canPlaceStructureBox(player, location)) {
            return true;
        }
        if (CAN_BUILD != null){
            try {
                return (boolean) CAN_BUILD.invoke(StructureBoxes.getInstance().getWorldGuardPlugin(), player, location);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return true;
            }
        } else {
            ApplicableRegionSet regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(player.getWorld())).getApplicableRegions(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
            LocalPlayer lp = StructureBoxes.getInstance().getWorldGuardPlugin().wrapPlayer(player);
            return lp.hasPermission("worldguard.region.bypass." + player.getWorld().getName()) || regions.isMemberOfAll(lp) || regions.isOwnerOfAll(lp);
        }

    }

    public static boolean insideRegion(Location location){
        if (GET_REGION_MANAGER != null && GET_APPLICABLE_REGIONS != null){
            try {
                RegionManager manager = (RegionManager) GET_REGION_MANAGER.invoke(StructureBoxes.getInstance().getWorldGuardPlugin(), location.getWorld());
                ApplicableRegionSet regions = (ApplicableRegionSet) GET_APPLICABLE_REGIONS.invoke(manager, location);
                return regions.size() > 0;
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(location.getWorld())).getApplicableRegions(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ())).size() > 0;
        }
    }

    public static boolean canPlaceStructureBox(Player player, Location loc) {
        LocalPlayer lp = StructureBoxes.getInstance().getWorldGuardPlugin().wrapPlayer(player);
        return getRegions(loc).queryState(lp) == StateFlag.State.ALLOW;
    }

    public static void registerFlag() {
        FlagRegistry flags;
        if (Settings.IsLegacy) {
            try {
                final Method getFlagRegistry = WorldGuardPlugin.class.getDeclaredMethod("getFlagRegistry");
                flags = (FlagRegistry) getFlagRegistry.invoke(StructureBoxes.getInstance().getWorldGuardPlugin());
            } catch (Exception e) {
                return;
            }

        } else {
            flags = WorldGuard.getInstance().getFlagRegistry();
        }
        flags.register(STRUCTUREBOX_FLAG);
    }

    public static ApplicableRegionSet getRegions(Location location) {
        final ApplicableRegionSet regions;
        if (GET_REGION_MANAGER != null && GET_APPLICABLE_REGIONS != null){
            try {
                RegionManager manager = (RegionManager) GET_REGION_MANAGER.invoke(StructureBoxes.getInstance().getWorldGuardPlugin(), location.getWorld());
                regions = (ApplicableRegionSet) GET_APPLICABLE_REGIONS.invoke(manager, location);

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(location.getWorld())).getApplicableRegions(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        }
        return regions;
    }
}
