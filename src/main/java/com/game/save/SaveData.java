package com.game.save;

import com.game.dialog.QuestSystem;
import com.game.entity.CatFollower;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Immutable save snapshot for the long-lived gameplay state.
 */
public record SaveData(
        boolean isGirl,
        double playerX,
        double playerY,
        int playerDirection,
        long savedAtEpochSeconds,
        Map<String, QuestSystem.QuestState> questStates,
    Map<String, Double> questTimers,
        Set<String> collectedQuestItems,
    Map<String, QuestItemPosition> questItemPositions,
        Map<String, Integer> inventoryItems,
        boolean catUnlocked,
        double catX,
        double catY,
        CatFollower.State catState,
        int catMood,
        int catAffection) {

    public SaveData {
        savedAtEpochSeconds = Math.max(0L, savedAtEpochSeconds);
        questStates = copyQuestStates(questStates);
        questTimers = copyQuestTimers(questTimers);
        collectedQuestItems = copyCollectedItems(collectedQuestItems);
        questItemPositions = copyQuestItemPositions(questItemPositions);
        inventoryItems = copyInventoryItems(inventoryItems);
        catState = catState == null ? CatFollower.State.IDLE : catState;
    }

    private static Map<String, QuestSystem.QuestState> copyQuestStates(Map<String, QuestSystem.QuestState> source) {
        Map<String, QuestSystem.QuestState> copy = new LinkedHashMap<>();
        if (source != null) {
            copy.putAll(source);
        }
        return copy;
    }

    private static Map<String, Double> copyQuestTimers(Map<String, Double> source) {
        Map<String, Double> copy = new LinkedHashMap<>();
        if (source != null) {
            copy.putAll(source);
        }
        return copy;
    }

    private static Set<String> copyCollectedItems(Set<String> source) {
        Set<String> copy = new LinkedHashSet<>();
        if (source != null) {
            copy.addAll(source);
        }
        return copy;
    }

    private static Map<String, QuestItemPosition> copyQuestItemPositions(Map<String, QuestItemPosition> source) {
        Map<String, QuestItemPosition> copy = new LinkedHashMap<>();
        if (source != null) {
            copy.putAll(source);
        }
        return copy;
    }

    private static Map<String, Integer> copyInventoryItems(Map<String, Integer> source) {
        Map<String, Integer> copy = new LinkedHashMap<>();
        if (source != null) {
            copy.putAll(source);
        }
        return copy;
    }

    public record QuestItemPosition(double x, double y) {
    }
}