package com.game.audio;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class AudioManagerTest {

    @BeforeAll
    public static void setupJavaFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            // Toolkit already initialized
            latch.countDown();
        }
        latch.await();
    }

    @BeforeEach
    public void init() {
        // Đảm bảo trạng thái sạch trước mỗi test
        AudioManager.getInstance().shutdown();
    }

    @Test
    public void testSingletonInstance() {
        AudioManager instance1 = AudioManager.getInstance();
        AudioManager instance2 = AudioManager.getInstance();
        assertNotNull(instance1, "Instance không được null");
        assertSame(instance1, instance2, "Chỉ được phép có duy nhất 1 instance (Singleton)");
    }

    @Test
    public void testApplySettings() throws Exception {
        AudioManager manager = AudioManager.getInstance();
        AudioSettings customSettings = new AudioSettings(0.5, 0.5, false, false);
        manager.applySettings(customSettings);
        
        // Sử dụng reflection để kiểm tra private field 'settings'
        java.lang.reflect.Field field = AudioManager.class.getDeclaredField("settings");
        field.setAccessible(true);
        AudioSettings appliedSettings = (AudioSettings) field.get(manager);
        
        assertNotNull(appliedSettings);
        assertEquals(0.5, appliedSettings.getMusicVolume(), 0.001);
    }
    
    @Test
    public void testInitializeWithNullSettings() throws Exception {
        AudioManager manager = AudioManager.getInstance();
        manager.initialize(null);
        
        java.lang.reflect.Field field = AudioManager.class.getDeclaredField("settings");
        field.setAccessible(true);
        AudioSettings appliedSettings = (AudioSettings) field.get(manager);
        
        assertNotNull(appliedSettings, "Phải sử dụng defaults() nếu settings truyền vào là null");
    }
}
