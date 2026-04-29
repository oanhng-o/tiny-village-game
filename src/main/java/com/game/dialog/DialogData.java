package com.game.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DialogData — Model chứa nội dung dialog cho NPC.
 * Hỗ trợ 3 kiểu: SIMPLE, CHOICE, QUEST.
 */
public class DialogData {

    public enum Type {
        SIMPLE, CHOICE, QUEST
    }

    /**
     * Một lựa chọn trong dialog CHOICE.
     */
    public static class Choice {
        public final String text;
        public final String response;

        public Choice(String text, String response) {
            this.text = text;
            this.response = response;
        }
    }

    private final Type type;

    // SIMPLE dialog
    private List<String> lines;

    // CHOICE dialog
    private String question;
    private List<Choice> choices;

    // QUEST dialog
    private Map<QuestSystem.QuestState, List<String>> questDialogs;

    private DialogData(Type type) {
        this.type = type;
    }

    // ==================== Factory methods ====================

    /**
     * Tạo dialog đơn giản — NPC nói từng dòng, nhấn Space next.
     */
    public static DialogData simple(String... dialogLines) {
        DialogData data = new DialogData(Type.SIMPLE);
        data.lines = new ArrayList<>(List.of(dialogLines));
        return data;
    }

    /**
     * Tạo dialog có lựa chọn — câu hỏi + 2-3 options.
     */
    public static DialogData choice(String question, Choice... choices) {
        DialogData data = new DialogData(Type.CHOICE);
        data.question = question;
        data.choices = new ArrayList<>(List.of(choices));
        return data;
    }

    /**
     * Tạo dialog quest — nội dung thay đổi theo quest state.
     */
    public static DialogData quest(
            List<String> notStarted,
            List<String> active,
            List<String> completed) {
        DialogData data = new DialogData(Type.QUEST);
        data.questDialogs = new HashMap<>();
        data.questDialogs.put(QuestSystem.QuestState.NOT_STARTED, notStarted);
        data.questDialogs.put(QuestSystem.QuestState.ACTIVE, active);
        data.questDialogs.put(QuestSystem.QuestState.COMPLETED, completed);
        return data;
    }

    // ==================== Getters ====================

    public Type getType() { return type; }
    public List<String> getLines() { return lines; }
    public String getQuestion() { return question; }
    public List<Choice> getChoices() { return choices; }

    public List<String> getQuestLines(QuestSystem.QuestState state) {
        if (questDialogs == null) return List.of();
        return questDialogs.getOrDefault(state, List.of("..."));
    }
}
