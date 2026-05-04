package com.game.world;

import com.game.core.InputHandler;
import com.game.dialog.QuestSystem;
import com.game.entity.Item;
import com.game.entity.NPC;
import com.game.entity.Player;
import com.game.entity.CatFollower;
import com.game.world.FishingMiniGame;
import com.game.world.TileMap;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FishingRodQuestTest — Kiểm tra quest tìm cần câu của Bạn nhỏ câu cá.
 * Bao gồm: Nhận quest, nhặt cần câu, và hoàn thành quest để mở khóa mèo.
 */
public class FishingRodQuestTest {

    private GameWorld gameWorld;
    private Player player;
    private QuestSystem questSystem;
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
        questSystem = (QuestSystem) getPrivateField(gameWorld, "questSystem");
        input = InputHandler.getInstance();
        resetInputState();
    }

    @Test
    public void testInitialState() throws Exception {
        assertEquals(QuestSystem.QuestState.NOT_STARTED, questSystem.getQuestState(QuestSystem.FISHING_ROD_QUEST_ID));
        Item rod = getRodItem();
        assertFalse(rod.isVisible(), "Cần câu phải ẩn khi chưa nhận quest.");
    }

    @Test
    public void testAcceptQuest() throws Exception {
        NPC fisherKid = getFisherKidNPC();
        player.setX(fisherKid.getX());
        player.setY(fisherKid.getY());

        // Nhấn Enter để mở hội thoại và Space để qua lời thoại nhận quest
        simulateInteraction(10); 

        assertEquals(QuestSystem.QuestState.ACTIVE, questSystem.getQuestState(QuestSystem.FISHING_ROD_QUEST_ID),
                "Quest phải chuyển sang ACTIVE.");
        assertTrue(getRodItem().isVisible(), "Cần câu phải hiện sau khi nhận quest.");
    }

    @Test
    public void testPickupRod() throws Exception {
        // Fast-forward: Bắt đầu quest
        questSystem.startQuest(QuestSystem.FISHING_ROD_QUEST_ID);
        Item rod = getRodItem();
        rod.setVisible(true);

        // Di chuyển đến nhặt
        player.setX(rod.getX());
        player.setY(rod.getY());
        gameWorld.update(0.016, input);

        assertTrue(rod.isCollected(), "Cần câu phải được đánh dấu đã nhặt.");
        assertTrue(questSystem.hasItem(QuestSystem.FISHING_ROD_QUEST_ID), "QuestSystem phải ghi nhận đã có cần câu.");
        assertTrue(player.hasFishingRod(), "Player phải được set đã có cần câu.");
    }

    @Test
    public void testCompleteQuest() throws Exception {
        // Fast-forward: Đã nhặt cần câu
        questSystem.startQuest(QuestSystem.FISHING_ROD_QUEST_ID);
        questSystem.addItem(QuestSystem.FISHING_ROD_QUEST_ID);
        player.setHasFishingRod(true);

        NPC fisherKid = getFisherKidNPC();
        player.setX(fisherKid.getX());
        player.setY(fisherKid.getY());

        // Trả quest
        simulateInteraction(10);

        assertEquals(QuestSystem.QuestState.COMPLETED, questSystem.getQuestState(QuestSystem.FISHING_ROD_QUEST_ID), 
                "Quest phải chuyển sang COMPLETED.");
        
        CatFollower cat = (CatFollower) getPrivateField(gameWorld, "cat");
        assertTrue(cat.isCareUnlocked(), "Mèo phải được mở khóa chăm sóc sau khi hoàn thành quest.");

        // Bổ sung: Kiểm tra xem fishing minigame đã được mở khóa chưa
        // Di chuyển đến gần nước (tọa độ từ FishingTriggerTest)
        player.setX(13 * TileMap.TILE_SIZE);
        player.setY(14 * TileMap.TILE_SIZE);
        
        // Nhấn F để bắt đầu câu cá
        simulateKeyPress(KeyCode.F, 0.016);
        
        FishingMiniGame fishingMiniGame = (FishingMiniGame) getPrivateField(gameWorld, "fishingMiniGame");
        assertTrue(fishingMiniGame.isActive(), "Fishing Mini-game phải được mở khóa sau khi hoàn thành quest.");
        assertEquals(Player.PlayerState.FISHING, player.getState(), "Trạng thái Player phải chuyển sang FISHING.");
    }

    private void simulateInteraction(int spacePresses) {
        simulateKeyPress(KeyCode.ENTER, 0.016); // Mở dialog

        for (int i = 0; i < spacePresses; i++) {
            simulateKeyPress(KeyCode.SPACE, 0.2);
        }
    }

    // --- Helper Methods ---

    private NPC getFisherKidNPC() throws Exception {
        @SuppressWarnings("unchecked")
        List<NPC> npcs = (List<NPC>) getPrivateField(gameWorld, "npcs");
        return npcs.stream()
                .filter(n -> "Bạn nhỏ câu cá".equals(n.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy NPC Bạn nhỏ câu cá"));
    }

    private Item getRodItem() throws Exception {
        @SuppressWarnings("unchecked")
        List<Item> items = (List<Item>) getPrivateField(gameWorld, "items");
        return items.stream()
                .filter(i -> QuestSystem.FISHING_ROD_QUEST_ID.equals(i.getItemId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm cần câu"));
    }

    private void simulateKeyPress(KeyCode code, double dt) {
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
