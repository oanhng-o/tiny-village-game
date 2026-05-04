package com.game;

import com.game.audio.AudioManager;
import com.game.audio.AudioSettings;
import com.game.audio.AudioSettingsStore;
import com.game.core.GameLoop;
import com.game.core.InputHandler;
import com.game.save.SaveData;
import com.game.save.SaveSystem;
import com.game.ui.AudioSettingsOverlay;
import com.game.util.AssetManager;
import com.game.util.Constants;
import com.game.world.GameWorld;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * GameApplication — Khởi tạo JavaFX Stage, Scene, Canvas.
 * Hiển thị character selection trước, rồi mới tới màn hình Continue/New Game.
 */
public class GameApplication extends Application {

    private enum FrontScreenState {
        TITLE,
        CHARACTER_SELECT
    }

    private enum TitleOption {
        CONTINUE,
        NEW_GAME
    }

    private Canvas canvas;
    private GraphicsContext gc;
    private GameLoop gameLoop;
    private GameWorld gameWorld;
    private InputHandler inputHandler;
    private final SaveSystem saveSystem = new SaveSystem();
    private final AudioSettingsStore audioSettingsStore = new AudioSettingsStore();
    private final AudioSettingsOverlay audioSettingsOverlay = new AudioSettingsOverlay();
    private final AudioManager audioManager = AudioManager.getInstance();

    private FrontScreenState frontScreenState = FrontScreenState.CHARACTER_SELECT;
    private TitleOption titleSelection = TitleOption.NEW_GAME;
    private int selectedOption = 0; // 0 = girl, 1 = boy
    private String titleStatusMessage = null;
    private String characterSelectStatusMessage = null;
    private AudioSettings audioSettings;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tiny village");
        primaryStage.setResizable(false);

        canvas = new Canvas(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        inputHandler = InputHandler.getInstance();
        scene.setOnKeyPressed(e -> inputHandler.keyPressed(e.getCode()));
        scene.setOnKeyReleased(e -> inputHandler.keyReleased(e.getCode()));

        primaryStage.setScene(scene);
        primaryStage.show();

        audioSettings = audioSettingsStore.load();
        audioManager.initialize(audioSettings);
        audioManager.playMusicEvent(Constants.AUDIO_EVENT_MENU_MUSIC);

        titleSelection = saveSystem.hasSave(true) ? TitleOption.CONTINUE : TitleOption.NEW_GAME;
        startFrontScreenLoop();
    }

    @Override
    public void stop() {
        autoSaveCurrentProgress();
        audioSettingsStore.save(audioSettings);
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (inputHandler != null) {
            inputHandler.reset();
        }
        audioManager.shutdown();
    }

    private void autoSaveCurrentProgress() {
        if (gameWorld == null) {
            return;
        }

        try {
            saveSystem.save(gameWorld.captureSaveData());
        } catch (Exception exception) {
            System.err.println("Could not auto-save Tiny Village progress: " + exception.getMessage());
        }
    }

    private void startFrontScreenLoop() {
        javafx.animation.AnimationTimer frontTimer = new javafx.animation.AnimationTimer() {
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

                Runnable launchAction = null;
                if (inputHandler.isKeyJustPressed(KeyCode.P)) {
                    toggleAudioSettingsOverlay();
                }

                if (frontScreenState == FrontScreenState.TITLE) {
                    if (!audioSettingsOverlay.isOpen()) {
                        launchAction = handleTitleScreenInput();
                    }
                    renderTitleScreen(bounceTimer);
                } else {
                    if (!audioSettingsOverlay.isOpen()) {
                        handleCharacterSelectInput();
                    }
                    renderCharacterSelect(bounceTimer);
                }

                if (audioSettingsOverlay.isOpen()) {
                    boolean wasOpen = true;
                    audioSettingsOverlay.handleInput(inputHandler, audioSettings, GameApplication.this::applyAudioSettings);
                    if (wasOpen && !audioSettingsOverlay.isOpen()) {
                        persistAudioSettings();
                    }
                    if (audioSettingsOverlay.isOpen()) {
                        audioSettingsOverlay.render(gc);
                        audioSettingsOverlay.renderRows(gc, audioSettings);
                    }
                    inputHandler.update();
                    return;
                }

                inputHandler.update();

                if (launchAction != null) {
                    this.stop();
                    launchAction.run();
                }
            }
        };
        frontTimer.start();
    }

    private Runnable handleTitleScreenInput() {
        boolean isGirl = selectedOption == 0;

        if (inputHandler.isKeyJustPressed(KeyCode.ESCAPE)) {
            frontScreenState = FrontScreenState.CHARACTER_SELECT;
            titleStatusMessage = null;
            audioManager.playSfxEvent(Constants.AUDIO_EVENT_BACK);
            return null;
        }

        TitleOption previousSelection = titleSelection;
        if (inputHandler.isKeyJustPressed(KeyCode.UP) || inputHandler.isKeyJustPressed(KeyCode.W)) {
            titleSelection = TitleOption.CONTINUE;
        }
        if (inputHandler.isKeyJustPressed(KeyCode.DOWN) || inputHandler.isKeyJustPressed(KeyCode.S)) {
            titleSelection = TitleOption.NEW_GAME;
        }
        if (titleSelection != previousSelection) {
            audioManager.playSfxEvent(Constants.AUDIO_EVENT_DIALOG_ADVANCE);
        }

        if (!inputHandler.isKeyJustPressed(KeyCode.ENTER) && !inputHandler.isKeyJustPressed(KeyCode.SPACE)) {
            return null;
        }

        if (titleSelection == TitleOption.CONTINUE) {
            SaveData saveData = saveSystem.load(isGirl).orElse(null);
            if (saveData == null) {
                titleStatusMessage = isGirl
                        ? "Girl chưa có save để tiếp tục."
                        : "Boy chưa có save để tiếp tục.";
                return null;
            }
            titleStatusMessage = null;
            characterSelectStatusMessage = null;
            audioManager.playSfxEvent(Constants.AUDIO_EVENT_CONFIRM);
            return () -> loadSavedGame(saveData);
        }

        titleStatusMessage = null;
        characterSelectStatusMessage = null;
        audioManager.playSfxEvent(Constants.AUDIO_EVENT_CONFIRM);
        return () -> startGame(isGirl);
    }

    private void handleCharacterSelectInput() {
        int previousOption = selectedOption;
        if (inputHandler.isKeyJustPressed(KeyCode.LEFT) || inputHandler.isKeyJustPressed(KeyCode.A)) {
            selectedOption = 0;
        }
        if (inputHandler.isKeyJustPressed(KeyCode.RIGHT) || inputHandler.isKeyJustPressed(KeyCode.D)) {
            selectedOption = 1;
        }
        if (selectedOption != previousOption) {
            audioManager.playSfxEvent(Constants.AUDIO_EVENT_DIALOG_ADVANCE);
        }
        if (!inputHandler.isKeyJustPressed(KeyCode.ENTER) && !inputHandler.isKeyJustPressed(KeyCode.SPACE)) {
            return;
        }

        titleSelection = saveSystem.hasSave(selectedOption == 0) ? TitleOption.CONTINUE : TitleOption.NEW_GAME;
        titleStatusMessage = null;
        characterSelectStatusMessage = null;
        frontScreenState = FrontScreenState.TITLE;
        audioManager.playSfxEvent(Constants.AUDIO_EVENT_CONFIRM);
    }

    private void renderTitleScreen(double bounceTimer) {
        boolean isGirl = selectedOption == 0;
        String selectedLabel = isGirl ? "Girl" : "Boy";
        boolean hasSaveForSelectedCharacter = saveSystem.hasSave(isGirl);

        renderFrontBackground(bounceTimer);
        renderFrontTitle("Tiny village", "Chọn hành động cho " + selectedLabel);

        double panelW = 360;
        double panelH = 210;
        double panelX = (Constants.WINDOW_WIDTH - panelW) / 2.0;
        double panelY = 230;

        gc.setFill(Color.web("#FFFFFF", 0.92));
        gc.fillRoundRect(panelX, panelY, panelW, panelH, 22, 22);
        gc.setStroke(Color.web("#A0C4FF"));
        gc.setLineWidth(4);
        gc.strokeRoundRect(panelX, panelY, panelW, panelH, 22, 22);

        renderTitleOption(panelX + 26, panelY + 34, 308, 48, TitleOption.CONTINUE,
            "Continue", "Tiếp tục save của " + selectedLabel, hasSaveForSelectedCharacter);
        renderTitleOption(panelX + 26, panelY + 96, 308, 48, TitleOption.NEW_GAME,
            "New Game", "Bắt đầu phiên mới với " + selectedLabel, true);

        gc.setFill(Color.web("#6D4C41"));
        gc.setFont(Font.font("Monospaced", 12));

        // String controls = "↑ ↓ để chọn  |  Enter để xác nhận  |  Esc để quay lại";
        String controls = "↑ ↓ để chọn  |  Enter để xác nhận";
        double controlsWidth = controls.length() * 7.5;
        gc.fillText(controls, (Constants.WINDOW_WIDTH - controlsWidth) / 2, panelY + 172);

        // String saveSummary = selectedLabel + ": " + (hasSaveForSelectedCharacter ? "co save" : "chua co save")
        //     + "   |   Esc de doi nhan vat";
        String saveSummary = "Esc để quay lại";
        double summaryWidth = saveSummary.length() * 7.2;
        gc.fillText(saveSummary, (Constants.WINDOW_WIDTH - summaryWidth) / 2, panelY + 194);

        if (titleStatusMessage != null) {
            gc.setFill(Color.web("#C62828", 0.92));
            gc.setFont(Font.font("Monospaced", 13));
            double statusWidth = titleStatusMessage.length() * 7.2;
            gc.fillText(titleStatusMessage, (Constants.WINDOW_WIDTH - statusWidth) / 2, 500);
        }
    }

    private void renderTitleOption(double x, double y, double w, double h, TitleOption option,
                                   String label, String description, boolean enabled) {
        boolean selected = titleSelection == option;

        gc.setFill(selected ? Color.web(enabled ? "#DCEEFF" : "#E8E8E8") : Color.web("#FFFFFF", 0.78));
        gc.fillRoundRect(x, y, w, h, 16, 16);
        gc.setStroke(selected ? Color.web(enabled ? "#4F8EDC" : "#B0B0B0") : Color.web("#D6C49C", 0.9));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, w, h, 16, 16);

        gc.setFill(enabled ? Color.web("#4A4A4A") : Color.web("#9E9E9E"));
        gc.setFont(Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 16));
        gc.fillText((selected ? "▶ " : "  ") + label, x + 18, y + 21);

        gc.setFont(Font.font("Monospaced", 11));
        gc.fillText(description, x + 20, y + 38);
    }

    private void renderCharacterSelect(double bounceTimer) {
        renderFrontBackground(bounceTimer);
        renderFrontTitle("Tiny village", "Chọn nhân vật trước khi tiếp tục");

        double girlX = Constants.WINDOW_WIDTH / 2 - 130;
        double boyX = Constants.WINDOW_WIDTH / 2 + 50;
        double charY = 250;

        drawCharacterCard(gc, girlX - 20, charY - 30, selectedOption == 0, bounceTimer);
        drawCharacterCard(gc, boyX - 20, charY - 30, selectedOption == 1, bounceTimer);

        double girlBounce = selectedOption == 0 ? Math.sin(bounceTimer * 4) * 5 : 0;
        drawCharacterPreview(gc, girlX, charY + girlBounce, true);
        gc.setFill(Color.web("#5D4037"));
        gc.setFont(Font.font("Monospaced", 16));
        gc.fillText("  Girl", girlX, charY + 120 + girlBounce);
        // renderCharacterSlotStatus(girlX - 4, charY + 144 + girlBounce, true);

        double boyBounce = selectedOption == 1 ? Math.sin(bounceTimer * 4) * 5 : 0;
        drawCharacterPreview(gc, boyX, charY + boyBounce, false);
        gc.setFill(Color.web("#5D4037"));
        gc.setFont(Font.font("Monospaced", 16));
        gc.fillText("   Boy", boyX, charY + 120 + boyBounce);
        //renderCharacterSlotStatus(boyX - 2, charY + 144 + boyBounce, false);

        gc.setFill(Color.web("#5D4037", 0.75));
        gc.setFont(Font.font("Monospaced", 14));
        String instr = "← → de chon  |  ENTER de tiep theo";
        double instrWidth = instr.length() * 8.0;
        gc.fillText(instr, (Constants.WINDOW_WIDTH - instrWidth) / 2, 500);

        if (characterSelectStatusMessage != null) {
            gc.setFill(Color.web("#C62828", 0.92));
            gc.setFont(Font.font("Monospaced", 13));
            double statusWidth = characterSelectStatusMessage.length() * 7.2;
            gc.fillText(characterSelectStatusMessage, (Constants.WINDOW_WIDTH - statusWidth) / 2, 540);
        }
    }

    private void renderCharacterSlotStatus(double x, double y, boolean isGirl) {
        boolean hasSave = saveSystem.hasSave(isGirl);
        String statusText = hasSave ? "Continue co san" : "Chi co New Game";

        gc.setFill(hasSave ? Color.web("#2E7D32") : Color.web("#8D6E63"));
        gc.setFont(Font.font("Monospaced", 11));
        gc.fillText(statusText, x, y);
    }

    private void renderFrontBackground(double bounceTimer) {
        javafx.scene.paint.LinearGradient bgGradient = new javafx.scene.paint.LinearGradient(
                0, 0, 0, Constants.WINDOW_HEIGHT, false, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, Color.web("#E0F7FA")),
                new javafx.scene.paint.Stop(1, Color.web("#B2EBF2"))
        );
        gc.setFill(bgGradient);
        gc.fillRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        gc.setFill(Color.web("#ffffff", 0.8));
        for (int i = 0; i < 5; i++) {
            double sx = (bounceTimer * 20 + i * 200) % (Constants.WINDOW_WIDTH + 150) - 100;
            double sy = 50 + (i * 73) % 150;
            gc.fillRoundRect(sx, sy, 80, 40, 30, 30);
            gc.fillRoundRect(sx + 20, sy - 20, 50, 50, 25, 25);
            gc.fillRoundRect(sx - 15, sy + 15, 40, 25, 20, 20);
        }
    }

    private void renderFrontTitle(String title, String subtitle) {
        gc.setFont(javafx.scene.text.Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 40));
        double titleWidth = title.length() * 24;
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(4);
        gc.strokeText(title, (Constants.WINDOW_WIDTH - titleWidth) / 2, 100);
        gc.setFill(Color.web("#5D4037"));
        gc.fillText(title, (Constants.WINDOW_WIDTH - titleWidth) / 2, 100);

        gc.setFill(Color.web("#5D4037", 0.85));
        gc.setFont(Font.font("Monospaced", 18));
        double subWidth = subtitle.length() * 10;
        gc.fillText(subtitle, (Constants.WINDOW_WIDTH - subWidth) / 2, 140);
    }

    private void drawCharacterCard(GraphicsContext gc, double x, double y, boolean selected, double bounceTimer) {
        double bounce = selected ? Math.sin(bounceTimer * 4) * 5 : 0;
        double cardY = y + bounce;

        gc.setFill(Color.web("#000000", 0.1));
        gc.fillRoundRect(x + 5, cardY + 5, 120, 180, 20, 20);

        gc.setFill(Color.web("#FFFFFF", 0.9));
        gc.fillRoundRect(x, cardY, 120, 180, 20, 20);

        if (selected) {
            gc.setStroke(Color.web("#FFB6C1"));
            gc.setLineWidth(4);
            gc.strokeRoundRect(x, cardY, 120, 180, 20, 20);

            gc.setFill(Color.web("#FFB6C1"));
            gc.setFont(Font.font("Monospaced", 24));
            gc.fillText("▼", x + 50, cardY - 15);
        } else {
            gc.setStroke(Color.web("#E0E0E0"));
            gc.setLineWidth(2);
            gc.strokeRoundRect(x, cardY, 120, 180, 20, 20);
        }
    }

    private void drawCharacterPreview(GraphicsContext gc, double x, double y, boolean isGirl) {
        Image preview = AssetManager.getInstance().getPlayerPreview(isGirl);
        gc.setImageSmoothing(false);
        gc.drawImage(preview, x - 8, y, 96, 96);
    }

    private void startGame(boolean isGirl) {
        titleStatusMessage = null;
        characterSelectStatusMessage = null;
        gameWorld = createGameWorld(isGirl);
        launchGameWorld(gameWorld);
    }

    private void loadSavedGame(SaveData saveData) {
        titleStatusMessage = null;
        characterSelectStatusMessage = null;
        GameWorld loadedWorld = createGameWorld(saveData.isGirl());
        loadedWorld.applySaveData(saveData);
        launchGameWorld(loadedWorld);
    }

    private GameWorld createGameWorld(boolean isGirl) {
        AssetManager.getInstance().reset();
        return new GameWorld(isGirl, audioSettings, audioSettingsStore);
    }

    private void launchGameWorld(GameWorld world) {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        inputHandler.reset();
        gameWorld = world;
        gameLoop = new GameLoop(gc, gameWorld, inputHandler);
        audioManager.playMusicEvent(Constants.AUDIO_EVENT_GAMEPLAY_MUSIC);
        gameLoop.start();
    }

    private void toggleAudioSettingsOverlay() {
        if (audioSettingsOverlay.isOpen()) {
            audioSettingsOverlay.close();
            persistAudioSettings();
            audioManager.playSfxEvent(Constants.AUDIO_EVENT_BACK);
            return;
        }

        audioSettingsOverlay.open();
        audioManager.playSfxEvent(Constants.AUDIO_EVENT_CONFIRM);
    }

    private void applyAudioSettings() {
        audioManager.applySettings(audioSettings);
    }

    private void persistAudioSettings() {
        applyAudioSettings();
        audioSettingsStore.save(audioSettings);
    }
}
