package com.game;

import javafx.application.Application;

/**
 * Entry point cho game "Tiny village".
 * Tách riêng khỏi Application class để tránh lỗi JavaFX module.
 */
public class Main {
    public static void main(String[] args) {
        Application.launch(GameApplication.class, args);
    }
}
