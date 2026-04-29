package com.game.entity;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Entity — Base class cho tất cả đối tượng trong game.
 * Chứa vị trí, kích thước, sprite và collision bounds.
 */
public abstract class Entity {

    protected double x, y;
    protected int width, height;
    protected Image currentSprite;

    // Animation
    protected int currentFrame = 0;
    protected double frameTimer = 0;
    protected double animationSpeed = 0.2; // seconds per frame

    public Entity(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Lấy collision bounds (AABB).
     */
    public Rectangle2D getBounds() {
        // Collision box nhỏ hơn sprite một chút (chỉ phần chân)
        return new Rectangle2D(x + 4, y + height / 2.0, width - 8, height / 2.0);
    }

    /**
     * Lấy full bounds (dùng cho rendering).
     */
    public Rectangle2D getFullBounds() {
        return new Rectangle2D(x, y, width, height);
    }

    /**
     * Update logic mỗi frame.
     */
    public abstract void update(double dt);

    /**
     * Render entity lên canvas.
     */
    public void render(GraphicsContext gc, double camX, double camY) {
        if (currentSprite != null) {
            gc.drawImage(currentSprite, x - camX, y - camY, width, height);
        }
    }

    /**
     * Kiểm tra collision giữa 2 entity.
     */
    public boolean collidesWith(Entity other) {
        return getBounds().intersects(other.getBounds());
    }

    /**
     * Kiểm tra collision với một rectangle.
     */
    public boolean collidesWith(Rectangle2D rect) {
        return getBounds().intersects(rect);
    }

    // Getters/Setters
    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public double getCenterX() { return x + width / 2.0; }
    public double getCenterY() { return y + height / 2.0; }
}
