package com.game;

import com.game.core.GameLoop;
import com.game.core.InputHandler;
import com.game.world.GameWorld;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * GameApplication — Khởi tạo JavaFX Stage, Scene, Canvas.
 * Hiển thị màn hình chọn nhân vật trước khi vào game.
 */
public class GameApplication extends Application {

    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;

    private Canvas canvas;
    private GraphicsContext gc;
    private GameLoop gameLoop;
    private GameWorld gameWorld;
    private InputHandler inputHandler;

    // Character selection state
    private boolean characterSelected = false;
    private int selectedOption = 0; // 0 = girl, 1 = boy
    private boolean selectionConfirmed = false;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tiny village");
        primaryStage.setResizable(false);

        canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        // Pixel art: tắt anti-aliasing để giữ sharp pixels
        gc.setImageSmoothing(false);

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Setup input handler
        inputHandler = InputHandler.getInstance();
        scene.setOnKeyPressed(e -> inputHandler.keyPressed(e.getCode()));
        scene.setOnKeyReleased(e -> inputHandler.keyReleased(e.getCode()));

        primaryStage.setScene(scene);
        primaryStage.show();

        // Bắt đầu với character select screen
        startCharacterSelectLoop();
    }

    /**
     * Vòng lặp cho màn hình chọn nhân vật.
     */
    private void startCharacterSelectLoop() {
        javafx.animation.AnimationTimer selectTimer = new javafx.animation.AnimationTimer() {
            private long lastTime = 0;
            private double bounceTimer = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                bounceTimer += dt;

                // Handle input
                if (inputHandler.isKeyJustPressed(javafx.scene.input.KeyCode.LEFT) ||
                    inputHandler.isKeyJustPressed(javafx.scene.input.KeyCode.A)) {
                    selectedOption = 0;
                }
                if (inputHandler.isKeyJustPressed(javafx.scene.input.KeyCode.RIGHT) ||
                    inputHandler.isKeyJustPressed(javafx.scene.input.KeyCode.D)) {
                    selectedOption = 1;
                }
                if (inputHandler.isKeyJustPressed(javafx.scene.input.KeyCode.ENTER) ||
                    inputHandler.isKeyJustPressed(javafx.scene.input.KeyCode.SPACE)) {
                    selectionConfirmed = true;
                }

                inputHandler.update();

                // Render character select screen
                renderCharacterSelect(bounceTimer);

                if (selectionConfirmed) {
                    this.stop();
                    startGame(selectedOption == 0);
                }
            }
        };
        selectTimer.start();
    }

    /**
     * Render màn hình chọn nhân vật.
     */
    private void renderCharacterSelect(double bounceTimer) {
        // Background gradient (Sky to Grass)
        javafx.scene.paint.LinearGradient bgGradient = new javafx.scene.paint.LinearGradient(
            0, 0, 0, WINDOW_HEIGHT, false, javafx.scene.paint.CycleMethod.NO_CYCLE,
            new javafx.scene.paint.Stop(0, Color.web("#E0F7FA")),
            new javafx.scene.paint.Stop(1, Color.web("#B2EBF2"))
        );
        gc.setFill(bgGradient);
        gc.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Clouds background
        gc.setFill(Color.web("#ffffff", 0.8));
        for (int i = 0; i < 5; i++) {
            double sx = (bounceTimer * 20 + i * 200) % (WINDOW_WIDTH + 150) - 100;
            double sy = 50 + (i * 73) % 150;
            gc.fillRoundRect(sx, sy, 80, 40, 30, 30);
            gc.fillRoundRect(sx + 20, sy - 20, 50, 50, 25, 25);
            gc.fillRoundRect(sx - 15, sy + 15, 40, 25, 20, 20);
        }

        // Title
        gc.setFont(javafx.scene.text.Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 40));
        String title = "Tiny village";
        double titleWidth = title.length() * 24; // approximate width
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(4);
        gc.strokeText(title, (WINDOW_WIDTH - titleWidth) / 2, 100);
        gc.setFill(Color.web("#5D4037"));
        gc.fillText(title, (WINDOW_WIDTH - titleWidth) / 2, 100);

        // Subtitle
        gc.setFill(Color.web("#5D4037", 0.8));
        gc.setFont(Font.font("Monospaced", 18));
        String subtitle = "Chọn nhân vật của bạn";
        double subWidth = subtitle.length() * 10;
        gc.fillText(subtitle, (WINDOW_WIDTH - subWidth) / 2, 140);

        // Character previews
        double girlX = WINDOW_WIDTH / 2 - 130;
        double boyX = WINDOW_WIDTH / 2 + 50;
        double charY = 250;

        // Draw cards
        drawCharacterCard(gc, girlX - 20, charY - 30, selectedOption == 0, bounceTimer);
        drawCharacterCard(gc, boyX - 20, charY - 30, selectedOption == 1, bounceTimer);

        // Draw girl preview (simple pixel art representation)
        double girlBounce = selectedOption == 0 ? Math.sin(bounceTimer * 4) * 5 : 0;
        drawCharacterPreview(gc, girlX, charY + girlBounce, true);
        gc.setFill(Color.web("#5D4037"));
        gc.setFont(Font.font("Monospaced", 16));
        gc.fillText("  Girl", girlX, charY + 120 + girlBounce);

        // Draw boy preview
        double boyBounce = selectedOption == 1 ? Math.sin(bounceTimer * 4) * 5 : 0;
        drawCharacterPreview(gc, boyX, charY + boyBounce, false);
        gc.setFill(Color.web("#5D4037"));
        gc.setFont(Font.font("Monospaced", 16));
        gc.fillText("   Boy", boyX, charY + 120 + boyBounce);

        // Instructions
        gc.setFill(Color.web("#5D4037", 0.7));
        gc.setFont(Font.font("Monospaced", 14));
        String instr = "← → để chọn  |  ENTER để xác nhận";
        double instrWidth = instr.length() * 8.5;
        gc.fillText(instr, (WINDOW_WIDTH - instrWidth) / 2, 500);
    }

    /**
     * Render the character card background.
     */
    private void drawCharacterCard(GraphicsContext gc, double x, double y, boolean selected, double bounceTimer) {
        double bounce = selected ? Math.sin(bounceTimer * 4) * 5 : 0;
        double cardY = y + bounce;
        
        // Shadow
        gc.setFill(Color.web("#000000", 0.1));
        gc.fillRoundRect(x + 5, cardY + 5, 120, 180, 20, 20);
        
        // Card Background
        gc.setFill(Color.web("#FFFFFF", 0.9));
        gc.fillRoundRect(x, cardY, 120, 180, 20, 20);
        
        // Selection highlight
        if (selected) {
            gc.setStroke(Color.web("#FFB6C1"));
            gc.setLineWidth(4);
            gc.strokeRoundRect(x, cardY, 120, 180, 20, 20);
            
            // Arrow indicator
            gc.setFill(Color.web("#FFB6C1"));
            gc.setFont(Font.font("Monospaced", 24));
            gc.fillText("▼", x + 50, cardY - 15);
        } else {
            gc.setStroke(Color.web("#E0E0E0"));
            gc.setLineWidth(2);
            gc.strokeRoundRect(x, cardY, 120, 180, 20, 20);
        }
    }

    /**
     * Vẽ preview nhân vật pixel art trên màn hình chọn.
     */
    private void drawCharacterPreview(GraphicsContext gc, double x, double y, boolean isGirl) {
        int scale = 4; // Scale up for preview
        int s = scale;

        if (isGirl) {
            // Hair (pink-brown)
            gc.setFill(Color.web("#8B4513"));
            gc.fillRect(x + 2*s, y, 8*s, 2*s);
            gc.fillRect(x + 1*s, y + 2*s, 10*s, 2*s);
            gc.fillRect(x + 1*s, y + 4*s, 2*s, 4*s);
            gc.fillRect(x + 9*s, y + 4*s, 2*s, 4*s);

            // Face
            gc.setFill(Color.web("#FFDAB9"));
            gc.fillRect(x + 3*s, y + 4*s, 6*s, 5*s);

            // Eyes
            gc.setFill(Color.web("#2c1810"));
            gc.fillRect(x + 4*s, y + 5*s, 1*s, 2*s);
            gc.fillRect(x + 7*s, y + 5*s, 1*s, 2*s);

            // Blush
            gc.setFill(Color.web("#FFB6C1", 0.6));
            gc.fillRect(x + 3*s, y + 7*s, 1*s, 1*s);
            gc.fillRect(x + 8*s, y + 7*s, 1*s, 1*s);

            // Smile
            gc.setFill(Color.web("#e07060"));
            gc.fillRect(x + 5*s, y + 7*s, 2*s, 1*s);

            // Ribbon
            gc.setFill(Color.web("#FF69B4"));
            gc.fillRect(x + 8*s, y + 1*s, 3*s, 2*s);

            // Dress
            gc.setFill(Color.web("#FF69B4"));
            gc.fillRect(x + 3*s, y + 9*s, 6*s, 2*s);
            gc.fillRect(x + 2*s, y + 11*s, 8*s, 3*s);

            // Legs
            gc.setFill(Color.web("#FFDAB9"));
            gc.fillRect(x + 3*s, y + 14*s, 2*s, 2*s);
            gc.fillRect(x + 7*s, y + 14*s, 2*s, 2*s);

            // Shoes
            gc.setFill(Color.web("#FF1493"));
            gc.fillRect(x + 3*s, y + 16*s, 2*s, 1*s);
            gc.fillRect(x + 7*s, y + 16*s, 2*s, 1*s);
        } else {
            // Hair (dark)
            gc.setFill(Color.web("#2c3e50"));
            gc.fillRect(x + 2*s, y, 8*s, 2*s);
            gc.fillRect(x + 1*s, y + 2*s, 10*s, 2*s);

            // Face
            gc.setFill(Color.web("#FFDAB9"));
            gc.fillRect(x + 3*s, y + 4*s, 6*s, 5*s);

            // Eyes
            gc.setFill(Color.web("#2c1810"));
            gc.fillRect(x + 4*s, y + 5*s, 1*s, 2*s);
            gc.fillRect(x + 7*s, y + 5*s, 1*s, 2*s);

            // Smile
            gc.setFill(Color.web("#e07060"));
            gc.fillRect(x + 5*s, y + 7*s, 2*s, 1*s);

            // T-shirt
            gc.setFill(Color.web("#3498db"));
            gc.fillRect(x + 3*s, y + 9*s, 6*s, 2*s);
            gc.fillRect(x + 2*s, y + 9*s, 1*s, 3*s);
            gc.fillRect(x + 9*s, y + 9*s, 1*s, 3*s);
            gc.fillRect(x + 3*s, y + 11*s, 6*s, 2*s);

            // Pants
            gc.setFill(Color.web("#2c3e50"));
            gc.fillRect(x + 3*s, y + 13*s, 6*s, 2*s);
            gc.fillRect(x + 3*s, y + 14*s, 2*s, 2*s);
            gc.fillRect(x + 7*s, y + 14*s, 2*s, 2*s);

            // Shoes
            gc.setFill(Color.web("#e74c3c"));
            gc.fillRect(x + 3*s, y + 16*s, 2*s, 1*s);
            gc.fillRect(x + 7*s, y + 16*s, 2*s, 1*s);
        }
    }

    /**
     * Khởi tạo game world và bắt đầu game loop.
     */
    private void startGame(boolean isGirl) {
        characterSelected = true;
        gameWorld = new GameWorld(isGirl);
        gameLoop = new GameLoop(gc, gameWorld, inputHandler);
        gameLoop.start();
    }
}
