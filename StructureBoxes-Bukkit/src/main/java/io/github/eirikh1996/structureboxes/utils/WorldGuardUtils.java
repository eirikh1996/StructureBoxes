package io.github.eirikh1996.structureboxes.utils;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
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

    public static boolean allowBuild(Player player, Location location){
        if (CAN_BUILD != null){
            try {
                return (boolean) CAN_BUILD.invoke(StructureBoxes.getInstance().getWorldGuardPlugin(), player, location);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return true;
            }
        }
        ApplicableRegionSet regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(player.getWorld())).getApplicableRegions(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        LocalPlayer lp = StructureBoxes.getInstance().getWorldGuardPlugin().wrapPlayer(player);
        return regions.isMemberOfAll(lp) || regions.isOwnerOfAll(lp);
    }
}
