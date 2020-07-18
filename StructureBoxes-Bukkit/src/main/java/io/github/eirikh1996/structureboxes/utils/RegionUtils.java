package io.github.eirikh1996.structureboxes.utils;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.settings.Settings;
import org.bukkit.Location;
import org.bukkit.World;

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
        boolean civs = false;
        boolean lands = false;
        StructureBoxes structureBoxes = StructureBoxes.getInstance();
        if (structureBoxes.getWorldGuardPlugin() != null){
            worldguard = WorldGuardUtils.insideRegion(location);
        }
        if (structureBoxes.getFactionsPlugin() != null){
            factions = FactionsUtils.withinRegion(location);
        }
        if (structureBoxes.isFactionsUUIDInstalled()) {
            factions = FactionsUUIDUtils.isWithinRegion(location);
        }
        if (structureBoxes.getRedProtectPlugin() != null){
            redprotect = RedProtectUtils.withinRegion(location);
        }
        if (structureBoxes.getGriefPreventionPlugin() != null){
            griefprevention = GriefPreventionUtils.withinClaim(location);
        }
        if (structureBoxes.isPlotSquaredInstalled()){
            plotSquared = Settings.IsLegacy ? PlotSquaredUtils.withinPlot(location) : (Settings.UsePS5 ? PlotSquared5Utils.withinPlot(location) : PlotSquared4Utils.withinPlot(location)) ;
        }
        if (structureBoxes.getLandClaimingPlugin() != null){
            landClaiming = LandClaimingUtils.insideClaimedLand(location);
        }
        if (structureBoxes.getTownyPlugin() != null){
            towny = TownyUtils.insideTownBlock(location);
        }
        if (structureBoxes.getCivsPlugin() != null) {
            civs = CivsUtils.withinRegion(location);
        }
        if (structureBoxes.getLandsPlugin() != null) {
            lands = LandsUtils.isWithinRegion(location);
        }
        return worldguard || factions || redprotect || griefprevention || plotSquared || landClaiming || towny || civs || lands;
    }
}
