package io.github.eirikh1996.structureboxes.localisation;

import io.github.eirikh1996.structureboxes.settings.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class I18nSupport {
    private static Properties languageFile;

    public static boolean initialize(File datafolder){
        languageFile = new Properties();
        final File file = new File(datafolder.getAbsolutePath() + "/localisation/lang_" + Settings.locale + ".properties");
        try {
            languageFile.load(new FileInputStream(file));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getInternationalisedString(String key){
        String property = languageFile.getProperty(key);
        return property != null ? property : key;
    }
}
