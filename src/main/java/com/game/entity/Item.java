package com.game.entity;

import com.game.util.AssetManager;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Item — Vật phẩm nhặt được trên map.
 * Ẩn cho đến khi quest active, tự động nhặt khi player đi qua.
 */
public class Item extends Entity {

    private final String itemId;
    private boolean visible = false;
    private boolean collected = false;
    private double sparkleTimer = 0;
    private double bounceTimer = 0;
    private Image sprite;

    public Item(String itemId, double x, double y) {
        super(x, y, 32, 32);
        this.itemId = itemId;
        this.sprite = AssetManager.getInstance().getImage(itemId);
    }

    @Override
    public void update(double dt) {
        if (!visible || collected) return;

        sparkleTimer += dt;
        bounceTimer += dt;
    }

    /**
     * Kiểm tra player có nhặt được item không (walk over).
     */
    public boolean checkPickup(Player player) {
        if (!visible || collected) return false;
        return player.getBounds().intersects(getFullBounds());
    }

    @Override
    public void render(GraphicsContext gc, double camX, double camY) {
        if (!visible || collected) return;

        double renderX = x - camX;
        double renderY = y - camY;

        // Bounce effect
        double bounce = Math.sin(bounceTimer * 3) * 3;

        // Draw item sprite
        if (sprite != null) {
            gc.setImageSmoothing(false);
            gc.drawImage(sprite, renderX, renderY + bounce, width, height);
        } else {
            gc.setFill(Color.web("#FFD700"));
            gc.fillRect(renderX + 8, renderY + 8 + bounce, 16, 16);
        }

        // Sparkle effect
        gc.setFill(Color.web("#FFD700", 0.5 + 0.5 * Math.sin(sparkleTimer * 5)));
        double sparkleOffset = sparkleTimer * 2;
        for (int i = 0; i < 4; i++) {
            double sx = renderX + 16 + Math.cos(sparkleOffset + i * 1.57) * 18;
            double sy = renderY + 16 + bounce + Math.sin(sparkleOffset + i * 1.57) * 18;
            gc.fillRect(sx - 1, sy - 1, 3, 3);
        }
    }

    // Getters/Setters
    public String getItemId() { return itemId; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public boolean isCollected() { return collected; }
    public void setCollected(boolean collected) { this.collected = collected; }
}
