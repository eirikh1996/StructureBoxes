package io.github.eirikh1996.structureboxes.utils;

import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.StructureManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

public class SessionTask extends BukkitRunnable {
    @Override
    public void run() {
        for (OfflinePlayer op : Bukkit.getOfflinePlayers()){
            StructureManager.getInstance().processRemovalOfSavedStructures(op.getUniqueId());
        }
    }
}
