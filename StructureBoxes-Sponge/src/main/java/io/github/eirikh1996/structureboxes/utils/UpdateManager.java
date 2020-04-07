package io.github.eirikh1996.structureboxes.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class UpdateManager implements Runnable {
    private static UpdateManager instance;
    public String newVersion;
    private UpdateManager() {}

    @Override
    public void run() {
        StructureBoxes.getInstance().getConsole().sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Update - Checking for updates")));
        Task.builder().async().delay(5, TimeUnit.SECONDS).execute(() -> {
            final String newVersion = getNewVersion();
            if (newVersion == null) {
                StructureBoxes.getInstance().getConsole().sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Update - Up to date")));
                return;
            }
            for (Player p : Sponge.getServer().getOnlinePlayers()) {
                if (!p.hasPermission("structureboxes.update")) {
                    continue;
                }
                p.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Update - Update available").replace("%f", newVersion).replace("%d", newVersion).replace("{NewVersion}", newVersion)));
                p.sendMessage(Text.of(COMMAND_PREFIX + "https://ore.spongepowered.org/eirikh1996/Structure-Boxes/versions"));
            }
            StructureBoxes.getInstance().getConsole().sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Update - Update available").replace("%f", newVersion).replace("%d", newVersion).replace("{NewVersion}", newVersion)));
            StructureBoxes.getInstance().getConsole().sendMessage(Text.of(COMMAND_PREFIX + "https://ore.spongepowered.org/eirikh1996/Structure-Boxes/versions"));
            this.newVersion = newVersion;
        }).submit(StructureBoxes.getInstance());
    }

    private String getNewVersion() {
        String currentVersion = StructureBoxes.getInstance().getPlugin().getVersion().get();
        try {


            final URL url = new URL("https://ore.spongepowered.org/api/v1/projects/structureboxes/versions");
            URLConnection conn = url.openConnection();
            conn.setReadTimeout(5000);
            conn.addRequestProperty("User-Agent", "Structure Boxes Update Checker");
            conn.setDoOutput(true);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String response = reader.readLine();
            final Gson gson = new Gson();
            final JsonArray jsonArray = gson.fromJson(response, JsonArray.class);
            if (jsonArray.size() == 0) {
                StructureBoxes.getInstance().getLogger().warn("No files found, or Feed URL is bad.");
                return null;
            }
            JsonObject jsonObj = (JsonObject) jsonArray.get(jsonArray.size() - 1);
            String versionName = jsonObj.get("name").getAsString();
            String newVersion = versionName.substring(versionName.lastIndexOf("v") + 1);
            int nv = Integer.parseInt(newVersion.replace(".", ""));
            int cv = Integer.parseInt(currentVersion.replace("v", "").replace(".", ""));
            if (nv > cv)
                return newVersion;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event, @Root Player p) {
        if (!p.hasPermission("structureboxes.update"))
            return;
        Task.builder().async().delay(2, TimeUnit.SECONDS).execute(() -> {
            final String newVersion = getNewVersion();
            if (newVersion == null) {
                return;
            }
            p.sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Update - Update available").replace("%f", newVersion).replace("%d", newVersion).replace("{NewVersion}", newVersion)));
            p.sendMessage(Text.of(COMMAND_PREFIX + "https://ore.spongepowered.org/eirikh1996/Structure-Boxes/versions"));

        }).submit(StructureBoxes.getInstance());
        }

    public static synchronized UpdateManager getInstance() {
        if (instance == null) {
            instance = new UpdateManager();
        }
        return instance;
    }
}
