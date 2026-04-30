package com.game.util;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

/**
 * PixelArtGenerator — Tạo tất cả pixel art sprites bằng code.
 * Mỗi sprite 32x32 px, phong cách chibi dễ thương.
 */
public class PixelArtGenerator {

    private void setPixel(PixelWriter pw, int x, int y, Color c) {
        if (x >= 0 && y >= 0)
            pw.setColor(x, y, c);
    }

    private void fillRect(PixelWriter pw, int x, int y, int w, int h, Color c) {
        for (int dy = 0; dy < h; dy++)
            for (int dx = 0; dx < w; dx++)
                setPixel(pw, x + dx, y + dy, c);
    }

    // ======================== PLAYER ========================
    public Image generatePlayerSheet(boolean isGirl) {
        // 4 cols (frames) x 4 rows (directions: down, left, right, up)
        int cols = 4, rows = 4, size = Constants.SPRITE_SIZE;
        WritableImage sheet = new WritableImage(cols * size, rows * size);
        PixelWriter pw = sheet.getPixelWriter();

        Color hair = isGirl ? Color.web("#8B4513") : Color.web("#2c3e50");
        Color skin = Color.web("#FFDAB9");
        Color eyes = Color.web("#2c1810");
        Color shirt = isGirl ? Color.web("#FF69B4") : Color.web("#3498db");
        Color pants = isGirl ? Color.web("#FF69B4") : Color.web("#2c3e50");
        Color shoes = isGirl ? Color.web("#FF1493") : Color.web("#e74c3c");
        Color blush = Color.web("#FFB6C1");
        Color ribbon = Color.web("#FF69B4");

        for (int dir = 0; dir < 4; dir++) {
            for (int frame = 0; frame < 4; frame++) {
                int ox = frame * size;
                int oy = dir * size;
                int walk = (frame % 2 == 0) ? 0 : (frame == 1 ? 1 : -1);

                // Hair
                fillRect(pw, ox + 10, oy + 4, 12, 4, hair);
                fillRect(pw, ox + 8, oy + 8, 16, 4, hair);
                if (dir != 3) { // not facing up
                    fillRect(pw, ox + 8, oy + 12, 3, 4, hair);
                    fillRect(pw, ox + 21, oy + 12, 3, 4, hair);
                }

                // Face
                fillRect(pw, ox + 11, oy + 12, 10, 8, skin);

                // Eyes (direction-dependent)
                if (dir == 0) { // down
                    fillRect(pw, ox + 13, oy + 14, 2, 3, eyes);
                    fillRect(pw, ox + 17, oy + 14, 2, 3, eyes);
                    fillRect(pw, ox + 12, oy + 17, 2, 2, blush);
                    fillRect(pw, ox + 18, oy + 17, 2, 2, blush);
                } else if (dir == 1) { // left
                    fillRect(pw, ox + 12, oy + 14, 2, 3, eyes);
                    fillRect(pw, ox + 16, oy + 14, 2, 3, eyes);
                } else if (dir == 2) { // right
                    fillRect(pw, ox + 14, oy + 14, 2, 3, eyes);
                    fillRect(pw, ox + 18, oy + 14, 2, 3, eyes);
                } else { // up - no eyes
                    fillRect(pw, ox + 8, oy + 8, 16, 6, hair);
                }

                // Ribbon for girl
                if (isGirl && dir != 3) {
                    fillRect(pw, ox + 20, oy + 6, 4, 3, ribbon);
                }

                // Body
                fillRect(pw, ox + 10, oy + 20, 12, 4, shirt);

                // Arms
                fillRect(pw, ox + 8, oy + 20, 2, 4, skin);
                fillRect(pw, ox + 22, oy + 20, 2, 4, skin);

                // Legs with walk animation
                int legOffset = walk;
                fillRect(pw, ox + 11 + legOffset, oy + 24, 4, 4, pants);
                fillRect(pw, ox + 17 - legOffset, oy + 24, 4, 4, pants);

                // Shoes
                fillRect(pw, ox + 11 + legOffset, oy + 28, 4, 2, shoes);
                fillRect(pw, ox + 17 - legOffset, oy + 28, 4, 2, shoes);
            }
        }
        return sheet;
    }

    // ======================== NPC ========================
    public Image generateNPCSheet(String type) {
        int cols = 2, rows = 1, size = 32;
        WritableImage sheet = new WritableImage(cols * size, rows * size);
        PixelWriter pw = sheet.getPixelWriter();

        switch (type) {
            case "grandma" -> drawGrandma(pw, 0, 0, size);
            case "drink_seller" -> drawDrinkSeller(pw, 0, 0, size);
            case "fisher_kid" -> drawFisherKid(pw, 0, 0, size);
            case "gardener" -> drawGardener(pw, 0, 0, size);
        }
        // Frame 2: slight variation (blink/idle)
        switch (type) {
            case "grandma" -> drawGrandmaIdle(pw, size, 0, size);
            case "drink_seller" -> drawDrinkSellerIdle(pw, size, 0, size);
            case "fisher_kid" -> drawFisherKidIdle(pw, size, 0, size);
            case "gardener" -> drawGardenerIdle(pw, size, 0, size);
        }
        return sheet;
    }

    private void drawGrandma(PixelWriter pw, int ox, int oy, int s) {
        Color hair = Color.web("#C0C0C0");
        Color skin = Color.web("#F5DEB3");
        Color dress = Color.web("#8B7355");
        Color eyes = Color.web("#4a3520");
        Color apron = Color.web("#90EE90");

        fillRect(pw, ox + 10, oy + 4, 12, 4, hair);
        fillRect(pw, ox + 8, oy + 8, 16, 4, hair);
        fillRect(pw, ox + 11, oy + 12, 10, 8, skin);
        fillRect(pw, ox + 13, oy + 14, 2, 2, eyes);
        fillRect(pw, ox + 17, oy + 14, 2, 2, eyes);
        fillRect(pw, ox + 14, oy + 18, 4, 1, Color.web("#c08070"));
        fillRect(pw, ox + 10, oy + 20, 12, 6, dress);
        fillRect(pw, ox + 12, oy + 21, 8, 4, apron);
        fillRect(pw, ox + 11, oy + 26, 4, 4, dress);
        fillRect(pw, ox + 17, oy + 26, 4, 4, dress);
        fillRect(pw, ox + 11, oy + 29, 4, 2, Color.web("#6B4226"));
        fillRect(pw, ox + 17, oy + 29, 4, 2, Color.web("#6B4226"));
    }

    private void drawGrandmaIdle(PixelWriter pw, int ox, int oy, int s) {
        drawGrandma(pw, ox, oy, s);
        // Blink: close eyes
        fillRect(pw, ox + 13, oy + 14, 2, 1, Color.web("#4a3520"));
        fillRect(pw, ox + 17, oy + 14, 2, 1, Color.web("#4a3520"));
    }

    private void drawDrinkSeller(PixelWriter pw, int ox, int oy, int s) {
        Color hair = Color.web("#1a1a1a");
        Color skin = Color.web("#DEB887");
        Color shirt = Color.web("#FF6347");
        Color apron = Color.web("#FFFFFF");
        Color eyes = Color.web("#2c1810");
        Color hat = Color.web("#FF6347");

        fillRect(pw, ox + 9, oy + 2, 14, 3, hat);
        fillRect(pw, ox + 10, oy + 5, 12, 3, hair);
        fillRect(pw, ox + 11, oy + 8, 10, 8, skin);
        fillRect(pw, ox + 13, oy + 10, 2, 3, eyes);
        fillRect(pw, ox + 17, oy + 10, 2, 3, eyes);
        fillRect(pw, ox + 14, oy + 14, 4, 1, Color.web("#c08070"));
        // Mustache
        fillRect(pw, ox + 13, oy + 12, 6, 1, Color.web("#1a1a1a"));
        fillRect(pw, ox + 10, oy + 16, 12, 6, shirt);
        fillRect(pw, ox + 12, oy + 17, 8, 4, apron);
        fillRect(pw, ox + 8, oy + 16, 2, 5, skin);
        fillRect(pw, ox + 22, oy + 16, 2, 5, skin);
        fillRect(pw, ox + 11, oy + 22, 4, 6, Color.web("#4a4a4a"));
        fillRect(pw, ox + 17, oy + 22, 4, 6, Color.web("#4a4a4a"));
        fillRect(pw, ox + 11, oy + 27, 4, 2, Color.web("#2c2c2c"));
        fillRect(pw, ox + 17, oy + 27, 4, 2, Color.web("#2c2c2c"));
    }

    private void drawDrinkSellerIdle(PixelWriter pw, int ox, int oy, int s) {
        drawDrinkSeller(pw, ox, oy, s);
    }

    private void drawFisherKid(PixelWriter pw, int ox, int oy, int s) {
        Color hair = Color.web("#D2691E");
        Color skin = Color.web("#FFDAB9");
        Color shirt = Color.web("#FFD700");
        Color shorts = Color.web("#4169E1");
        Color eyes = Color.web("#2c1810");
        Color hat = Color.web("#F0E68C");

        fillRect(pw, ox + 8, oy + 3, 16, 3, hat);
        fillRect(pw, ox + 10, oy + 6, 12, 3, hair);
        fillRect(pw, ox + 11, oy + 9, 10, 8, skin);
        fillRect(pw, ox + 13, oy + 11, 2, 3, eyes);
        fillRect(pw, ox + 17, oy + 11, 2, 3, eyes);
        // Freckles
        fillRect(pw, ox + 12, oy + 14, 1, 1, Color.web("#DEB887"));
        fillRect(pw, ox + 19, oy + 14, 1, 1, Color.web("#DEB887"));
        fillRect(pw, ox + 14, oy + 15, 4, 1, Color.web("#e07060"));
        fillRect(pw, ox + 10, oy + 17, 12, 5, shirt);
        fillRect(pw, ox + 8, oy + 17, 2, 4, skin);
        fillRect(pw, ox + 22, oy + 17, 2, 4, skin);
        fillRect(pw, ox + 11, oy + 22, 10, 4, shorts);
        fillRect(pw, ox + 11, oy + 26, 4, 3, skin);
        fillRect(pw, ox + 17, oy + 26, 4, 3, skin);
        fillRect(pw, ox + 11, oy + 28, 4, 2, Color.web("#8B4513"));
        fillRect(pw, ox + 17, oy + 28, 4, 2, Color.web("#8B4513"));
    }

    private void drawFisherKidIdle(PixelWriter pw, int ox, int oy, int s) {
        drawFisherKid(pw, ox, oy, s);
    }

    private void drawGardener(PixelWriter pw, int ox, int oy, int s) {
        Color hat = Color.web("#D2B48C");
        Color hatDark = Color.web("#A9824F");
        Color hair = Color.web("#5A3A22");
        Color skin = Color.web("#DEB887");
        Color shirt = Color.web("#3CB371");
        Color overalls = Color.web("#4169A1");
        Color eyes = Color.web("#2c1810");
        Color boots = Color.web("#6B4226");

        // Straw hat
        fillRect(pw, ox+7, oy+3, 18, 3, hat);
        fillRect(pw, ox+10, oy+1, 12, 4, hat);
        fillRect(pw, ox+8, oy+5, 16, 1, hatDark);

        // Head
        fillRect(pw, ox+10, oy+7, 12, 4, hair);
        fillRect(pw, ox+11, oy+10, 10, 8, skin);
        fillRect(pw, ox+13, oy+12, 2, 3, eyes);
        fillRect(pw, ox+17, oy+12, 2, 3, eyes);
        fillRect(pw, ox+14, oy+16, 4, 1, Color.web("#8B4513"));

        // Body
        fillRect(pw, ox+9, oy+18, 14, 6, shirt);
        fillRect(pw, ox+12, oy+18, 8, 9, overalls);
        fillRect(pw, ox+9, oy+19, 3, 5, skin);
        fillRect(pw, ox+22, oy+19, 3, 5, skin);

        // Small trowel
        fillRect(pw, ox+24, oy+18, 1, 8, Color.web("#8B6914"));
        fillRect(pw, ox+23, oy+25, 3, 3, Color.web("#C0C0C0"));

        // Legs and boots
        fillRect(pw, ox+11, oy+26, 4, 3, overalls);
        fillRect(pw, ox+17, oy+26, 4, 3, overalls);
        fillRect(pw, ox+10, oy+29, 5, 2, boots);
        fillRect(pw, ox+17, oy+29, 5, 2, boots);
    }

    private void drawGardenerIdle(PixelWriter pw, int ox, int oy, int s) {
        drawGardener(pw, ox, oy, s);
        fillRect(pw, ox+13, oy+12, 2, 1, Color.web("#2c1810"));
        fillRect(pw, ox+17, oy+12, 2, 1, Color.web("#2c1810"));
    }

    // ======================== CAT ========================
    public Image generateCatSheet() {
        // 4 cols x 2 rows (idle, walk)
        int cols = 4, rows = 2, size = 32;
        WritableImage sheet = new WritableImage(cols * size, rows * size);
        PixelWriter pw = sheet.getPixelWriter();

        Color body = Color.web("#FFB347");
        Color dark = Color.web("#E8962E");
        Color belly = Color.web("#FFF8DC");
        Color eyes = Color.web("#2c1810");
        Color nose = Color.web("#FF69B4");

        for (int frame = 0; frame < 4; frame++) {
            for (int row = 0; row < 2; row++) {
                int ox = frame * size;
                int oy = row * size;
                int tailWag = (frame % 2 == 0) ? 0 : 1;
                int legMove = (row == 1 && frame % 2 != 0) ? 1 : 0;

                // Ears
                fillRect(pw, ox + 10, oy + 10, 3, 3, body);
                fillRect(pw, ox + 19, oy + 10, 3, 3, body);
                fillRect(pw, ox + 11, oy + 11, 1, 1, Color.web("#FFB6C1"));
                fillRect(pw, ox + 20, oy + 11, 1, 1, Color.web("#FFB6C1"));

                // Head
                fillRect(pw, ox + 10, oy + 13, 12, 8, body);

                // Eyes
                fillRect(pw, ox + 12, oy + 15, 2, 2, eyes);
                fillRect(pw, ox + 18, oy + 15, 2, 2, eyes);
                fillRect(pw, ox + 13, oy + 15, 1, 1, Color.WHITE);
                fillRect(pw, ox + 19, oy + 15, 1, 1, Color.WHITE);

                // Nose & mouth
                fillRect(pw, ox + 15, oy + 17, 2, 1, nose);
                fillRect(pw, ox + 14, oy + 18, 1, 1, dark);
                fillRect(pw, ox + 17, oy + 18, 1, 1, dark);

                // Body
                fillRect(pw, ox + 10, oy + 21, 12, 5, body);
                fillRect(pw, ox + 12, oy + 22, 8, 3, belly);

                // Legs
                fillRect(pw, ox + 10 - legMove, oy + 26, 3, 3, body);
                fillRect(pw, ox + 19 + legMove, oy + 26, 3, 3, body);

                // Tail
                fillRect(pw, ox + 22, oy + 20 + tailWag, 3, 2, body);
                fillRect(pw, ox + 24, oy + 18 + tailWag, 2, 3, body);
            }
        }
        return sheet;
    }

    // ======================== TILESET ========================
    public Image generateTileSet() {
        // 8 cols x 4 rows = 32 tiles max
        int cols = 8, rows = 4, size = 32;
        WritableImage sheet = new WritableImage(cols * size, rows * size);
        PixelWriter pw = sheet.getPixelWriter();

        // Tile 0: Grass
        drawGrassTile(pw, 0, 0, size);
        // Tile 1: Water
        drawWaterTile(pw, size, 0, size);
        // Tile 2: Path
        drawPathTile(pw, 2 * size, 0, size);
        // Tile 3: Tree trunk
        drawTreeTile(pw, 3 * size, 0, size);
        // Tile 4: Flower
        drawFlowerTile(pw, 4 * size, 0, size);
        // Tile 5: Bench
        drawBenchTile(pw, 5 * size, 0, size);
        // Tile 6: Fence
        drawFenceTile(pw, 6 * size, 0, size);
        // Tile 7: Bridge
        drawBridgeTile(pw, 7 * size, 0, size);
        // Tile 8: Tree top (row 1, col 0) — decoration layer
        drawTreeTopTile(pw, 0, size, size);
        // Tile 9: Dark grass
        drawDarkGrassTile(pw, size, size, size);
        // Tile 10: Water edge
        drawWaterEdgeTile(pw, 2 * size, size, size);

        return sheet;
    }

    private void drawGrassTile(PixelWriter pw, int ox, int oy, int s) {
        Color base = Color.web("#7EC850");
        Color light = Color.web("#8FD860");
        Color dark = Color.web("#6AB040");
        fillRect(pw, ox, oy, s, s, base);
        // Grass detail
        fillRect(pw, ox + 4, oy + 6, 2, 2, light);
        fillRect(pw, ox + 20, oy + 14, 2, 2, light);
        fillRect(pw, ox + 12, oy + 24, 2, 2, dark);
        fillRect(pw, ox + 26, oy + 4, 2, 2, dark);
        fillRect(pw, ox + 8, oy + 18, 1, 2, light);
    }

    private void drawWaterTile(PixelWriter pw, int ox, int oy, int s) {
        Color base = Color.web("#4A90D9");
        Color light = Color.web("#6BB5FF");
        Color dark = Color.web("#3A7AC0");
        fillRect(pw, ox, oy, s, s, base);
        fillRect(pw, ox + 4, oy + 8, 6, 2, light);
        fillRect(pw, ox + 18, oy + 20, 8, 2, light);
        fillRect(pw, ox + 10, oy + 4, 4, 1, dark);
        fillRect(pw, ox + 22, oy + 14, 4, 1, dark);
    }

    private void drawPathTile(PixelWriter pw, int ox, int oy, int s) {
        Color base = Color.web("#D2B48C");
        Color light = Color.web("#DEC49E");
        Color dark = Color.web("#C0A070");
        fillRect(pw, ox, oy, s, s, base);
        fillRect(pw, ox + 6, oy + 10, 3, 2, light);
        fillRect(pw, ox + 20, oy + 22, 4, 2, dark);
        fillRect(pw, ox + 14, oy + 6, 2, 2, light);
    }

    private void drawTreeTile(PixelWriter pw, int ox, int oy, int s) {
        Color grass = Color.web("#7EC850");
        Color trunk = Color.web("#8B6914");
        Color trunkDark = Color.web("#6B4F10");
        fillRect(pw, ox, oy, s, s, grass);
        fillRect(pw, ox + 12, oy + 8, 8, 24, trunk);
        fillRect(pw, ox + 12, oy + 8, 2, 24, trunkDark);
        // Leaves base
        Color leaves = Color.web("#2E8B57");
        Color leafLight = Color.web("#3CB371");
        fillRect(pw, ox + 4, oy + 0, 24, 16, leaves);
        fillRect(pw, ox + 8, oy + 0, 16, 4, leafLight);
        fillRect(pw, ox + 6, oy + 4, 6, 6, leafLight);
    }

    private void drawTreeTopTile(PixelWriter pw, int ox, int oy, int s) {
        // Transparent background with just leaves
        Color leaves = Color.web("#2E8B57");
        Color leafLight = Color.web("#3CB371");
        fillRect(pw, ox + 2, oy + 4, 28, 20, leaves);
        fillRect(pw, ox + 6, oy + 2, 20, 6, leafLight);
        fillRect(pw, ox + 10, oy + 0, 12, 4, leaves);
        fillRect(pw, ox + 4, oy + 10, 8, 6, leafLight);
        fillRect(pw, ox + 20, oy + 8, 6, 8, leafLight);
    }

    private void drawFlowerTile(PixelWriter pw, int ox, int oy, int s) {
        Color grass = Color.web("#7EC850");
        fillRect(pw, ox, oy, s, s, grass);
        // Flowers
        Color[] flowerColors = { Color.web("#FF6B8A"), Color.web("#FFD700"), Color.web("#DA70D6") };
        Color stem = Color.web("#228B22");
        int[][] positions = { { 8, 10 }, { 20, 8 }, { 14, 22 }, { 24, 18 } };
        for (int i = 0; i < positions.length; i++) {
            int fx = ox + positions[i][0];
            int fy = oy + positions[i][1];
            fillRect(pw, fx, fy + 4, 1, 4, stem);
            fillRect(pw, fx - 1, fy, 3, 3, flowerColors[i % flowerColors.length]);
            fillRect(pw, fx, fy + 1, 1, 1, Color.web("#FFD700"));
        }
    }

    private void drawBenchTile(PixelWriter pw, int ox, int oy, int s) {
        Color grass = Color.web("#7EC850");
        Color wood = Color.web("#CD853F");
        Color woodDark = Color.web("#A0682D");
        Color metal = Color.web("#808080");
        fillRect(pw, ox, oy, s, s, grass);
        // Bench seat
        fillRect(pw, ox + 4, oy + 14, 24, 4, wood);
        fillRect(pw, ox + 4, oy + 14, 24, 1, woodDark);
        // Bench back
        fillRect(pw, ox + 4, oy + 8, 24, 3, wood);
        fillRect(pw, ox + 4, oy + 11, 24, 1, woodDark);
        // Legs
        fillRect(pw, ox + 6, oy + 18, 2, 8, metal);
        fillRect(pw, ox + 24, oy + 18, 2, 8, metal);
    }

    private void drawFenceTile(PixelWriter pw, int ox, int oy, int s) {
        Color grass = Color.web("#7EC850");
        Color wood = Color.web("#DEB887");
        Color woodDark = Color.web("#C4A06A");
        fillRect(pw, ox, oy, s, s, grass);
        // Horizontal bars
        fillRect(pw, ox, oy + 10, s, 3, wood);
        fillRect(pw, ox, oy + 20, s, 3, wood);
        // Vertical posts
        fillRect(pw, ox + 4, oy + 6, 3, 22, woodDark);
        fillRect(pw, ox + 25, oy + 6, 3, 22, woodDark);
        // Post tops
        fillRect(pw, ox + 3, oy + 4, 5, 3, wood);
        fillRect(pw, ox + 24, oy + 4, 5, 3, wood);
    }

    private void drawBridgeTile(PixelWriter pw, int ox, int oy, int s) {
        Color water = Color.web("#4A90D9");
        Color plank = Color.web("#CD853F");
        Color plankDark = Color.web("#A0682D");
        Color rail = Color.web("#DEB887");
        fillRect(pw, ox, oy, s, s, water);
        // Planks
        fillRect(pw, ox + 2, oy + 4, 28, 24, plank);
        // Plank lines
        for (int i = 0; i < 4; i++) {
            fillRect(pw, ox + 2, oy + 4 + i * 7, 28, 1, plankDark);
        }
        // Rails
        fillRect(pw, ox + 2, oy + 4, 2, 24, rail);
        fillRect(pw, ox + 28, oy + 4, 2, 24, rail);
    }

    private void drawDarkGrassTile(PixelWriter pw, int ox, int oy, int s) {
        Color base = Color.web("#5A9A38");
        Color dark = Color.web("#4A8A28");
        fillRect(pw, ox, oy, s, s, base);
        fillRect(pw, ox + 6, oy + 8, 3, 3, dark);
        fillRect(pw, ox + 22, oy + 20, 3, 3, dark);
    }

    private void drawWaterEdgeTile(PixelWriter pw, int ox, int oy, int s) {
        Color grass = Color.web("#7EC850");
        Color water = Color.web("#4A90D9");
        fillRect(pw, ox, oy, s, s / 2, grass);
        fillRect(pw, ox, oy + s / 2, s, s / 2, water);
        // Edge blend
        Color edge = Color.web("#5BA548");
        fillRect(pw, ox, oy + s / 2 - 2, s, 4, edge);
    }

    // ======================== ITEMS ========================
    public Image generateFishingRodActionSheet() {
        int cols = 1, rows = 4, size = 32;
        WritableImage sheet = new WritableImage(cols * size, rows * size);
        PixelWriter pw = sheet.getPixelWriter();

        Color rod = Color.web("#CD853F");
        Color rodDark = Color.web("#8B6914");
        Color line = Color.web("#E0E0E0");

        // 0: Down (facing user)
        int oy = 0 * size;
        fillRect(pw, 10, oy + 18, 2, 14, rod); // rod sticking down
        fillRect(pw, 11, oy + 18, 1, 14, rodDark);
        fillRect(pw, 9, oy + 16, 4, 6, Color.web("#4a3520")); // handle
        fillRect(pw, 10, oy + 31, 1, 1, line); // line at tip

        // 1: Left
        oy = 1 * size;
        fillRect(pw, 0, oy + 20, 16, 2, rod); // rod from x=0 to 16
        fillRect(pw, 0, oy + 21, 16, 1, rodDark);
        fillRect(pw, 12, oy + 19, 6, 4, Color.web("#4a3520")); // handle
        fillRect(pw, 0, oy + 22, 1, 10, line); // line drops

        // 2: Right
        oy = 2 * size;
        fillRect(pw, 16, oy + 20, 16, 2, rod); // rod from x=16 to 32
        fillRect(pw, 16, oy + 21, 16, 1, rodDark);
        fillRect(pw, 14, oy + 19, 6, 4, Color.web("#4a3520")); // handle
        fillRect(pw, 31, oy + 22, 1, 10, line); // line drops

        // 3: Up (facing away)
        oy = 3 * size;
        fillRect(pw, 20, oy + 4, 2, 16, rod); // rod sticking up
        fillRect(pw, 21, oy + 4, 1, 16, rodDark);
        fillRect(pw, 19, oy + 16, 4, 6, Color.web("#4a3520")); // handle
        fillRect(pw, 21, oy + 0, 1, 4, line); // line extending up

        return sheet;
    }

    public Image generateItemSprite(String type) {
        int size = 32;
        WritableImage img = new WritableImage(size, size);
        PixelWriter pw = img.getPixelWriter();

        if ("fishing_rod".equals(type)) {
            Color rod = Color.web("#CD853F");
            Color rodDark = Color.web("#8B6914");
            Color line = Color.web("#C0C0C0");
            Color hook = Color.web("#A0A0A0");
            Color sparkle = Color.web("#FFD700");

            // Rod
            fillRect(pw, 8, 4, 3, 20, rod);
            fillRect(pw, 8, 4, 1, 20, rodDark);
            // Handle
            fillRect(pw, 7, 20, 5, 6, Color.web("#4a3520"));
            // Line
            fillRect(pw, 10, 4, 1, 1, line);
            fillRect(pw, 11, 5, 1, 1, line);
            fillRect(pw, 12, 6, 1, 3, line);
            fillRect(pw, 13, 9, 1, 4, line);
            // Hook
            fillRect(pw, 13, 13, 3, 1, hook);
            fillRect(pw, 15, 13, 1, 3, hook);
            fillRect(pw, 14, 15, 1, 1, hook);
            // Sparkle
            fillRect(pw, 20, 4, 2, 2, sparkle);
            fillRect(pw, 24, 8, 2, 2, sparkle);
            fillRect(pw, 18, 10, 1, 1, sparkle);
        } else if ("seeds".equals(type)) {
            Color bag = Color.web("#D2B48C");
            Color bagDark = Color.web("#A9824F");
            Color seed = Color.web("#6B4226");
            Color leaf = Color.web("#3CB371");
            Color sparkle = Color.web("#FFD700");

            // Seed pouch
            fillRect(pw, 9, 10, 14, 16, bag);
            fillRect(pw, 8, 14, 16, 11, bag);
            fillRect(pw, 10, 10, 12, 2, bagDark);
            fillRect(pw, 11, 12, 10, 1, bagDark);
            fillRect(pw, 9, 24, 14, 2, bagDark);

            // Seeds spilling out
            fillRect(pw, 13, 14, 2, 2, seed);
            fillRect(pw, 18, 15, 2, 2, seed);
            fillRect(pw, 15, 19, 2, 2, seed);
            fillRect(pw, 21, 23, 2, 2, seed);

            // Tiny sprout icon on the pouch
            fillRect(pw, 15, 8, 2, 5, leaf);
            fillRect(pw, 12, 9, 4, 2, leaf);
            fillRect(pw, 17, 9, 4, 2, leaf);

            // Sparkle
            fillRect(pw, 5, 8, 2, 2, sparkle);
            fillRect(pw, 25, 7, 2, 2, sparkle);
        } else if ("rose".equals(type)) {
            drawRewardFlower(pw, Color.web("#E84A5F"), Color.web("#B71C3A"), Color.web("#2E8B57"));
        } else if ("sunflower".equals(type)) {
            drawRewardFlower(pw, Color.web("#FFD34D"), Color.web("#8B5A2B"), Color.web("#3CB371"));
        } else if ("tulip".equals(type)) {
            drawRewardFlower(pw, Color.web("#DA70D6"), Color.web("#9B2FAE"), Color.web("#2E8B57"));
        } else if ("bonsai".equals(type)) {
            drawRewardBonsai(pw);
        } else if ("fish_carp".equals(type)) {
            drawFish(pw, Color.web("#E85D4F"), Color.web("#FFB347"), Color.web("#8B2F2F"), false);
        } else if ("fish_perch".equals(type)) {
            drawFish(pw, Color.web("#8CC152"), Color.web("#DDEB75"), Color.web("#4A7C2E"), false);
        } else if ("fish_catfish".equals(type)) {
            drawFish(pw, Color.web("#6E8898"), Color.web("#B8C7D1"), Color.web("#394B59"), true);
        } else if ("fish_goldfish".equals(type)) {
            drawFish(pw, Color.web("#FFA000"), Color.web("#FFD54F"), Color.web("#E65100"), false);
        }
        return img;
    }

    private void drawRewardFlower(PixelWriter pw, Color petal, Color center, Color leaf) {
        Color pot = Color.web("#CD853F");
        Color potDark = Color.web("#8B5A2B");
        Color stem = Color.web("#228B22");
        Color soil = Color.web("#4A2C17");

        // Pot
        fillRect(pw, 10, 22, 12, 6, pot);
        fillRect(pw, 9, 20, 14, 3, pot);
        fillRect(pw, 11, 27, 10, 2, potDark);
        fillRect(pw, 11, 21, 10, 1, soil);

        // Stem and leaves
        fillRect(pw, 15, 11, 2, 10, stem);
        fillRect(pw, 11, 15, 5, 2, leaf);
        fillRect(pw, 17, 17, 5, 2, leaf);

        // Flower head
        fillRect(pw, 13, 7, 6, 2, petal);
        fillRect(pw, 11, 9, 10, 4, petal);
        fillRect(pw, 13, 13, 6, 2, petal);
        fillRect(pw, 15, 10, 2, 2, center);

        // Sparkle
        fillRect(pw, 5, 7, 2, 2, Color.web("#FFD700"));
        fillRect(pw, 25, 10, 2, 2, Color.web("#FFD700"));
    }

    private void drawRewardBonsai(PixelWriter pw) {
        Color pot = Color.web("#A0522D");
        Color potDark = Color.web("#6B3417");
        Color trunk = Color.web("#8B5A2B");
        Color leaves = Color.web("#2E8B57");
        Color leafLight = Color.web("#3CB371");

        // Pot
        fillRect(pw, 9, 22, 14, 6, pot);
        fillRect(pw, 8, 20, 16, 3, pot);
        fillRect(pw, 10, 27, 12, 2, potDark);

        // Trunk
        fillRect(pw, 15, 12, 3, 10, trunk);
        fillRect(pw, 13, 15, 5, 2, trunk);
        fillRect(pw, 17, 13, 4, 2, trunk);

        // Leaves
        fillRect(pw, 8, 8, 11, 6, leaves);
        fillRect(pw, 15, 6, 10, 6, leaves);
        fillRect(pw, 18, 12, 8, 5, leaves);
        fillRect(pw, 11, 8, 5, 2, leafLight);
        fillRect(pw, 18, 6, 5, 2, leafLight);
        fillRect(pw, 20, 13, 4, 2, leafLight);
    }

    private void drawFish(PixelWriter pw, Color body, Color belly, Color dark, boolean whiskers) {
        Color eye = Color.web("#1A1A1A");
        Color shine = Color.WHITE;

        // Tail
        fillRect(pw, 4, 12, 4, 3, dark);
        fillRect(pw, 4, 17, 4, 3, dark);
        fillRect(pw, 7, 14, 4, 4, body);

        // Body silhouette
        fillRect(pw, 10, 10, 13, 2, body);
        fillRect(pw, 8, 12, 18, 8, body);
        fillRect(pw, 10, 20, 13, 2, body);
        fillRect(pw, 13, 15, 11, 4, belly);

        // Fins and details
        fillRect(pw, 14, 8, 5, 3, dark);
        fillRect(pw, 15, 22, 5, 3, dark);
        fillRect(pw, 12, 13, 1, 6, dark);
        fillRect(pw, 17, 13, 1, 6, dark);

        // Head and eye
        fillRect(pw, 24, 13, 3, 6, body);
        fillRect(pw, 23, 12, 2, 8, body);
        fillRect(pw, 24, 14, 2, 2, eye);
        fillRect(pw, 25, 14, 1, 1, shine);

        if (whiskers) {
            fillRect(pw, 26, 17, 4, 1, dark);
            fillRect(pw, 26, 19, 4, 1, dark);
            fillRect(pw, 28, 16, 3, 1, dark);
        }

        // Sparkle
        fillRect(pw, 5, 7, 2, 2, Color.web("#FFFFFF", 0.8));
        fillRect(pw, 26, 24, 2, 2, Color.web("#FFFFFF", 0.65));
    }

    // ======================== UI ========================
    public Image generateInteractIcon() {
        int size = 32;
        WritableImage img = new WritableImage(size, size);
        PixelWriter pw = img.getPixelWriter();

        // "!" exclamation mark
        Color bg = Color.web("#FFD700");
        Color text = Color.web("#4a3520");

        // Circle background
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - size / 2.0;
                double dy = y - size / 2.0;
                if (dx * dx + dy * dy < 12 * 12) {
                    pw.setColor(x, y, bg);
                }
            }
        }
        // "!" symbol
        fillRect(pw, 14, 6, 4, 12, text);
        fillRect(pw, 14, 21, 4, 4, text);

        return img;
    }
}
