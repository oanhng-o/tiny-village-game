module com.game {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;

    opens com.game to javafx.graphics;
    exports com.game;
    exports com.game.core;
    exports com.game.entity;
    exports com.game.world;
    exports com.game.dialog;
    exports com.game.util;
}
