package io.github.eirikh1996.structureboxes.localisation;

import io.github.eirikh1996.structureboxes.SBMain;
import io.github.eirikh1996.structureboxes.settings.Settings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class I18nSupport {
    private static Properties languageFile;
    private static SBMain main;

    public static boolean initialize(File datafolder, SBMain sbmain){
        main = sbmain;
        languageFile = new Properties();
        final File file = new File(datafolder.getAbsolutePath() + "/localisation/lang_" + Settings.locale + ".properties");
        try {
            InputStream stream = new FileInputStream(file);
            languageFile.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Deprecated
    public static String getInternationalisedString(String key){
        String property = languageFile.getProperty(key);
        return property != null ? property : key;
    }
}
