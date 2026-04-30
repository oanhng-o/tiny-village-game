package com.game.world;

import com.game.core.Camera;
import com.game.core.InputHandler;
import com.game.dialog.DialogData;
import com.game.dialog.DialogSystem;
import com.game.dialog.QuestSystem;
import com.game.entity.*;
import com.game.inventory.InventorySystem;
import com.game.util.AssetManager;
import com.game.util.Constants;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * GameWorld — Central manager: giữ tất cả entities, map, systems.
 * Điều phối update/render cycle và game logic.
 */
public class GameWorld {

    private TileMap tileMap;
    private Player player;
    private List<NPC> npcs;
    private List<Item> items;
    private CatFollower cat;
    private Camera camera;

    private DialogSystem dialogSystem;
    private QuestSystem questSystem;
    private InventorySystem inventorySystem;
    private MapOverlay mapOverlay;
    private boolean inventoryOpen = false;

    // Pickup notification
    private String pickupNotification = null;
    private double pickupTimer = 0;

    public GameWorld(boolean isGirl) {
        // Load assets
        AssetManager.getInstance().loadAll(isGirl);

        // Initialize systems
        questSystem = new QuestSystem();
        inventorySystem = new InventorySystem();
        dialogSystem = new DialogSystem(questSystem);

        // Create map
        tileMap = new TileMap();

        // Create player (spawn near lake, on path)
        player = new Player(19 * TileMap.TILE_SIZE, 12 * TileMap.TILE_SIZE, tileMap);

        // Create camera
        camera = new Camera(tileMap.getPixelWidth(), tileMap.getPixelHeight());
        camera.snapTo(player.getCenterX(), player.getCenterY());

        // Create NPCs
        npcs = new ArrayList<>();
        createNPCs();

        // Create items
        items = new ArrayList<>();
        createItems();

        // Create cat
        cat = new CatFollower(25 * TileMap.TILE_SIZE, 22 * TileMap.TILE_SIZE);

        // Create minimap
        mapOverlay = new MapOverlay(tileMap);

        // Setup quest callbacks
        dialogSystem.setOnQuestStart((questId) -> {
            // Show quest item on map
            for (Item item : items) {
                if (questId.equals(item.getItemId())) {
                    item.setVisible(true);
                }
            }
        });

        dialogSystem.setOnQuestComplete((questId) -> {
            if (QuestSystem.FISHING_ROD_QUEST_ID.equals(questId)) {
                // Cat starts following player after the main fishing quest.
                cat.startFollowing(player);
                showPickupNotification("🐱 Mèo con bắt đầu đi theo bạn!");
            } else if (QuestSystem.SEEDS_QUEST_ID.equals(questId)) {
                InventorySystem.InventoryItem reward = inventorySystem.addRandomGardenReward();
                showPickupNotification("🌱 Bác làm vườn tặng bạn: " + reward.getDisplayName() + "!");
            }
        });
    }

    /**
     * Tạo tất cả NPCs với dialog data.
     */
    private void createNPCs() {
        // === Bà cụ bán rau — Cổng công viên (trên) ===
        NPC grandma = new NPC("Bà cụ bán rau", 17 * TileMap.TILE_SIZE, 3 * TileMap.TILE_SIZE,
                NPC.DialogType.SIMPLE, "npc_grandma");
        grandma.setDialogData(DialogData.simple(
            "Chào cháu! Đây là công viên Hồ Xanh đấy.",
            "Bà bán rau ở đây lâu lắm rồi, cũng thấy nhiều người đến tập thể dục.",
            "Cháu cứ đi dạo thoải mái nhé, cảnh ở đây đẹp lắm!",
            "À, bà thấy có mấy bạn nhỏ câu cá dọc hồ, cháu đi thăm xem~"
        ));
        npcs.add(grandma);

        // === Ông chú bán nước — Bên phải hồ ===
        NPC drinkSeller = new NPC("Ông chú bán nước", 22 * TileMap.TILE_SIZE, 13 * TileMap.TILE_SIZE,
                NPC.DialogType.CHOICE, "npc_drink_seller");
        drinkSeller.setDialogData(DialogData.choice(
            "Chào em! Anh bán nước giải khát đây. Em muốn uống gì nào?",
            new DialogData.Choice("Trà sữa",
                "Trà sữa trân châu đây! Ngọt ngào như nụ cười em vậy~ 🧋"),
            new DialogData.Choice("Cà phê",
                "Cà phê đen đá nhé! Uống xong tỉnh táo đi dạo tiếp! ☕"),
            new DialogData.Choice("Nước cam",
                "Nước cam tươi mát lành! Bổ sung vitamin C nè~ 🍊")
        ));
        npcs.add(drinkSeller);

        // === Bạn nhỏ câu cá — Dọc hồ dưới ===
        NPC fisherKid = new NPC("Bạn nhỏ câu cá", 18 * TileMap.TILE_SIZE, 20 * TileMap.TILE_SIZE,
                NPC.DialogType.QUEST, "npc_fisher_kid");
        fisherKid.setDialogData(DialogData.quest(
            List.of(
                "Huhu... Anh/chị ơi cứu em với!",
                "Em đang câu cá ở đây thì bị mất cần câu rồi...",
                "Hình như nó rơi đâu đó trong công viên.",
                "Anh/chị giúp em tìm được không? 🥺"
            ),
            List.of(
                "Anh/chị tìm được cần câu chưa ạ?",
                "Em nhớ lúc nãy chạy qua mấy bụi cây phía góc công viên..."
            ),
            List.of(
                "Ôi cần câu của em! Cảm ơn anh/chị nhiều lắm! 🎉",
                "Để em tặng anh/chị con mèo nhỏ này nhé~",
                "Nó cứ đi theo em hoài, giờ nó sẽ theo anh/chị!"
            )
        ));
        npcs.add(fisherKid);

        // === Bác làm vườn — Góc dưới bên trái ===
        NPC gardener = new NPC("Bác làm vườn", 5 * TileMap.TILE_SIZE, 25 * TileMap.TILE_SIZE,
                NPC.DialogType.QUEST, Constants.KEY_NPC_GARDENER);
        gardener.setDialogData(DialogData.quest(
            QuestSystem.SEEDS_QUEST_ID,
            List.of(
                "Chào cháu, bác đang chăm vườn hoa ở góc này.",
                "Sáng nay bác làm rơi túi hạt giống khi đi qua khu hoa phía trên công viên.",
                "Cháu giúp bác tìm lại túi hạt giống được không?"
            ),
            List.of(
                "Cháu tìm thấy túi hạt giống của bác chưa?",
                "Bác nhớ nó rơi gần những luống hoa phía trên bên phải công viên."
            ),
            List.of(
                "Đúng là túi hạt giống của bác rồi!",
                "Cảm ơn cháu nhé, vườn hoa sắp có thêm nhiều mầm mới."
            )
        ));
        npcs.add(gardener);
    }

    /**
     * Tạo items trên map.
     */
    private void createItems() {
        // Cần câu ẩn ở góc dưới-phải map (trong bụi cây)
        Item fishingRod = new Item("fishing_rod", 35 * TileMap.TILE_SIZE, 25 * TileMap.TILE_SIZE);
        fishingRod.setVisible(false); // Hidden until quest active
        items.add(fishingRod);

        // Túi hạt giống ẩn ở khu hoa phía trên-phải map
        Item seeds = new Item(QuestSystem.SEEDS_QUEST_ID, 32 * TileMap.TILE_SIZE, 5 * TileMap.TILE_SIZE);
        seeds.setVisible(false); // Hidden until quest active
        items.add(seeds);
    }

    /**
     * Update game logic mỗi frame.
     */
    public void update(double dt, InputHandler input) {
        // Update dialog system
        dialogSystem.update(dt);

        if (inventoryOpen) {
            if (input.isKeyJustPressed(KeyCode.I) || input.isKeyJustPressed(KeyCode.ESCAPE)) {
                inventoryOpen = false;
            }
            updatePickupNotification(dt);
            return;
        }

        if (dialogSystem.isActive()) {
            // Dialog active → only handle dialog input
            dialogSystem.handleInput(input);
            updatePickupNotification(dt);
            return;
        }

        // Toggle runtime inventory overlay
        if (input.isKeyJustPressed(KeyCode.I)) {
            inventoryOpen = true;
            updatePickupNotification(dt);
            return;
        }

        // Handle map overlay toggle
        if (input.isKeyJustPressed(KeyCode.M)) {
            mapOverlay.toggle();
        }

        // Player movement
        player.handleInput(dt, input);
        player.update(dt);

        // Check NPC interaction
        if (input.isKeyJustPressed(KeyCode.E) || input.isKeyJustPressed(KeyCode.ENTER)) {
            for (NPC npc : npcs) {
                if (npc.canInteract(player)) {
                    dialogSystem.startDialog(npc);
                    break;
                }
            }
        }

        // Update NPCs
        for (NPC npc : npcs) {
            npc.update(dt);
            npc.isPlayerInRange(player);
        }

        // Update items
        for (Item item : items) {
            item.update(dt);
            if (item.checkPickup(player)) {
                item.setCollected(true);
                questSystem.addItem(item.getItemId());
                if (QuestSystem.FISHING_ROD_QUEST_ID.equals(item.getItemId())) {
                    player.setHasFishingRod(true);
                    showPickupNotification("🎣 Đã nhặt được cần câu!");
                } else if (QuestSystem.SEEDS_QUEST_ID.equals(item.getItemId())) {
                    showPickupNotification("🌱 Đã nhặt được túi hạt giống!");
                }
            }
        }

        // Update cat
        cat.update(dt);

        // Update camera
        camera.update(player.getCenterX(), player.getCenterY(), dt);

        updatePickupNotification(dt);
    }

    /**
     * Render toàn bộ game.
     */
    public void render(GraphicsContext gc) {
        double camX = camera.getOffsetX();
        double camY = camera.getOffsetY();

        // Layer 0: Ground tiles
        tileMap.renderGround(gc, camX, camY);

        // Collect all entities for Y-sorting
        List<Entity> entities = new ArrayList<>();
        entities.add(player);
        entities.addAll(npcs);
        entities.add(cat);

        // Add visible items
        for (Item item : items) {
            if (item.isVisible() && !item.isCollected()) {
                entities.add(item);
            }
        }

        // Sort by Y position for depth
        entities.sort(Comparator.comparingDouble(Entity::getY));

        // Render entities
        for (Entity entity : entities) {
            entity.render(gc, camX, camY);
        }

        // Layer 1: Decorations (on top of entities)
        tileMap.renderDecorations(gc, camX, camY);

        // === UI Layer (screen space, no camera offset) ===

        // Dialog box
        dialogSystem.render(gc);

        // Quest indicator
        dialogSystem.renderQuestIndicator(gc);

        // Minimap overlay
        mapOverlay.render(gc, tileMap, player, npcs, items, cat);

        // Pickup notification
        renderPickupNotification(gc);

        // Controls hint (top-left)
        renderControlsHint(gc);

        // Inventory overlay should be topmost and pauses gameplay while open.
        if (inventoryOpen) {
            renderInventoryOverlay(gc);
        }
    }

    /**
     * Hiện notification khi nhặt item.
     */
    private void showPickupNotification(String text) {
        pickupNotification = text;
        pickupTimer = 3.0; // 3 seconds
    }

    private void updatePickupNotification(double dt) {
        if (pickupNotification != null) {
            pickupTimer -= dt;
            if (pickupTimer <= 0) {
                pickupNotification = null;
            }
        }
    }

    private void renderPickupNotification(GraphicsContext gc) {
        if (pickupNotification == null) return;

        double alpha = Math.min(1.0, pickupTimer);
        double w = Constants.WINDOW_WIDTH;

        // Slide in from top
        double slideY = pickupTimer > 2.5 ? (3.0 - pickupTimer) * 100 : 50;
        if (pickupTimer < 0.5) slideY = pickupTimer * 100;

        double textWidth = pickupNotification.length() * 8;
        double boxX = w / 2 - textWidth / 2 - 15;

        gc.setFill(Color.web("#2E8B57", 0.85 * alpha));
        gc.fillRoundRect(boxX, slideY - 10, textWidth + 30, 32, 8, 8);
        gc.setStroke(Color.web("#90EE90", 0.6 * alpha));
        gc.setLineWidth(1);
        gc.strokeRoundRect(boxX, slideY - 10, textWidth + 30, 32, 8, 8);

        gc.setFill(Color.web("#FFFFFF", alpha));
        gc.setFont(Font.font("Monospaced", 12));
        gc.fillText(pickupNotification, boxX + 15, slideY + 10);
    }

    private void renderControlsHint(GraphicsContext gc) {
        gc.setFill(Color.web("#FFFFFF", 0.4));
        gc.setFont(Font.font("Monospaced", 10));
        gc.fillText("WASD: Di chuyển | E: Nói chuyện | M: Bản đồ | I: Kho | Space: Tiếp tục", 10, Constants.WINDOW_HEIGHT - 10);
    }

    private void renderInventoryOverlay(GraphicsContext gc) {
        double w = Constants.WINDOW_WIDTH;
        double h = Constants.WINDOW_HEIGHT;
        double panelW = 520;
        double panelH = 380;
        double panelX = (w - panelW) / 2.0;
        double panelY = (h - panelH) / 2.0;

        gc.setFill(Color.web("#000000", 0.45));
        gc.fillRect(0, 0, w, h);

        gc.setFill(Color.web("#FFF8DC", 0.97));
        gc.fillRoundRect(panelX, panelY, panelW, panelH, 18, 18);
        gc.setStroke(Color.web("#A8E6CF"));
        gc.setLineWidth(4);
        gc.strokeRoundRect(panelX, panelY, panelW, panelH, 18, 18);

        gc.setFill(Color.web("#5D4037"));
        gc.setFont(Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 24));
        gc.fillText("Kho lưu trữ", panelX + 28, panelY + 44);

        gc.setFill(Color.web("#7A5A44"));
        gc.setFont(Font.font("Monospaced", 11));
        gc.fillText("I / Esc: Đóng", panelX + panelW - 115, panelY + 42);

        if (inventorySystem.isEmpty()) {
            gc.setFill(Color.web("#7A5A44", 0.75));
            gc.setFont(Font.font("Monospaced", 16));
            gc.fillText("Chưa có vật phẩm nào", panelX + 155, panelY + 190);
            return;
        }

        List<InventorySystem.InventoryItem> inventoryItems = inventorySystem.getItems();
        int cols = 2;
        double cellW = 220;
        double cellH = 74;
        double gap = 18;
        double startX = panelX + 30;
        double startY = panelY + 72;

        for (int i = 0; i < inventoryItems.size(); i++) {
            InventorySystem.InventoryItem item = inventoryItems.get(i);
            int col = i % cols;
            int row = i / cols;
            double cellX = startX + col * (cellW + gap);
            double cellY = startY + row * (cellH + gap);

            gc.setFill(Color.web("#FFFFFF", 0.72));
            gc.fillRoundRect(cellX, cellY, cellW, cellH, 10, 10);
            gc.setStroke(Color.web("#D6C49C", 0.9));
            gc.setLineWidth(2);
            gc.strokeRoundRect(cellX, cellY, cellW, cellH, 10, 10);

            Image icon = item.getIcon();
            if (icon != null) {
                gc.setImageSmoothing(false);
                gc.drawImage(icon, cellX + 14, cellY + 15, 44, 44);
            }

            gc.setFill(Color.web("#4A4A4A"));
            gc.setFont(Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 14));
            gc.fillText(item.getDisplayName(), cellX + 70, cellY + 31);

            gc.setFill(Color.web("#2E7D32"));
            gc.setFont(Font.font("Monospaced", 13));
            gc.fillText("Số lượng: x" + item.getQuantity(), cellX + 70, cellY + 54);
        }
    }

    // Getters for MapOverlay
    public TileMap getTileMap() { return tileMap; }
    public Player getPlayer() { return player; }
    public List<NPC> getNPCs() { return npcs; }
    public List<Item> getItems() { return items; }
    public CatFollower getCat() { return cat; }
}
