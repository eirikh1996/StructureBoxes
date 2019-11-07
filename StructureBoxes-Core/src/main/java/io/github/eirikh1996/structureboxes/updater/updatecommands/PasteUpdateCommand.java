package io.github.eirikh1996.structureboxes.updater.updatecommands;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import io.github.eirikh1996.structureboxes.SBMain;
import io.github.eirikh1996.structureboxes.utils.Location;

import java.util.ArrayList;

public class PasteUpdateCommand implements UpdateCommand {
    private final Operation operation;
    private final SBMain sbMain;
    private final ArrayList<Location> interior;

    public PasteUpdateCommand(Operation operation, ArrayList<Location> interior, SBMain sbMain){
        this.operation = operation;
        this.interior = interior;
        this.sbMain = sbMain;
    }
    @Override
    public void update() {
        try {
            Operations.complete(operation);
            sbMain.clearInterior(interior);
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
    }
}
