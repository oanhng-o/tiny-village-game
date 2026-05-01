package com.game.world;

import com.game.core.InputHandler;
import com.game.dialog.QuestSystem;
import com.game.entity.Player;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FishingTriggerTest — Kiểm tra điều kiện kích hoạt hành động câu cá (Nhấn phím F).
 */
public class FishingTriggerTest {

    private GameWorld gameWorld;
    private Player player;
    private FishingMiniGame fishingMiniGame;
    private QuestSystem questSystem;
    private InputHandler input;

    @BeforeAll
    public static void setupJavaFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            latch.countDown();
        }
        latch.await();
    }

    @BeforeEach
    public void init() throws Exception {
        gameWorld = new GameWorld(true);
        player = (Player) getPrivateField(gameWorld, "player");
        fishingMiniGame = (FishingMiniGame) getPrivateField(gameWorld, "fishingMiniGame");
        questSystem = (QuestSystem) getPrivateField(gameWorld, "questSystem");
        input = InputHandler.getInstance();
        resetInputState();
    }

    @Test
    public void testStartFishingFailWithoutRod() throws Exception {
        // Thiết lập: Đứng gần nước nhưng chưa có cần câu
        player.setX(13 * TileMap.TILE_SIZE);
        player.setY(14 * TileMap.TILE_SIZE);
        player.setHasFishingRod(false);

        simulateKeyPress(KeyCode.F, 0.016);

        assertFalse(fishingMiniGame.isActive(), "Mini-game không được bắt đầu nếu thiếu cần câu.");
    }

    @Test
    public void testStartFishingFailNotNearWater() throws Exception {
        // Thiết lập: Có cần câu nhưng đứng ở giữa bãi cỏ (xa nước)
        player.setX(5 * TileMap.TILE_SIZE);
        player.setY(5 * TileMap.TILE_SIZE);
        player.setHasFishingRod(true);
        questSystem.completeQuest(QuestSystem.FISHING_ROD_QUEST_ID);

        simulateKeyPress(KeyCode.F, 0.016);

        assertFalse(fishingMiniGame.isActive(), "Mini-game không được bắt đầu nếu không ở gần nước.");
    }

    @Test
    public void testStartFishingSuccess() throws Exception {
        // Thiết lập: Đủ điều kiện (gần nước + có cần câu)
        player.setX(13 * TileMap.TILE_SIZE);
        player.setY(14 * TileMap.TILE_SIZE);
        player.setHasFishingRod(true);
        questSystem.completeQuest(QuestSystem.FISHING_ROD_QUEST_ID);

        simulateKeyPress(KeyCode.F, 0.016);

        assertTrue(fishingMiniGame.isActive(), "Mini-game phải bắt đầu khi đủ điều kiện.");
        assertEquals(Player.PlayerState.FISHING, player.getState());
    }

    private void simulateKeyPress(KeyCode code, double dt) {
        input.keyPressed(code);
        input.update();
        gameWorld.update(dt, input);
        input.keyReleased(code);
        input.update();
    }

    private void resetInputState() throws Exception {
        getPrivateSet(input, "pressedKeys").clear();
        getPrivateSet(input, "justPressedKeys").clear();
        getPrivateSet(input, "justPressedBuffer").clear();
    }

    private Object getPrivateField(Object obj, String fieldName) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    @SuppressWarnings("unchecked")
    private java.util.Set<KeyCode> getPrivateSet(Object obj, String fieldName) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (java.util.Set<KeyCode>) field.get(obj);
    }
}
