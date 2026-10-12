package dev.codex.ironspelltweaker.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.codex.ironspelltweaker.IronSpellsTweakerConstants;
import dev.codex.ironspelltweaker.platform.Services;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

public class ClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "iron_spell_tweaker-client.json";

    public enum ButtonStyle {
        SQUARE,
        RECTANGLE
    }

    private static boolean showTitleScreenButton = true;
    private static boolean showPauseScreenButton = true;
    private static ButtonStyle buttonStyle = ButtonStyle.SQUARE;
    private static int titleScreenXOffset = 0;
    private static int titleScreenYOffset = 0;
    private static int pauseScreenXOffset = 0;
    private static int pauseScreenYOffset = 0;

    private static boolean loaded = false;

    public static synchronized void ensureLoaded() {
        if (!loaded) {
            load();
        }
    }

    public static synchronized void load() {
        File configFile = Services.PLATFORM.getConfigDir().resolve(CONFIG_FILE_NAME).toFile();
        if (!configFile.exists()) {
            save(); // Write default config file
            loaded = true;
            return;
        }

        try (FileReader reader = new FileReader(configFile, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has("showTitleScreenButton")) {
                showTitleScreenButton = json.get("showTitleScreenButton").getAsBoolean();
            }
            if (json.has("showPauseScreenButton")) {
                showPauseScreenButton = json.get("showPauseScreenButton").getAsBoolean();
            }
            if (json.has("buttonStyle")) {
                String styleStr = json.get("buttonStyle").getAsString().toUpperCase();
                try {
                    buttonStyle = ButtonStyle.valueOf(styleStr);
                } catch (Exception e) {
                    buttonStyle = ButtonStyle.SQUARE;
                }
            }
            if (json.has("titleScreenXOffset")) {
                titleScreenXOffset = json.get("titleScreenXOffset").getAsInt();
            }
            if (json.has("titleScreenYOffset")) {
                titleScreenYOffset = json.get("titleScreenYOffset").getAsInt();
            }
            if (json.has("pauseScreenXOffset")) {
                pauseScreenXOffset = json.get("pauseScreenXOffset").getAsInt();
            }
            if (json.has("pauseScreenYOffset")) {
                pauseScreenYOffset = json.get("pauseScreenYOffset").getAsInt();
            }
        } catch (Exception e) {
            IronSpellsTweakerConstants.LOGGER.error("Failed to load " + CONFIG_FILE_NAME, e);
        }
        loaded = true;
    }

    public static synchronized void save() {
        File configFile = Services.PLATFORM.getConfigDir().resolve(CONFIG_FILE_NAME).toFile();
        try {
            JsonObject json = new JsonObject();
            json.addProperty("showTitleScreenButton", showTitleScreenButton);
            json.addProperty("showPauseScreenButton", showPauseScreenButton);
            json.addProperty("buttonStyle", buttonStyle.name());
            json.addProperty("titleScreenXOffset", titleScreenXOffset);
            json.addProperty("titleScreenYOffset", titleScreenYOffset);
            json.addProperty("pauseScreenXOffset", pauseScreenXOffset);
            json.addProperty("pauseScreenYOffset", pauseScreenYOffset);

            try (FileWriter writer = new FileWriter(configFile, StandardCharsets.UTF_8)) {
                GSON.toJson(json, writer);
            }
        } catch (Exception e) {
            IronSpellsTweakerConstants.LOGGER.error("Failed to save " + CONFIG_FILE_NAME, e);
        }
    }

    public static boolean isShowTitleScreenButton() {
        ensureLoaded();
        return showTitleScreenButton;
    }

    public static void setShowTitleScreenButton(boolean value) {
        showTitleScreenButton = value;
    }

    public static boolean isShowPauseScreenButton() {
        ensureLoaded();
        return showPauseScreenButton;
    }

    public static void setShowPauseScreenButton(boolean value) {
        showPauseScreenButton = value;
    }

    public static ButtonStyle getButtonStyle() {
        ensureLoaded();
        return buttonStyle;
    }

    public static void setButtonStyle(ButtonStyle style) {
        buttonStyle = style;
    }

    public static int getTitleScreenXOffset() {
        ensureLoaded();
        return titleScreenXOffset;
    }

    public static void setTitleScreenXOffset(int offset) {
        titleScreenXOffset = offset;
    }

    public static int getTitleScreenYOffset() {
        ensureLoaded();
        return titleScreenYOffset;
    }

    public static void setTitleScreenYOffset(int offset) {
        titleScreenYOffset = offset;
    }

    public static int getPauseScreenXOffset() {
        ensureLoaded();
        return pauseScreenXOffset;
    }

    public static void setPauseScreenXOffset(int offset) {
        pauseScreenXOffset = offset;
    }

    public static int getPauseScreenYOffset() {
        ensureLoaded();
        return pauseScreenYOffset;
    }

    public static void setPauseScreenYOffset(int offset) {
        pauseScreenYOffset = offset;
    }
}
