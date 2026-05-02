package com.game.world;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * GrassTile — Xử lý logic Autotiling cho tile cỏ.
 * Tạo viền chìm (bevel/shadow) ở biên và viền bo tròn ở góc.
 */
public class GrassTile {

    private static final int TILE_SIZE = 32;
    private static final Image[] grassCache = new Image[16];
    private static final Image[] darkGrassCache = new Image[16];
    
    private static boolean isInitialized = false;

    public static void init() {
        if (isInitialized) return;
        
        for (int i = 0; i < 16; i++) {
            grassCache[i] = generateGrassImage(i, false);
            darkGrassCache[i] = generateGrassImage(i, true);
        }
        
        isInitialized = true;
    }

    /**
     * Render tile cỏ với autotiling dựa trên các tile xung quanh.
     */
    public static void render(GraphicsContext gc, int[][] groundLayer, int c, int r, double x, double y) {
        if (!isInitialized) {
            init();
        }

        int tileId = groundLayer[r][c];
        boolean isDark = (tileId == Tile.DARK_GRASS.getId());

        int mask = getTileMask(groundLayer, c, r);
        Image tileImg = isDark ? darkGrassCache[mask] : grassCache[mask];

        if (tileImg != null) {
            gc.drawImage(tileImg, x, y, TILE_SIZE, TILE_SIZE);
        }
    }

    /**
     * Tính bitmask dựa trên 4 lân cận. 
     * Nếu tile liền kề cũng là GRASS hoặc DARK_GRASS thì bật bit tương ứng.
     * bit 0 (1): UP
     * bit 1 (2): RIGHT
     * bit 2 (4): DOWN
     * bit 3 (8): LEFT
     */
    private static int getTileMask(int[][] layer, int c, int r) {
        int mask = 0;
        int rows = layer.length;
        int cols = layer[0].length;

        if (isWater(layer, c, r - 1, rows, cols)) mask |= 1; // UP
        if (isWater(layer, c + 1, r, rows, cols)) mask |= 2; // RIGHT
        if (isWater(layer, c, r + 1, rows, cols)) mask |= 4; // DOWN
        if (isWater(layer, c - 1, r, rows, cols)) mask |= 8; // LEFT

        return mask;
    }

    private static boolean isWater(int[][] layer, int c, int r, int rows, int cols) {
        if (c < 0 || c >= cols || r < 0 || r >= rows) {
            return true; // Out of bounds coi như là nước
        }
        int id = layer[r][c];
        return id == Tile.WATER.getId() || id == Tile.WATER_EDGE.getId();
    }

    /**
     * Sinh ra texture cho 1 mask cụ thể.
     */
    private static Image generateGrassImage(int mask, boolean isDark) {
        WritableImage img = new WritableImage(TILE_SIZE, TILE_SIZE);
        PixelWriter pw = img.getPixelWriter();

        // Base colors
        Color base = isDark ? Color.web("#5A9A38") : Color.web("#7EC850");
        Color light = isDark ? Color.web("#6AB048") : Color.web("#8FD860");
        Color dark = isDark ? Color.web("#4A8A28") : Color.web("#6AB040");
        
        // Autotile border colors
        Color outline = Color.web("#4A8A28"); // Dark green outline
        Color shadow = Color.web("#5A9A38"); // Dark shadow

        // 1. Fill base grass color
        for (int x = 0; x < TILE_SIZE; x++) {
            for (int y = 0; y < TILE_SIZE; y++) {
                pw.setColor(x, y, base);
            }
        }

        // 2. Add some grass details
        pw.setColor(4, 6, light);
        pw.setColor(5, 6, light);
        pw.setColor(4, 7, light);
        pw.setColor(5, 7, light);
        
        pw.setColor(20, 14, light);
        pw.setColor(21, 14, light);
        pw.setColor(20, 15, light);
        pw.setColor(21, 15, light);
        
        pw.setColor(12, 24, dark);
        pw.setColor(13, 24, dark);
        pw.setColor(12, 25, dark);
        pw.setColor(13, 25, dark);
        
        pw.setColor(26, 4, dark);
        pw.setColor(27, 4, dark);
        pw.setColor(26, 5, dark);
        pw.setColor(27, 5, dark);
        
        pw.setColor(8, 18, light);
        pw.setColor(8, 19, light);

        boolean upWater = (mask & 1) != 0;
        boolean rightWater = (mask & 2) != 0;
        boolean downWater = (mask & 4) != 0;
        boolean leftWater = (mask & 8) != 0;

        // 3. Draw Borders (Shadow only)
        if (upWater) {
            for (int x = 0; x < TILE_SIZE; x++) {
                pw.setColor(x, 0, outline);
                pw.setColor(x, 1, shadow);
                pw.setColor(x, 2, shadow);
            }
        }
        if (downWater) {
            for (int x = 0; x < TILE_SIZE; x++) {
                pw.setColor(x, TILE_SIZE - 1, outline);
                pw.setColor(x, TILE_SIZE - 2, shadow);
                pw.setColor(x, TILE_SIZE - 3, shadow);
            }
        }
        if (leftWater) {
            for (int y = 0; y < TILE_SIZE; y++) {
                pw.setColor(0, y, outline);
                pw.setColor(1, y, shadow);
                pw.setColor(2, y, shadow);
            }
        }
        if (rightWater) {
            for (int y = 0; y < TILE_SIZE; y++) {
                pw.setColor(TILE_SIZE - 1, y, outline);
                pw.setColor(TILE_SIZE - 2, y, shadow);
                pw.setColor(TILE_SIZE - 3, y, shadow);
            }
        }

        // 4. Draw Rounded Corners
        if (upWater && leftWater) {
            pw.setColor(0, 0, outline);
            pw.setColor(1, 0, outline);
            pw.setColor(0, 1, outline);
            pw.setColor(2, 0, outline);
            pw.setColor(0, 2, outline);
            pw.setColor(1, 1, outline);
            pw.setColor(2, 1, shadow);
            pw.setColor(1, 2, shadow);
        }
        if (upWater && rightWater) {
            pw.setColor(TILE_SIZE - 1, 0, outline);
            pw.setColor(TILE_SIZE - 2, 0, outline);
            pw.setColor(TILE_SIZE - 1, 1, outline);
            pw.setColor(TILE_SIZE - 3, 0, outline);
            pw.setColor(TILE_SIZE - 1, 2, outline);
            pw.setColor(TILE_SIZE - 2, 1, outline);
            pw.setColor(TILE_SIZE - 3, 1, shadow);
            pw.setColor(TILE_SIZE - 2, 2, shadow);
        }
        if (downWater && leftWater) {
            pw.setColor(0, TILE_SIZE - 1, outline);
            pw.setColor(1, TILE_SIZE - 1, outline);
            pw.setColor(0, TILE_SIZE - 2, outline);
            pw.setColor(2, TILE_SIZE - 1, outline);
            pw.setColor(0, TILE_SIZE - 3, outline);
            pw.setColor(1, TILE_SIZE - 2, outline);
            pw.setColor(2, TILE_SIZE - 2, shadow);
            pw.setColor(1, TILE_SIZE - 3, shadow);
        }
        if (downWater && rightWater) {
            pw.setColor(TILE_SIZE - 1, TILE_SIZE - 1, outline);
            pw.setColor(TILE_SIZE - 2, TILE_SIZE - 1, outline);
            pw.setColor(TILE_SIZE - 1, TILE_SIZE - 2, outline);
            pw.setColor(TILE_SIZE - 3, TILE_SIZE - 1, outline);
            pw.setColor(TILE_SIZE - 1, TILE_SIZE - 3, outline);
            pw.setColor(TILE_SIZE - 2, TILE_SIZE - 2, outline);
            pw.setColor(TILE_SIZE - 3, TILE_SIZE - 2, shadow);
            pw.setColor(TILE_SIZE - 2, TILE_SIZE - 3, shadow);
        }

        return img;
    }
}
