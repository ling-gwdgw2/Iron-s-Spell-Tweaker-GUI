package dev.codex.ironspelltweaker.platform;

import java.nio.file.Path;

public interface IPlatformHelper {
    /**
     * Gets the game's configuration directory (e.g. .minecraft/config).
     */
    Path getConfigDir();

    /**
     * Checks if a mod with the given mod ID is loaded.
     */
    boolean isModLoaded(String modId);

    /**
     * Checks if running in a client environment.
     */
    boolean isClient();

    /**
     * Gets the current platform name (e.g. "NeoForge", "Fabric").
     */
    String getPlatformName();
}
