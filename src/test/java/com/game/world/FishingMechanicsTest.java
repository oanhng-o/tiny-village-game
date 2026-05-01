package com.game.world;

import com.game.core.InputHandler;
import com.game.dialog.QuestSystem;
import com.game.entity.Player;
import com.game.inventory.InventorySystem;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FishingMechanicsTest — Kiểm tra diễn biến và logic bên trong mini-game câu
 * cá.
 */
public class FishingMechanicsTest {

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
    public void testFishingWinSequence() throws Exception {
        // Khởi tạo mini-game (giả định đã vượt qua bước kích hoạt)
        player.setX(13 * TileMap.TILE_SIZE);
        player.setY(14 * TileMap.TILE_SIZE);
        player.setHasFishingRod(true);
        questSystem.completeQuest(QuestSystem.FISHING_ROD_QUEST_ID);
        simulateKeyPress(KeyCode.F, 0.016);

        // 1. Kiểm tra giai đoạn chờ (WAITING)
        Object phase = getPrivateField(fishingMiniGame, "phase");
        assertEquals("WAITING", phase.toString());

        // 2. Chuyển sang giai đoạn gồng lực (POWER_BAR)
        setPrivateField(fishingMiniGame, "waitTimer", 6.0);
        gameWorld.update(0.016, input);

        phase = getPrivateField(fishingMiniGame, "phase");
        assertEquals("POWER_BAR", phase.toString());

        // 3. Nhấn SPACE để thắng
        for (int i = 0; i < 15; i++) {
            simulateKeyPress(KeyCode.SPACE, 0.016);
        }

        // 4. Kiểm tra kết quả
        assertFalse(fishingMiniGame.isActive(), "Mini-game phải kết thúc sau khi thắng.");
        assertEquals(Player.PlayerState.NORMAL, player.getState());

        InventorySystem inventorySystem = (InventorySystem) getPrivateField(gameWorld, "inventorySystem");
        assertFalse(inventorySystem.isEmpty(), "Kho đồ phải có cá sau khi thắng.");
    }

    @Test
    public void testFishingLoseSequence() throws Exception {
        // Khởi tạo và vào giai đoạn gồng lực
        player.setX(13 * TileMap.TILE_SIZE);
        player.setY(14 * TileMap.TILE_SIZE);
        player.setHasFishingRod(true);
        questSystem.completeQuest(QuestSystem.FISHING_ROD_QUEST_ID);
        simulateKeyPress(KeyCode.F, 0.016);

        setPrivateField(fishingMiniGame, "waitTimer", 6.0);
        gameWorld.update(0.016, input);

        // 2. Để thời gian trôi qua quá 7 giây (Thua cuộc)
        setPrivateField(fishingMiniGame, "powerTimer", 8.0);
        gameWorld.update(0.016, input);

        // 3. Kiểm tra kết quả
        assertFalse(fishingMiniGame.isActive(), "Mini-game phải kết thúc khi thua.");
        assertEquals(Player.PlayerState.NORMAL, player.getState());

        InventorySystem inventorySystem = (InventorySystem) getPrivateField(gameWorld, "inventorySystem");
        assertTrue(inventorySystem.isEmpty(), "Kho đồ phải trống nếu thua.");
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

    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @SuppressWarnings("unchecked")
    private java.util.Set<KeyCode> getPrivateSet(Object obj, String fieldName) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (java.util.Set<KeyCode>) field.get(obj);
    }
}
