package io.github.eirikh1996.structureboxes.utils;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.massivecore.ps.PS;
import com.palmergames.bukkit.towny.TownyAPI;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.settings.Settings;
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
        boolean plotSquared = false;
        boolean landClaiming = false;
        boolean towny = false;
        StructureBoxes structureBoxes = StructureBoxes.getInstance();
        if (structureBoxes.getWorldGuardPlugin() != null){
            worldguard = WorldGuardUtils.insideRegion(location);
        }
        if (structureBoxes.getFactionsPlugin() != null){
            factions = FactionsUtils.withinRegion(location);
        }
        if (structureBoxes.getRedProtectPlugin() != null){
            redprotect = RedProtectUtils.withinRegion(location);
        }
        if (structureBoxes.getGriefPreventionPlugin() != null){
            griefprevention = GriefPreventionUtils.withinClaim(location);
        }
        if (structureBoxes.isPlotSquaredInstalled()){
            plotSquared = Settings.IsLegacy ? PlotSquaredUtils.withinPlot(location) : PlotSquared4Utils.withinPlot(location);
        }
        if (structureBoxes.getLandClaimingPlugin() != null){
            landClaiming = LandClaimingUtils.insideClaimedLand(location);
        }
        if (structureBoxes.getTownyPlugin() != null){
            towny = TownyUtils.insideTownBlock(location);
        }
        return worldguard || factions || redprotect || griefprevention || plotSquared || landClaiming || towny;
    }
}
