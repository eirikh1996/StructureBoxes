package io.github.eirikh1996.structureboxes.compat.we6;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
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

import static com.sk89q.worldedit.WorldEdit.getInstance;
import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;
import static java.lang.Math.PI;

public class IWorldEditHandler extends WorldEditHandler {

    private final int[] fragileBlocks = {26, 34, 50, 55, 63, 64, 65, 68, 69, 70, 71, 72, 75, 76, 77, 93, 94, 96, 131, 132, 143, 147, 148, 149, 150, 151, 171, 323, 324, 330, 331, 356, 404};
    private final int[] attachedBlocks = {50, 68 };

    public IWorldEditHandler(File schemDir, SBMain sbMain){
        super(schemDir, sbMain);
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
        Vector to = new Vector(pasteLoc.getX(), pasteLoc.getY(), pasteLoc.getZ());

        final Set<Location> structureLocs = new HashSet<>();
        int minX = clipboard.getMinimumPoint().getBlockX();
        int minY = clipboard.getMinimumPoint().getBlockY();
        int minZ = clipboard.getMinimumPoint().getBlockZ();
        int xLength = clipboard.getDimensions().getBlockX();
        int yLength = clipboard.getDimensions().getBlockY() + 1;
        int zLength = clipboard.getDimensions().getBlockZ();

        Vector offset = clipboard.getMinimumPoint().subtract(clipboard.getOrigin());
        Location minPoint = new Location(pasteLoc.getWorld().getName(), to.add(offset).getBlockX(), to.add(offset).getBlockY(), to.add(offset).getBlockZ());
        final double theta = -(angle * (PI / 180.0));

        final Collection<Location> solidStructure = new HashSet<>();
        final Collection<Location> boundingBox = new HashSet<>();
        final HashMap<Location, BaseBlock> blockHashMap = new HashMap<>();
        final Queue<Location> locationQueue = new LinkedList<>();

        final Clipboard fClipboard = new BlockArrayClipboard(clipboard.getRegion());
        fClipboard.setOrigin(clipboard.getOrigin());

        for (int x = 0 ; x <= xLength ; x++){
            for (int y = 0 ; y <= yLength ; y++){
                for (int z = 0 ; z <= zLength ; z++){
                    Location loc = minPoint.add(x, y, z).rotate(theta, pasteLoc.toSBloc());
                    solidStructure.add(loc);
                    if (x == 0 || x == xLength || y == 0 || y == yLength || z == 0 || z == zLength) {
                        boundingBox.add(loc);
                    }
                    Vector pos = new Vector(minX + x, minY + y, minZ + z);
                    BaseBlock baseBlock = clipboard.getBlock(pos);
                    int type = baseBlock.getType();
                    int data = baseBlock.getData();
                    if (type == 0){
                        continue;
                    }
/*
                    if (Arrays.binarySearch(fragileBlocks, type) >= 0) {
                        Location support = loc.subtract(0, 1, 0);
                        BaseBlock block = clipboard.getBlock(pos.subtract(0, 1, 0));
                        if (type == 65 || type == 68 || type == 177) {
                            sbMain.broadcast(String.valueOf(data));
                            Location add = new Location(pasteLoc.getWorldID(), 0, 1, 0);
                            switch (data) {
                                case 0x2: //north
                                    add = new Location(pasteLoc.getWorldID(), 0, 0, 1);
                                    block = clipboard.getBlock(pos.add(0, 0, 1));
                                    break;
                                case 0x3: //south
                                    add = new Location(pasteLoc.getWorldID(), 0, 0, -1);
                                    block = clipboard.getBlock(pos.subtract(0, 0, 1));
                                    break;
                                case 0x4: //east
                                    add = new Location(pasteLoc.getWorldID(), 1, 0, 0);
                                    block = clipboard.getBlock(pos.add(1, 0, 0));
                                    break;
                                case 0x5: //west
                                    add = new Location(pasteLoc.getWorldID(), -1, 0, 0);
                                    block = clipboard.getBlock(pos.subtract(1, 0, 0));
                                    break;
                            }
                            support = support.add(add.rotate(theta, pasteLoc.toSBloc()));
                        }
                        supportBlocks.put(support, block.getType());
                    }*/
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
        //sbMain.placeSupportBlocks(supportBlocks);

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

        if (Settings.IncrementalBlockPlacement) {
            final int queueSize = locationQueue.size();
            TimerTask task = new TimerTask(){
                int placedBlocks = 0;
                /**
                 * The action to be performed by this timer task.
                 */
                @Override
                public void run() {
                    if (locationQueue.isEmpty()) {
                        sbMain.sendMessageToPlayer(playerID, COMMAND_PREFIX + I18nSupport.getInternationalisedString("Placement - Complete"));
                        cancel();
                        playerIncrementPlacementMap.remove(playerID);
                        return;
                    }
                    final Location poll = locationQueue.poll();
                    final BaseBlock block = blockHashMap.remove(poll);
                    placedBlocks++;
                    final float percent = (placedBlocks / queueSize) * 100f;
                    if ((int) percent % 5 == 0) {
                        sbMain.sendMessageToPlayer(playerID, COMMAND_PREFIX + I18nSupport.getInternationalisedString("Placement - Progress").replace("{PERCENTAGE}", String.valueOf(percent)));
                    }
                    sbMain.scheduleSyncTask(() -> {
                        try {
                            world.setBlock(vectorFromLocation(poll), block, true);
                        } catch (WorldEditException e) {
                            e.printStackTrace();
                        }
                    });
                }
            };
            new Timer().schedule(task, 0, Settings.IncrementalPlacementDelay);
            playerIncrementPlacementMap.put(playerID, task);
            return true;
        }

        sbMain.scheduleSyncTask(() -> {
            final long startTime = System.currentTimeMillis();
            try {
                final EditSession session = getInstance().getEditSessionFactory().getEditSession(world, -1);
                session.enableQueue();
                Operation op = holder.createPaste(session, session.getWorld().getWorldData())
                        .ignoreAirBlocks(true)
                        .to(to)
                        .build();
                Operations.completeLegacy(op);
                session.flushQueue();
                //sbMain.clearInterior(interior);
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

    private Vector vectorFromLocation(Location poll) {
        return new Vector(poll.getX(), poll.getY(), poll.getZ());
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
