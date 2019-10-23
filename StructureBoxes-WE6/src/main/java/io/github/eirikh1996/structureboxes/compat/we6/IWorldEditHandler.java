package io.github.eirikh1996.structureboxes.compat.we6;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.PasteBuilder;
import com.sk89q.worldedit.world.World;
import io.github.eirikh1996.structureboxes.Direction;
import io.github.eirikh1996.structureboxes.SBMain;
import io.github.eirikh1996.structureboxes.StructureManager;
import io.github.eirikh1996.structureboxes.WorldEditHandler;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.eirikh1996.structureboxes.utils.RotationUtils;
import io.github.eirikh1996.structureboxes.utils.WorldEditLocation;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static java.lang.Math.PI;

public class IWorldEditHandler implements WorldEditHandler {
    private final File weDataFolder;
    private final SBMain sbMain;
    public IWorldEditHandler(File weDataFolder, SBMain sbMain){
        this.weDataFolder = weDataFolder;
        this.sbMain = sbMain;
    }
    @Override
    public Clipboard loadClipboardFromSchematic(World world, String schematicName) {
        File weConfig = new File(weDataFolder, "config.yml");
        Yaml yaml = new Yaml();
        final Map<String, Object> data;
        try {
            data = (Map<String, Object>) yaml.load(new FileInputStream(weConfig));
        } catch (FileNotFoundException e) {
            throw new WorldEditConfigException("Failed to load WorldEdit config", e);
        }
        String schematicDirectory = ((Map<String, String>) data.get("saving")).get("dir");
        String path = weDataFolder.getAbsolutePath() + "/" + schematicDirectory + "/" + schematicName + ".schematic";
        File schematicFile = new File(path);
        Clipboard clipboard;
        ClipboardFormat format = ClipboardFormat.findByFile(schematicFile);
        try {
            ClipboardReader reader = format.getReader(new FileInputStream(schematicFile));
            clipboard = reader.read(world.getWorldData());
        } catch (IOException e) {
            clipboard = null;
            e.printStackTrace();
        }
        return clipboard;
    }

    @Override
    public Direction getClipboardFacingFromOrigin(Clipboard clipboard, Location location) {
        Vector distance = clipboard.getMaximumPoint().subtract(clipboard.getOrigin());
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
        sbMain.getLogger().info(String.valueOf(angle));
        World world = pasteLoc.getWorld();
        ClipboardHolder holder = new ClipboardHolder(clipboard, world.getWorldData());
        AffineTransform transform = new AffineTransform();
        transform = transform.rotateY(angle);
        holder.setTransform(transform);
        PasteBuilder builder = holder.createPaste(world, world.getWorldData());
        builder.ignoreAirBlocks(true);
        Vector to = new Vector(pasteLoc.getX(), pasteLoc.getY(), pasteLoc.getZ());
        builder.to(to);
        ArrayList<Location> structureLocs = new ArrayList<Location>();
        int minX = clipboard.getMinimumPoint().getBlockX();
        int minY = clipboard.getMinimumPoint().getBlockY();
        int minZ = clipboard.getMinimumPoint().getBlockZ();
        int xLength = clipboard.getDimensions().getBlockX();
        int yLength = clipboard.getDimensions().getBlockY();
        int zLength = clipboard.getDimensions().getBlockZ();
        sbMain.getLogger().info(clipboard.getMinimumPoint().toString() + " " + clipboard.getMaximumPoint().toString());
        Vector offset = clipboard.getMinimumPoint().subtract(clipboard.getOrigin());
        Location minPoint = new Location(pasteLoc.getWorldID(), to.add(offset).getBlockX(), to.add(offset).getBlockY(), to.add(offset).getBlockZ());
        final double theta = angle * (PI / 180.0);
        for (int x = 0 ; x <= xLength ; x++){
            for (int y = 0 ; y <= yLength ; y++){
                for (int z = 0 ; z <= zLength ; z++){
                    BaseBlock baseBlock = clipboard.getBlock(new Vector(minX + x, minY + y, minZ + z));
                    if (baseBlock.getType() == 0){
                        continue;
                    }
                    Location loc = minPoint.add(x, y, z);

                    structureLocs.add(loc.rotate(theta, pasteLoc.toSBloc()));
                }
            }
        }
        sbMain.getLogger().info(String.valueOf(structureLocs.size()));
        final boolean freeSpace = sbMain.isFreeSpace(structureLocs);
        if (!freeSpace){
            return false;
        }
        StructureManager.getInstance().addStructure(structureLocs);
        try {
            Operation op = builder.build();
            Operations.complete(op);
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
