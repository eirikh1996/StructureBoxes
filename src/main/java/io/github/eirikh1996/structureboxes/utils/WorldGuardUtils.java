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
    public static StateFlag STRUCTUREBOX_FLAG = new StateFlag("structurebox", false);

    public static boolean allowBuild(Player player, Location location){
        if (canPlaceStructureBox(player, location))
            return true;

        ApplicableRegionSet regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(player.getWorld())).getApplicableRegions(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        LocalPlayer lp = StructureBoxes.getInstance().getWorldGuardPlugin().wrapPlayer(player);
        return lp.hasPermission("worldguard.region.bypass." + player.getWorld().getName()) || regions.isMemberOfAll(lp) || regions.isOwnerOfAll(lp);
    }

    public static boolean insideRegion(Location location){
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(location.getWorld())).getApplicableRegions(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ())).size() > 0;
    }

    public static boolean canPlaceStructureBox(Player player, Location loc) {
        LocalPlayer lp = StructureBoxes.getInstance().getWorldGuardPlugin().wrapPlayer(player);
        return getRegions(loc).queryValue(lp, STRUCTUREBOX_FLAG) == StateFlag.State.ALLOW;
    }

    public static void registerFlag() {
        FlagRegistry flags;
        flags = WorldGuard.getInstance().getFlagRegistry();
        flags.register(STRUCTUREBOX_FLAG);
    }

    public static ApplicableRegionSet getRegions(Location location) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(location.getWorld())).getApplicableRegions(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }
}
