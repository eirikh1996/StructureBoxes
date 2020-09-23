package io.github.eirikh1996.structureboxes;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.world.World;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.eirikh1996.structureboxes.utils.WorldEditLocation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.UUID;

public abstract class WorldEditHandler {
    protected final File schemDir;
    protected final SBMain sbMain;
    protected final Map<UUID, TimerTask> playerIncrementPlacementMap = new HashMap<>();

    protected WorldEditHandler(File schemDir, SBMain sbMain) {
        this.schemDir = schemDir;
        this.sbMain = sbMain;
    }

    public abstract Clipboard loadClipboardFromSchematic(World world, String schematicName);
    public abstract Direction getClipboardFacingFromOrigin(Clipboard clipboard, Location location);
    public abstract boolean pasteClipboard(UUID playerID, String schematicName, Clipboard clipboard, double angle, WorldEditLocation pasteLoc);
    public abstract int getStructureSize(Clipboard clipboard);

    public Map<UUID, TimerTask> getPlayerIncrementPlacementMap() {
        return playerIncrementPlacementMap;
    }
}
