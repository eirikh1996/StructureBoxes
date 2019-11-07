package io.github.eirikh1996.structureboxes.async;

public abstract class AsyncTask implements Runnable {
    @Override
    public void run() {
        execute();
        AsyncManager.getInstance().submitCompletedTask(this);
    }

    protected abstract void execute();
}
