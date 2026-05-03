package com.game.world;

import com.game.core.InputHandler;
import com.game.dialog.DialogSystem;
import com.game.entity.CatFollower;
import com.game.entity.NPC;
import com.game.entity.Player;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PlayerInteractionTest — Kiểm tra các thao tác của player
 */
public class PlayerInteractionTest {

    private GameWorld gameWorld;
    private Player player;
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
        input = InputHandler.getInstance();
        resetInputState();
    }

    @Test
    public void testPlayerMovementWASD() throws Exception {
        // Đặt player ở khu vực an toàn (đường đi giữa map)
        player.setX(20 * 32); // 640
        player.setY(15 * 32); // 480

        // 1. Di chuyển xuống (S)
        double initialY = player.getY();
        simulateKeyDown(KeyCode.S, 0.1);
        assertTrue(player.getY() > initialY, "Player phải di chuyển xuống khi nhấn S");

        // 2. Di chuyển lên (W)
        double yAfterS = player.getY();
        simulateKeyDown(KeyCode.W, 0.1);
        assertTrue(player.getY() < yAfterS, "Player phải di chuyển lên khi nhấn W");

        // 3. Di chuyển trái (A)
        double initialX = player.getX();
        simulateKeyDown(KeyCode.A, 0.1);
        assertTrue(player.getX() < initialX, "Player phải di chuyển sang trái khi nhấn A");

        // 4. Di chuyển phải (D)
        double xAfterA = player.getX();
        simulateKeyDown(KeyCode.D, 0.1);
        assertTrue(player.getX() > xAfterA, "Player phải di chuyển sang phải khi nhấn D");
    }

    @Test
    public void testPlayerCollision() throws Exception {
        // Đặt player ở sát viền map (viền ngoài là nước, solid tile)
        // Viền nước ở row 1, col 19 là nước không? Border nước ở col 0, 1.
        // Cố tình đẩy player ra ngoài map (âm)
        player.setX(-10);
        player.setY(-10);
        
        double obstacleX = player.getX();
        double obstacleY = player.getY();
        
        simulateKeyDown(KeyCode.A, 0.1);
        assertEquals(obstacleX, player.getX(), "Player không thể di chuyển xuyên qua vật cản");
        
        simulateKeyDown(KeyCode.W, 0.1);
        assertEquals(obstacleY, player.getY(), "Player không thể di chuyển xuyên qua vật cản");
    }

    @Test
    public void testInteractWithNPC() throws Exception {
        @SuppressWarnings("unchecked")
        List<NPC> npcs = (List<NPC>) getPrivateField(gameWorld, "npcs");
        NPC firstNpc = npcs.get(0);

        player.setX(firstNpc.getX());
        player.setY(firstNpc.getY());

        simulateKeyPress(KeyCode.ENTER, 0.016);
        
        DialogSystem dialogSystem = (DialogSystem) getPrivateField(gameWorld, "dialogSystem");
        assertTrue(dialogSystem.isActive(), "DialogSystem phải mở khi nhấn Enter gần NPC");
    }

    @Test
    public void testInventoryToggle() throws Exception {
        boolean inventoryOpen = (boolean) getPrivateField(gameWorld, "inventoryOpen");
        assertFalse(inventoryOpen, "Kho đồ phải đóng mặc định");

        // Mở bằng I
        simulateKeyPress(KeyCode.I, 0.016);
        inventoryOpen = (boolean) getPrivateField(gameWorld, "inventoryOpen");
        assertTrue(inventoryOpen, "Kho đồ phải mở sau khi nhấn I");

        // Đóng bằng Esc
        simulateKeyPress(KeyCode.ESCAPE, 0.016);
        inventoryOpen = (boolean) getPrivateField(gameWorld, "inventoryOpen");
        assertFalse(inventoryOpen, "Kho đồ phải đóng sau khi nhấn Esc");
    }

    @Test
    public void testCatCareMenuToggle() throws Exception {
        CatFollower cat = (CatFollower) getPrivateField(gameWorld, "cat");
        
        // Mở khoá chăm sóc mèo
        cat.unlockCare(player);
        
        // Di chuyển player tới mèo
        player.setX(cat.getX());
        player.setY(cat.getY());

        boolean catCareOpen = (boolean) getPrivateField(gameWorld, "catCareOpen");
        assertFalse(catCareOpen, "Cat care menu phải đóng mặc định");

        // Mở bằng C
        simulateKeyPress(KeyCode.C, 0.016);
        catCareOpen = (boolean) getPrivateField(gameWorld, "catCareOpen");
        assertTrue(catCareOpen, "Cat care menu phải mở sau khi nhấn C gần mèo");

        // Đóng bằng Esc
        simulateKeyPress(KeyCode.ESCAPE, 0.016);
        catCareOpen = (boolean) getPrivateField(gameWorld, "catCareOpen");
        assertFalse(catCareOpen, "Cat care menu phải đóng sau khi nhấn Esc");
    }

    // --- Helpers ---
    private void simulateKeyPress(KeyCode code, double dt) {
        input.keyPressed(code);
        input.update();
        gameWorld.update(dt, input);
        input.keyReleased(code);
        input.update();
    }
    
    private void simulateKeyDown(KeyCode code, double dt) {
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
