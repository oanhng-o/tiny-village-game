package com.game;

import com.game.core.InputHandler;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GameApplicationTest — Kiểm tra màn hình chọn nhân vật và màn hình New Game/Continue.
 */
public class GameApplicationTest {

    private GameApplication app;
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
        app = new GameApplication();
        input = InputHandler.getInstance();
        setPrivateField(app, "inputHandler", input);
        resetInputState();
    }

    @Test
    public void testCharacterSelection() throws Exception {
        // Mặc định ban đầu selectedOption = 0 (Girl)
        assertEquals(0, getPrivateIntField(app, "selectedOption"), "Mặc định phải chọn nhân vật Nữ (0)");

        // Nhấn phím Mũi tên phải (RIGHT) để chọn nhân vật Nam (1)
        simulateKeyPress(KeyCode.RIGHT);
        invokePrivateMethod(app, "handleCharacterSelectInput");
        assertEquals(1, getPrivateIntField(app, "selectedOption"), "Phải chuyển sang chọn nhân vật Nam (1) khi nhấn RIGHT");

        // Nhấn phím A để chuyển lại nhân vật Nữ (0)
        simulateKeyPress(KeyCode.A);
        invokePrivateMethod(app, "handleCharacterSelectInput");
        assertEquals(0, getPrivateIntField(app, "selectedOption"), "Phải chuyển lại nhân vật Nữ (0) khi nhấn A");
    }

    @Test
    public void testTransitionToTitleScreen() throws Exception {
        // Mặc định ở màn hình CHARACTER_SELECT
        Object state = getPrivateField(app, "frontScreenState");
        assertEquals("CHARACTER_SELECT", state.toString());

        // Nhấn ENTER để chuyển sang màn hình TITLE
        simulateKeyPress(KeyCode.ENTER);
        invokePrivateMethod(app, "handleCharacterSelectInput");

        Object newState = getPrivateField(app, "frontScreenState");
        assertEquals("TITLE", newState.toString(), "Phải chuyển sang màn hình TITLE sau khi nhấn ENTER");
    }

    @Test
    public void testTitleScreenOptions() throws Exception {
        // Đưa ứng dụng vào trạng thái TITLE
        setPrivateField(app, "frontScreenState", getEnumConstant(app, "FrontScreenState", "TITLE"));

        // Nhấn phím Mũi tên lên (UP) để chọn CONTINUE
        simulateKeyPress(KeyCode.UP);
        invokePrivateMethod(app, "handleTitleScreenInput");
        Object titleSelection = getPrivateField(app, "titleSelection");
        assertEquals("CONTINUE", titleSelection.toString(), "Phải chọn CONTINUE khi nhấn UP");

        // Nhấn phím S để chọn NEW_GAME
        simulateKeyPress(KeyCode.S);
        invokePrivateMethod(app, "handleTitleScreenInput");
        titleSelection = getPrivateField(app, "titleSelection");
        assertEquals("NEW_GAME", titleSelection.toString(), "Phải chọn NEW_GAME khi nhấn S");
    }

    @Test
    public void testBackToCharacterSelect() throws Exception {
        // Đưa ứng dụng vào trạng thái TITLE
        setPrivateField(app, "frontScreenState", getEnumConstant(app, "FrontScreenState", "TITLE"));

        // Nhấn ESCAPE để quay lại CHARACTER_SELECT
        simulateKeyPress(KeyCode.ESCAPE);
        invokePrivateMethod(app, "handleTitleScreenInput");

        Object newState = getPrivateField(app, "frontScreenState");
        assertEquals("CHARACTER_SELECT", newState.toString(), "Phải quay lại CHARACTER_SELECT khi nhấn ESCAPE");
    }

    // --- Helpers ---
    private void simulateKeyPress(KeyCode code) {
        input.keyPressed(code);
        input.update();
        // Không gọi keyReleased ngay lập tức nếu logic dùng isKeyJustPressed
        // isKeyJustPressed sẽ hoạt động khi update() được gọi 
    }

    private void resetInputState() {
        try {
            getPrivateSet(input, "pressedKeys").clear();
            getPrivateSet(input, "justPressedKeys").clear();
            getPrivateSet(input, "justPressedBuffer").clear();
        } catch (Exception ignored) {}
    }

    private Object getPrivateField(Object obj, String fieldName) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    private int getPrivateIntField(Object obj, String fieldName) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getInt(obj);
    }

    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private void invokePrivateMethod(Object obj, String methodName) throws Exception {
        java.lang.reflect.Method method = obj.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(obj);
    }

    private Object getEnumConstant(Object parentObj, String enumName, String constantName) throws Exception {
        Class<?>[] classes = parentObj.getClass().getDeclaredClasses();
        for (Class<?> clazz : classes) {
            if (clazz.getSimpleName().equals(enumName)) {
                for (Object constant : clazz.getEnumConstants()) {
                    if (constant.toString().equals(constantName)) {
                        return constant;
                    }
                }
            }
        }
        throw new RuntimeException("Enum constant not found");
    }

    @SuppressWarnings("unchecked")
    private java.util.Set<KeyCode> getPrivateSet(Object obj, String fieldName) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (java.util.Set<KeyCode>) field.get(obj);
    }
}
