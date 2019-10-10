package io.github.eirikh1996.structureboxes.compat.we6;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.PasteBuilder;
import com.sk89q.worldedit.world.World;
import io.github.eirikh1996.structureboxes.Direction;
import io.github.eirikh1996.structureboxes.Platform;
import io.github.eirikh1996.structureboxes.SBMain;
import io.github.eirikh1996.structureboxes.WorldEditHandler;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.WorldEditLocation;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

public class IWorldEditHandler implements WorldEditHandler {
    private final Plugin plugin;
    public IWorldEditHandler(Plugin plugin){
        this.plugin = plugin;
    }
    @Override
    public Clipboard loadClipboardFromSchematic(Player player, String schematicName) {
        WorldEditPlugin worldEditPlugin = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
        File weConfig = new File(worldEditPlugin.getDataFolder(), "config.yml");
        Yaml yaml = new Yaml();
        final Map<String, Object> data;
        try {
            data = (Map<String, Object>) yaml.load(new FileInputStream(weConfig));
        } catch (FileNotFoundException e) {
            throw new WorldEditConfigException("Failed to load WorldEdit config", e);
        }
        String schematicDirectory = ((Map<String, String>) data.get("saving")).get("dir");
        String path = worldEditPlugin.getDataFolder().getAbsolutePath() + "/" + schematicDirectory + "/" + schematicName + ".schematic";
        File schematicFile = new File(path);
        BukkitWorld bukkitWorld = new BukkitWorld(player.getWorld());
        Clipboard clipboard;
        ClipboardFormat format = ClipboardFormat.findByFile(schematicFile);
        try {
            ClipboardReader reader = format.getReader(new FileInputStream(schematicFile));
            clipboard = reader.read(bukkitWorld.getWorldData());
        } catch (IOException e) {
            clipboard = null;
            e.printStackTrace();
        }
        return clipboard;
    }

    @Override
    public Direction getClipboardFacingFromOrigin(Clipboard clipboard, Location location) {
        Vector distance = clipboard.getOrigin().subtract(new Vector(clipboard.getMinimumPoint().getBlockX() + clipboard.getDimensions().getBlockX() / 2,
                clipboard.getMinimumPoint().getBlockY() + clipboard.getDimensions().getBlockY() / 2,
                clipboard.getMinimumPoint().getBlockZ() + clipboard.getDimensions().getBlockZ() / 2));
        Direction returnFace;
        if (Math.abs(distance.getBlockX()) > Math.abs(distance.getBlockZ())){
            if (distance.getBlockX() > 0){
                return Direction.EAST;
            } else {
                return Direction.WEST;
            }
        } else {
            if (distance.getBlockZ() > 0){
                return Direction.SOUTH;
            } else {
                return Direction.NORTH;
            }
        }
    }


    @Override
    public boolean pasteClipboard(Clipboard clipboard, double angle, WorldEditLocation pasteLoc) {
        World world = pasteLoc.getWorld();
        ClipboardHolder holder = new ClipboardHolder(clipboard, world.getWorldData());
        AffineTransform transform = new AffineTransform();
        transform = transform.rotateY(angle);
        holder.setTransform(transform);
        PasteBuilder builder = holder.createPaste(world, world.getWorldData());
        builder.ignoreAirBlocks(true);
        Vector to = new Vector(pasteLoc.getX(), pasteLoc.getY(), pasteLoc.getZ());
        builder.to(to);
        final SBMain sbMain = (SBMain) plugin;
        final boolean freeSpace;
        if (sbMain.getPlatform() == Platform.BUKKIT){
            freeSpace = BukkitUtils.isFreeSpace(holder.getClipboard(), pasteLoc);
        } else {
            freeSpace = true;
        }
        if (!freeSpace){
            return false;
        }
        try {
            Operations.complete(builder.build());
        } catch (WorldEditException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private final class WorldEditConfigException extends RuntimeException{
        public WorldEditConfigException(String message, Throwable cause){
            super(message, cause);
        }
    }
}
