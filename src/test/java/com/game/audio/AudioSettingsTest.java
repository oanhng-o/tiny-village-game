package com.game.audio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AudioSettingsTest {

    @Test
    public void testDefaultSettings() {
        AudioSettings settings = AudioSettings.defaults();
        assertFalse(settings.isMusicMuted(), "Music should not be muted by default");
        assertFalse(settings.isSfxMuted(), "SFX should not be muted by default");
        assertTrue(settings.getMusicVolume() > 0, "Music volume should be greater than 0");
        assertTrue(settings.getSfxVolume() > 0, "SFX volume should be greater than 0");
    }

    @Test
    public void testVolumeClamping() {
        AudioSettings settings = new AudioSettings();
        
        // Test upper bound
        settings.setMusicVolume(1.5);
        assertEquals(1.0, settings.getMusicVolume(), 0.001, "Music volume should be clamped to 1.0");
        
        settings.setSfxVolume(1.5);
        assertEquals(1.0, settings.getSfxVolume(), 0.001, "SFX volume should be clamped to 1.0");

        // Test lower bound
        settings.setMusicVolume(-0.5);
        assertEquals(0.0, settings.getMusicVolume(), 0.001, "Music volume should be clamped to 0.0");
        
        settings.setSfxVolume(-0.5);
        assertEquals(0.0, settings.getSfxVolume(), 0.001, "SFX volume should be clamped to 0.0");
    }

    @Test
    public void testAdjustVolume() {
        AudioSettings settings = new AudioSettings();
        settings.setMusicVolume(0.5);
        
        settings.adjustMusicVolume(0.2);
        assertEquals(0.7, settings.getMusicVolume(), 0.001, "Music volume should increase by 0.2");
        
        settings.adjustMusicVolume(-0.3);
        assertEquals(0.4, settings.getMusicVolume(), 0.001, "Music volume should decrease by 0.3");
        
        // Adjust past limits
        settings.adjustMusicVolume(1.0);
        assertEquals(1.0, settings.getMusicVolume(), 0.001, "Music volume should clamp at 1.0 when adjusting");
    }

    @Test
    public void testEffectiveVolume() {
        AudioSettings settings = new AudioSettings();
        settings.setMusicVolume(0.8);
        settings.setSfxVolume(0.6);
        
        assertEquals(0.8, settings.getEffectiveMusicVolume(), 0.001);
        assertEquals(0.6, settings.getEffectiveSfxVolume(), 0.001);
        
        settings.setMusicMuted(true);
        assertEquals(0.0, settings.getEffectiveMusicVolume(), 0.001, "Effective music volume should be 0 when muted");
        
        settings.setSfxMuted(true);
        assertEquals(0.0, settings.getEffectiveSfxVolume(), 0.001, "Effective SFX volume should be 0 when muted");
        
        settings.setMusicMuted(false);
        assertEquals(0.8, settings.getEffectiveMusicVolume(), 0.001, "Effective music volume should return to normal when unmuted");
    }
}
