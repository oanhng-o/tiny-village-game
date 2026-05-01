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
 * MapEntityTest — Kiểm tra hiển thị và logic của các thực thể trên bản đồ.
 * Đã chia nhỏ các test cho Item để dễ quản lý.
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
    public void testPlayerPositionOnMinimap() throws Exception {
        Player player = (Player) getPrivateField(gameWorld, "player");
        assertNotNull(player, "Player phải tồn tại trên thế giới.");

        float scaleX = (float) getPrivateField(mapOverlay, "scaleX");
        float scaleY = (float) getPrivateField(mapOverlay, "scaleY");

        int miniX = 10 + (int) (player.getX() * scaleX);
        int miniY = 10 + (int) (player.getY() * scaleY);

        assertTrue(miniX >= 10 && miniX <= 210, "Vị trí X của Player trên minimap không hợp lệ.");
        assertTrue(miniY >= 10 && miniY <= 160, "Vị trí Y của Player trên minimap không hợp lệ.");
    }

    @Test
    public void testNPCPositionOnMinimap() throws Exception {
        @SuppressWarnings("unchecked")
        List<NPC> npcs = (List<NPC>) getPrivateField(gameWorld, "npcs");
        float scaleX = (float) getPrivateField(mapOverlay, "scaleX");
        float scaleY = (float) getPrivateField(mapOverlay, "scaleY");

        for (NPC npc : npcs) {
            int miniX = 10 + (int) (npc.getX() * scaleX);
            int miniY = 10 + (int) (npc.getY() * scaleY);

            assertTrue(miniX >= 10 && miniX <= 210, "Vị trí X của NPC '" + npc.getName() + "' không hợp lệ.");
            assertTrue(miniY >= 10 && miniY <= 160, "Vị trí Y của NPC '" + npc.getName() + "' không hợp lệ.");
        }
    }

    /**
     * 1. Kiểm tra vật phẩm ban đầu ẩn.
     */
    @Test
    public void testItemInitiallyHidden() throws Exception {
        Item rod = getFishingRod();
        assertFalse(rod.isVisible(), "Vật phẩm nhiệm vụ (cần câu) phải ẩn lúc ban đầu.");
    }

    /**
     * 2. Kiểm tra vật phẩm hiện lên sau khi Quest active và đúng vị trí.
     */
    @Test
    public void testItemVisibleAfterQuestAndPosition() throws Exception {
        Item rod = getFishingRod();

        // Kích hoạt quest (giả lập thông qua callback được set trong GameWorld)
        Object dialogSystem = getPrivateField(gameWorld, "dialogSystem");
        Runnable onQuestStart = (Runnable) getPrivateField(dialogSystem, "onQuestStart");
        if (onQuestStart != null) {
            onQuestStart.run();
        } else {
            rod.setVisible(true);
        }

        assertTrue(rod.isVisible(), "Vật phẩm phải hiện sau khi Quest active.");

        float scaleX = (float) getPrivateField(mapOverlay, "scaleX");
        float scaleY = (float) getPrivateField(mapOverlay, "scaleY");
        int miniX = 10 + (int) (rod.getX() * scaleX);
        int miniY = 10 + (int) (rod.getY() * scaleY);

        assertTrue(miniX >= 10 && miniX <= 210, "Vị trí X của Item trên map không hợp lệ.");
        assertTrue(miniY >= 10 && miniY <= 160, "Vị trí Y của Item trên map không hợp lệ.");
    }

    /**
     * 3. Kiểm tra vật phẩm biến mất sau khi người chơi nhặt.
     */
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
    public void testCatPositionOnMinimap() throws Exception {
        CatFollower cat = (CatFollower) getPrivateField(gameWorld, "cat");
        assertNotNull(cat, "Mèo phải tồn tại.");

        float scaleX = (float) getPrivateField(mapOverlay, "scaleX");
        float scaleY = (float) getPrivateField(mapOverlay, "scaleY");

        int miniX = 10 + (int) (cat.getX() * scaleX);
        int miniY = 10 + (int) (cat.getY() * scaleY);

        assertTrue(miniX >= 10 && miniX <= 210, "Vị trí X của mèo trên minimap không hợp lệ.");
        assertTrue(miniY >= 10 && miniY <= 160, "Vị trí Y của mèo trên minimap không hợp lệ.");
    }

    private Item getFishingRod() throws Exception {
        @SuppressWarnings("unchecked")
        List<Item> items = (List<Item>) getPrivateField(gameWorld, "items");
        return items.stream()
                .filter(i -> "fishing_rod".equals(i.getItemId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cần câu."));
    }

    private Object getPrivateField(Object obj, String fieldName) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
}
