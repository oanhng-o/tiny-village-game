package com.game.inventory;

import com.game.util.AssetManager;
import com.game.util.Constants;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Runtime inventory for garden rewards.
 * Inventory is session-only: it resets when a new GameWorld is created.
 */
public class InventorySystem {

    public static class InventoryItem {
        private final String id;
        private final String displayName;
        private final String iconKey;
        private final int quantity;

        private InventoryItem(String id, String displayName, String iconKey, int quantity) {
            this.id = id;
            this.displayName = displayName;
            this.iconKey = iconKey;
            this.quantity = quantity;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getIconKey() { return iconKey; }
        public int getQuantity() { return quantity; }
        public Image getIcon() { return AssetManager.getInstance().getImage(iconKey); }
    }

    private record RewardDefinition(String id, String displayName, String iconKey) {}

    private static final List<RewardDefinition> GARDEN_REWARDS = List.of(
            new RewardDefinition("rose", "Hoa hồng", Constants.KEY_REWARD_ROSE),
            new RewardDefinition("sunflower", "Hoa hướng dương", Constants.KEY_REWARD_SUNFLOWER),
            new RewardDefinition("tulip", "Hoa tulip", Constants.KEY_REWARD_TULIP),
            new RewardDefinition("bonsai", "Cây bonsai", Constants.KEY_REWARD_BONSAI)
    );

    private final Map<String, Integer> itemCounts = new LinkedHashMap<>();
    private final Random random = new Random();

    public InventoryItem addRandomGardenReward() {
        RewardDefinition reward = GARDEN_REWARDS.get(random.nextInt(GARDEN_REWARDS.size()));
        addItem(reward.id());
        return toInventoryItem(reward, itemCounts.get(reward.id()));
    }

    public void addItem(String itemId) {
        itemCounts.merge(itemId, 1, Integer::sum);
    }

    public boolean isEmpty() {
        return itemCounts.isEmpty();
    }

    public List<InventoryItem> getItems() {
        List<InventoryItem> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
            RewardDefinition reward = findReward(entry.getKey());
            items.add(toInventoryItem(reward, entry.getValue()));
        }
        return items;
    }

    public String getDisplayName(String itemId) {
        return findReward(itemId).displayName();
    }

    public String getIconKey(String itemId) {
        return findReward(itemId).iconKey();
    }

    private RewardDefinition findReward(String itemId) {
        for (RewardDefinition reward : GARDEN_REWARDS) {
            if (reward.id().equals(itemId)) {
                return reward;
            }
        }
        return new RewardDefinition(itemId, itemId, itemId);
    }

    private InventoryItem toInventoryItem(RewardDefinition reward, int quantity) {
        return new InventoryItem(reward.id(), reward.displayName(), reward.iconKey(), quantity);
    }
}
