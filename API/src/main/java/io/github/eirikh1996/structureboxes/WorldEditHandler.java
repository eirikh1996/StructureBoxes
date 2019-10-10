package io.github.eirikh1996.structureboxes;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import io.github.eirikh1996.structureboxes.utils.WorldEditLocation;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public interface WorldEditHandler {
    Clipboard loadClipboardFromSchematic(Player player, String schematicName);
    Direction getClipboardFacingFromOrigin(Clipboard clipboard, Location location);
    boolean pasteClipboard(Clipboard clipboard, double angle, WorldEditLocation pasteLoc);
}
