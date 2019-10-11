package io.github.eirikh1996.structureboxes;

import io.github.eirikh1996.structureboxes.utils.Location;

import java.util.ArrayList;

public interface SBMain {
    WorldEditHandler getWorldEditHandler();
    Platform getPlatform();
    boolean isFreeSpace(ArrayList<Location> locations);
}
