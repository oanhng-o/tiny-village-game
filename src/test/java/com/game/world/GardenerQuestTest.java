package com.game.world;

import com.game.core.InputHandler;
import com.game.dialog.QuestSystem;
import com.game.entity.Item;
import com.game.entity.NPC;
import com.game.entity.Player;
import com.game.inventory.InventorySystem;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GardenerQuestTest — Kiểm tra quest tìm hạt giống của Bác làm vườn.
 * Bao gồm: Nhận quest, nhặt vật phẩm, và nhận quà sau khi hoàn thành.
 */
public class GardenerQuestTest {

    private GameWorld gameWorld;
    private Player player;
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
        gameWorld = new GameWorld(true);
        player = (Player) getPrivateField(gameWorld, "player");
        questSystem = (QuestSystem) getPrivateField(gameWorld, "questSystem");
        inventorySystem = (InventorySystem) getPrivateField(gameWorld, "inventorySystem");
        input = InputHandler.getInstance();
        resetInputState();
    }

    @Test
    public void testInitialState() throws Exception {
        assertEquals(QuestSystem.QuestState.NOT_STARTED, questSystem.getQuestState(QuestSystem.SEEDS_QUEST_ID));
        Item seeds = getSeedsItem();
        assertFalse(seeds.isVisible(), "Hạt giống phải ẩn khi chưa nhận quest.");
    }

    @Test
    public void testAcceptQuest() throws Exception {
        NPC gardener = getGardenerNPC();
        player.setX(gardener.getX());
        player.setY(gardener.getY());
        
        simulateInteraction(5); // Nhấn Space 5 lần để nhận quest

        assertEquals(QuestSystem.QuestState.ACTIVE, questSystem.getQuestState(QuestSystem.SEEDS_QUEST_ID), "Quest phải chuyển sang ACTIVE.");
        assertTrue(getSeedsItem().isVisible(), "Hạt giống phải hiện sau khi nhận quest.");
    }

    @Test
    public void testPickupSeeds() throws Exception {
        // Fast-forward: Nhận quest
        questSystem.startQuest(QuestSystem.SEEDS_QUEST_ID);
        Item seeds = getSeedsItem();
        seeds.setVisible(true);

        // Di chuyển đến nhặt
        player.setX(seeds.getX());
        player.setY(seeds.getY());
        gameWorld.update(0.016, input);

        assertTrue(seeds.isCollected(), "Hạt giống phải được đánh dấu đã nhặt.");
        assertTrue(questSystem.hasItem(QuestSystem.SEEDS_QUEST_ID), "QuestSystem phải ghi nhận đã có hạt giống.");
    }

    @Test
    public void testCompleteQuestAndReceiveReward() throws Exception {
        // Fast-forward: Đã có hạt giống
        questSystem.startQuest(QuestSystem.SEEDS_QUEST_ID);
        questSystem.addItem(QuestSystem.SEEDS_QUEST_ID);
        
        NPC gardener = getGardenerNPC();
        player.setX(gardener.getX());
        player.setY(gardener.getY());

        simulateInteraction(5); // Nhấn Space 5 lần để trả quest

        assertEquals(QuestSystem.QuestState.COMPLETED, questSystem.getQuestState(QuestSystem.SEEDS_QUEST_ID), "Quest phải chuyển sang COMPLETED.");
        assertFalse(inventorySystem.isEmpty(), "Kho đồ phải nhận được quà tặng.");
        
        InventorySystem.InventoryItem reward = inventorySystem.getItems().get(0);
        assertTrue(List.of("rose", "sunflower", "tulip", "bonsai").contains(reward.getId()), 
                "Món quà phải thuộc danh mục sân vườn.");
    }

    private void simulateInteraction(int spacePresses) {
        simulateKeyPress(KeyCode.ENTER); // Mở dialog
        gameWorld.update(0.016, input);
        
        for (int i = 0; i < spacePresses; i++) {
            simulateKeyPress(KeyCode.SPACE);
            gameWorld.update(0.2, input);
        }
    }

    // --- Helper Methods ---

    private NPC getGardenerNPC() throws Exception {
        @SuppressWarnings("unchecked")
        List<NPC> npcs = (List<NPC>) getPrivateField(gameWorld, "npcs");
        return npcs.stream()
                .filter(n -> "Bác làm vườn".equals(n.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy NPC Bác làm vườn"));
    }

    private Item getSeedsItem() throws Exception {
        @SuppressWarnings("unchecked")
        List<Item> items = (List<Item>) getPrivateField(gameWorld, "items");
        return items.stream()
                .filter(i -> QuestSystem.SEEDS_QUEST_ID.equals(i.getItemId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm hạt giống"));
    }

    private void simulateKeyPress(KeyCode code) {
        input.keyPressed(code);
        input.update();
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
