package com.game.ui;

import com.game.audio.AudioManager;
import com.game.audio.AudioSettings;
import com.game.core.InputHandler;
import com.game.util.Constants;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Shared lightweight audio settings overlay used by menu and gameplay.
 */
public final class AudioSettingsOverlay {

    private static final String TITLE = "Âm thanh";
    private static final String[] ROW_LABELS = {
            "Nhạc nền",
            "Hiệu ứng",
            "Tắt nhạc",
            "Tắt hiệu ứng"
    };

    private boolean open;
    private int selectedRow;

    public boolean isOpen() {
        return open;
    }

    public void open() {
        open = true;
    }

    public void close() {
        open = false;
    }

    public void handleInput(InputHandler input, AudioSettings settings, Runnable onSettingsChanged) {
        if (!open || settings == null) {
            return;
        }

        if (input.isKeyJustPressed(KeyCode.ESCAPE)) {
            AudioManager.getInstance().playSfxEvent(Constants.AUDIO_EVENT_BACK);
            close();
            return;
        }

        if (input.isKeyJustPressed(KeyCode.UP) || input.isKeyJustPressed(KeyCode.W)) {
            selectedRow = (selectedRow - 1 + ROW_LABELS.length) % ROW_LABELS.length;
            AudioManager.getInstance().playSfxEvent(Constants.AUDIO_EVENT_DIALOG_ADVANCE);
            return;
        }

        if (input.isKeyJustPressed(KeyCode.DOWN) || input.isKeyJustPressed(KeyCode.S)) {
            selectedRow = (selectedRow + 1) % ROW_LABELS.length;
            AudioManager.getInstance().playSfxEvent(Constants.AUDIO_EVENT_DIALOG_ADVANCE);
            return;
        }

        boolean changed = false;
        if (input.isKeyJustPressed(KeyCode.LEFT) || input.isKeyJustPressed(KeyCode.A)) {
            changed = adjustValue(settings, -AudioSettings.VOLUME_STEP);
        } else if (input.isKeyJustPressed(KeyCode.RIGHT) || input.isKeyJustPressed(KeyCode.D)) {
            changed = adjustValue(settings, AudioSettings.VOLUME_STEP);
        } else if (input.isKeyJustPressed(KeyCode.ENTER) || input.isKeyJustPressed(KeyCode.SPACE)) {
            toggleSelected(settings);
            AudioManager.getInstance().playSfxEvent(Constants.AUDIO_EVENT_CONFIRM);
            changed = true;
        }

        if (changed && onSettingsChanged != null) {
            onSettingsChanged.run();
        }
    }

    public void render(GraphicsContext gc) {
        if (!open) {
            return;
        }

        double panelW = 380;
        double panelH = 270;
        double panelX = (Constants.WINDOW_WIDTH - panelW) / 2.0;
        double panelY = (Constants.WINDOW_HEIGHT - panelH) / 2.0;

        gc.setFill(Color.web("#000000", 0.55));
        gc.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        gc.setFill(Color.web("#FFF8DC", 0.98));
        gc.fillRoundRect(panelX, panelY, panelW, panelH, 18, 18);
        gc.setStroke(Color.web("#A0C4FF"));
        gc.setLineWidth(3);
        gc.strokeRoundRect(panelX, panelY, panelW, panelH, 18, 18);

        gc.setFill(Color.web("#5D4037"));
        gc.setFont(Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 24));
        gc.fillText(TITLE, panelX + 24, panelY + 38);

        gc.setFill(Color.web("#7A5A44"));
        gc.setFont(Font.font("Monospaced", 11));
        gc.fillText("P / Esc: Đóng", panelX + panelW - 110, panelY + 36);
    }

    public void renderRows(GraphicsContext gc, AudioSettings settings) {
        if (!open || settings == null) {
            return;
        }

        double panelW = 380;
        double panelH = 270;
        double panelX = (Constants.WINDOW_WIDTH - panelW) / 2.0;
        double panelY = (Constants.WINDOW_HEIGHT - panelH) / 2.0;
        double rowX = panelX + 24;
        double rowY = panelY + 68;
        double rowW = panelW - 48;
        double rowH = 42;

        for (int rowIndex = 0; rowIndex < ROW_LABELS.length; rowIndex++) {
            double currentRowY = rowY + rowIndex * 50;
            boolean selected = rowIndex == selectedRow;

            gc.setFill(selected ? Color.web("#E8F4FF") : Color.web("#FFFFFF", 0.75));
            gc.fillRoundRect(rowX, currentRowY, rowW, rowH, 10, 10);
            gc.setStroke(selected ? Color.web("#4F8EDC") : Color.web("#D6C49C", 0.9));
            gc.setLineWidth(2);
            gc.strokeRoundRect(rowX, currentRowY, rowW, rowH, 10, 10);

            gc.setFill(Color.web("#4A4A4A"));
            gc.setFont(Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 13));
            gc.fillText((selected ? "▶ " : "  ") + ROW_LABELS[rowIndex], rowX + 14, currentRowY + 18);

            gc.setFont(Font.font("Monospaced", 12));
            gc.fillText(resolveValueText(settings, rowIndex), rowX + 220, currentRowY + 18);
        }

        gc.setFill(Color.web("#7A5A44"));
        gc.setFont(Font.font("Monospaced", 11));
        gc.fillText("↑/↓ chọn | ←/→ chỉnh | Enter bật/tắt", panelX + 48, panelY + panelH - 14);
    }

    private boolean adjustValue(AudioSettings settings, double delta) {
        switch (selectedRow) {
            case 0 -> settings.adjustMusicVolume(delta);
            case 1 -> settings.adjustSfxVolume(delta);
            default -> {
                return false;
            }
        }

        AudioManager.getInstance().playSfxEvent(Constants.AUDIO_EVENT_DIALOG_ADVANCE);
        return true;
    }

    private void toggleSelected(AudioSettings settings) {
        switch (selectedRow) {
            case 0, 2 -> settings.toggleMusicMuted();
            case 1, 3 -> settings.toggleSfxMuted();
            default -> {
            }
        }
    }

    private String resolveValueText(AudioSettings settings, int rowIndex) {
        return switch (rowIndex) {
            case 0 -> formatPercent(settings.getMusicVolume());
            case 1 -> formatPercent(settings.getSfxVolume());
            case 2 -> settings.isMusicMuted() ? "Bật lại bằng Enter" : "Đang bật";
            case 3 -> settings.isSfxMuted() ? "Bật lại bằng Enter" : "Đang bật";
            default -> "";
        };
    }

    private String formatPercent(double value) {
        return (int) Math.round(value * 100.0) + "%";
    }
}