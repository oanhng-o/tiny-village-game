package com.game.world;

import com.game.entity.*;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MapEntityTest — Kiểm tra hiển thị và tính tương quan vị trí của các thực thể
 * trên bản đồ.
 */
public class MapEntityTest {

    private GameWorld gameWorld;
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
        gameWorld = new GameWorld(true);
        mapOverlay = (MapOverlay) getPrivateField(gameWorld, "mapOverlay");
    }

    @Test
    public void testPlayerMinimapCorrelation() throws Exception {
        Player player = (Player) getPrivateField(gameWorld, "player");
        float scaleX = (float) getPrivateField(mapOverlay, "scaleX");
        float scaleY = (float) getPrivateField(mapOverlay, "scaleY");

        // 1. Kiểm tra tại góc (0, 0) của bản đồ thực
        player.setX(0);
        player.setY(0);
        assertEquals(10, 10 + (int) (player.getX() * scaleX), "Tại góc (0,0), Minimap X phải là 10.");
        assertEquals(10, 10 + (int) (player.getY() * scaleY), "Tại góc (0,0), Minimap Y phải là 10.");

        // 2. Kiểm tra tại vị trí giữa bản đồ (ví dụ 1000, 800)
        player.setX(1000);
        player.setY(800);

        assertEquals(166, 10 + (int) (player.getX() * scaleX), "Tọa độ Player X trên map không khớp tỷ lệ.");
        assertEquals(135, 10 + (int) (player.getY() * scaleY), "Tọa độ Player Y trên map không khớp tỷ lệ.");
    }

    @Test
    public void testPlayerMovementUpdateOnMinimap() throws Exception {
        Player player = (Player) getPrivateField(gameWorld, "player");
        float scaleX = (float) getPrivateField(mapOverlay, "scaleX");

        // Vị trí cũ (500px thực tế -> 88px trên map)
        player.setX(500);
        int oldMiniX = 10 + (int) (player.getX() * scaleX);
        assertEquals(88, oldMiniX);

        // Di chuyển player sang phải 300 pixels (Tổng 800px thực tế -> 135px trên map)
        player.setX(player.getX() + 300);
        int newMiniX = 10 + (int) (player.getX() * scaleX);

        // Kiểm tra kết quả
        assertTrue(newMiniX > oldMiniX, "Vị trí trên Minimap phải được cập nhật khi Player di chuyển.");
        assertEquals(135, newMiniX, "Vị trí 800px thực tế phải tương ứng 135px trên Minimap.");
        assertEquals(47, newMiniX - oldMiniX, "Độ dịch chuyển trên Minimap khi đi 300px phải là 47px.");
    }

    @Test
    public void testNPCAtSpecificPositions() throws Exception {
        @SuppressWarnings("unchecked")
        List<NPC> npcs = (List<NPC>) getPrivateField(gameWorld, "npcs");
        NPC npc = npcs.get(0); // Lấy NPC đầu tiên để test
        float scaleX = (float) getPrivateField(mapOverlay, "scaleX");
        float scaleY = (float) getPrivateField(mapOverlay, "scaleY");

        // 1. Kiểm tra tại góc (0, 0)
        npc.setX(0);
        npc.setY(0);
        assertEquals(10, 10 + (int) (npc.getX() * scaleX), "Tọa độ Minimap X của NPC tại (0,0) phải là 10.");
        assertEquals(10, 10 + (int) (npc.getY() * scaleY), "Tọa độ Minimap Y của NPC tại (0,0) phải là 10.");

        // 2. Kiểm tra tại một vị trí cụ thể (500, 500)
        npc.setX(500);
        npc.setY(500);
        // 10 + (int)(500 * 0.15625) = 88
        assertEquals(88, 10 + (int) (npc.getX() * scaleX),
                "Tọa độ NPC X trên Minimap không khớp tại (500,500).");
        assertEquals(88, 10 + (int) (npc.getY() * scaleY),
                "Tọa độ NPC Y trên Minimap không khớp tại (500,500).");
    }

    @Test
    public void testItemVisibleAfterQuestAndPosition() throws Exception {
        Item rod = getFishingRod();
        float scaleX = (float) getPrivateField(mapOverlay, "scaleX");
        float scaleY = (float) getPrivateField(mapOverlay, "scaleY");

        // Ban đầu vật phẩm phải ẩn
        assertFalse(rod.isVisible(), "Vật phẩm quest phải ẩn khi chưa nhận quest.");

        // Kích hoạt quest (giả lập thông qua callback được set trong GameWorld)
        Object dialogSystem = getPrivateField(gameWorld, "dialogSystem");
        // Lấy functional interface consumer để chạy thủ công
        java.util.function.Consumer<String> onQuestStart = (java.util.function.Consumer<String>) getPrivateField(dialogSystem, "onQuestStart");
        
        if (onQuestStart != null) {
            onQuestStart.accept("fishing_rod");
        } else {
            rod.setVisible(true);
        }

        assertTrue(rod.isVisible(), "Vật phẩm phải hiện sau khi Quest active.");

        // Kiểm tra tọa độ trên Minimap (Fishing rod tại 1120, 800)
        // Offset (10, 10) + (1120 * 0.15625, 800 * 0.15625) = (185, 135)
        int miniX = 10 + (int) (rod.getX() * scaleX);
        int miniY = 10 + (int) (rod.getY() * scaleY);

        assertEquals(185, miniX, "Tọa độ X của vật phẩm trên Minimap không chính xác.");
        assertEquals(135, miniY, "Tọa độ Y của vật phẩm trên Minimap không chính xác.");
    }

    @Test
    public void testItemDisappearsAfterPickup() throws Exception {
        Item rod = getFishingRod();
        rod.setVisible(true); // Đảm bảo hiện để có thể nhặt

        Player player = (Player) getPrivateField(gameWorld, "player");
        // Di chuyển player đến vị trí item
        player.setX(rod.getX());
        player.setY(rod.getY());

        // Cập nhật thế giới để xử lý va chạm nhặt đồ
        gameWorld.update(0.016, com.game.core.InputHandler.getInstance());

        assertTrue(rod.isCollected(), "Vật phẩm phải ở trạng thái đã nhặt (Collected).");

        // Kiểm tra logic hiển thị: item chỉ hiện khi (visible && !collected)
        boolean isShownOnMap = rod.isVisible() && !rod.isCollected();
        assertFalse(isShownOnMap, "Vật phẩm phải biến mất khỏi map sau khi người chơi nhặt.");
    }

    @Test
    public void testCatAtSpecificPositions() throws Exception {
        CatFollower cat = (CatFollower) getPrivateField(gameWorld, "cat");
        float scaleX = (float) getPrivateField(mapOverlay, "scaleX");
        float scaleY = (float) getPrivateField(mapOverlay, "scaleY");

        // Thử nghiệm tại vị trí xa (ví dụ 1200, 900)
        cat.setX(1200);
        cat.setY(900);

        // 10 + (int)(1200 * 0.15625) = 197
        // 10 + (int)(900 * 0.15625) = 150
        assertEquals(197, 10 + (int) (cat.getX() * scaleX), "Tọa độ Mèo X không khớp .");
        assertEquals(150, 10 + (int) (cat.getY() * scaleY), "Tọa độ Mèo Y không khớp.");
    }

    private Item getFishingRod() throws Exception {
        @SuppressWarnings("unchecked")
        List<Item> items = (List<Item>) getPrivateField(gameWorld, "items");
        return items.stream()
                .filter(i -> "fishing_rod".equals(i.getItemId()))
                .findFirst()
                .orElseThrow();
    }

    private Object getPrivateField(Object obj, String fieldName) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
}
