package io.github.eirikh1996.structureboxes.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class UpdateChecker extends BukkitRunnable implements Listener {
    private static UpdateChecker instance;
    private int secondCooldown = 0;
    private int delayBeforeCheckingUpdate = 0;

    private UpdateChecker(){
    }

    @Override
    public void run() {
        StructureBoxes sb = StructureBoxes.getInstance();
        sb.getLogger().info(I18nSupport.getInternationalisedString("Update - Checking for updates"));
        new BukkitRunnable() {
            @Override
            public void run() {
                String newVersion = getNewVersion();
                if (newVersion == null){
                    sb.getLogger().info(I18nSupport.getInternationalisedString("Update - Up to date"));
                    return;
                }
                Bukkit.broadcast(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Update - Update available").replace("%f", newVersion).replace("{NewVersion}", newVersion), "structureboxes.update");
                Bukkit.broadcast(COMMAND_PREFIX + "https://dev.bukkit.org/projects/structure-boxes/files", "structureboxes.update");
                sb.getLogger().warning(I18nSupport.getInternationalisedString("Update - Update available").replace("%f", newVersion).replace("{NewVersion}", newVersion));
                sb.getLogger().warning("https://dev.bukkit.org/projects/structure-boxes/files");

            }
        }.runTaskLaterAsynchronously(sb, 120);
        delayBeforeCheckingUpdate++;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event){
        new BukkitRunnable() {
            @Override
            public void run() {
                final Player player = event.getPlayer();
                String newVersion = getNewVersion();
                if (newVersion == null){
                    return;
                }
                if (!player.hasPermission("structureboxes.update")){
                    return;
                }
                player.sendMessage(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Update - Update available").replace("%d", newVersion).replace("{NewVersion}", newVersion));
                player.sendMessage("https://dev.bukkit.org/projects/structure-boxes/files");
            }
        }.runTaskLaterAsynchronously(StructureBoxes.getInstance(), 120);

    }

    public static synchronized UpdateChecker getInstance() {
        if (instance == null){
            instance = new UpdateChecker();
        }
        return instance;
    }

    private String getNewVersion() {
        String currentVersion = StructureBoxes.getInstance().getDescription().getVersion();
        try {
            URL url = new URL("https://servermods.forgesvc.net/servermods/files?projectids=349569");
            URLConnection conn = url.openConnection();
            conn.setReadTimeout(5000);
            conn.addRequestProperty("User-Agent", "Structure Boxes Update Checker");
            conn.setDoOutput(true);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String response = reader.readLine();
            final Gson gson = new Gson();
            final JsonArray jsonArray = gson.fromJson(response, JsonArray.class);
            if (jsonArray.size() == 0) {
                StructureBoxes.getInstance().getLogger().warning("No files found, or Feed URL is bad.");
                return currentVersion;
            }
            JsonObject jsonObj = (JsonObject) jsonArray.get(jsonArray.size() - 1);
            String versionName = jsonObj.get("name").getAsString();
            String newVersion = versionName.substring(versionName.lastIndexOf("v") + 1);
            int nv = Integer.parseInt(newVersion.replace(".", ""));
            int cv = Integer.parseInt(currentVersion.replace("v", "").replace(".", ""));
            //If a new major update, multiply nv by 1k
            if (Integer.parseInt(newVersion.substring(0, 1)) > Integer.parseInt(currentVersion.substring(0, 1))) {
                final String[] parts = newVersion.split("\\.");
                nv = (Integer.parseInt(parts[0]) * 10000) + Integer.parseInt(parts[1]) ;
            }
            if (nv > cv)
                return newVersion;
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }
}
