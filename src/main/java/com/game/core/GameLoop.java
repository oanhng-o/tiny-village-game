package com.game.core;

import com.game.world.GameWorld;
import com.game.util.Constants;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * GameLoop — AnimationTimer chạy 60 FPS.
 * Tính delta time và gọi update/render mỗi frame.
 */
public class GameLoop extends AnimationTimer {

    private final GraphicsContext gc;
    private final GameWorld gameWorld;
    private final InputHandler inputHandler;
    private long lastTime = 0;

    // Cap delta time to prevent physics glitches (e.g., after window drag)
    private static final double MAX_DELTA = 0.05;

    public GameLoop(GraphicsContext gc, GameWorld gameWorld, InputHandler inputHandler) {
        this.gc = gc;
        this.gameWorld = gameWorld;
        this.inputHandler = inputHandler;
    }

    @Override
    public void handle(long now) {
        if (lastTime == 0) {
            lastTime = now;
            return;
        }

        double deltaTime = (now - lastTime) / 1_000_000_000.0;
        lastTime = now;

        // Clamp delta time
        if (deltaTime > MAX_DELTA) {
            deltaTime = MAX_DELTA;
        }

        // Update game logic
        gameWorld.update(deltaTime, inputHandler);

        // Clear screen
        gc.setFill(Color.web("#1a1a2e"));
        gc.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        // Render game
        gameWorld.render(gc);

        // Clear justPressed keys at end of frame
        inputHandler.update();
    }
}
