package com.game.core;

import com.game.util.Constants;

/**
 * Camera — Theo dõi player với smooth lerp.
 * Clamp trong map bounds để không hiện void.
 */
public class Camera {

    private double x, y;
    private double targetX, targetY;
    private double mapPixelWidth, mapPixelHeight;

    public Camera(double mapPixelWidth, double mapPixelHeight) {
        this.mapPixelWidth = mapPixelWidth;
        this.mapPixelHeight = mapPixelHeight;
    }

    /**
     * Cập nhật camera position để center vào target (player).
     */
    public void update(double targetWorldX, double targetWorldY, double dt) {
        // Target: center player on screen
        targetX = targetWorldX - Constants.WINDOW_WIDTH / 2.0;
        targetY = targetWorldY - Constants.WINDOW_HEIGHT / 2.0;

        // Smooth lerp
        x += (targetX - x) * Constants.CAMERA_LERP_SPEED * dt;
        y += (targetY - y) * Constants.CAMERA_LERP_SPEED * dt;

        // Clamp to map bounds
        x = Math.max(0, Math.min(x, mapPixelWidth - Constants.WINDOW_WIDTH));
        y = Math.max(0, Math.min(y, mapPixelHeight - Constants.WINDOW_HEIGHT));

        // Handle maps smaller than screen
        if (mapPixelWidth <= Constants.WINDOW_WIDTH) {
            x = (mapPixelWidth - Constants.WINDOW_WIDTH) / 2.0;
        }
        if (mapPixelHeight <= Constants.WINDOW_HEIGHT) {
            y = (mapPixelHeight - Constants.WINDOW_HEIGHT) / 2.0;
        }
    }

    /**
     * Snap camera ngay vào target (dùng lúc khởi tạo).
     */
    public void snapTo(double targetWorldX, double targetWorldY) {
        x = targetWorldX - Constants.WINDOW_WIDTH / 2.0;
        y = targetWorldY - Constants.WINDOW_HEIGHT / 2.0;

        x = Math.max(0, Math.min(x, mapPixelWidth - Constants.WINDOW_WIDTH));
        y = Math.max(0, Math.min(y, mapPixelHeight - Constants.WINDOW_HEIGHT));
    }

    public double getOffsetX() {
        return x;
    }

    public double getOffsetY() {
        return y;
    }
}
