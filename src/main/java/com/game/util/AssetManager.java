package com.game.util;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * AssetManager — Singleton quản lý tất cả assets.
 * Load sprites từ PixelArtGenerator hoặc từ file.
 */
public class AssetManager {

    private static AssetManager instance;
    private final Map<String, Image> images = new HashMap<>();
    private final Map<String, SpriteSheet> spriteSheets = new HashMap<>();
    private boolean loaded = false;

    private AssetManager() {
    }

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    /**
     * Helper to load an image from resources, or fallback to generated one.
     */

    private Image loadOrGenerate(String filename, java.util.function.Supplier<Image> generator) {
        try {
            var resource = getClass().getResource("/assets/" + filename);
            if (resource != null) {
                return new Image(resource.toExternalForm());
            }
        } catch (Exception e) {
            System.out.println("Could not load " + filename + ", falling back to generator.");
        }
        return generator.get();
    }

    /**
     * Player has gender-specific sprite sheets because character selection has two
     * visual variants. Girl uses player.png, boy uses player2.png.
     */
    private Image loadPlayer(boolean isGirl, PixelArtGenerator generator) {
        String filename = isGirl ? Constants.ASSET_PLAYER_GIRL : Constants.ASSET_PLAYER_BOY;
        return loadOrGenerate(filename, () -> generator.generatePlayerSheet(isGirl));
    }

    /**
     * Loads the idle, front-facing player frame for the character select screen.
     * This does not mark the full asset set as loaded, so the chosen in-game
     * sprite sheet can still be loaded later with the selected gender.
     */
    public Image getPlayerPreview(boolean isGirl) {
        String key = isGirl ? "player_preview_girl" : "player_preview_boy";
        Image preview = images.get(key);
        if (preview == null) {
            PixelArtGenerator generator = new PixelArtGenerator();
            String previewFile = isGirl ? Constants.ASSET_PLAYER_PREVIEW_GIRL : Constants.ASSET_PLAYER_PREVIEW_BOY;
            preview = loadOrGenerate(previewFile,
                    () -> new SpriteSheet(loadPlayer(isGirl, generator),
                            Constants.SPRITE_SIZE, Constants.SPRITE_SIZE).getFrame(0, 0));
            images.put(key, preview);
        }
        return preview;
    }

    /**
     * Load tất cả assets. Gọi 1 lần khi khởi tạo game.
     */
    public void loadAll(boolean isGirl) {
        if (loaded)
            return;

        PixelArtGenerator gen = new PixelArtGenerator();

        // Player sprite sheet (4 directions x 4 frames = 16 frames, 32x32 each)
        spriteSheets.put(Constants.KEY_PLAYER,
                new SpriteSheet(loadPlayer(isGirl, gen),
                        Constants.SPRITE_SIZE, Constants.SPRITE_SIZE));

        // NPC sprite sheets
        spriteSheets.put(Constants.KEY_NPC_GRANDMA,
                new SpriteSheet(loadOrGenerate(Constants.ASSET_NPC_GRANDMA, () -> gen.generateNPCSheet("grandma")),
                        Constants.SPRITE_SIZE, Constants.SPRITE_SIZE));
        spriteSheets.put(Constants.KEY_NPC_DRINK_SELLER,
                new SpriteSheet(
                        loadOrGenerate(Constants.ASSET_NPC_DRINK_SELLER, () -> gen.generateNPCSheet("drink_seller")),
                        Constants.SPRITE_SIZE, Constants.SPRITE_SIZE));
        spriteSheets.put(Constants.KEY_NPC_FISHER_KID,
                new SpriteSheet(
                        loadOrGenerate(Constants.ASSET_NPC_FISHER_KID, () -> gen.generateNPCSheet("fisher_kid")),
                        Constants.SPRITE_SIZE, Constants.SPRITE_SIZE));
        spriteSheets.put(Constants.KEY_NPC_GARDENER,
                new SpriteSheet(loadOrGenerate(Constants.ASSET_NPC_GARDENER, () -> gen.generateNPCSheet("gardener")),
                        Constants.SPRITE_SIZE, Constants.SPRITE_SIZE));

        // Cat sprite sheet
        spriteSheets.put(Constants.KEY_CAT,
                new SpriteSheet(loadOrGenerate(Constants.ASSET_CAT, () -> gen.generateCatSheet()),
                        Constants.SPRITE_SIZE, Constants.SPRITE_SIZE));

        // Tileset
        spriteSheets.put(Constants.KEY_TILES,
                new SpriteSheet(loadOrGenerate(Constants.ASSET_TILES, () -> gen.generateTileSet()),
                        Constants.SPRITE_SIZE, Constants.SPRITE_SIZE));

        // Fishing Rod Action
        spriteSheets.put(Constants.KEY_FISHING_ROD_ACTION,
                new SpriteSheet(loadOrGenerate(Constants.ASSET_FISHING_ROD_ACTION, () -> gen.generateFishingRodActionSheet()),
                        Constants.SPRITE_SIZE, Constants.SPRITE_SIZE));

        // Items
        images.put(Constants.KEY_FISHING_ROD,
                loadOrGenerate(Constants.ASSET_FISHING_ROD, () -> gen.generateItemSprite("fishing_rod")));
        images.put(Constants.KEY_SEEDS,
                loadOrGenerate(Constants.ASSET_SEEDS, () -> gen.generateItemSprite("seeds")));
        images.put(Constants.KEY_REWARD_ROSE,
                loadOrGenerate(Constants.ASSET_REWARD_ROSE, () -> gen.generateItemSprite("rose")));
        images.put(Constants.KEY_REWARD_SUNFLOWER,
                loadOrGenerate(Constants.ASSET_REWARD_SUNFLOWER, () -> gen.generateItemSprite("sunflower")));
        images.put(Constants.KEY_REWARD_TULIP,
                loadOrGenerate(Constants.ASSET_REWARD_TULIP, () -> gen.generateItemSprite("tulip")));
        images.put(Constants.KEY_REWARD_BONSAI,
                loadOrGenerate(Constants.ASSET_REWARD_BONSAI, () -> gen.generateItemSprite("bonsai")));
        images.put(Constants.KEY_FISH_CARP,
                loadOrGenerate(Constants.ASSET_FISH_CARP, () -> gen.generateItemSprite("fish_carp")));
        images.put(Constants.KEY_FISH_PERCH,
                loadOrGenerate(Constants.ASSET_FISH_PERCH, () -> gen.generateItemSprite("fish_perch")));
        images.put(Constants.KEY_FISH_CATFISH,
                loadOrGenerate(Constants.ASSET_FISH_CATFISH, () -> gen.generateItemSprite("fish_catfish")));
        images.put(Constants.KEY_FISH_GOLDFISH,
                loadOrGenerate(Constants.ASSET_FISH_GOLDFISH, () -> gen.generateItemSprite("fish_goldfish")));

        // Interaction indicator
        images.put(Constants.KEY_INTERACT_ICON,
                loadOrGenerate(Constants.ASSET_INTERACT_ICON, () -> gen.generateInteractIcon()));

        loaded = true;
    }

    public void reset() {
        images.clear();
        spriteSheets.clear();
        loaded = false;
    }

    /**
     * Lấy image đã load.
     */
    public Image getImage(String key) {
        Image img = images.get(key);
        if (img == null) {
            // Fallback: colored rectangle
            return createFallback(Constants.SPRITE_SIZE, Constants.SPRITE_SIZE, Color.MAGENTA);
        }
        return img;
    }

    /**
     * Lấy sprite sheet đã load.
     */
    public SpriteSheet getSpriteSheet(String key) {
        return spriteSheets.get(key);
    }

    /**
     * Tạo fallback image khi asset không tìm thấy.
     */
    private Image createFallback(int width, int height, Color color) {
        WritableImage img = new WritableImage(width, height);
        PixelWriter pw = img.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    pw.setColor(x, y, Color.BLACK);
                } else {
                    pw.setColor(x, y, color);
                }
            }
        }
        return img;
    }
}