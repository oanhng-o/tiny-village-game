package com.game.world;

import com.game.core.Camera;
import com.game.core.InputHandler;
import com.game.dialog.DialogData;
import com.game.dialog.DialogSystem;
import com.game.dialog.QuestSystem;
import com.game.entity.*;
import com.game.util.AssetManager;
import com.game.util.Constants;

import javafx.scene.canvas.GraphicsContext;
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
    private MapOverlay mapOverlay;

    // Pickup notification
    private String pickupNotification = null;
    private double pickupTimer = 0;

    public GameWorld(boolean isGirl) {
        // Load assets
        AssetManager.getInstance().loadAll(isGirl);

        // Initialize systems
        questSystem = new QuestSystem();
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
        dialogSystem.setOnQuestStart(() -> {
            // Show fishing rod item on map
            for (Item item : items) {
                if ("fishing_rod".equals(item.getItemId())) {
                    item.setVisible(true);
                }
            }
        });

        dialogSystem.setOnQuestComplete(() -> {
            // Cat starts following player
            cat.startFollowing(player);
            showPickupNotification("🐱 Mèo con bắt đầu đi theo bạn!");
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
    }

    /**
     * Tạo items trên map.
     */
    private void createItems() {
        // Cần câu ẩn ở góc dưới-phải map (trong bụi cây)
        Item fishingRod = new Item("fishing_rod", 35 * TileMap.TILE_SIZE, 25 * TileMap.TILE_SIZE);
        fishingRod.setVisible(false); // Hidden until quest active
        items.add(fishingRod);
    }

    /**
     * Update game logic mỗi frame.
     */
    public void update(double dt, InputHandler input) {
        // Update dialog system
        dialogSystem.update(dt);

        // Handle map overlay toggle
        if (input.isKeyJustPressed(KeyCode.M)) {
            mapOverlay.toggle();
        }

        if (dialogSystem.isActive()) {
            // Dialog active → only handle dialog input
            dialogSystem.handleInput(input);
            return;
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
                if ("fishing_rod".equals(item.getItemId())) {
                    questSystem.pickUpRod();
                    player.setHasFishingRod(true);
                    showPickupNotification("🎣 Đã nhặt được cần câu!");
                }
            }
        }

        // Update cat
        cat.update(dt);

        // Update camera
        camera.update(player.getCenterX(), player.getCenterY(), dt);

        // Update pickup notification
        if (pickupNotification != null) {
            pickupTimer -= dt;
            if (pickupTimer <= 0) {
                pickupNotification = null;
            }
        }
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
    }

    /**
     * Hiện notification khi nhặt item.
     */
    private void showPickupNotification(String text) {
        pickupNotification = text;
        pickupTimer = 3.0; // 3 seconds
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
        gc.fillText("WASD: Di chuyển | E: Nói chuyện | M: Bản đồ | Space: Tiếp tục", 10, Constants.WINDOW_HEIGHT - 10);
    }

    // Getters for MapOverlay
    public TileMap getTileMap() { return tileMap; }
    public Player getPlayer() { return player; }
    public List<NPC> getNPCs() { return npcs; }
    public List<Item> getItems() { return items; }
    public CatFollower getCat() { return cat; }
}
