package io.github.eirikh1996.structureboxes.async;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import io.github.eirikh1996.structureboxes.SBMain;
import io.github.eirikh1996.structureboxes.updater.updatecommands.UpdateCommand;
import io.github.eirikh1996.structureboxes.utils.WorldEditLocation;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public abstract class StructureAlgorithmTask extends AsyncTask {
    protected final UUID playerID;
    protected final String schematicName;
    protected final Clipboard clipboard;
    protected final double angle;
    protected final WorldEditLocation pasteLoc;
    protected final SBMain sbMain;
    protected boolean isFreeSpace = false;
    protected final ArrayList<UpdateCommand> updateCommands = new ArrayList<>();
    protected StructureAlgorithmTask(UUID playerID, String schematicName, Clipboard clipboard, double angle, WorldEditLocation pasteLoc, SBMain sbMain){
        this.playerID = playerID;
        this.schematicName = schematicName;
        this.clipboard = clipboard;
        this.angle = angle;
        this.pasteLoc = pasteLoc;
        this.sbMain = sbMain;
    }

    public Clipboard getClipboard() {
        return clipboard;
    }

    public UUID getPlayerID() {
        return playerID;
    }

    public double getAngle() {
        return angle;
    }

    public String getSchematicName() {
        return schematicName;
    }

    public WorldEditLocation getPasteLoc() {
        return pasteLoc;
    }

    public boolean isFreeSpace() {
        return isFreeSpace;
    }

    public ArrayList<UpdateCommand> getUpdateCommands() {
        return updateCommands;
    }

    public SBMain getSbMain() {
        return sbMain;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerID, schematicName, clipboard, angle, pasteLoc);
    }
}
