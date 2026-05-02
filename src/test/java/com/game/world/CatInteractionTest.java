package com.game.world;

import com.game.core.InputHandler;
import com.game.dialog.QuestSystem;
import com.game.entity.CatFollower;
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
 * CatInteractionTest — Kiểm tra các tính năng tương tác với mèo:
 * - Mở khóa sau quest.
 * - Vuốt ve (E) và gọi mèo (C).
 * - Cho ăn cá qua menu Cat Care.
 * - Tiến trình tình cảm (Heart Level).
 */
public class CatInteractionTest {

    private GameWorld gameWorld;
    private Player player;
    private CatFollower cat;
    private QuestSystem questSystem;
    private InventorySystem inventorySystem;
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
        // Khởi tạo thế giới game (isGirl = true)
        gameWorld = new GameWorld(true);
        player = (Player) getPrivateField(gameWorld, "player");
        cat = (CatFollower) getPrivateField(gameWorld, "cat");
        questSystem = (QuestSystem) getPrivateField(gameWorld, "questSystem");
        inventorySystem = (InventorySystem) getPrivateField(gameWorld, "inventorySystem");
        input = InputHandler.getInstance();
        input.reset();
    }

    @Test
    public void testCatUnlockAfterQuest() throws Exception {
        // 1. Kiểm tra ban đầu mèo ở trạng thái IDLE và chưa mở khóa care
        assertEquals(CatFollower.State.IDLE, cat.getState());
        assertFalse(cat.isCareUnlocked());

        // 2. Giả lập hoàn thành quest fishing_rod
        questSystem.completeQuest(QuestSystem.FISHING_ROD_QUEST_ID);

        // Kích hoạt callback trong GameWorld khi quest hoàn thành
        Object dialogSystem = getPrivateField(gameWorld, "dialogSystem");
        @SuppressWarnings("unchecked")
        java.util.function.Consumer<String> onQuestComplete = (java.util.function.Consumer<String>) getPrivateField(dialogSystem, "onQuestComplete");
        onQuestComplete.accept(QuestSystem.FISHING_ROD_QUEST_ID);

        // 3. Kiểm tra mèo đã chuyển sang trạng thái WAITING và có mood ban đầu
        assertEquals(CatFollower.State.WAITING, cat.getState());
        assertTrue(cat.isCareUnlocked());
        assertEquals(60, cat.getMood(), "Mood ban đầu sau khi mở khóa phải là 60.");
    }

    @Test
    public void testPettingIncreasesStats() throws Exception {
        unlockCat();

        // Di chuyển player đến sát vị trí mèo để có thể tương tác
        player.setX(cat.getX());
        player.setY(cat.getY());

        int initialMood = cat.getMood();
        int initialAffection = cat.getAffectionPoints();

        // Giả lập nhấn phím E (Vuốt ve)
        simulateKeyPress(KeyCode.E, 0.016);

        // Kiểm tra chỉ số tăng trưởng
        assertEquals(initialMood + 5, cat.getMood(), "Mood phải tăng 5 điểm sau khi vuốt ve.");
        assertEquals(initialAffection + 2, cat.getAffectionPoints(), "Affection phải tăng 2 điểm sau khi vuốt ve.");
        assertTrue(cat.getPetCooldownRemaining() > 0, "Phải có cooldown sau khi vuốt ve.");
    }

    @Test
    public void testPettingCooldown() throws Exception {
        unlockCat();
        player.setX(cat.getX());
        player.setY(cat.getY());

        // Vuốt ve lần 1 -> Cooldown bắt đầu
        simulateKeyPress(KeyCode.E, 0.016);
        int moodAfterFirstPet = cat.getMood();

        // Vuốt ve lần 2 ngay lập tức
        simulateKeyPress(KeyCode.E, 0.016);
        assertEquals(moodAfterFirstPet, cat.getMood(), "Mood không được tăng khi đang trong thời gian chờ (cooldown).");
    }

    @Test
    public void testCallingCat() throws Exception {
        unlockCat();

        // Đặt mèo ở xa player
        player.setX(100);
        player.setY(100);
        cat.setX(600);
        cat.setY(600);

        // Giả lập nhấn phím C để gọi mèo
        simulateKeyPress(KeyCode.C, 0.016);

        assertEquals(CatFollower.State.CALLING, cat.getState(), "Mèo phải chuyển sang trạng thái CALLING.");

        // Cập nhật thế giới trong một khoảng thời gian để mèo di chuyển
        for (int i = 0; i < 300; i++) {
            gameWorld.update(0.016, input);
        }

        double distance = Math.sqrt(Math.pow(cat.getX() - player.getX(), 2) + Math.pow(cat.getY() - player.getY(), 2));
        assertTrue(distance < 100, "Mèo phải di chuyển lại gần player sau khi được gọi.");
    }

    @Test
    public void testFeedingCatViaMenu() throws Exception {
        unlockCat();

        // 1. Thêm cá vào Inventory runtime
        inventorySystem.addRandomFishReward();
        assertFalse(inventorySystem.getFishItems().isEmpty(), "Phải có ít nhất 1 con cá trong kho.");

        // 2. Player đứng gần mèo
        player.setX(cat.getX());
        player.setY(cat.getY());

        // 3. Nhấn C để mở Cat Care menu
        simulateKeyPress(KeyCode.C, 0.016);
        assertTrue((boolean) getPrivateField(gameWorld, "catCareOpen"), "Menu chăm sóc mèo phải hiển thị.");

        int initialMood = cat.getMood();
        int initialAffection = cat.getAffectionPoints();

        // 4. Nhấn ENTER để xác nhận cho ăn (vị trí chọn mặc định là 0)
        simulateKeyPress(KeyCode.ENTER, 0.016);

        // 5. Kiểm tra kết quả: Mood +15, Affection +8
        assertEquals(initialMood + 15, cat.getMood(), "Mood phải tăng 15 điểm sau khi cho ăn.");
        assertEquals(initialAffection + 8, cat.getAffectionPoints(), "Affection phải tăng 8 điểm sau khi cho ăn.");
        assertTrue(inventorySystem.getFishItems().isEmpty(), "Cá phải bị trừ khỏi kho đồ sau khi cho ăn.");
    }

    @Test
    public void testFeedingCatCooldown() throws Exception {
        unlockCat();

        // 1. Chuẩn bị 2 con cá (giả sử cùng loại để dễ check quantity)
        inventorySystem.addRandomFishReward();
        inventorySystem.addRandomFishReward();

        player.setX(cat.getX());
        player.setY(cat.getY());

        // 2. Mở menu và cho ăn lần 1
        simulateKeyPress(KeyCode.C, 0.016);
        simulateKeyPress(KeyCode.ENTER, 0.016);
        int moodAfterFirstFeed = cat.getMood();

        // 3. Thử cho ăn lần 2 ngay lập tức
        simulateKeyPress(KeyCode.ENTER, 0.016);

        // 4. Kiểm tra: Mood không đổi và số lượng cá không giảm thêm
        assertEquals(moodAfterFirstFeed, cat.getMood(), "Mood không được tăng khi đang trong cooldown cho ăn.");
        // Kiểm tra xem vẫn còn cá trong kho (vì lần 2 không ăn được nên không bị trừ)
        assertFalse(inventorySystem.getFishItems().isEmpty(), "Cá không được bị trừ khi đang trong cooldown.");
    }

    @Test
    public void testHeartLevelProgression() throws Exception {
        unlockCat();

        // Kiểm tra các mốc Heart Level dựa trên điểm Affection
        setPrivateField(cat, "affectionPoints", 19);
        assertEquals(1, cat.getHeartLevel(), "Affection < 20 phải là Heart 1.");

        setPrivateField(cat, "affectionPoints", 20);
        assertEquals(2, cat.getHeartLevel(), "Affection 20-39 phải là Heart 2.");

        setPrivateField(cat, "affectionPoints", 40);
        assertEquals(3, cat.getHeartLevel(), "Affection 40-69 phải là Heart 3.");

        setPrivateField(cat, "affectionPoints", 70);
        assertEquals(4, cat.getHeartLevel(), "Affection 70-109 phải là Heart 4.");

        setPrivateField(cat, "affectionPoints", 110);
        assertEquals(5, cat.getHeartLevel(), "Affection >= 110 phải là Heart 5.");
    }

    private void unlockCat() throws Exception {
        cat.unlockCare(player);
    }

    private void simulateKeyPress(KeyCode code, double dt) {
        input.keyPressed(code);
        input.update();
        gameWorld.update(dt, input);
        input.keyReleased(code);
        input.update();
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
}
