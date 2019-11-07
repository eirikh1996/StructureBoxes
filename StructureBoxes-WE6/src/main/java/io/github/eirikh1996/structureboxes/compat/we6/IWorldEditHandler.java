package io.github.eirikh1996.structureboxes.compat.we6;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.world.World;
import io.github.eirikh1996.structureboxes.Direction;
import io.github.eirikh1996.structureboxes.SBMain;
import io.github.eirikh1996.structureboxes.WorldEditHandler;
import io.github.eirikh1996.structureboxes.async.AsyncManager;
import io.github.eirikh1996.structureboxes.utils.Location;
import io.github.eirikh1996.structureboxes.utils.WorldEditLocation;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class IWorldEditHandler extends WorldEditHandler {
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
    public void pasteClipboard(UUID playerID, String schematicName, Clipboard clipboard, double angle, WorldEditLocation pasteLoc) {
        AsyncManager.getInstance().submitTask(new IStructureAlgorithmTask(playerID, schematicName, clipboard, angle, pasteLoc, sbMain));
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
