package io.github.eirikh1996.structureboxes;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.world.World;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.eirikh1996.structureboxes.utils.WorldEditLocation;

import java.util.UUID;

public interface WorldEditHandler {
    Clipboard loadClipboardFromSchematic(World world, String schematicName);
    Direction getClipboardFacingFromOrigin(Clipboard clipboard, Location location);
    boolean pasteClipboard(Clipboard clipboard, double angle, WorldEditLocation pasteLoc);
}
