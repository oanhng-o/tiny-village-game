package com.game.core;

import com.game.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CameraTest - Kiểm tra hệ thống camera theo dõi người chơi
 */
public class CameraTest {

    private Camera camera;
    private final double mapWidth = Constants.MAP_COLS * Constants.TILE_SIZE; // 1280
    private final double mapHeight = Constants.MAP_ROWS * Constants.TILE_SIZE; // 960

    @BeforeEach
    public void setup() {
        camera = new Camera(mapWidth, mapHeight);
    }

    @Test
    public void testSnapToCenter() {
        // Giả sử player ở giữa bản đồ (x=640, y=480)
        camera.snapTo(640, 480);

        // Cửa sổ 800x600 -> offset X phải là 640 - 400 = 240
        // offset Y phải là 480 - 300 = 180
        assertEquals(240.0, camera.getOffsetX(), 0.01, "Camera X phải ở vị trí trung tâm");
        assertEquals(180.0, camera.getOffsetY(), 0.01, "Camera Y phải ở vị trí trung tâm");
    }

    @Test
    public void testSnapToBoundsTopLeft() {
        // Đặt player ở góc trên bên trái sát viền (x=0, y=0)
        camera.snapTo(0, 0);

        // Offset không được âm
        assertEquals(0.0, camera.getOffsetX(), 0.01, "Camera X không được vượt qua viền trái");
        assertEquals(0.0, camera.getOffsetY(), 0.01, "Camera Y không được vượt qua viền trên");
    }

    @Test
    public void testSnapToBoundsBottomRight() {
        // Đặt player ở góc dưới bên phải
        camera.snapTo(mapWidth, mapHeight);

        // Offset lớn nhất là mapWidth - windowWidth (1280 - 800 = 480)
        // Offset Y lớn nhất là mapHeight - windowHeight (960 - 600 = 360)
        assertEquals(mapWidth - Constants.WINDOW_WIDTH, camera.getOffsetX(), 0.01,
                "Camera X không được vượt qua viền phải");
        assertEquals(mapHeight - Constants.WINDOW_HEIGHT, camera.getOffsetY(), 0.01,
                "Camera Y không được vượt qua viền dưới");
    }

    @Test
    public void testSmoothLerpUpdate() {
        // Đặt camera ở (0, 0) trước
        camera.snapTo(Constants.WINDOW_WIDTH / 2.0, Constants.WINDOW_HEIGHT / 2.0); // Offset sẽ là (0,0)

        assertEquals(0.0, camera.getOffsetX(), 0.01);

        // Di chuyển player sang (x=600, y=500)
        // Target offset sẽ là (200, 200)
        double dt = 0.1;
        camera.update(600, 500, dt);

        // Sau 1 update, x += (200 - 0) * 5.0 * 0.1 = 100
        // y += (200 - 0) * 5.0 * 0.1 = 100
        assertEquals(100.0, camera.getOffsetX(), 0.01, "Camera X phải lerp tới target");
        assertEquals(100.0, camera.getOffsetY(), 0.01, "Camera Y phải lerp tới target");
    }
}
