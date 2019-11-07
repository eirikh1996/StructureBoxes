package io.github.eirikh1996.structureboxes.async;

import io.github.eirikh1996.structureboxes.updater.UpdateCommandProcessor;
import io.github.eirikh1996.structureboxes.utils.ItemManager;

import java.util.LinkedList;
import java.util.Queue;

public class AsyncManager implements Runnable {
    private static AsyncManager instance;
    Queue<AsyncTask> queue = new LinkedList<>();

    private AsyncManager(){}
    @Override
    public void run() {
        processQueue();
    }

    public static synchronized AsyncManager getInstance(){
        if (instance == null){
            instance = new AsyncManager();
        }
        return instance;
    }

    public void submitTask(AsyncTask task){
        final Thread taskThread = new Thread(task);
        taskThread.start();
    }

    public void submitCompletedTask(AsyncTask task){
        queue.add(task);
    }

    private void processQueue(){
        if (queue.isEmpty()){
            return;
        }
        final AsyncTask poll = queue.poll();
        if (poll instanceof StructureAlgorithmTask){
            final StructureAlgorithmTask task = (StructureAlgorithmTask) poll;
            if (!task.isFreeSpace()){
                task.getSbMain().sendMessageToPlayer(task.getPlayerID(), "Place - No free space");
                final Object item = ItemManager.getInstance().getItem(task.getPlayerID());
                task.getSbMain().addItemToPlayerInventory(task.getPlayerID(), item);
                return;
            }
            UpdateCommandProcessor.getInstance().scheduleUpdates(task.getUpdateCommands());
        }
    }
}
