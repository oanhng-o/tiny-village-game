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

    private AssetManager() {}

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    /**
     * Load tất cả assets. Gọi 1 lần khi khởi tạo game.
     */
    public void loadAll(boolean isGirl) {
        if (loaded) return;

        PixelArtGenerator gen = new PixelArtGenerator();

        // Player sprite sheet (4 directions x 4 frames = 16 frames, 32x32 each)
        spriteSheets.put("player", new SpriteSheet(gen.generatePlayerSheet(isGirl), 32, 32));

        // NPC sprite sheets
        spriteSheets.put("npc_grandma", new SpriteSheet(gen.generateNPCSheet("grandma"), 32, 32));
        spriteSheets.put("npc_drink_seller", new SpriteSheet(gen.generateNPCSheet("drink_seller"), 32, 32));
        spriteSheets.put("npc_fisher_kid", new SpriteSheet(gen.generateNPCSheet("fisher_kid"), 32, 32));

        // Cat sprite sheet
        spriteSheets.put("cat", new SpriteSheet(gen.generateCatSheet(), 32, 32));

        // Tileset
        spriteSheets.put("tiles", new SpriteSheet(gen.generateTileSet(), 32, 32));

        // Items
        images.put("fishing_rod", gen.generateItemSprite("fishing_rod"));

        // Interaction indicator
        images.put("interact_icon", gen.generateInteractIcon());

        loaded = true;
    }

    /**
     * Lấy image đã load.
     */
    public Image getImage(String key) {
        Image img = images.get(key);
        if (img == null) {
            // Fallback: colored rectangle
            return createFallback(32, 32, Color.MAGENTA);
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
                if (x == 0 || x == width-1 || y == 0 || y == height-1) {
                    pw.setColor(x, y, Color.BLACK);
                } else {
                    pw.setColor(x, y, color);
                }
            }
        }
        return img;
    }
}
