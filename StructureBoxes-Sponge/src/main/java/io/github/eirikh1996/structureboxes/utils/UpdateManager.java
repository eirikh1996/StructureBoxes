package io.github.eirikh1996.structureboxes.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.eirikh1996.structureboxes.StructureBoxes;
import io.github.eirikh1996.structureboxes.localisation.I18nSupport;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import static io.github.eirikh1996.structureboxes.utils.ChatUtils.COMMAND_PREFIX;

public class UpdateManager implements Runnable {
    private static UpdateManager instance;

    private UpdateManager() {}

    @Override
    public void run() {
        final double newVersion;
        final double currentVersion = Double.parseDouble(StructureBoxes.getInstance().getPlugin().getVersion().get());
        try {
            newVersion = getNewVersion(currentVersion);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        StructureBoxes.getInstance().getConsole().sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Update - Checking for updates")));
        Task.builder().async().delay(5, TimeUnit.SECONDS).execute(new Runnable() {
            @Override
            public void run() {
                if (newVersion <= currentVersion) {
                    StructureBoxes.getInstance().getConsole().sendMessage(Text.of(COMMAND_PREFIX + I18nSupport.getInternationalisedString("Update - Up to date")));
                    return;
                }
                StructureBoxes.getInstance().getConsole().sendMessage(Text.of(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Update - Update available"))));
                StructureBoxes.getInstance().getConsole().sendMessage(Text.of(COMMAND_PREFIX + "https://ore.spongepowered.org/eirikh1996/Structure-Boxes/versions"));
            }
        }).submit(StructureBoxes.getInstance());
    }

    private double getNewVersion(double currentVersion) throws IOException {
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
            StructureBoxes.getInstance().getLogger().warning("No files found, or Feed URL is bad.");
            return currentVersion;
        }
        JsonObject jsonObj = (JsonObject) jsonArray.get(0);
        String versionName = jsonObj.get("name").getAsString();
        return Double.parseDouble(versionName);
    }

    public void onPlayerJoin(ClientConnectionEvent.Join event, @Root Player p) {
        final double newVersion;
        final double currentVersion = Double.parseDouble(StructureBoxes.getInstance().getPlugin().getVersion().get());
        try {
            newVersion = getNewVersion(currentVersion);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (newVersion <= currentVersion) {
            return;
        }
        p.sendMessage(Text.of(COMMAND_PREFIX + String.format(I18nSupport.getInternationalisedString("Update - Update available"))));
        p.sendMessage(Text.of(COMMAND_PREFIX + "https://ore.spongepowered.org/eirikh1996/Structure-Boxes/versions"));
    }

    public static synchronized UpdateManager getInstance() {
        if (instance == null) {
            instance = new UpdateManager();
        }
        return instance;
    }
}
