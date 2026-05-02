package com.game.world;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import com.game.util.Constants;
import com.game.util.SpriteSheet;

/**
 * GrassTile — Xử lý logic Autotiling cho tile cỏ.
 * Tạo viền chìm (bevel/shadow) ở biên và viền bo tròn ở góc.
 */
public class GrassTile {

    private static final int TILE_SIZE = 32;
    private static final int CARDINAL_MASK = 0x0F;
    private static final int LEFT_WATER = 1;
    private static final int DOWN_WATER = 2;
    private static final int RIGHT_WATER = 4;
    private static final int UP_WATER = 8;
    private static final int NORTH_WEST_WATER = 16;
    private static final int SOUTH_WEST_WATER = 32;
    private static final int SOUTH_EAST_WATER = 64;
    private static final int NORTH_EAST_WATER = 128;

    private static final int[][] OUTER_CORNER_CUT = {
        { 0, 0 }, { 1, 0 }, { 2, 0 },
        { 0, 1 }, { 1, 1 }, { 2, 1 },
        { 0, 2 }, { 1, 2 },
        { 0, 3 },
        { 3, 0 }, { 2, 2 }, { 1, 3 }
    };
    private static final int[][] OUTER_CORNER_OUTLINE = {
        { 4, 0 }, { 3, 1 }, { 2, 3 }, { 1, 4 }, { 0, 5 }
    };
    private static final int[][] OUTER_CORNER_SHADOW = {
        { 5, 0 }, { 4, 1 }, { 3, 2 }, { 2, 4 }, { 1, 5 }, { 0, 6 }
    };
    private static final int[][] INNER_CORNER_CUT = {
        { 0, 0 }, { 1, 0 }, { 2, 0 },
        { 0, 1 }, { 1, 1 },
        { 0, 2 },
        { 3, 0 }, { 2, 1 }, { 1, 2 }, { 0, 3 }
    };
    private static final int[][] INNER_CORNER_OUTLINE = {
        { 4, 0 }, { 3, 1 }, { 2, 2 }, { 1, 3 }, { 0, 4 }
    };
    private static final int[][] INNER_CORNER_SHADOW = {
        { 5, 0 }, { 4, 1 }, { 3, 2 }, { 2, 3 }, { 1, 4 }, { 0, 5 }
    };

    private static final Image[] grassBaseCache = new Image[16];
    private static final Image[] darkGrassBaseCache = new Image[16];
    private static final Image[] grassCache = new Image[256];
    private static final Image[] darkGrassCache = new Image[256];

    private static boolean isInitialized = false;

    private static final int[] MASK_TO_COL = new int[16];
    private static final int[] MASK_TO_ROW = new int[16];

    static {
        // Layout:
        // 1001 (9) 1000 (8) 1100 (12) 1101 (13)
        // 0001 (1) 0000 (0) 0100 (4) 0101 (5)
        // 0011 (3) 0010 (2) 0110 (6) 0111 (7)
        // 1011 (11) 1010 (10) 1110 (14) 1111 (15)
        int[][] layout = {
                { 9, 8, 12, 13 },
                { 1, 0, 4, 5 },
                { 3, 2, 6, 7 },
                { 11, 10, 14, 15 }
        };
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                int m = layout[r][c];
                MASK_TO_COL[m] = c;
                MASK_TO_ROW[m] = r;
            }
        }
    }

    public static void init() {
        if (isInitialized)
            return;

        SpriteSheet extendedGrass = loadExtendedSheet(Constants.ASSET_GRASS_AUTOTILE_EXTENDED);
        SpriteSheet extendedDarkGrass = loadExtendedSheet(Constants.ASSET_DARK_GRASS_AUTOTILE_EXTENDED);
        SpriteSheet externalGrass = loadExternalSheet(Constants.ASSET_GRASS_AUTOTILE);
        SpriteSheet externalDarkGrass = loadExternalSheet(Constants.ASSET_DARK_GRASS_AUTOTILE);

        for (int i = 0; i < 16; i++) {
            if (externalGrass != null) {
                grassBaseCache[i] = externalGrass.getFrame(MASK_TO_COL[i], MASK_TO_ROW[i]);
            } else {
                grassBaseCache[i] = generateGrassImage(i, false);
            }

            if (externalDarkGrass != null) {
                darkGrassBaseCache[i] = externalDarkGrass.getFrame(MASK_TO_COL[i], MASK_TO_ROW[i]);
            } else {
                darkGrassBaseCache[i] = generateGrassImage(i, true);
            }
        }

        for (int i = 0; i < 256; i++) {
            grassCache[i] = resolveTileVariant(extendedGrass, grassBaseCache, i, false);
            darkGrassCache[i] = resolveTileVariant(extendedDarkGrass, darkGrassBaseCache, i, true);
        }

        isInitialized = true;
    }

    private static SpriteSheet loadExternalSheet(String filename) {
        try {
            var resource = GrassTile.class.getResource(Constants.getAssetPath(filename));
            if (resource != null) {
                Image img = new Image(resource.toExternalForm());
                return new SpriteSheet(img, TILE_SIZE, TILE_SIZE);
            }
        } catch (Exception e) {
            System.out.println("Could not load external grass autotile: " + filename);
        }
        return null;
    }

    private static SpriteSheet loadExtendedSheet(String filename) {
        SpriteSheet sheet = loadExternalSheet(filename);
        if (sheet == null) {
            return null;
        }

        Image image = sheet.getSheet();
        int columns = (int) Math.round(image.getWidth() / TILE_SIZE);
        int rows = (int) Math.round(image.getHeight() / TILE_SIZE);
        if (columns < 16 || rows < 16) {
            System.out.println("Ignoring grass autotile sheet " + filename
                    + " because it must be at least 16x16 tiles (512x512 px).");
            return null;
        }

        return sheet;
    }

    private static Image resolveTileVariant(SpriteSheet extendedSheet, Image[] baseCache, int mask, boolean isDark) {
        if (extendedSheet != null) {
            int cardinalColumn = mask & CARDINAL_MASK;
            Image frame = extendedSheet.getFrame(cardinalColumn, 0);
            if (frame != null) {
                return frame;
            }
        }

        return baseCache[mask & CARDINAL_MASK];
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
    public static int getTileMask(int[][] layer, int c, int r) {
        int mask = 0;
        int rows = layer.length;
        int cols = layer[0].length;

        boolean leftWater = isTransitionTile(layer, c - 1, r, rows, cols);
        boolean downWater = isTransitionTile(layer, c, r + 1, rows, cols);
        boolean rightWater = isTransitionTile(layer, c + 1, r, rows, cols);
        boolean upWater = isTransitionTile(layer, c, r - 1, rows, cols);

        if (leftWater)
            mask |= LEFT_WATER;
        if (downWater)
            mask |= DOWN_WATER;
        if (rightWater)
            mask |= RIGHT_WATER;
        if (upWater)
            mask |= UP_WATER;

        if (!leftWater && !upWater && isTransitionTile(layer, c - 1, r - 1, rows, cols))
            mask |= NORTH_WEST_WATER;
        if (!leftWater && !downWater && isTransitionTile(layer, c - 1, r + 1, rows, cols))
            mask |= SOUTH_WEST_WATER;
        if (!rightWater && !downWater && isTransitionTile(layer, c + 1, r + 1, rows, cols))
            mask |= SOUTH_EAST_WATER;
        if (!rightWater && !upWater && isTransitionTile(layer, c + 1, r - 1, rows, cols))
            mask |= NORTH_EAST_WATER;

        return mask;
    }

    private static boolean isTransitionTile(int[][] layer, int c, int r, int rows, int cols) {
        if (c < 0 || c >= cols || r < 0 || r >= rows) {
            return true; // Out of bounds coi như là nước
        }
        int id = layer[r][c];
        return id == Tile.WATER.getId()
                || id == Tile.WATER_EDGE.getId()
                || id == Tile.PATH.getId();
    }

    private static Image applyRoundedCorners(Image baseImage, int mask, boolean isDark) {
        boolean hasOuterCorner = ((mask & UP_WATER) != 0 && (mask & LEFT_WATER) != 0)
                || ((mask & UP_WATER) != 0 && (mask & RIGHT_WATER) != 0)
                || ((mask & DOWN_WATER) != 0 && (mask & LEFT_WATER) != 0)
                || ((mask & DOWN_WATER) != 0 && (mask & RIGHT_WATER) != 0);
        boolean hasInnerCorner = (mask & ~CARDINAL_MASK) != 0;

        if (!hasOuterCorner && !hasInnerCorner) {
            return baseImage;
        }

        WritableImage composite = new WritableImage(TILE_SIZE, TILE_SIZE);
        PixelWriter writer = composite.getPixelWriter();
        var reader = baseImage.getPixelReader();

        for (int x = 0; x < TILE_SIZE; x++) {
            for (int y = 0; y < TILE_SIZE; y++) {
                writer.setColor(x, y, reader.getColor(x, y));
            }
        }

        Color outline = isDark ? Color.web("#4A8A28") : Color.web("#4A8A28");
        Color shadow = isDark ? Color.web("#5A9A38") : Color.web("#5A9A38");

        if ((mask & UP_WATER) != 0 && (mask & LEFT_WATER) != 0) {
            carveOuterCorner(writer, outline, shadow, Corner.NORTH_WEST);
        }
        if ((mask & UP_WATER) != 0 && (mask & RIGHT_WATER) != 0) {
            carveOuterCorner(writer, outline, shadow, Corner.NORTH_EAST);
        }
        if ((mask & DOWN_WATER) != 0 && (mask & LEFT_WATER) != 0) {
            carveOuterCorner(writer, outline, shadow, Corner.SOUTH_WEST);
        }
        if ((mask & DOWN_WATER) != 0 && (mask & RIGHT_WATER) != 0) {
            carveOuterCorner(writer, outline, shadow, Corner.SOUTH_EAST);
        }

        if ((mask & NORTH_WEST_WATER) != 0) {
            carveInnerCorner(writer, outline, shadow, Corner.NORTH_WEST);
        }
        if ((mask & SOUTH_WEST_WATER) != 0) {
            carveInnerCorner(writer, outline, shadow, Corner.SOUTH_WEST);
        }
        if ((mask & SOUTH_EAST_WATER) != 0) {
            carveInnerCorner(writer, outline, shadow, Corner.SOUTH_EAST);
        }
        if ((mask & NORTH_EAST_WATER) != 0) {
            carveInnerCorner(writer, outline, shadow, Corner.NORTH_EAST);
        }

        return composite;
    }

    private static void carveOuterCorner(PixelWriter writer, Color outline, Color shadow, Corner corner) {
        for (int[] point : OUTER_CORNER_CUT) {
            int[] transformed = transform(point[0], point[1], corner);
            writer.setColor(transformed[0], transformed[1], Color.TRANSPARENT);
        }

        for (int[] point : OUTER_CORNER_OUTLINE) {
            int[] transformed = transform(point[0], point[1], corner);
            writer.setColor(transformed[0], transformed[1], outline);
        }

        for (int[] point : OUTER_CORNER_SHADOW) {
            int[] transformed = transform(point[0], point[1], corner);
            writer.setColor(transformed[0], transformed[1], shadow);
        }
    }

    private static void carveInnerCorner(PixelWriter writer, Color outline, Color shadow, Corner corner) {
        for (int[] point : INNER_CORNER_CUT) {
            int[] transformed = transform(point[0], point[1], corner);
            writer.setColor(transformed[0], transformed[1], Color.TRANSPARENT);
        }

        for (int[] point : INNER_CORNER_OUTLINE) {
            int[] transformed = transform(point[0], point[1], corner);
            writer.setColor(transformed[0], transformed[1], outline);
        }

        for (int[] point : INNER_CORNER_SHADOW) {
            int[] transformed = transform(point[0], point[1], corner);
            writer.setColor(transformed[0], transformed[1], shadow);
        }
    }

    private static int[] transform(int x, int y, Corner corner) {
        return switch (corner) {
            case NORTH_WEST -> new int[] { x, y };
            case NORTH_EAST -> new int[] { TILE_SIZE - 1 - x, y };
            case SOUTH_WEST -> new int[] { x, TILE_SIZE - 1 - y };
            case SOUTH_EAST -> new int[] { TILE_SIZE - 1 - x, TILE_SIZE - 1 - y };
        };
    }

    private enum Corner {
        NORTH_WEST,
        NORTH_EAST,
        SOUTH_WEST,
        SOUTH_EAST
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

        boolean leftWater = (mask & 1) != 0;
        boolean downWater = (mask & 2) != 0;
        boolean rightWater = (mask & 4) != 0;
        boolean upWater = (mask & 8) != 0;

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

        return img;
    }
}
