package io.github.eirikh1996.structureboxes;

import io.github.eirikh1996.structureboxes.utils.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public interface SBMain {
    WorldEditHandler getWorldEditHandler();
    Platform getPlatform();
    boolean isFreeSpace(List<Location> locations);
    void sendMessageToPlayer(UUID recipient, String message);
    Logger getLogger();
}
