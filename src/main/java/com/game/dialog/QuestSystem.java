package com.game.dialog;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * QuestSystem — Quản lý trạng thái quest.
 * Hỗ trợ nhiều quest nhỏ bằng questId.
 */
public class QuestSystem {

    public static final String FISHING_ROD_QUEST_ID = "fishing_rod";
    public static final String SEEDS_QUEST_ID = "seeds";

    public enum QuestState {
        NOT_STARTED,
        ACTIVE,
        COMPLETED
    }

    private final Map<String, QuestState> questStates = new LinkedHashMap<>();
    private final Set<String> collectedItems = new HashSet<>();

    public QuestSystem() {
        registerQuest(FISHING_ROD_QUEST_ID);
        registerQuest(SEEDS_QUEST_ID);
    }

    private void registerQuest(String questId) {
        questStates.putIfAbsent(questId, QuestState.NOT_STARTED);
    }

    /**
     * Bắt đầu quest theo id.
     */
    public void startQuest(String questId) {
        if (getQuestState(questId) == QuestState.NOT_STARTED) {
            questStates.put(questId, QuestState.ACTIVE);
        }
    }

    /**
     * Hoàn thành quest theo id.
     */
    public void completeQuest(String questId) {
        questStates.put(questId, QuestState.COMPLETED);
    }

    /**
     * Reset quest về trạng thái chưa bắt đầu.
     */
    public void resetQuest(String questId) {
        questStates.put(questId, QuestState.NOT_STARTED);
    }

    /**
     * Đánh dấu đã nhặt item theo id.
     */
    public void addItem(String itemId) {
        collectedItems.add(itemId);
    }

    /**
     * Xóa đánh dấu đã nhặt item theo id.
     */
    public void removeItem(String itemId) {
        collectedItems.remove(itemId);
    }

    /**
     * Kiểm tra đã nhặt item theo id chưa.
     */
    public boolean hasItem(String itemId) {
        return collectedItems.contains(itemId);
    }

    /**
     * Lấy trạng thái quest theo id.
     */
    public QuestState getQuestState(String questId) {
        return questStates.getOrDefault(questId, QuestState.NOT_STARTED);
    }

    /**
     * Lấy toàn bộ quest state theo thứ tự đăng ký.
     */
    public Map<String, QuestState> getQuestStates() {
        return Collections.unmodifiableMap(questStates);
    }

    public Map<String, QuestState> getQuestStatesSnapshot() {
        return new LinkedHashMap<>(questStates);
    }

    public Set<String> getCollectedItemsSnapshot() {
        return new LinkedHashSet<>(collectedItems);
    }

    public void replaceQuestStates(Map<String, QuestState> states) {
        Set<String> registeredQuestIds = new LinkedHashSet<>(questStates.keySet());
        for (String questId : registeredQuestIds) {
            questStates.put(questId, QuestState.NOT_STARTED);
        }

        if (states == null) {
            return;
        }

        for (Map.Entry<String, QuestState> entry : states.entrySet()) {
            QuestState state = entry.getValue() == null ? QuestState.NOT_STARTED : entry.getValue();
            questStates.put(entry.getKey(), state);
        }
    }

    public void replaceCollectedItems(Set<String> itemIds) {
        collectedItems.clear();
        if (itemIds != null) {
            collectedItems.addAll(itemIds);
        }
    }

    /**
     * Kiểm tra quest theo id đã active chưa.
     */
    public boolean isQuestActive(String questId) {
        return getQuestState(questId) == QuestState.ACTIVE;
    }

    /**
     * Kiểm tra quest theo id đã hoàn thành chưa.
     */
    public boolean isQuestCompleted(String questId) {
        return getQuestState(questId) == QuestState.COMPLETED;
    }

    /**
     * Bắt đầu quest tìm cần câu.
     */
    public void startFishingQuest() {
        startQuest(FISHING_ROD_QUEST_ID);
    }

    /**
     * Hoàn thành quest.
     */
    public void completeFishingQuest() {
        completeQuest(FISHING_ROD_QUEST_ID);
    }

    /**
     * Đánh dấu đã nhặt cần câu.
     */
    public void pickUpRod() {
        addItem(FISHING_ROD_QUEST_ID);
    }

    /**
     * Kiểm tra đã nhặt cần câu chưa.
     */
    public boolean hasRod() {
        return hasItem(FISHING_ROD_QUEST_ID);
    }

    /**
     * Lấy trạng thái quest hiện tại.
     */
    public QuestState getFishingQuestState() {
        return getQuestState(FISHING_ROD_QUEST_ID);
    }

    /**
     * Kiểm tra quest đã active chưa.
     */
    public boolean isQuestActive() {
        return isQuestActive(FISHING_ROD_QUEST_ID);
    }

    /**
     * Kiểm tra quest đã hoàn thành chưa.
     */
    public boolean isQuestCompleted() {
        return isQuestCompleted(FISHING_ROD_QUEST_ID);
    }
}
