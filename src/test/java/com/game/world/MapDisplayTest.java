package com.game.world;

import com.game.core.InputHandler;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MapDisplayTest — Kiểm tra logic đóng/mở bản đồ.
 */
public class MapDisplayTest {

    private GameWorld gameWorld;
    private InputHandler inputHandler;
    private MapOverlay mapOverlay;

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
        inputHandler = InputHandler.getInstance();
        inputHandler.keyReleased(KeyCode.M);

        gameWorld = new GameWorld(true);

        // Sử dụng Reflection để lấy mapOverlay
        java.lang.reflect.Field field = GameWorld.class.getDeclaredField("mapOverlay");
        field.setAccessible(true);
        mapOverlay = (MapOverlay) field.get(gameWorld);
    }

    @Test
    public void testPressMWhenInitiallyHidden() {
        // GIVEN: Ban đầu bản đồ ẩn
        mapOverlay.setVisible(false);
        assertFalse(mapOverlay.isVisible(), "Bản đồ phải ẩn lúc đầu.");

        // WHEN: Nhấn phím M 1 lần
        inputHandler.keyPressed(KeyCode.M);
        inputHandler.update();
        gameWorld.update(0.016, inputHandler);
        inputHandler.keyReleased(KeyCode.M);

        // THEN: Bản đồ hiện ra
        assertTrue(mapOverlay.isVisible(), "Bản đồ phải hiện ra khi nhấn M (nếu đang ẩn).");
    }

    @Test
    public void testPressMWhenInitiallyShown() {
        // GIVEN: Ban đầu bản đồ hiện (giả lập)
        mapOverlay.setVisible(true);
        assertTrue(mapOverlay.isVisible(), "Bản đồ phải hiện trước khi test.");

        // WHEN: Nhấn phím M 1 lần
        inputHandler.keyPressed(KeyCode.M);
        inputHandler.update();
        gameWorld.update(0.016, inputHandler);
        inputHandler.keyReleased(KeyCode.M);

        // THEN: Bản đồ biến mất
        assertFalse(mapOverlay.isVisible(), "Bản đồ phải biến mất khi nhấn M (nếu đang hiện).");
    }

    @Test
    public void testDoublePressM() {
        // GIVEN: Ban đầu bản đồ ẩn
        mapOverlay.setVisible(false);

        // WHEN: Nhấn M lần 1
        inputHandler.keyPressed(KeyCode.M);
        inputHandler.update();
        gameWorld.update(0.016, inputHandler);
        inputHandler.keyReleased(KeyCode.M);
        assertTrue(mapOverlay.isVisible(), "Lần nhấn 1 phải làm bản đồ hiện.");

        // WHEN: Nhấn M lần 2
        inputHandler.keyPressed(KeyCode.M);
        inputHandler.update();
        gameWorld.update(0.016, inputHandler);
        inputHandler.keyReleased(KeyCode.M);

        // THEN: Bản đồ quay về trạng thái ẩn
        assertFalse(mapOverlay.isVisible(), "Nhấn M 2 lần phải làm bản đồ ẩn đi.");
    }
}
