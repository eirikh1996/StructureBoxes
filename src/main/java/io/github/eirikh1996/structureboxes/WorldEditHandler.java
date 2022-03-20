package io.github.eirikh1996.structureboxes;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.world.World;
import io.github.eirikh1996.structureboxes.utils.IncrementalPlacementTask;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.eirikh1996.structureboxes.utils.WorldEditLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class WorldEditHandler {
    protected final File schemDir;
    protected final SBMain sbMain;
    protected final Map<UUID, IncrementalPlacementTask> playerIncrementPlacementMap = new HashMap<>();

    protected WorldEditHandler(File schemDir, SBMain sbMain) {
        this.schemDir = schemDir;
        this.sbMain = sbMain;
    }

    @Nullable public abstract Clipboard loadClipboardFromSchematic(@NotNull World world, @NotNull String schematicName);
    @NotNull public abstract Direction getClipboardFacingFromOrigin(@NotNull Clipboard clipboard, @NotNull Location location);
    public abstract boolean pasteClipboard(@NotNull UUID playerID, @NotNull String schematicName, @NotNull Clipboard clipboard, double angle, WorldEditLocation pasteLoc);
    public abstract int getStructureSize(@NotNull Clipboard clipboard);

    public Map<UUID, IncrementalPlacementTask> getPlayerIncrementPlacementMap() {
        return playerIncrementPlacementMap;
    }

    public File getSchemDir() {
        return schemDir;
    }
}
