package com.game.audio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Persists global audio settings alongside the existing save directory.
 */
public final class AudioSettingsStore {

    private static final String STORAGE_DIRECTORY = ".tiny-village-game";
    private static final String SETTINGS_FILE_NAME = "settings.properties";
    private static final String KEY_MUSIC_VOLUME = "audio.musicVolume";
    private static final String KEY_SFX_VOLUME = "audio.sfxVolume";
    private static final String KEY_MUSIC_MUTED = "audio.musicMuted";
    private static final String KEY_SFX_MUTED = "audio.sfxMuted";

    private final Path settingsPath;

    public AudioSettingsStore() {
        this.settingsPath = resolveDefaultDirectory().resolve(SETTINGS_FILE_NAME);
    }

    public Path getSettingsPath() {
        return settingsPath;
    }

    public AudioSettings load() {
        AudioSettings defaults = AudioSettings.defaults();
        if (!Files.isRegularFile(settingsPath)) {
            return defaults;
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(settingsPath)) {
            properties.load(inputStream);
        } catch (IOException exception) {
            System.err.println("Could not load audio settings: " + exception.getMessage());
            return defaults;
        }

        return new AudioSettings(
                parseDouble(properties.getProperty(KEY_MUSIC_VOLUME), defaults.getMusicVolume()),
                parseDouble(properties.getProperty(KEY_SFX_VOLUME), defaults.getSfxVolume()),
                Boolean.parseBoolean(properties.getProperty(KEY_MUSIC_MUTED,
                        Boolean.toString(defaults.isMusicMuted()))),
                Boolean.parseBoolean(properties.getProperty(KEY_SFX_MUTED,
                        Boolean.toString(defaults.isSfxMuted()))));
    }

    public void save(AudioSettings settings) {
        if (settings == null) {
            return;
        }

        try {
            Files.createDirectories(settingsPath.getParent());
        } catch (IOException exception) {
            System.err.println("Could not create audio settings directory: " + exception.getMessage());
            return;
        }

        Properties properties = new Properties();
        properties.setProperty(KEY_MUSIC_VOLUME, Double.toString(settings.getMusicVolume()));
        properties.setProperty(KEY_SFX_VOLUME, Double.toString(settings.getSfxVolume()));
        properties.setProperty(KEY_MUSIC_MUTED, Boolean.toString(settings.isMusicMuted()));
        properties.setProperty(KEY_SFX_MUTED, Boolean.toString(settings.isSfxMuted()));

        try (OutputStream outputStream = Files.newOutputStream(settingsPath)) {
            properties.store(outputStream, "Tiny Village Audio Settings");
        } catch (IOException exception) {
            System.err.println("Could not save audio settings: " + exception.getMessage());
        }
    }

    private static Path resolveDefaultDirectory() {
        String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.isBlank()) {
            userHome = System.getenv("USERPROFILE");
        }
        if (userHome == null || userHome.isBlank()) {
            userHome = ".";
        }
        return Paths.get(userHome, STORAGE_DIRECTORY);
    }

    private static double parseDouble(String value, double defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }
}