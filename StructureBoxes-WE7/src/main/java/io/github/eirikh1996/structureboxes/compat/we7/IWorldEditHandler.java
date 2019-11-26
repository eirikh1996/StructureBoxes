package io.github.eirikh1996.structureboxes.compat.we7;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.PasteBuilder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import io.github.eirikh1996.structureboxes.Direction;
import io.github.eirikh1996.structureboxes.SBMain;
import io.github.eirikh1996.structureboxes.StructureManager;
import io.github.eirikh1996.structureboxes.WorldEditHandler;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.CollectionUtils;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.eirikh1996.structureboxes.utils.WorldEditLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static java.lang.Math.PI;

public class IWorldEditHandler extends WorldEditHandler {
    private final File schemDir;
    private final SBMain sbMain;
    public IWorldEditHandler(File schemDir, SBMain sbMain){
        this.schemDir = schemDir;
        this.sbMain = sbMain;
    }
    @Override
    public Clipboard loadClipboardFromSchematic(World world, String schematicName) {
        String path = schemDir.getAbsolutePath() + "/" + schematicName + ".schematic";
        File schematicFile = new File(path);
        if (!schematicFile.exists()){
            path = schemDir.getAbsolutePath() + "/" + schematicName + ".schem";
            schematicFile = new File(path);
        }
        if (!schematicFile.exists()){
            return null;
        }
        Clipboard clipboard;
        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        try {
            ClipboardReader reader = format.getReader(new FileInputStream(schematicFile));
            clipboard = reader.read();
        } catch (IOException e) {
            clipboard = null;
            e.printStackTrace();
        }
        return clipboard;
    }

    @Override
    public Direction getClipboardFacingFromOrigin(Clipboard clipboard, Location location) {
        BlockVector3 centerpoint = clipboard.getMinimumPoint().add(clipboard.getDimensions().divide(2));
        BlockVector3 distance = centerpoint.subtract(clipboard.getOrigin());
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
    public boolean pasteClipboard(UUID playerID, String schematicName, Clipboard clipboard, double angle, WorldEditLocation pasteLoc) {
        final long start = System.currentTimeMillis();
        World world = pasteLoc.getWorld();
        ClipboardHolder holder = new ClipboardHolder(clipboard);
        AffineTransform transform = new AffineTransform();
        transform = transform.rotateY(angle);
        holder.setTransform(transform);
        PasteBuilder builder = holder.createPaste(world);
        builder.ignoreAirBlocks(true);
        BlockVector3 to = BlockVector3.at(pasteLoc.getX(), pasteLoc.getY(), pasteLoc.getZ());
        builder.to(to);
        final Set<Location> structureLocs = new HashSet<>();
        int minX = clipboard.getMinimumPoint().getBlockX();
        int minY = clipboard.getMinimumPoint().getBlockY();
        int minZ = clipboard.getMinimumPoint().getBlockZ();
        int xLength = clipboard.getDimensions().getBlockX();
        int yLength = clipboard.getDimensions().getBlockY();
        int zLength = clipboard.getDimensions().getBlockZ();
        BlockVector3 offset = clipboard.getMinimumPoint().subtract(clipboard.getOrigin());
        Location minPoint = new Location(pasteLoc.getWorldID(), to.add(offset).getBlockX(), to.add(offset).getBlockY(), to.add(offset).getBlockZ());
        final double theta = -(angle * (PI / 180.0));
        final Set<Location> invertedStructure = new HashSet<>();
        Location min = minPoint.rotate(theta, pasteLoc.toSBloc());
        Location max = minPoint.add(xLength, yLength, zLength).rotate(theta, pasteLoc.toSBloc());
        final ArrayList<Collection<Location>> surfaces = new ArrayList<>(5);
        for (int i = 0 ; i <= 4 ; i++){
            surfaces.add(i, new HashSet<>());
        }
        for (int x = 0 ; x <= xLength ; x++){
            for (int y = 0 ; y <= yLength ; y++){
                for (int z = 0 ; z <= zLength ; z++){
                    Location loc = minPoint.add(x, y, z).rotate(theta, pasteLoc.toSBloc());
                    if (x == 0){
                        surfaces.get(0).add(loc);
                    }
                    if (x == xLength){
                        surfaces.get(1).add(loc);
                    }
                    if (y == 0){
                        surfaces.get(2).add(loc);
                    }
                    if (z == 0){
                        surfaces.get(3).add(loc);
                    }
                    if (z == xLength){
                        surfaces.get(4).add(loc);
                    }
                    BaseBlock baseBlock = clipboard.getFullBlock(BlockVector3.at(minX + x, minY + y, minZ + z));
                    if (baseBlock.getBlockType().getId().equalsIgnoreCase("minecraft:air") || baseBlock.getBlockType().getId().equalsIgnoreCase("minecraft:cave_air") || baseBlock.getBlockType().getId().equalsIgnoreCase("minecraft:void_air")){
                        invertedStructure.add(loc);
                        continue;
                    }
                    structureLocs.add(loc);
                }
            }
        }
        final Collection<Location> exterior = new HashSet<>();
        for (Collection<Location> surface : surfaces){
            exterior.addAll(surface);
        }
        Collection<Location> confirmed = new HashSet<>();
        for (Location exteriorLoc : exterior){
            Collection<Location> visited = new HashSet<>();
            Queue<Location> queue = new LinkedList<>();
            queue.add(exteriorLoc);
            while (!queue.isEmpty()){
                Location node = queue.poll();
                for (Location neighbor : CollectionUtils.neighbors(invertedStructure, node)){
                    if (confirmed.contains(neighbor) || visited.contains(neighbor)){
                        continue;
                    }
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
            confirmed.addAll(visited);
        }


        final Collection<Location> interior = CollectionUtils.filter(invertedStructure, exterior);
        if (!sbMain.isFreeSpace(playerID, schematicName, structureLocs)){
            return false;
        }

        if (!sbMain.structureWithinRegion(playerID, schematicName, structureLocs)){
            return false;
        }
        StructureManager.getInstance().addStructure(structureLocs);
        if (Settings.Debug){
            final long end = System.currentTimeMillis();
            sbMain.broadcast("Structure algorithm took (ms): " + (end - start));
        }
        sbMain.scheduleSyncTask(() -> {
            final long startTime = System.currentTimeMillis();
            try {
                Operations.complete(builder.build());
                sbMain.clearInterior(interior);
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
            StructureManager.getInstance().addStructureByPlayer(playerID, structureLocs);
            if (Settings.Debug){
                final long end = System.currentTimeMillis();
                sbMain.broadcast("Structure placement took (ms): " + (end - startTime));
            }
        });


        return true;
    }

    @Override
    public int getStructureSize(Clipboard clipboard) {
        int count = 0;
        int minX = clipboard.getMinimumPoint().getBlockX();
        int minY = clipboard.getMinimumPoint().getBlockY();
        int minZ = clipboard.getMinimumPoint().getBlockZ();
        for (int x = 0 ; x <= clipboard.getDimensions().getBlockX(); x++){
            for (int y = 0 ; y <= clipboard.getDimensions().getBlockY(); y++){
                for (int z = 0 ; z <= clipboard.getDimensions().getBlockZ(); z++){
                    BlockVector3 pos = BlockVector3.at(minX + x, minY + y, minZ + z);
                    if (clipboard.getBlock(pos).getBlockType().getId().equalsIgnoreCase("minecraft:air")||clipboard.getBlock(pos).getBlockType().getId().equalsIgnoreCase("minecraft:cave_air")||clipboard.getBlock(pos).getBlockType().getId().equalsIgnoreCase("minecraft:void_air")){
                        continue;
                    }
                    count++;
                }
            }
        }
        return count;
    }




    private static final class WorldEditConfigException extends RuntimeException{
        public WorldEditConfigException(String message, Throwable cause){
            super(message, cause);
        }
    }
}
