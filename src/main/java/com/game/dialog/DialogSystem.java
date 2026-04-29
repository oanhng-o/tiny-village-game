package com.game.dialog;

import com.game.core.InputHandler;
import com.game.entity.NPC;
import com.game.util.Constants;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

/**
 * DialogSystem — State machine quản lý hiển thị dialog.
 * Render dialog box nửa trong suốt ở dưới màn hình.
 * Hỗ trợ typewriter effect và choice selection.
 */
public class DialogSystem {

    public enum State {
        INACTIVE,
        SHOWING_TEXT,
        SHOWING_CHOICES,
        WAITING_QUEST_RESPONSE
    }

    private State state = State.INACTIVE;
    private NPC currentNPC;
    private DialogData currentDialog;

    // Text display
    private List<String> currentLines;
    private int currentLineIndex = 0;
    private String displayText = "";
    private int charIndex = 0;
    private double charTimer = 0;
    private static final double CHARS_PER_SECOND = 35;

    // Choice selection
    private int selectedChoice = 0;
    private String choiceResponse = null;
    private boolean showingChoiceResponse = false;

    // UI dimensions
    private static final int BOX_HEIGHT = 140;
    private static final int BOX_MARGIN = 20;
    private static final int TEXT_PADDING = 20;

    // Quest callback
    private QuestSystem questSystem;
    private Runnable onQuestStart;
    private Runnable onQuestComplete;

    public DialogSystem(QuestSystem questSystem) {
        this.questSystem = questSystem;
    }

    /**
     * Bắt đầu dialog với NPC.
     */
    public void startDialog(NPC npc) {
        this.currentNPC = npc;
        this.currentDialog = npc.getDialogData();
        if (currentDialog == null) return;

        state = State.SHOWING_TEXT;
        currentLineIndex = 0;
        charIndex = 0;
        charTimer = 0;
        selectedChoice = 0;
        choiceResponse = null;
        showingChoiceResponse = false;

        switch (currentDialog.getType()) {
            case SIMPLE -> {
                currentLines = currentDialog.getLines();
            }
            case CHOICE -> {
                currentLines = List.of(currentDialog.getQuestion());
            }
            case QUEST -> {
                QuestSystem.QuestState questState = questSystem.getFishingQuestState();
                currentLines = currentDialog.getQuestLines(questState);
            }
        }

        if (currentLines == null || currentLines.isEmpty()) {
            state = State.INACTIVE;
            return;
        }

        displayText = currentLines.get(0);
    }

    /**
     * Xử lý input trong dialog.
     */
    public void handleInput(InputHandler input) {
        if (state == State.INACTIVE) return;

        if (state == State.SHOWING_TEXT) {
            if (input.isKeyJustPressed(KeyCode.SPACE) || input.isKeyJustPressed(KeyCode.ENTER)) {
                // If text is still typing, skip to full
                if (charIndex < displayText.length()) {
                    charIndex = displayText.length();
                    return;
                }

                // Next line
                currentLineIndex++;

                if (currentLineIndex >= currentLines.size()) {
                    // End of lines
                    if (currentDialog.getType() == DialogData.Type.CHOICE && !showingChoiceResponse) {
                        // Show choices
                        state = State.SHOWING_CHOICES;
                        selectedChoice = 0;
                    } else if (currentDialog.getType() == DialogData.Type.QUEST) {
                        handleQuestEnd();
                        closeDialog();
                    } else {
                        closeDialog();
                    }
                } else {
                    // Show next line
                    displayText = currentLines.get(currentLineIndex);
                    charIndex = 0;
                    charTimer = 0;
                }
            }
        } else if (state == State.SHOWING_CHOICES) {
            List<DialogData.Choice> choices = currentDialog.getChoices();

            if (input.isKeyJustPressed(KeyCode.UP) || input.isKeyJustPressed(KeyCode.W)) {
                selectedChoice = Math.max(0, selectedChoice - 1);
            }
            if (input.isKeyJustPressed(KeyCode.DOWN) || input.isKeyJustPressed(KeyCode.S)) {
                selectedChoice = Math.min(choices.size() - 1, selectedChoice + 1);
            }
            if (input.isKeyJustPressed(KeyCode.DIGIT1) && choices.size() > 0) selectedChoice = 0;
            if (input.isKeyJustPressed(KeyCode.DIGIT2) && choices.size() > 1) selectedChoice = 1;
            if (input.isKeyJustPressed(KeyCode.DIGIT3) && choices.size() > 2) selectedChoice = 2;

            if (input.isKeyJustPressed(KeyCode.SPACE) || input.isKeyJustPressed(KeyCode.ENTER)) {
                // Select choice and show response
                DialogData.Choice choice = choices.get(selectedChoice);
                choiceResponse = choice.response;
                showingChoiceResponse = true;

                // Switch to showing the response
                currentLines = List.of(choiceResponse);
                currentLineIndex = 0;
                displayText = choiceResponse;
                charIndex = 0;
                charTimer = 0;
                state = State.SHOWING_TEXT;
            }
        }
    }

    /**
     * Xử lý kết thúc quest dialog.
     */
    private void handleQuestEnd() {
        QuestSystem.QuestState qs = questSystem.getFishingQuestState();
        if (qs == QuestSystem.QuestState.NOT_STARTED) {
            questSystem.startFishingQuest();
            if (onQuestStart != null) onQuestStart.run();
        } else if (qs == QuestSystem.QuestState.ACTIVE && questSystem.hasRod()) {
            questSystem.completeFishingQuest();
            if (onQuestComplete != null) onQuestComplete.run();
        }
    }

    /**
     * Đóng dialog.
     */
    private void closeDialog() {
        state = State.INACTIVE;
        currentNPC = null;
        currentDialog = null;
        currentLines = null;
    }

    /**
     * Update typewriter effect.
     */
    public void update(double dt) {
        if (state == State.INACTIVE) return;

        if (state == State.SHOWING_TEXT && charIndex < displayText.length()) {
            charTimer += dt;
            double charDelay = 1.0 / CHARS_PER_SECOND;
            while (charTimer >= charDelay && charIndex < displayText.length()) {
                charTimer -= charDelay;
                charIndex++;
            }
        }
    }

    /**
     * Render dialog box.
     */
    public void render(GraphicsContext gc) {
        if (state == State.INACTIVE) return;

        double w = Constants.WINDOW_WIDTH;
        double h = Constants.WINDOW_HEIGHT;
        double boxX = BOX_MARGIN + 20;
        double boxY = h - BOX_HEIGHT - BOX_MARGIN;
        double boxW = w - BOX_MARGIN * 2 - 40;

        // Dialog box background (semi-transparent cream)
        gc.setFill(Color.web("#FFF8DC", 0.95));
        gc.fillRoundRect(boxX, boxY, boxW, BOX_HEIGHT, 20, 20);

        // Border (pastel blue)
        gc.setStroke(Color.web("#A0C4FF"));
        gc.setLineWidth(4);
        gc.strokeRoundRect(boxX, boxY, boxW, BOX_HEIGHT, 20, 20);

        // NPC name tag (bubble on top left)
        if (currentNPC != null) {
            String name = currentNPC.getName();
            double nameWidth = name.length() * 9 + 20;
            double tagHeight = 30;
            double tagX = boxX + 20;
            double tagY = boxY - tagHeight / 2;
            
            // Tag background
            gc.setFill(Color.web("#FFD6A5"));
            gc.fillRoundRect(tagX, tagY, nameWidth, tagHeight, 15, 15);
            gc.setStroke(Color.web("#FFFFFF"));
            gc.setLineWidth(2);
            gc.strokeRoundRect(tagX, tagY, nameWidth, tagHeight, 15, 15);

            // Tag text
            gc.setFill(Color.web("#5D4037"));
            gc.setFont(Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 14));
            gc.fillText(name, tagX + 10, tagY + 20);
        }

        // Dialog text (with typewriter effect) - chỉ hiển thị khi đang show text, không phải khi show choices
        if (state == State.SHOWING_TEXT) {
            gc.setFill(Color.web("#4A4A4A"));
            gc.setFont(Font.font("Monospaced", 15));
            if (displayText != null && charIndex > 0) {
                String visibleText = displayText.substring(0, Math.min(charIndex, displayText.length()));
                // Word wrap
                renderWrappedText(gc, visibleText, boxX + TEXT_PADDING + 10, boxY + 40,
                                boxW - TEXT_PADDING * 2 - 20, 22);
            }
        }

        // Continue indicator
        if (state == State.SHOWING_TEXT && charIndex >= displayText.length()) {
            double blinkAlpha = 0.4 + 0.6 * Math.sin(System.currentTimeMillis() / 150.0);
            gc.setFill(Color.web("#A0C4FF", blinkAlpha));
            gc.setFont(Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 14));
            gc.fillText("▼ Space", boxX + boxW - 90, boxY + BOX_HEIGHT - 15);
        }

        // Render choices
        if (state == State.SHOWING_CHOICES) {
            renderChoices(gc, boxX, boxY, boxW);
        }
    }

    /**
     * Render choice options.
     */
    private void renderChoices(GraphicsContext gc, double boxX, double boxY, double boxW) {
        List<DialogData.Choice> choices = currentDialog.getChoices();
        double choiceY = boxY + 45;

        gc.setFont(Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 14));

        for (int i = 0; i < choices.size(); i++) {
            double cy = choiceY + i * 32;
            boolean selected = (i == selectedChoice);

            // Choice background
            if (selected) {
                gc.setFill(Color.web("#A0C4FF", 0.3));
                gc.fillRoundRect(boxX + TEXT_PADDING + 5, cy - 18, boxW - TEXT_PADDING * 2 - 10, 28, 10, 10);
                gc.setStroke(Color.web("#A0C4FF"));
                gc.setLineWidth(2);
                gc.strokeRoundRect(boxX + TEXT_PADDING + 5, cy - 18, boxW - TEXT_PADDING * 2 - 10, 28, 10, 10);
            }

            // Choice text
            gc.setFill(selected ? Color.web("#2B547E") : Color.web("#4A4A4A"));
            String prefix = selected ? "▶ " : "  ";
            String numLabel = "[" + (i + 1) + "] ";
            gc.fillText(prefix + numLabel + choices.get(i).text, boxX + TEXT_PADDING + 15, cy);
        }
    }

    /**
     * Render text với word wrap.
     */
    private void renderWrappedText(GraphicsContext gc, String text, double x, double y,
                                    double maxWidth, double lineHeight) {
        double charWidth = 7.8; // approximate for Monospaced 13
        int charsPerLine = (int)(maxWidth / charWidth);

        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        double currentY = y;

        for (String word : words) {
            if (line.length() + word.length() + 1 > charsPerLine) {
                gc.fillText(line.toString(), x, currentY);
                currentY += lineHeight;
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) {
            gc.fillText(line.toString(), x, currentY);
        }
    }

    /**
     * Render quest indicator ở góc màn hình.
     */
    public void renderQuestIndicator(GraphicsContext gc) {
        QuestSystem.QuestState qs = questSystem.getFishingQuestState();
        if (qs == QuestSystem.QuestState.NOT_STARTED) return;

        String questText;
        Color questColor;

        if (qs == QuestSystem.QuestState.ACTIVE) {
            String status = questSystem.hasRod() ? "✓ Đã tìm thấy!" : "Đang tìm...";
            questText = "🎣 Tìm cần câu: " + status;
            questColor = Color.web("#5D4037"); // Dark brown
        } else {
            questText = "🎣 Tìm cần câu: ✓ Hoàn thành!";
            questColor = Color.web("#2E7D32"); // Dark green
        }

        // Background
        gc.setFont(Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 13));
        double qw = questText.length() * 8.5 + 20;
        double qx = Constants.WINDOW_WIDTH - qw - 15;
        double qy = 15;
        
        gc.setFill(Color.web("#FFF8DC", 0.9));
        gc.fillRoundRect(qx, qy, qw, 35, 15, 15);
        gc.setStroke(Color.web("#A8E6CF")); // Pastel green
        gc.setLineWidth(3);
        gc.strokeRoundRect(qx, qy, qw, 35, 15, 15);

        // Text
        gc.setFill(questColor);
        gc.fillText(questText, qx + 10, qy + 22);
    }

    public boolean isActive() { return state != State.INACTIVE; }
    public void setOnQuestStart(Runnable r) { this.onQuestStart = r; }
    public void setOnQuestComplete(Runnable r) { this.onQuestComplete = r; }
}
