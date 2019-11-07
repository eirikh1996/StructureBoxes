package io.github.eirikh1996.structureboxes.updater;

import io.github.eirikh1996.structureboxes.updater.updatecommands.UpdateCommand;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class UpdateCommandProcessor implements Runnable {
    private final LinkedList<UpdateCommand> queue = new LinkedList<>();
    private static UpdateCommandProcessor instance;
    @Override
    public void run() {

        synchronized (queue){
            if (queue.isEmpty()){
                return;
            }
            final UpdateCommand updateCommand = queue.pollFirst();
            updateCommand.update();
        }
    }

    public void scheduleUpdate(UpdateCommand updateCommand){
        queue.addLast(updateCommand);
    }

    public void scheduleUpdate(UpdateCommand... updates){
        Collections.addAll(queue, updates);
    }

    public void scheduleUpdates(Collection<UpdateCommand> updateCommands){
        queue.addAll(updateCommands);
    }


    public static synchronized UpdateCommandProcessor getInstance(){
        if (instance == null){
            instance = new UpdateCommandProcessor();
        }
        return instance;
    }
}
