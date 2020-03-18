package io.github.eirikh1996.structureboxes.compat.we6;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.PasteBuilder;
import com.sk89q.worldedit.world.World;
import io.github.eirikh1996.structureboxes.*;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.CollectionUtils;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.eirikh1996.structureboxes.utils.WorldEditLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;
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

        //String path = weDataFolder.getAbsolutePath() + "/" + schematicDirectory + "/" + schematicName + ".schematic";
        File schematicFile = new File(schemDir, schematicName + ".schematic");
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
        Vector centerpoint = clipboard.getMinimumPoint().add(clipboard.getDimensions().divide(2));
        Vector distance = centerpoint.subtract(clipboard.getOrigin());
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
        ClipboardHolder holder = new ClipboardHolder(clipboard, world.getWorldData());
        AffineTransform transform = new AffineTransform();
        transform = transform.rotateY(angle);
        holder.setTransform(transform);
        PasteBuilder builder = holder.createPaste(world, world.getWorldData());
        builder.ignoreAirBlocks(true);
        Vector to = new Vector(pasteLoc.getX(), pasteLoc.getY(), pasteLoc.getZ());
        builder.to(to);
        final Set<Location> structureLocs = new HashSet<>();
        int minX = clipboard.getMinimumPoint().getBlockX();
        int minY = clipboard.getMinimumPoint().getBlockY();
        int minZ = clipboard.getMinimumPoint().getBlockZ();
        int xLength = clipboard.getDimensions().getBlockX();
        int yLength = clipboard.getDimensions().getBlockY() + 1;
        int zLength = clipboard.getDimensions().getBlockZ();


        Vector offset = clipboard.getMinimumPoint().subtract(clipboard.getOrigin());
        Location minPoint = new Location(pasteLoc.getWorldID(), to.add(offset).getBlockX(), to.add(offset).getBlockY(), to.add(offset).getBlockZ());
        final double theta = -(angle * (PI / 180.0));

        final Collection<Location> solidStructure = new HashSet<>();
        final Collection<Location> boundingBox = new HashSet<>();

        for (int x = 0 ; x <= xLength ; x++){
            for (int y = 0 ; y <= yLength ; y++){
                for (int z = 0 ; z <= zLength ; z++){
                    Location loc = minPoint.add(x, y, z).rotate(theta, pasteLoc.toSBloc());
                    solidStructure.add(loc);
                    if (x == 0 || x == xLength || y == 0 || y == yLength || z == 0 || z == zLength) {
                        boundingBox.add(loc);
                    }
                    BaseBlock baseBlock = clipboard.getBlock(new Vector(minX + x, minY + y, minZ + z));
                    if (baseBlock.getType() == 0){
                        continue;
                    }


                    structureLocs.add(loc);
                }
            }
        }
        Collection<Location> invertedStructure = CollectionUtils.filter(solidStructure, structureLocs);
        Collection<Location> exterior = CollectionUtils.filter(boundingBox, structureLocs);
        Collection<Location> visited = new HashSet<>();
        Queue<Location> queue = new LinkedList<>(exterior);
        while (!queue.isEmpty()){
            Location node = queue.poll();
            if (visited.contains(node))
                continue;
            visited.add(node);
            queue.addAll(CollectionUtils.neighbors(invertedStructure, node));
        }
        Collection<Location> confirmed = new HashSet<>(visited);
        final Collection<Location> interior = CollectionUtils.filter(invertedStructure, confirmed);
        structureLocs.addAll(interior);
        final boolean freeSpace = sbMain.isFreeSpace(playerID, schematicName, structureLocs);
        if (!freeSpace){
            return false;
        }
        if (!sbMain.structureWithinRegion(playerID, schematicName, structureLocs)){
            return false;
        }
        final Structure structure = StructureManager.getInstance().getCorrespondingStructure(structureLocs);
        for (Location loc : structureLocs) {
            if (loc.getY() <= 255) {
                continue;
            }
            sbMain.sendMessageToPlayer(playerID, COMMAND_PREFIX + I18nSupport.getInternationalisedString("Place - World height exceeded"));
            return false;
        }
        if (Settings.Debug){
            final long end = System.currentTimeMillis();
            sbMain.broadcast("Structure algorithm took (ms): " + (end - start));
        }
        sbMain.scheduleSyncTask(() -> {
            final long startTime = System.currentTimeMillis();
            try {
                final ForwardExtentCopy copy = (ForwardExtentCopy) builder.build();
                Operations.completeLegacy(copy);
                sbMain.clearInterior(interior);
                sbMain.removeItems(pasteLoc.getWorldID(), structure);
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
            if (structure != null) {
                structure.setProcessing(false);
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
                    Vector pos = new Vector(minX + x, minY + y, minZ + z);
                    if (clipboard.getBlock(pos).getType() == 0){
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
