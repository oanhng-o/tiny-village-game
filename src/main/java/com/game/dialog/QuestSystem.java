package com.game.dialog;

/**
 * QuestSystem — Quản lý trạng thái quest.
 * Chỉ có 1 quest: "Tìm cần câu cho bạn nhỏ".
 */
public class QuestSystem {

    public enum QuestState {
        NOT_STARTED,
        ACTIVE,
        COMPLETED
    }

    private QuestState fishingRodQuest = QuestState.NOT_STARTED;
    private boolean hasPickedUpRod = false;

    /**
     * Bắt đầu quest tìm cần câu.
     */
    public void startFishingQuest() {
        if (fishingRodQuest == QuestState.NOT_STARTED) {
            fishingRodQuest = QuestState.ACTIVE;
        }
    }

    /**
     * Hoàn thành quest.
     */
    public void completeFishingQuest() {
        fishingRodQuest = QuestState.COMPLETED;
    }

    /**
     * Đánh dấu đã nhặt cần câu.
     */
    public void pickUpRod() {
        hasPickedUpRod = true;
    }

    /**
     * Kiểm tra đã nhặt cần câu chưa.
     */
    public boolean hasRod() {
        return hasPickedUpRod;
    }

    /**
     * Lấy trạng thái quest hiện tại.
     */
    public QuestState getFishingQuestState() {
        return fishingRodQuest;
    }

    /**
     * Kiểm tra quest đã active chưa.
     */
    public boolean isQuestActive() {
        return fishingRodQuest == QuestState.ACTIVE;
    }

    /**
     * Kiểm tra quest đã hoàn thành chưa.
     */
    public boolean isQuestCompleted() {
        return fishingRodQuest == QuestState.COMPLETED;
    }
}
