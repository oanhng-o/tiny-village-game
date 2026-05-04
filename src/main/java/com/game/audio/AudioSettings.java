package com.game.audio;

import com.game.util.Constants;

/**
 * Mutable global audio settings shared by front-screen and gameplay audio.
 */
public final class AudioSettings {

    public static final double VOLUME_STEP = 0.05;

    private double musicVolume;
    private double sfxVolume;
    private boolean musicMuted;
    private boolean sfxMuted;

    public AudioSettings() {
        this(Constants.DEFAULT_MUSIC_VOLUME, Constants.DEFAULT_SFX_VOLUME, false, false);
    }

    public AudioSettings(double musicVolume, double sfxVolume, boolean musicMuted, boolean sfxMuted) {
        setMusicVolume(musicVolume);
        setSfxVolume(sfxVolume);
        this.musicMuted = musicMuted;
        this.sfxMuted = sfxMuted;
    }

    public static AudioSettings defaults() {
        return new AudioSettings();
    }

    public AudioSettings copy() {
        return new AudioSettings(musicVolume, sfxVolume, musicMuted, sfxMuted);
    }

    public double getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(double musicVolume) {
        this.musicVolume = clamp(musicVolume);
    }

    public void adjustMusicVolume(double delta) {
        setMusicVolume(musicVolume + delta);
    }

    public double getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(double sfxVolume) {
        this.sfxVolume = clamp(sfxVolume);
    }

    public void adjustSfxVolume(double delta) {
        setSfxVolume(sfxVolume + delta);
    }

    public boolean isMusicMuted() {
        return musicMuted;
    }

    public void setMusicMuted(boolean musicMuted) {
        this.musicMuted = musicMuted;
    }

    public void toggleMusicMuted() {
        musicMuted = !musicMuted;
    }

    public boolean isSfxMuted() {
        return sfxMuted;
    }

    public void setSfxMuted(boolean sfxMuted) {
        this.sfxMuted = sfxMuted;
    }

    public void toggleSfxMuted() {
        sfxMuted = !sfxMuted;
    }

    public double getEffectiveMusicVolume() {
        return musicMuted ? 0.0 : musicVolume;
    }

    public double getEffectiveSfxVolume() {
        return sfxMuted ? 0.0 : sfxVolume;
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}