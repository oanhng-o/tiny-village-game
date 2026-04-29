package com.game.core;

import com.game.GameApplication;

/**
 * Camera — Theo dõi player với smooth lerp.
 * Clamp trong map bounds để không hiện void.
 */
public class Camera {

    private double x, y;
    private double targetX, targetY;
    private double mapPixelWidth, mapPixelHeight;

    // Lerp speed: 0.0 = no follow, 1.0 = instant
    private static final double LERP_SPEED = 5.0;

    public Camera(double mapPixelWidth, double mapPixelHeight) {
        this.mapPixelWidth = mapPixelWidth;
        this.mapPixelHeight = mapPixelHeight;
    }

    /**
     * Cập nhật camera position để center vào target (player).
     */
    public void update(double targetWorldX, double targetWorldY, double dt) {
        // Target: center player on screen
        targetX = targetWorldX - GameApplication.WINDOW_WIDTH / 2.0;
        targetY = targetWorldY - GameApplication.WINDOW_HEIGHT / 2.0;

        // Smooth lerp
        x += (targetX - x) * LERP_SPEED * dt;
        y += (targetY - y) * LERP_SPEED * dt;

        // Clamp to map bounds
        x = Math.max(0, Math.min(x, mapPixelWidth - GameApplication.WINDOW_WIDTH));
        y = Math.max(0, Math.min(y, mapPixelHeight - GameApplication.WINDOW_HEIGHT));

        // Handle maps smaller than screen
        if (mapPixelWidth <= GameApplication.WINDOW_WIDTH) {
            x = (mapPixelWidth - GameApplication.WINDOW_WIDTH) / 2.0;
        }
        if (mapPixelHeight <= GameApplication.WINDOW_HEIGHT) {
            y = (mapPixelHeight - GameApplication.WINDOW_HEIGHT) / 2.0;
        }
    }

    /**
     * Snap camera ngay vào target (dùng lúc khởi tạo).
     */
    public void snapTo(double targetWorldX, double targetWorldY) {
        x = targetWorldX - GameApplication.WINDOW_WIDTH / 2.0;
        y = targetWorldY - GameApplication.WINDOW_HEIGHT / 2.0;

        x = Math.max(0, Math.min(x, mapPixelWidth - GameApplication.WINDOW_WIDTH));
        y = Math.max(0, Math.min(y, mapPixelHeight - GameApplication.WINDOW_HEIGHT));
    }

    public double getOffsetX() {
        return x;
    }

    public double getOffsetY() {
        return y;
    }
}
