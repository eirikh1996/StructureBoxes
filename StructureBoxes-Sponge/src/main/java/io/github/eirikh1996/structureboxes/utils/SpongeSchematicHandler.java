package io.github.eirikh1996.structureboxes.utils;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.world.World;
import io.github.eirikh1996.structureboxes.Direction;
import io.github.eirikh1996.structureboxes.WorldEditHandler;

import java.util.UUID;

public class SpongeSchematicHandler extends WorldEditHandler {
    @Override
    public Clipboard loadClipboardFromSchematic(World world, String schematicName) {
        return null;
    }

    @Override
    public Direction getClipboardFacingFromOrigin(Clipboard clipboard, Location location) {
        return null;
    }

    @Override
    public boolean pasteClipboard(UUID playerID, String schematicName, Clipboard clipboard, double angle, WorldEditLocation pasteLoc) {
        return false;
    }

    @Override
    public int getStructureSize(Clipboard clipboard) {
        return 0;
    }
}
