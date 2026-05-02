package com.game.save;

import com.game.dialog.QuestSystem;
import com.game.entity.CatFollower;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * Properties-backed save storage with one slot per character gender.
 */
public class SaveSystem {

    private static final String SAVE_DIRECTORY = ".tiny-village-game";
    private static final String SAVE_FILE_NAME_GIRL = "save-girl.properties";
    private static final String SAVE_FILE_NAME_BOY = "save-boy.properties";
    private static final String LEGACY_SAVE_FILE_NAME = "save.properties";
    private static final String KEY_VERSION = "meta.version";
    private static final String KEY_SAVED_AT_EPOCH_SECONDS = "meta.savedAtEpochSeconds";
    private static final String KEY_IS_GIRL = "character.isGirl";
    private static final String KEY_PLAYER_X = "player.x";
    private static final String KEY_PLAYER_Y = "player.y";
    private static final String KEY_PLAYER_DIRECTION = "player.direction";
    private static final String KEY_CAT_UNLOCKED = "cat.unlocked";
    private static final String KEY_CAT_X = "cat.x";
    private static final String KEY_CAT_Y = "cat.y";
    private static final String KEY_CAT_STATE = "cat.state";
    private static final String KEY_CAT_MOOD = "cat.mood";
    private static final String KEY_CAT_AFFECTION = "cat.affection";
    private static final String PREFIX_QUEST = "quest.";
    private static final String PREFIX_QUEST_TIMER = "questTimer.";
    private static final String PREFIX_COLLECTED_ITEM = "questItem.";
    private static final String PREFIX_QUEST_ITEM_POSITION_X = "questItemPosX.";
    private static final String PREFIX_QUEST_ITEM_POSITION_Y = "questItemPosY.";
    private static final String PREFIX_INVENTORY = "inventory.";
    private static final int CURRENT_VERSION = 2;

    private final Path saveDirectory;

    public SaveSystem() {
        this.saveDirectory = resolveDefaultSaveDirectory();
    }

    public Path getSavePath(boolean isGirl) {
        return resolveSavePath(isGirl);
    }

    public boolean hasSave(boolean isGirl) {
        if (Files.isRegularFile(resolveSavePath(isGirl))) {
            return true;
        }
        return loadLegacySave().filter(saveData -> saveData.isGirl() == isGirl).isPresent();
    }

    public boolean hasAnySave() {
        return hasSave(true) || hasSave(false);
    }

    public void save(SaveData data) throws IOException {
        Path savePath = resolveSavePath(data.isGirl());
        Files.createDirectories(saveDirectory);

        Properties properties = new Properties();
        properties.setProperty(KEY_VERSION, Integer.toString(CURRENT_VERSION));
        properties.setProperty(KEY_SAVED_AT_EPOCH_SECONDS, Long.toString(data.savedAtEpochSeconds()));
        properties.setProperty(KEY_IS_GIRL, Boolean.toString(data.isGirl()));
        properties.setProperty(KEY_PLAYER_X, Double.toString(data.playerX()));
        properties.setProperty(KEY_PLAYER_Y, Double.toString(data.playerY()));
        properties.setProperty(KEY_PLAYER_DIRECTION, Integer.toString(data.playerDirection()));
        properties.setProperty(KEY_CAT_UNLOCKED, Boolean.toString(data.catUnlocked()));
        properties.setProperty(KEY_CAT_X, Double.toString(data.catX()));
        properties.setProperty(KEY_CAT_Y, Double.toString(data.catY()));
        properties.setProperty(KEY_CAT_STATE, data.catState().name());
        properties.setProperty(KEY_CAT_MOOD, Integer.toString(data.catMood()));
        properties.setProperty(KEY_CAT_AFFECTION, Integer.toString(data.catAffection()));

        for (Map.Entry<String, QuestSystem.QuestState> entry : data.questStates().entrySet()) {
            properties.setProperty(PREFIX_QUEST + entry.getKey(), entry.getValue().name());
        }
        for (Map.Entry<String, Double> entry : data.questTimers().entrySet()) {
            if (entry.getValue() != null && entry.getValue() > 0) {
                properties.setProperty(PREFIX_QUEST_TIMER + entry.getKey(), Double.toString(entry.getValue()));
            }
        }
        for (String itemId : data.collectedQuestItems()) {
            properties.setProperty(PREFIX_COLLECTED_ITEM + itemId, Boolean.TRUE.toString());
        }
        for (Map.Entry<String, SaveData.QuestItemPosition> entry : data.questItemPositions().entrySet()) {
            properties.setProperty(PREFIX_QUEST_ITEM_POSITION_X + entry.getKey(), Double.toString(entry.getValue().x()));
            properties.setProperty(PREFIX_QUEST_ITEM_POSITION_Y + entry.getKey(), Double.toString(entry.getValue().y()));
        }
        for (Map.Entry<String, Integer> entry : data.inventoryItems().entrySet()) {
            properties.setProperty(PREFIX_INVENTORY + entry.getKey(), Integer.toString(entry.getValue()));
        }

        try (OutputStream outputStream = Files.newOutputStream(savePath)) {
            properties.store(outputStream, "Tiny Village Game Save");
        }
    }

    public Optional<SaveData> load(boolean isGirl) {
        Path savePath = resolveSavePath(isGirl);
        if (Files.isRegularFile(savePath)) {
            return loadFromPath(savePath);
        }
        return loadLegacySave().filter(saveData -> saveData.isGirl() == isGirl);
    }

    private Optional<SaveData> loadLegacySave() {
        Path legacyPath = saveDirectory.resolve(LEGACY_SAVE_FILE_NAME);
        if (!Files.isRegularFile(legacyPath)) {
            return Optional.empty();
        }
        return loadFromPath(legacyPath);
    }

    private Optional<SaveData> loadFromPath(Path savePath) {
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(savePath)) {
            properties.load(inputStream);
        } catch (IOException exception) {
            return Optional.empty();
        }

        Map<String, QuestSystem.QuestState> questStates = new LinkedHashMap<>();
        Map<String, Double> questTimers = new LinkedHashMap<>();
        Set<String> collectedQuestItems = new LinkedHashSet<>();
        Map<String, Double> questItemPosX = new LinkedHashMap<>();
        Map<String, Double> questItemPosY = new LinkedHashMap<>();
        Map<String, Integer> inventoryItems = new LinkedHashMap<>();

        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (key.startsWith(PREFIX_QUEST)) {
                String questId = key.substring(PREFIX_QUEST.length());
                questStates.put(questId, parseQuestState(value));
            } else if (key.startsWith(PREFIX_QUEST_TIMER)) {
                String questId = key.substring(PREFIX_QUEST_TIMER.length());
                double timer = parseDouble(value, 0);
                if (timer > 0) {
                    questTimers.put(questId, timer);
                }
            } else if (key.startsWith(PREFIX_COLLECTED_ITEM) && Boolean.parseBoolean(value)) {
                collectedQuestItems.add(key.substring(PREFIX_COLLECTED_ITEM.length()));
            } else if (key.startsWith(PREFIX_QUEST_ITEM_POSITION_X)) {
                String itemId = key.substring(PREFIX_QUEST_ITEM_POSITION_X.length());
                questItemPosX.put(itemId, parseDouble(value, 0));
            } else if (key.startsWith(PREFIX_QUEST_ITEM_POSITION_Y)) {
                String itemId = key.substring(PREFIX_QUEST_ITEM_POSITION_Y.length());
                questItemPosY.put(itemId, parseDouble(value, 0));
            } else if (key.startsWith(PREFIX_INVENTORY)) {
                String itemId = key.substring(PREFIX_INVENTORY.length());
                int count = parseInt(value, 0);
                if (count > 0) {
                    inventoryItems.put(itemId, count);
                }
            }
        }

        Map<String, SaveData.QuestItemPosition> questItemPositions = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : questItemPosX.entrySet()) {
            Double y = questItemPosY.get(entry.getKey());
            if (y != null) {
                questItemPositions.put(entry.getKey(), new SaveData.QuestItemPosition(entry.getValue(), y));
            }
        }

        long savedAtEpochSeconds = parseLong(
            properties.getProperty(KEY_SAVED_AT_EPOCH_SECONDS),
            resolveFallbackSavedAtEpochSeconds(savePath));

        SaveData saveData = new SaveData(
                Boolean.parseBoolean(properties.getProperty(KEY_IS_GIRL, Boolean.TRUE.toString())),
                parseDouble(properties.getProperty(KEY_PLAYER_X), 19 * 32.0),
                parseDouble(properties.getProperty(KEY_PLAYER_Y), 12 * 32.0),
                parseInt(properties.getProperty(KEY_PLAYER_DIRECTION), 0),
            savedAtEpochSeconds,
                questStates,
            adjustQuestTimersForOfflineProgress(questTimers, savedAtEpochSeconds),
                collectedQuestItems,
                questItemPositions,
                inventoryItems,
                Boolean.parseBoolean(properties.getProperty(KEY_CAT_UNLOCKED, Boolean.FALSE.toString())),
                parseDouble(properties.getProperty(KEY_CAT_X), 25 * 32.0),
                parseDouble(properties.getProperty(KEY_CAT_Y), 22 * 32.0),
                parseCatState(properties.getProperty(KEY_CAT_STATE)),
                parseInt(properties.getProperty(KEY_CAT_MOOD), 0),
                parseInt(properties.getProperty(KEY_CAT_AFFECTION), 0));
        return Optional.of(saveData);
    }

    private Path resolveSavePath(boolean isGirl) {
        return saveDirectory.resolve(isGirl ? SAVE_FILE_NAME_GIRL : SAVE_FILE_NAME_BOY);
    }

    private static Path resolveDefaultSaveDirectory() {
        String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.isBlank()) {
            userHome = System.getenv("USERPROFILE");
        }
        if (userHome == null || userHome.isBlank()) {
            userHome = ".";
        }
        return Paths.get(userHome, SAVE_DIRECTORY);
    }

    private static int parseInt(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private static long parseLong(String value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private static double parseDouble(String value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private static Map<String, Double> adjustQuestTimersForOfflineProgress(Map<String, Double> questTimers, long savedAtEpochSeconds) {
        Map<String, Double> adjustedTimers = new LinkedHashMap<>();
        long elapsedSeconds = Math.max(0L, Instant.now().getEpochSecond() - savedAtEpochSeconds);
        for (Map.Entry<String, Double> entry : questTimers.entrySet()) {
            double remaining = Math.max(0.0, entry.getValue() - elapsedSeconds);
            if (remaining > 0.0) {
                adjustedTimers.put(entry.getKey(), remaining);
            }
        }
        return adjustedTimers;
    }

    private static long resolveFallbackSavedAtEpochSeconds(Path savePath) {
        try {
            return Files.getLastModifiedTime(savePath).toMillis() / 1000L;
        } catch (IOException exception) {
            return Instant.now().getEpochSecond();
        }
    }

    private static QuestSystem.QuestState parseQuestState(String value) {
        if (value == null || value.isBlank()) {
            return QuestSystem.QuestState.NOT_STARTED;
        }
        try {
            return QuestSystem.QuestState.valueOf(value.trim());
        } catch (IllegalArgumentException exception) {
            return QuestSystem.QuestState.NOT_STARTED;
        }
    }

    private static CatFollower.State parseCatState(String value) {
        if (value == null || value.isBlank()) {
            return CatFollower.State.IDLE;
        }
        try {
            return CatFollower.State.valueOf(value.trim());
        } catch (IllegalArgumentException exception) {
            return CatFollower.State.IDLE;
        }
    }
}