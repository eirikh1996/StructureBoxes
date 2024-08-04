package io.github.eirikh1996.structureboxes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.reorder.MultiStageReorder;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.PasteBuilder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import io.github.eirikh1996.structureboxes.settings.Settings;
import io.github.eirikh1996.structureboxes.utils.CollectionUtils;
import io.github.eirikh1996.structureboxes.utils.IncrementalPlacementTask;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.eirikh1996.structureboxes.utils.WorldEditLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;
import static java.lang.Math.PI;

public class WorldEditHandler {
    protected final File schemDir;
    protected final SBMain sbMain;
    protected final Map<UUID, IncrementalPlacementTask> playerIncrementPlacementMap = new HashMap<>();

    protected WorldEditHandler(File schemDir, SBMain sbMain) {
        this.schemDir = schemDir;
        this.sbMain = sbMain;
    }

    @NotNull private static final Map<BlockType, MultiStageReorder.PlacementPriority> priorityMap;
    static {
        Map<BlockType, MultiStageReorder.PlacementPriority> priorityMap1;
        try {
            Field pmField = MultiStageReorder.class.getDeclaredField("priorityMap");
            pmField.setAccessible(true);
            priorityMap1 = (Map<BlockType, MultiStageReorder.PlacementPriority>) pmField.get(null);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            priorityMap1 = new HashMap<>();
        }
        priorityMap = priorityMap1;
    }

    @Nullable
    public Clipboard loadClipboardFromSchematic(@NotNull World world, @NotNull String schematicName) {
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



    public @NotNull Direction getClipboardFacingFromOrigin(@NotNull Clipboard clipboard, @NotNull Location location) {
        BlockVector3 centerpoint = clipboard.getMinimumPoint().add(clipboard.getDimensions().divide(2));
        BlockVector3 distance = centerpoint.subtract(clipboard.getOrigin());
        if (Math.abs(distance.x()) > Math.abs(distance.z())){
            if (distance.x() > 0){
                return Direction.EAST;
            } else {
                return Direction.WEST;
            }
        } else {
            if (distance.z() > 0){
                return Direction.SOUTH;
            } else {
                return Direction.NORTH;
            }
        }
    }


    public boolean pasteClipboard(@NotNull UUID playerID, @NotNull String schematicName, @NotNull Clipboard clipboard, double angle, WorldEditLocation pasteLoc) {
        final long start = System.currentTimeMillis();
        World world = pasteLoc.getWorld();
        ClipboardHolder holder = new ClipboardHolder(clipboard);
        AffineTransform transform = new AffineTransform();
        transform = transform.rotateY(angle);
        holder.setTransform(transform);
        BlockVector3 to = BlockVector3.at(pasteLoc.getX(), pasteLoc.getY(), pasteLoc.getZ());
        final Set<Location> structureLocs = new HashSet<>();
        int minX = clipboard.getMinimumPoint().x();
        int minY = clipboard.getMinimumPoint().y();
        int minZ = clipboard.getMinimumPoint().z();
        int xLength = clipboard.getDimensions().x();
        int yLength = clipboard.getDimensions().y();
        int zLength = clipboard.getDimensions().z();
        BlockVector3 offset = clipboard.getMinimumPoint().subtract(clipboard.getOrigin());
        Location minPoint = new Location(pasteLoc.getWorld().getName(), to.add(offset).x(), to.add(offset).y(), to.add(offset).z());
        final double theta = -(angle * (PI / 180.0));


        final Collection<Location> solidStructure = new HashSet<>();
        final Collection<Location> boundingBox = new HashSet<>();
        final HashMap<Location, BaseBlock> blockHashMap = new HashMap<>();
        final LinkedList<Location> locationQueue = new LinkedList<>();

        for (MultiStageReorder.PlacementPriority priority : MultiStageReorder.PlacementPriority.values()) {
            for (int y = 0 ; y <= yLength ; y++){
                for (int x = 0 ; x <= xLength ; x++){
                    for (int z = 0 ; z <= zLength ; z++){
                        Location loc = minPoint.add(x, y, z).rotate(theta, pasteLoc.toSBloc());
                        solidStructure.add(loc);
                        if ((x == 0 || x == xLength || y == 0 || y == yLength || z == 0 || z == zLength) && !boundingBox.contains(loc)) {
                            boundingBox.add(loc);
                        }
                        BaseBlock baseBlock = clipboard.getFullBlock(BlockVector3.at(minX + x, minY + y, minZ + z));
                        if (priority != priorityMap.getOrDefault(baseBlock.getBlockType(), MultiStageReorder.PlacementPriority.FIRST))
                            continue;
                        locationQueue.add(loc);
                        if (
                                baseBlock.getBlockType().id().equalsIgnoreCase("minecraft:air") ||
                                        baseBlock.getBlockType().id().equalsIgnoreCase("minecraft:cave_air") ||
                                        baseBlock.getBlockType().id().equalsIgnoreCase("minecraft:void_air")
                        ){
                            continue;
                        }

                        if (Settings.IncrementalPlacement) {
                            baseBlock = BlockTransformExtent.transform(baseBlock, transform);
                            blockHashMap.put(loc, baseBlock);
                        }



                        structureLocs.add(loc);
                    }
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
        locationQueue.removeAll(confirmed);
        final Collection<Location> interior = CollectionUtils.filter(invertedStructure, confirmed);
        structureLocs.addAll(interior);
        if (!sbMain.isFreeSpace(playerID, schematicName, structureLocs)){
            return false;
        }

        if (!sbMain.structureWithinRegion(playerID, schematicName, structureLocs)){
            return false;
        }
        final Structure structure = StructureManager.getInstance().getCorrespondingStructure(structureLocs);
        for (Location loc : structureLocs) {
            if (loc.getY() > (Settings.Is1_17 ? world.getMaxY() : 255)) {
                sbMain.sendMessageToPlayer(playerID, COMMAND_PREFIX + I18nSupport.getInternationalisedString("Place - World height exceeded"));
                return false;
            }
            if (loc.getY() > (Settings.Is1_17 ? world.getMinY() : 0)) {
                sbMain.sendMessageToPlayer(playerID, COMMAND_PREFIX + I18nSupport.getInternationalisedString("Place - World height exceeded"));
                return false;
            }
        }
        if (Settings.Debug){
            final long end = System.currentTimeMillis();
            sbMain.broadcast("Structure algorithm took (ms): " + (end - start));
        }
        if (Settings.IncrementalPlacement) {
            final int queueSize = locationQueue.size();
            IncrementalPlacementTask task = new IncrementalPlacementTask(){
                int placedBlocks = 0;
                int lastPercentage = 0;
                /**
                 * The action to be performed by this timer task.
                 */
                @Override
                public void run() {
                    if (locationQueue.isEmpty()) {
                        sbMain.sendMessageToPlayer(playerID, COMMAND_PREFIX + I18nSupport.getInternationalisedString("Placement - Complete"));
                        cancel();
                        playerIncrementPlacementMap.remove(playerID);
                        structure.setPlacementTime(System.currentTimeMillis());
                        return;
                    }
                    for (int i = 1 ; i <= Settings.IncrementalPlacementBlocksPerTick; i++) {
                        final Location poll = locationQueue.poll();
                        if (poll == null)
                            break;
                        final BaseBlock block = blockHashMap.remove(poll);

                        placedBlocks++;
                        final int percent = (int) (((float) placedBlocks / (float) queueSize) * 100f);
                        if ( percent % 10 == 0 && lastPercentage < percent) {
                            lastPercentage = percent;
                            sbMain.sendMessageToPlayer(playerID, COMMAND_PREFIX + I18nSupport.getInternationalisedString("Placement - Progress") + ": " + percent );
                        }
                        placedLocations.add(poll);
                        sbMain.scheduleSyncTask(() -> {
                            try {
                                structure.getLocationsToRemove().add(poll);
                                world.setBlock(blockVectorFromLocation(poll), block, false);
                            } catch (WorldEditException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                }
            };
            new Timer().schedule(task, 0, Settings.IncrementalPlacementDelay * 50);
            structure.setIncrementalPlacementTask(task);
            return true;
        }
        if (structure != null) {
            do {
                structure.getLocationsToRemove().add(locationQueue.poll());
            } while (!locationQueue.isEmpty());
        }


        sbMain.scheduleSyncTask(() -> {
            final long startTime = System.currentTimeMillis();
            try {
                EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
                session.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
                PasteBuilder builder = holder.createPaste(session);
                builder.ignoreAirBlocks(true);
                builder.to(to);
                Operations.complete(builder.build());
                session.flushSession();
                sbMain.clearInterior(interior);
                structure.setPlacementTime(System.currentTimeMillis());
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

    public int getStructureSize(Clipboard clipboard) {
        int count = 0;
        int minX = clipboard.getMinimumPoint().x();
        int minY = clipboard.getMinimumPoint().y();
        int minZ = clipboard.getMinimumPoint().z();
        for (int x = 0 ; x <= clipboard.getDimensions().x(); x++){
            for (int y = 0 ; y <= clipboard.getDimensions().y(); y++){
                for (int z = 0 ; z <= clipboard.getDimensions().z(); z++){
                    BlockVector3 pos = BlockVector3.at(minX + x, minY + y, minZ + z);
                    if (
                            clipboard.getBlock(pos).getBlockType().id().equalsIgnoreCase("minecraft:air")||
                            clipboard.getBlock(pos).getBlockType().id().equalsIgnoreCase("minecraft:cave_air")||
                            clipboard.getBlock(pos).getBlockType().id().equalsIgnoreCase("minecraft:void_air")
                    ){
                        continue;
                    }
                    count++;
                }
            }
        }
        return count;
    }

    private BlockVector3 blockVectorFromLocation(Location loc) {
        return BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
    }




    private static final class WorldEditConfigException extends RuntimeException{
        public WorldEditConfigException(String message, Throwable cause){
            super(message, cause);
        }
    }

    public Map<UUID, IncrementalPlacementTask> getPlayerIncrementPlacementMap() {
        return playerIncrementPlacementMap;
    }

    public File getSchemDir() {
        return schemDir;
    }
}
