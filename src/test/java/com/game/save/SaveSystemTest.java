package com.game.save;

import com.game.dialog.QuestSystem;
import com.game.entity.CatFollower;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SaveSystemTest {

    @TempDir
    Path tempDir;

    private SaveSystem saveSystem;

    @BeforeEach
    void setUp() throws Exception {
        saveSystem = new SaveSystem();
        // Use reflection to set saveDirectory to our temp directory for safe testing
        Field saveDirectoryField = SaveSystem.class.getDeclaredField("saveDirectory");
        saveDirectoryField.setAccessible(true);
        saveDirectoryField.set(saveSystem, tempDir);
    }

    @AfterEach
    void tearDown() {
        saveSystem = null;
    }

    @Test
    void testSaveAndLoad_Success() throws IOException {
        long savedTime = Instant.now().getEpochSecond();
        SaveData dataToSave = new SaveData(
                true, // isGirl
                100.5, 200.5, // x, y
                2, // direction
                savedTime,
                Map.of("quest1", QuestSystem.QuestState.ACTIVE),
                Map.of("quest2_timer", 500.0),
                Set.of("itemA"),
                Map.of("itemB", new SaveData.QuestItemPosition(50.0, 60.0)),
                Map.of("wood", 10),
                true, // catUnlocked
                300.0, 400.0, // cat x, y
                CatFollower.State.WAITING,
                5, // mood
                10 // affection
        );

        saveSystem.save(dataToSave);
        assertTrue(saveSystem.hasSave(true), "System should report save exists for girl");
        assertFalse(saveSystem.hasSave(false), "System should not report save for boy");

        Optional<SaveData> loadedDataOpt = saveSystem.load(true);
        assertTrue(loadedDataOpt.isPresent(), "Should load saved data successfully");

        SaveData loaded = loadedDataOpt.get();
        assertEquals(true, loaded.isGirl());
        assertEquals(100.5, loaded.playerX());
        assertEquals(200.5, loaded.playerY());
        assertEquals(2, loaded.playerDirection());
        assertEquals(savedTime, loaded.savedAtEpochSeconds());
        
        assertEquals(QuestSystem.QuestState.ACTIVE, loaded.questStates().get("quest1"));
        assertEquals(10, loaded.inventoryItems().get("wood"));
        assertTrue(loaded.collectedQuestItems().contains("itemA"));
        
        assertEquals(50.0, loaded.questItemPositions().get("itemB").x());
        assertEquals(60.0, loaded.questItemPositions().get("itemB").y());
        
        assertTrue(loaded.catUnlocked());
        assertEquals(300.0, loaded.catX());
        assertEquals(400.0, loaded.catY());
        assertEquals(CatFollower.State.WAITING, loaded.catState());
        assertEquals(5, loaded.catMood());
        assertEquals(10, loaded.catAffection());
    }

    @Test
    void testHasSave_WhenNoSaveExists() {
        assertFalse(saveSystem.hasSave(true));
        assertFalse(saveSystem.hasSave(false));
        assertFalse(saveSystem.hasAnySave());
    }

    @Test
    void testOfflineProgress_AdjustsQuestTimers() throws IOException {
        long pastSavedTime = Instant.now().getEpochSecond() - 100; // 100 seconds ago
        
        SaveData dataToSave = new SaveData(
                false, // boy
                0, 0, 0,
                pastSavedTime,
                Map.of(),
                Map.of("quest_timer_test", 150.0), // 150 seconds timer
                Set.of(),
                Map.of(),
                Map.of(),
                false, 0, 0, null, 0, 0
        );

        saveSystem.save(dataToSave);
        
        Optional<SaveData> loadedDataOpt = saveSystem.load(false);
        assertTrue(loadedDataOpt.isPresent());
        
        SaveData loaded = loadedDataOpt.get();
        
        // Timer should have decreased by roughly 100 seconds
        Double remainingTimer = loaded.questTimers().get("quest_timer_test");
        assertNotNull(remainingTimer);
        assertTrue(remainingTimer <= 50.0 && remainingTimer > 40.0, 
                "Timer should have decremented by elapsed time. Remaining: " + remainingTimer);
    }
}
