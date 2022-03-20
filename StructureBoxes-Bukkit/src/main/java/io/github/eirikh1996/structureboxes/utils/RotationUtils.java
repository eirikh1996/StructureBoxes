package io.github.eirikh1996.structureboxes.utils;

import java.util.ArrayList;
import java.util.List;

public class RotationUtils {

    public static List<Location> rotatePasteLocs(List<Location> toRotate, Location originPoint, int angle){
        ArrayList<Location> ret = new ArrayList<Location>();
        double theta = (double) angle * (Math.PI / 180d);
        for (Location loc : toRotate){
            Location subtracted = originPoint.subtract(loc);
            int x = (int) Math.round((subtracted.getX() * Math.cos(theta)) + (subtracted.getZ() * (-1 * Math.sin(theta))));
            int z = (int) Math.round((subtracted.getX() * Math.cos(theta)) + (subtracted.getZ() * (-1 * Math.sin(theta))));
            Location rotated = new Location(originPoint.getWorld(), x, loc.getY(), z);
            ret.add(rotated.add(loc));
        }
        return ret;
    }
}
