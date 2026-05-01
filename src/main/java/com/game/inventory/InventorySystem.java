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

    private static final List<RewardDefinition> FISH_REWARDS = List.of(
            new RewardDefinition("fish_carp", "Cá chép", Constants.KEY_FISH_CARP),
            new RewardDefinition("fish_perch", "Cá rô", Constants.KEY_FISH_PERCH),
            new RewardDefinition("fish_catfish", "Cá trê", Constants.KEY_FISH_CATFISH),
            new RewardDefinition("fish_goldfish", "Cá vàng", Constants.KEY_FISH_GOLDFISH)
    );

    private final Map<String, Integer> itemCounts = new LinkedHashMap<>();
    private final Random random = new Random();

    public InventoryItem addRandomGardenReward() {
        RewardDefinition reward = GARDEN_REWARDS.get(random.nextInt(GARDEN_REWARDS.size()));
        addItem(reward.id());
        return toInventoryItem(reward, itemCounts.get(reward.id()));
    }

    public InventoryItem addRandomFishReward() {
        RewardDefinition reward = FISH_REWARDS.get(random.nextInt(FISH_REWARDS.size()));
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

    public List<InventoryItem> getFishItems() {
        List<InventoryItem> fishItems = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
            if (!isFishItem(entry.getKey())) {
                continue;
            }
            RewardDefinition reward = findReward(entry.getKey());
            fishItems.add(toInventoryItem(reward, entry.getValue()));
        }
        return fishItems;
    }

    public boolean hasFishItems() {
        for (String itemId : itemCounts.keySet()) {
            if (isFishItem(itemId)) {
                return true;
            }
        }
        return false;
    }

    public boolean consumeItem(String itemId) {
        Integer count = itemCounts.get(itemId);
        if (count == null || count <= 0) {
            return false;
        }

        if (count == 1) {
            itemCounts.remove(itemId);
        } else {
            itemCounts.put(itemId, count - 1);
        }
        return true;
    }

    public Map<String, Integer> getItemCountsSnapshot() {
        return new LinkedHashMap<>(itemCounts);
    }

    public void replaceItemCounts(Map<String, Integer> counts) {
        itemCounts.clear();
        if (counts == null) {
            return;
        }

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            Integer count = entry.getValue();
            if (count != null && count > 0) {
                itemCounts.put(entry.getKey(), count);
            }
        }
    }

    public String getDisplayName(String itemId) {
        return findReward(itemId).displayName();
    }

    public String getIconKey(String itemId) {
        return findReward(itemId).iconKey();
    }

    public boolean isFishItem(String itemId) {
        for (RewardDefinition reward : FISH_REWARDS) {
            if (reward.id().equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    private RewardDefinition findReward(String itemId) {
        for (RewardDefinition reward : GARDEN_REWARDS) {
            if (reward.id().equals(itemId)) {
                return reward;
            }
        }
        for (RewardDefinition reward : FISH_REWARDS) {
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
