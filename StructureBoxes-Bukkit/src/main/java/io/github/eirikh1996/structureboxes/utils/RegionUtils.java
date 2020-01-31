package io.github.eirikh1996.structureboxes.utils;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.settings.Settings;
import org.bukkit.Location;

public class RegionUtils {


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
        if (structureBoxes.getCivsPlugin() != null) {
            civs = CivsUtils.withinRegion(location);
        }
        if (structureBoxes.getLandsPlugin() != null) {
            lands = LandsUtils.isWithinRegion(location);
        }
        return worldguard || factions || redprotect || griefprevention || plotSquared || landClaiming || towny || civs || lands;
    }

    public static boolean canPlaceStructure(Location loc) {
        boolean worldguard = false;
        boolean factions = false;
        boolean redprotect = false;
        boolean plotSquared = false;
        final StructureBoxes sb = StructureBoxes.getInstance();
        if (sb.getFactionsPlugin() != null) {
            factions = FactionsUtils.canPlaceStructureBox(loc);
        }
        if (sb.getRedProtectPlugin() != null) {
            redprotect = RedProtectUtils.canPlaceStructureBox(loc);
        }
        if (sb.getWorldGuardPlugin() != null) {
            worldguard = WorldGuardUtils.canPlaceStructureBox(loc);
        }
        if (sb.isPlotSquaredInstalled()) {
            plotSquared = PlotSquaredUtils.canPlaceStructureBox(loc);
        }
        return worldguard || factions || redprotect || plotSquared;

    }

}
