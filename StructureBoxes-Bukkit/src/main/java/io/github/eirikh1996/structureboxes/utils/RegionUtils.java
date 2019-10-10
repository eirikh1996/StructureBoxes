package io.github.eirikh1996.structureboxes.utils;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.massivecore.ps.PS;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RegionUtils {
    private static Method GET_REGION_MANAGER;
    private static Method GET_APPLICABLE_REGIONS;

    static {
        try {
            GET_REGION_MANAGER = (StructureBoxes.getInstance().getWorldGuardPlugin() != null) ? WorldGuardPlugin.class.getDeclaredMethod("getRegionManager", World.class) : null ;
        } catch (NoSuchMethodException e) {
            GET_REGION_MANAGER = null;
        }
        try {
            GET_APPLICABLE_REGIONS = (StructureBoxes.getInstance().getWorldGuardPlugin() != null) ? RegionManager.class.getDeclaredMethod("getApplicableRegions", Location.class) : null;
        } catch (NoSuchMethodException e) {
            GET_APPLICABLE_REGIONS = null;
        }
    }

    public static boolean isWithinRegion(Location location){
        boolean worldguard = false;
        boolean factions = false;
        boolean redprotect = false;
        boolean griefprevention = false;
        StructureBoxes structureBoxes = StructureBoxes.getInstance();
        if (structureBoxes.getWorldGuardPlugin() != null){
            ApplicableRegionSet regions;
            if (GET_REGION_MANAGER != null){
                RegionManager manager;
                try {
                    manager = (RegionManager) GET_REGION_MANAGER.invoke(structureBoxes.getWorldGuardPlugin(), location.getWorld());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    manager = null;
                    e.printStackTrace();
                }
                if (manager != null){
                    try {
                        regions = (ApplicableRegionSet) GET_APPLICABLE_REGIONS.invoke(manager, location);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        regions = null;
                    }
                } else {
                    regions = null;
                }
            } else {
                regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(location.getWorld())).getApplicableRegions(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
            }
            worldguard = regions != null && regions.size() > 0;
        }
        if (structureBoxes.getFactionsPlugin() != null){
            PS ps = PS.valueOf(location);
            Faction faction = BoardColl.get().getFactionAt(ps);
            factions = faction != FactionColl.get().getNone();
        }
        if (structureBoxes.getRedProtectPlugin() != null){
            RedProtectAPI rpAPI = structureBoxes.getRedProtectPlugin().getAPI();
            redprotect = rpAPI.getRegion(location) != null;
        }
        if (structureBoxes.getGriefPreventionPlugin() != null){
            GriefPrevention gp = structureBoxes.getGriefPreventionPlugin();
            griefprevention = gp.dataStore.getClaimAt(location, false, null) != null;
        }
        return worldguard || factions || redprotect || griefprevention;
    }
}
