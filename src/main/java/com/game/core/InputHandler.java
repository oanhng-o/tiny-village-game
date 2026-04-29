package com.game.core;

import javafx.scene.input.KeyCode;
import java.util.HashSet;
import java.util.Set;

/**
 * InputHandler — Singleton quản lý trạng thái bàn phím.
 * Track các phím đang giữ và phím vừa nhấn (just pressed).
 */
public class InputHandler {

    private static InputHandler instance;

    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private final Set<KeyCode> justPressedKeys = new HashSet<>();
    private final Set<KeyCode> justPressedBuffer = new HashSet<>();

    private InputHandler() {}

    public static InputHandler getInstance() {
        if (instance == null) {
            instance = new InputHandler();
        }
        return instance;
    }

    /**
     * Gọi khi phím được nhấn xuống.
     */
    public void keyPressed(KeyCode code) {
        if (!pressedKeys.contains(code)) {
            justPressedBuffer.add(code);
        }
        pressedKeys.add(code);
    }

    /**
     * Gọi khi phím được thả ra.
     */
    public void keyReleased(KeyCode code) {
        pressedKeys.remove(code);
    }

    /**
     * Kiểm tra phím có đang được giữ không.
     */
    public boolean isKeyDown(KeyCode code) {
        return pressedKeys.contains(code);
    }

    /**
     * Kiểm tra phím vừa được nhấn trong frame này (chỉ true 1 frame).
     */
    public boolean isKeyJustPressed(KeyCode code) {
        return justPressedKeys.contains(code);
    }

    /**
     * Gọi cuối mỗi frame để clear justPressed.
     */
    public void update() {
        justPressedKeys.clear();
        justPressedKeys.addAll(justPressedBuffer);
        justPressedBuffer.clear();
    }
}
