package com.game.entity;

import com.game.util.AssetManager;
import com.game.util.SpriteSheet;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * CatFollower — Mèo con theo chân player sau khi quest hoàn thành.
 * Hai trạng thái: IDLE (đứng yên) và FOLLOWING (đi theo player).
 */
public class CatFollower extends Entity {

    public enum State {
        IDLE, FOLLOWING
    }

    private State state = State.IDLE;
    private SpriteSheet spriteSheet;
    private double followDistance = 40; // Khoảng cách duy trì với player
    private Player targetPlayer;

    // Animation
    private boolean isWalking = false;
    private int animFrame = 0;
    private double animTimer = 0;

    // Idle animation (tail wag)
    private double idleTimer = 0;

    public CatFollower(double x, double y) {
        super(x, y, 32, 32);
        this.spriteSheet = AssetManager.getInstance().getSpriteSheet("cat");
        this.animationSpeed = 0.25;
        updateSprite();
    }

    @Override
    public void update(double dt) {
        if (state == State.FOLLOWING && targetPlayer != null) {
            // Follow player using position history (smooth trailing)
            double targetX = targetPlayer.getHistoryX(30); // 30 frames back
            double targetY = targetPlayer.getHistoryY(30);

            double dx = targetX - x;
            double dy = targetY - y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > followDistance) {
                double speed = 100.0; // Slightly slower than player
                double moveX = (dx / dist) * speed * dt;
                double moveY = (dy / dist) * speed * dt;
                x += moveX;
                y += moveY;
                isWalking = true;
            } else {
                isWalking = false;
            }
        } else {
            isWalking = false;
        }

        // Update animation
        if (isWalking) {
            animTimer += dt;
            if (animTimer >= animationSpeed) {
                animTimer = 0;
                animFrame = (animFrame + 1) % 4;
            }
        } else {
            idleTimer += dt;
            if (idleTimer >= 0.8) {
                idleTimer = 0;
                animFrame = (animFrame + 1) % 4;
            }
        }
        updateSprite();
    }

    private void updateSprite() {
        if (spriteSheet != null) {
            int row = isWalking ? 1 : 0;
            currentSprite = spriteSheet.getFrame(animFrame, row);
        }
    }

    @Override
    public void render(GraphicsContext gc, double camX, double camY) {
        double renderX = x - camX;
        double renderY = y - camY;

        if (currentSprite != null) {
            gc.setImageSmoothing(false);
            gc.drawImage(currentSprite, renderX, renderY, width, height);
        }

        // Heart effect when following
        if (state == State.FOLLOWING) {
            double heartBounce = Math.sin(idleTimer * 4) * 2;
            gc.setFill(Color.web("#FF69B4", 0.7));
            gc.setFont(javafx.scene.text.Font.font("Monospaced", 10));
            gc.fillText("♥", renderX + width / 2 - 4, renderY - 6 + heartBounce);
        }
    }

    /**
     * Bắt đầu follow player.
     */
    public void startFollowing(Player player) {
        this.state = State.FOLLOWING;
        this.targetPlayer = player;
    }

    // Getters
    public State getState() { return state; }
}
