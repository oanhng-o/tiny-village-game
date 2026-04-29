package com.game.entity;

import com.game.core.InputHandler;
import com.game.util.AssetManager;
import com.game.util.SpriteSheet;
import com.game.world.TileMap;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;

/**
 * Player — Nhân vật chính điều khiển bằng WASD/Arrow keys.
 * Có animation 4 hướng và collision detection với map.
 */
public class Player extends Entity {

    private static final double SPEED = 120.0; // pixels per second
    private static final int SPRITE_SIZE = 32;

    private int direction = 0; // 0=down, 1=left, 2=right, 3=up
    private boolean moving = false;
    private SpriteSheet spriteSheet;
    private TileMap tileMap;

    // Position history for cat following
    private final double[] posHistoryX = new double[60];
    private final double[] posHistoryY = new double[60];
    private int posHistoryIndex = 0;
    private double posHistoryTimer = 0;

    // Inventory
    private boolean hasFishingRod = false;

    public Player(double x, double y, TileMap tileMap) {
        super(x, y, SPRITE_SIZE, SPRITE_SIZE);
        this.tileMap = tileMap;
        this.spriteSheet = AssetManager.getInstance().getSpriteSheet("player");

        // Initialize position history
        for (int i = 0; i < posHistoryX.length; i++) {
            posHistoryX[i] = x;
            posHistoryY[i] = y;
        }

        updateSprite();
    }

    /**
     * Update player: xử lý input, di chuyển, collision.
     */
    public void handleInput(double dt, InputHandler input) {
        double dx = 0, dy = 0;

        if (input.isKeyDown(KeyCode.W) || input.isKeyDown(KeyCode.UP)) {
            dy = -1;
            direction = 3;
        }
        if (input.isKeyDown(KeyCode.S) || input.isKeyDown(KeyCode.DOWN)) {
            dy = 1;
            direction = 0;
        }
        if (input.isKeyDown(KeyCode.A) || input.isKeyDown(KeyCode.LEFT)) {
            dx = -1;
            direction = 1;
        }
        if (input.isKeyDown(KeyCode.D) || input.isKeyDown(KeyCode.RIGHT)) {
            dx = 1;
            direction = 2;
        }

        // Normalize diagonal movement
        if (dx != 0 && dy != 0) {
            double len = Math.sqrt(dx * dx + dy * dy);
            dx /= len;
            dy /= len;
        }

        moving = (dx != 0 || dy != 0);

        if (moving) {
            double newX = x + dx * SPEED * dt;
            double newY = y + dy * SPEED * dt;

            // Check collision X
            double oldX = x;
            x = newX;
            if (checkMapCollision()) {
                x = oldX;
            }

            // Check collision Y
            double oldY = y;
            y = newY;
            if (checkMapCollision()) {
                y = oldY;
            }
        }

        // Update position history for cat follow
        posHistoryTimer += dt;
        if (posHistoryTimer >= 0.05) { // Record every 50ms
            posHistoryTimer = 0;
            posHistoryIndex = (posHistoryIndex + 1) % posHistoryX.length;
            posHistoryX[posHistoryIndex] = x;
            posHistoryY[posHistoryIndex] = y;
        }
    }

    @Override
    public void update(double dt) {
        if (moving) {
            frameTimer += dt;
            if (frameTimer >= animationSpeed) {
                frameTimer = 0;
                currentFrame = (currentFrame + 1) % 4;
            }
        } else {
            currentFrame = 0;
            frameTimer = 0;
        }
        updateSprite();
    }

    /**
     * Kiểm tra collision với solid tiles.
     */
    private boolean checkMapCollision() {
        Rectangle2D bounds = getBounds();
        int startCol = (int)(bounds.getMinX() / TileMap.TILE_SIZE);
        int endCol = (int)(bounds.getMaxX() / TileMap.TILE_SIZE);
        int startRow = (int)(bounds.getMinY() / TileMap.TILE_SIZE);
        int endRow = (int)(bounds.getMaxY() / TileMap.TILE_SIZE);

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                if (tileMap.isSolid(col, row)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Cập nhật sprite hiện tại theo direction và frame.
     */
    private void updateSprite() {
        if (spriteSheet != null) {
            currentSprite = spriteSheet.getFrame(currentFrame, direction);
        }
    }

    /**
     * Lấy interaction bounds (khu vực phía trước player).
     */
    public Rectangle2D getInteractionBounds() {
        double ix = x, iy = y;
        double iw = width, ih = height;
        double range = 16; // interaction range in pixels

        switch (direction) {
            case 0 -> { iy += height; ih = range; } // down
            case 1 -> { ix -= range; iw = range; }   // left
            case 2 -> { ix += width; iw = range; }    // right
            case 3 -> { iy -= range; ih = range; }   // up
        }
        return new Rectangle2D(ix, iy, iw, ih);
    }

    @Override
    public void render(GraphicsContext gc, double camX, double camY) {
        if (currentSprite != null) {
            gc.setImageSmoothing(false);
            gc.drawImage(currentSprite, x - camX, y - camY, width, height);
        }
    }

    // Position history for cat follow
    public double getHistoryX(int framesBack) {
        int idx = (posHistoryIndex - framesBack + posHistoryX.length) % posHistoryX.length;
        return posHistoryX[idx];
    }

    public double getHistoryY(int framesBack) {
        int idx = (posHistoryIndex - framesBack + posHistoryY.length) % posHistoryY.length;
        return posHistoryY[idx];
    }

    public boolean hasFishingRod() { return hasFishingRod; }
    public void setHasFishingRod(boolean has) { this.hasFishingRod = has; }
    public int getDirection() { return direction; }
    public boolean isMoving() { return moving; }
}
