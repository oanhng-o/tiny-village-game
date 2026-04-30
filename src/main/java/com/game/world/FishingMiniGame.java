package com.game.world;

import com.game.core.InputHandler;
import com.game.entity.Player;
import com.game.util.Constants;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Random;

/**
 * Handles the fishing waiting phase and power-bar challenge.
 */
public class FishingMiniGame {

    public enum Result {
        NONE,
        WIN,
        LOSE
    }

    private enum Phase {
        INACTIVE,
        WAITING,
        POWER_BAR
    }

    private static final double WAIT_MIN_SECONDS = 2.0;
    private static final double WAIT_MAX_SECONDS = 5.0;
    private static final double POWER_DURATION_SECONDS = 7.0;
    private static final double MAX_VALUE = 100.0;
    private static final double START_VALUE = 20.0;
    private static final double SPACE_GAIN = 8.0;
    private static final double DECAY_PER_SECOND = 15.0;

    private final Random random = new Random();

    private Phase phase = Phase.INACTIVE;
    private double waitDuration;
    private double waitTimer;
    private double powerTimer;
    private double currentValue;
    private double biteBounceTimer;

    private javafx.geometry.Point2D waterTile;

    public void start(Player player, javafx.geometry.Point2D waterTile) {
        this.waterTile = waterTile;
        phase = Phase.WAITING;
        waitDuration = WAIT_MIN_SECONDS + random.nextDouble() * (WAIT_MAX_SECONDS - WAIT_MIN_SECONDS);
        waitTimer = 0;
        powerTimer = 0;
        currentValue = START_VALUE;
        biteBounceTimer = 0;
        player.setState(Player.PlayerState.FISHING);
    }

    public Result update(double dt, InputHandler input) {
        if (phase == Phase.INACTIVE) {
            return Result.NONE;
        }

        if (phase == Phase.WAITING) {
            waitTimer += dt;
            if (waitTimer >= waitDuration) {
                phase = Phase.POWER_BAR;
                powerTimer = 0;
                currentValue = START_VALUE;
            }
            return Result.NONE;
        }

        biteBounceTimer += dt;
        powerTimer += dt;
        currentValue = Math.max(0, currentValue - DECAY_PER_SECOND * dt);

        if (input.isKeyJustPressed(KeyCode.SPACE)) {
            currentValue = Math.min(MAX_VALUE, currentValue + SPACE_GAIN);
        }

        if (currentValue >= MAX_VALUE) {
            phase = Phase.INACTIVE;
            return Result.WIN;
        }

        if (powerTimer >= POWER_DURATION_SECONDS) {
            phase = Phase.INACTIVE;
            return Result.LOSE;
        }

        return Result.NONE;
    }

    public boolean isActive() {
        return phase != Phase.INACTIVE;
    }

    public void render(GraphicsContext gc, Player player, double camX, double camY) {
        if (phase == Phase.INACTIVE) {
            return;
        }

        if (phase == Phase.WAITING) {
            renderWaitingHint(gc);
            return;
        }

        renderBiteIndicator(gc, player, camX, camY);
        renderPowerBar(gc);

        if (waterTile != null) {
            renderBubbles(gc, player, camX, camY);
        }
    }

    private void renderBubbles(GraphicsContext gc, Player player, double camX, double camY) {
        double px = player.getCenterX() - camX;
        double py = player.getCenterY() - camY;

        double offsetDistance = 40.0;
        int direction = player.getDirection();

        double cx = px;
        double cy = py + 8; // Adjust base Y slightly down since player center is usually higher

        switch (direction) {
            case 0 -> cy += offsetDistance; // down
            case 1 -> cx -= offsetDistance; // left
            case 2 -> cx += offsetDistance; // right
            case 3 -> cy -= offsetDistance; // up
        }

        double offset1 = (biteBounceTimer * 2) % 1.0;
        double alpha1 = Math.max(0.0, Math.min(1.0, 1.0 - offset1));
        gc.setStroke(Color.web("#FFFFFF", alpha1));
        gc.setLineWidth(1.5);
        gc.strokeOval(cx - 8 - offset1 * 2, cy - 8 - offset1 * 10, 4 + offset1 * 4, 4 + offset1 * 4);

        double offset2 = (biteBounceTimer * 2 + 0.5) % 1.0;
        double alpha2 = Math.max(0.0, Math.min(1.0, 1.0 - offset2));
        gc.setStroke(Color.web("#FFFFFF", alpha2));
        gc.strokeOval(cx + 4 - offset2 * 2, cy - 4 - offset2 * 10, 3 + offset2 * 3, 3 + offset2 * 3);
    }

    private void renderWaitingHint(GraphicsContext gc) {
        double boxW = 170;
        double boxH = 34;
        double boxX = (Constants.WINDOW_WIDTH - boxW) / 2.0;
        double boxY = Constants.WINDOW_HEIGHT - 92;

        gc.setFill(Color.web("#000000", 0.45));
        gc.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        gc.setStroke(Color.web("#6BB5FF", 0.8));
        gc.setLineWidth(2);
        gc.strokeRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(Constants.FONT_DEFAULT, FontWeight.BOLD, 13));
        gc.fillText("Fishing...", boxX + 44, boxY + 22);
    }

    private void renderBiteIndicator(GraphicsContext gc, Player player, double camX, double camY) {
        double bounce = Math.sin(biteBounceTimer * 10) * 4;
        double x = player.getCenterX() - camX - 6;
        double y = player.getY() - camY - 16 + bounce;

        gc.setFill(Color.web("#FFD700"));
        gc.fillOval(x - 6, y - 18, 24, 24);
        gc.setStroke(Color.web("#4A3520", 0.6));
        gc.setLineWidth(2);
        gc.strokeOval(x - 6, y - 18, 24, 24);

        gc.setFill(Color.web("#4A3520"));
        gc.setFont(Font.font(Constants.FONT_DEFAULT, FontWeight.BOLD, 18));
        gc.fillText("!", x + 1, y + 1);
    }

    private void renderPowerBar(GraphicsContext gc) {
        double panelW = 360;
        double panelH = 72;
        double panelX = (Constants.WINDOW_WIDTH - panelW) / 2.0;
        double panelY = Constants.WINDOW_HEIGHT - 116;
        double barX = panelX + 24;
        double barY = panelY + 34;
        double barW = panelW - 48;
        double barH = 18;
        double ratio = currentValue / MAX_VALUE;
        double timeLeft = Math.max(0, POWER_DURATION_SECONDS - powerTimer);

        gc.setFill(Color.web("#1A1A2E", 0.88));
        gc.fillRoundRect(panelX, panelY, panelW, panelH, 8, 8);
        gc.setStroke(Color.web("#6BB5FF", 0.9));
        gc.setLineWidth(2);
        gc.strokeRoundRect(panelX, panelY, panelW, panelH, 8, 8);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(Constants.FONT_DEFAULT, FontWeight.BOLD, 13));
        gc.fillText("SPACE", panelX + 24, panelY + 22);

        gc.setFont(Font.font(Constants.FONT_DEFAULT, 12));
        gc.setFill(Color.web("#FFFFFF", 0.75));
        gc.fillText(String.format("%.1fs", timeLeft), panelX + panelW - 62, panelY + 22);

        gc.setFill(Color.web("#0D1B2A"));
        gc.fillRoundRect(barX, barY, barW, barH, 6, 6);

        Color fill = ratio >= 0.75 ? Color.web("#4CAF50") : ratio >= 0.4 ? Color.web("#FFD54F") : Color.web("#EF5350");
        gc.setFill(fill);
        gc.fillRoundRect(barX, barY, barW * ratio, barH, 6, 6);

        gc.setStroke(Color.web("#FFFFFF", 0.35));
        gc.setLineWidth(1);
        gc.strokeRoundRect(barX, barY, barW, barH, 6, 6);
    }
}
