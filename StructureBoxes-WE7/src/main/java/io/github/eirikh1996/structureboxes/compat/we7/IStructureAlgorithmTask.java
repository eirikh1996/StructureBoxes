package io.github.eirikh1996.structureboxes.compat.we7;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.PasteBuilder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import io.github.eirikh1996.structureboxes.SBMain;
import io.github.eirikh1996.structureboxes.StructureManager;
import io.github.eirikh1996.structureboxes.async.StructureAlgorithmTask;
import io.github.eirikh1996.structureboxes.updater.updatecommands.PasteUpdateCommand;
import io.github.eirikh1996.structureboxes.utils.CollectionUtils;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.eirikh1996.structureboxes.utils.WorldEditLocation;

import java.util.ArrayList;
import java.util.UUID;

import static java.lang.Math.PI;

public class IStructureAlgorithmTask extends StructureAlgorithmTask {
    public IStructureAlgorithmTask(UUID playerID, String schematicName, Clipboard clipboard, double angle, WorldEditLocation pasteLoc, SBMain sbMain) {
        super(playerID, schematicName, clipboard, angle, pasteLoc, sbMain);
    }

    @Override
    protected void execute() {
        World world = pasteLoc.getWorld();
        ClipboardHolder holder = new ClipboardHolder(clipboard);
        AffineTransform transform = new AffineTransform();
        transform = transform.rotateY(angle);
        holder.setTransform(transform);
        PasteBuilder builder = holder.createPaste(world);
        builder.ignoreAirBlocks(true);
        BlockVector3 to = BlockVector3.at(pasteLoc.getX(), pasteLoc.getY(), pasteLoc.getZ());
        builder.to(to);
        final ArrayList<Location> structureLocs = new ArrayList<Location>();
        int minX = clipboard.getMinimumPoint().getBlockX();
        int minY = clipboard.getMinimumPoint().getBlockY();
        int minZ = clipboard.getMinimumPoint().getBlockZ();
        int xLength = clipboard.getDimensions().getBlockX();
        int yLength = clipboard.getDimensions().getBlockY();
        int zLength = clipboard.getDimensions().getBlockZ();
        BlockVector3 offset = clipboard.getMinimumPoint().subtract(clipboard.getOrigin());
        Location minPoint = new Location(pasteLoc.getWorldID(), to.add(offset).getBlockX(), to.add(offset).getBlockY(), to.add(offset).getBlockZ());
        final double theta = -(angle * (PI / 180.0));

        for (int x = 0 ; x <= xLength ; x++){
            for (int y = 0 ; y <= yLength ; y++){
                for (int z = 0 ; z <= zLength ; z++){
                    BaseBlock baseBlock = clipboard.getFullBlock(BlockVector3.at(minX + x, minY + y, minZ + z));
                    if (baseBlock.getBlockType().getId().equalsIgnoreCase("minecraft:air") || baseBlock.getBlockType().getId().equalsIgnoreCase("minecraft:cave_air") || baseBlock.getBlockType().getId().equalsIgnoreCase("minecraft:void_air")){
                        continue;
                    }
                    Location loc = minPoint.add(x, y, z);

                    structureLocs.add(loc.rotate(theta, pasteLoc.toSBloc()));
                }
            }
        }
        final ArrayList<Location> exterior = CollectionUtils.exterior(structureLocs);
        final ArrayList<Location> invertedStructure = CollectionUtils.invert(structureLocs);
        final ArrayList<Location> interior = CollectionUtils.filter(invertedStructure, exterior);
        isFreeSpace = sbMain.isFreeSpace(playerID, schematicName, structureLocs);
        if (!isFreeSpace){

            return;
        }
        StructureManager.getInstance().addStructure(structureLocs);
        updateCommands.add(new PasteUpdateCommand(builder.build(), interior, sbMain));
        StructureManager.getInstance().addStructureByPlayer(playerID, structureLocs);
    }
}
