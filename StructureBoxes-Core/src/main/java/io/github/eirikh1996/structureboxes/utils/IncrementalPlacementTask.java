package io.github.eirikh1996.structureboxes.utils;

import com.sk89q.worldedit.util.Location;

import java.util.LinkedList;
import java.util.TimerTask;

public abstract class IncrementalPlacementTask extends TimerTask {
    protected final LinkedList<Location> placedLocations = new LinkedList<>();

    public LinkedList<Location> getPlacedLocations() {
        return placedLocations;
    }
}
