package com.game.audio;

import com.game.util.Constants;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Minimal audio runtime for shared music and one-shot SFX playback.
 */
public final class AudioManager {

    private static AudioManager instance;

    private final Map<String, AudioClip> sfxCache = new HashMap<>();
    private final Map<String, String> sfxEventToAsset = new HashMap<>();
    private final Map<String, String> musicEventToAsset = new HashMap<>();
    private final Set<String> missingAssetsLogged = new HashSet<>();

    private AudioSettings settings = AudioSettings.defaults();
    private MediaPlayer musicPlayer;
    private String currentMusicEvent;
    private boolean useFirstFootstep = true;

    private AudioManager() {
        registerDefaultMappings();
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public void initialize(AudioSettings settings) {
        applySettings(settings == null ? AudioSettings.defaults() : settings);
    }

    public void applySettings(AudioSettings settings) {
        this.settings = settings == null ? AudioSettings.defaults() : settings;
        updateMusicVolume();
    }

    public void playMusicEvent(String eventKey) {
        String assetFile = musicEventToAsset.get(eventKey);
        if (assetFile == null) {
            return;
        }

        if (eventKey.equals(currentMusicEvent) && musicPlayer != null) {
            updateMusicVolume();
            if (musicPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                musicPlayer.play();
            }
            return;
        }

        URL resource = resolveAsset(assetFile);
        if (resource == null) {
            return;
        }

        disposeMusicPlayer();
        try {
            Media media = new Media(resource.toExternalForm());
            musicPlayer = new MediaPlayer(media);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            currentMusicEvent = eventKey;
            updateMusicVolume();
            musicPlayer.play();
        } catch (RuntimeException exception) {
            logMissingAsset(assetFile, exception);
            disposeMusicPlayer();
        }
    }

    public void stopMusic() {
        disposeMusicPlayer();
    }

    public void playSfxEvent(String eventKey) {
        String assetFile = sfxEventToAsset.get(eventKey);
        if (assetFile == null) {
            return;
        }
        playSfxAsset(assetFile);
    }

    public void playFootstep() {
        String assetFile = useFirstFootstep
                ? Constants.ASSET_AUDIO_FOOTSTEP_GRASS_1
                : Constants.ASSET_AUDIO_FOOTSTEP_GRASS_2;
        useFirstFootstep = !useFirstFootstep;
        playSfxAsset(assetFile);
    }

    public void shutdown() {
        disposeMusicPlayer();
        sfxCache.clear();
        currentMusicEvent = null;
        useFirstFootstep = true;
    }

    private void playSfxAsset(String assetFile) {
        double volume = settings.getEffectiveSfxVolume();
        if (volume <= 0.0) {
            return;
        }

        AudioClip clip = getOrLoadClip(assetFile);
        if (clip == null) {
            return;
        }

        try {
            clip.play(volume);
        } catch (RuntimeException exception) {
            logMissingAsset(assetFile, exception);
        }
    }

    private AudioClip getOrLoadClip(String assetFile) {
        AudioClip cached = sfxCache.get(assetFile);
        if (cached != null) {
            return cached;
        }

        URL resource = resolveAsset(assetFile);
        if (resource == null) {
            return null;
        }

        try {
            AudioClip clip = new AudioClip(resource.toExternalForm());
            sfxCache.put(assetFile, clip);
            return clip;
        } catch (RuntimeException exception) {
            logMissingAsset(assetFile, exception);
            return null;
        }
    }

    private URL resolveAsset(String assetFile) {
        URL resource = getClass().getResource(Constants.getAssetPath(assetFile));
        if (resource == null && missingAssetsLogged.add(assetFile)) {
            System.err.println("Missing audio asset: " + assetFile);
        }
        return resource;
    }

    private void updateMusicVolume() {
        if (musicPlayer == null) {
            return;
        }
        musicPlayer.setVolume(settings.getEffectiveMusicVolume());
    }

    private void disposeMusicPlayer() {
        if (musicPlayer == null) {
            return;
        }
        try {
            musicPlayer.stop();
            musicPlayer.dispose();
        } catch (RuntimeException exception) {
            System.err.println("Could not stop audio player cleanly: " + exception.getMessage());
        } finally {
            musicPlayer = null;
            currentMusicEvent = null;
        }
    }

    private void registerDefaultMappings() {
        musicEventToAsset.put(Constants.AUDIO_EVENT_MENU_MUSIC, Constants.ASSET_AUDIO_MENU_THEME);
        musicEventToAsset.put(Constants.AUDIO_EVENT_GAMEPLAY_MUSIC, Constants.ASSET_AUDIO_GAMEPLAY_LOOP);

        sfxEventToAsset.put(Constants.AUDIO_EVENT_DIALOG_OPEN, Constants.ASSET_AUDIO_DIALOG_OPEN);
        sfxEventToAsset.put(Constants.AUDIO_EVENT_DIALOG_ADVANCE, Constants.ASSET_AUDIO_DIALOG_ADVANCE);
        sfxEventToAsset.put(Constants.AUDIO_EVENT_CONFIRM, Constants.ASSET_AUDIO_UI_CONFIRM);
        sfxEventToAsset.put(Constants.AUDIO_EVENT_BACK, Constants.ASSET_AUDIO_UI_BACK);
        sfxEventToAsset.put(Constants.AUDIO_EVENT_QUEST_START, Constants.ASSET_AUDIO_QUEST_START);
        sfxEventToAsset.put(Constants.AUDIO_EVENT_QUEST_COMPLETE, Constants.ASSET_AUDIO_QUEST_COMPLETE);
    }

    private void logMissingAsset(String assetFile, RuntimeException exception) {
        if (missingAssetsLogged.add(assetFile)) {
            System.err.println("Could not play audio asset " + assetFile + ": " + exception.getMessage());
        }
    }
}