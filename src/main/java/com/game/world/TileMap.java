package com.game.world;

import com.game.util.AssetManager;
import com.game.util.SpriteSheet;
import com.game.util.Constants;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * TileMap — Map dạng tile-based, lưu trong 2D int array.
 * Render chỉ các tile visible trong viewport, hỗ trợ 2 layers.
 */
public class TileMap {

    public static final int TILE_SIZE = 32;

    // Map dimensions
    public static final int MAP_COLS = 40;
    public static final int MAP_ROWS = 30;

    // Layer 0: Ground, Layer 1: Decoration (trên ground)
    private final int[][] groundLayer;
    private final int[][] decoLayer;
    private SpriteSheet tileSet;

    // Pre-rendered tile images
    private final Image[] tileImages;

    public TileMap() {
        groundLayer = new int[MAP_ROWS][MAP_COLS];
        decoLayer = new int[MAP_ROWS][MAP_COLS];
        tileSet = AssetManager.getInstance().getSpriteSheet("tiles");

        // Pre-cache tile images
        tileImages = new Image[Tile.values().length];
        for (Tile t : Tile.values()) {
            if (tileSet != null) {
                tileImages[t.getId()] = tileSet.getFrame(t.getTilesetCol(), t.getTilesetRow());
            }
        }

        buildMap();
    }

    /**
     * Xây dựng map data — công viên với hồ nước ở giữa.
     */
    private void buildMap() {
        // Initialize all with grass
        for (int r = 0; r < MAP_ROWS; r++) {
            for (int c = 0; c < MAP_COLS; c++) {
                groundLayer[r][c] = Tile.GRASS.getId();
                decoLayer[r][c] = -1; // -1 = no decoration
            }
        }

        // === Border: Water around edges ===
        for (int c = 0; c < MAP_COLS; c++) {
            groundLayer[0][c] = Tile.WATER.getId();
            groundLayer[1][c] = Tile.WATER.getId();
            groundLayer[MAP_ROWS-1][c] = Tile.WATER.getId();
            groundLayer[MAP_ROWS-2][c] = Tile.WATER.getId();
        }
        for (int r = 0; r < MAP_ROWS; r++) {
            groundLayer[r][0] = Tile.WATER.getId();
            groundLayer[r][1] = Tile.WATER.getId();
            groundLayer[r][MAP_COLS-1] = Tile.WATER.getId();
            groundLayer[r][MAP_COLS-2] = Tile.WATER.getId();
        }

        // === Entrance path from top ===
        for (int r = 2; r < 8; r++) {
            groundLayer[r][19] = Tile.PATH.getId();
            groundLayer[r][20] = Tile.PATH.getId();
        }
        // Opening in trees for entrance
        // groundLayer[1][19] = Tile.PATH.getId();
        // groundLayer[1][20] = Tile.PATH.getId();
        // groundLayer[0][19] = Tile.PATH.getId();
        // groundLayer[0][20] = Tile.PATH.getId();

        // === Main path system ===
        // Horizontal path across
        for (int c = 6; c < 34; c++) {
            groundLayer[14][c] = Tile.PATH.getId();
            groundLayer[15][c] = Tile.PATH.getId();
        }
        // Vertical path
        for (int r = 6; r < 24; r++) {
            groundLayer[r][19] = Tile.PATH.getId();
            groundLayer[r][20] = Tile.PATH.getId();
        }

        // === Central Lake ===
        for (int r = 9; r < 20; r++) {
            for (int c = 11; c < 18; c++) {
                groundLayer[r][c] = Tile.WATER.getId();
            }
        }
        // Bridge over lake
        groundLayer[14][11] = Tile.BRIDGE.getId();
        groundLayer[14][12] = Tile.BRIDGE.getId();
        groundLayer[15][11] = Tile.BRIDGE.getId();
        groundLayer[15][12] = Tile.BRIDGE.getId();

        // Water edges (make lake look nicer)
        for (int c = 11; c < 18; c++) {
            groundLayer[8][c] = Tile.DARK_GRASS.getId();
            groundLayer[20][c] = Tile.DARK_GRASS.getId();
        }
        for (int r = 9; r < 20; r++) {
            groundLayer[r][10] = Tile.DARK_GRASS.getId();
            groundLayer[r][18] = Tile.DARK_GRASS.getId();
        }

        // === Flowers scattered around ===
        int[][] flowerPos = {
            {4, 8}, {4, 12}, {4, 28}, {4, 32},
            {8, 5}, {8, 25}, {8, 33},
            {12, 6}, {12, 24}, {12, 30},
            {17, 6}, {17, 24},
            {20, 8}, {20, 28}, {20, 33},
            {22, 5}, {22, 12}, {22, 24}, {22, 30},
            {25, 8}, {25, 15}, {25, 28},
        };
        for (int[] pos : flowerPos) {
            if (groundLayer[pos[0]][pos[1]] == Tile.GRASS.getId()) {
                groundLayer[pos[0]][pos[1]] = Tile.FLOWER.getId();
            }
        }

        // === Benches ===
        int[][] benchPos = {
            {10, 8}, {10, 24},
            {18, 8}, {18, 24},
            {7, 30}, {22, 30}
        };
        for (int[] pos : benchPos) {
            if (groundLayer[pos[0]][pos[1]] == Tile.GRASS.getId()) {
                groundLayer[pos[0]][pos[1]] = Tile.BENCH.getId();
            }
        }

        // === Tree clusters inside park ===
        int[][] treePos = {
            {4, 6}, {5, 6}, {4, 34}, {5, 34},
            {6, 4}, {6, 35},
            {23, 4}, {24, 4}, {23, 35}, {24, 35},
            {8, 22}, {9, 23},
            {20, 22}, {21, 23},
            {6, 26}, {7, 27},
            {23, 10}, {24, 10},
        };
        for (int[] pos : treePos) {
            if (pos[0] >= 2 && pos[0] < MAP_ROWS - 2 && pos[1] >= 2 && pos[1] < MAP_COLS - 2) {
                groundLayer[pos[0]][pos[1]] = Tile.TREE.getId();
            }
        }

        // === Fence sections ===
        for (int c = 4; c < 10; c++) {
            groundLayer[3][c] = Tile.FENCE.getId();
            groundLayer[3][MAP_COLS - 1 - c] = Tile.FENCE.getId();
        }

        // === Dark grass patches for variety ===
        int[][] darkGrassPos = {
            {6, 8}, {7, 9}, {10, 30}, {11, 31},
            {16, 5}, {17, 4}, {21, 28}, {22, 27},
            {13, 26}, {14, 27}
        };
        for (int[] pos : darkGrassPos) {
            if (groundLayer[pos[0]][pos[1]] == Tile.GRASS.getId()) {
                groundLayer[pos[0]][pos[1]] = Tile.DARK_GRASS.getId();
            }
        }
    }

    /**
     * Render ground layer (layer 0) — chỉ tiles visible trong viewport.
     */
    public void renderGround(GraphicsContext gc, double camX, double camY) {
        int startCol = Math.max(0, (int)(camX / TILE_SIZE));
        int endCol = Math.min(MAP_COLS - 1, (int)((camX + Constants.WINDOW_WIDTH) / TILE_SIZE) + 1);
        int startRow = Math.max(0, (int)(camY / TILE_SIZE));
        int endRow = Math.min(MAP_ROWS - 1, (int)((camY + Constants.WINDOW_HEIGHT) / TILE_SIZE) + 1);

        gc.setImageSmoothing(false);

        for (int r = startRow; r <= endRow; r++) {
            for (int c = startCol; c <= endCol; c++) {
                int tileId = groundLayer[r][c];
                Image tileImg = tileImages[tileId];
                if (tileImg != null) {
                    gc.drawImage(tileImg,
                        c * TILE_SIZE - camX,
                        r * TILE_SIZE - camY,
                        TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    /**
     * Render decoration layer (layer 1).
     */
    public void renderDecorations(GraphicsContext gc, double camX, double camY) {
        int startCol = Math.max(0, (int)(camX / TILE_SIZE));
        int endCol = Math.min(MAP_COLS - 1, (int)((camX + Constants.WINDOW_WIDTH) / TILE_SIZE) + 1);
        int startRow = Math.max(0, (int)(camY / TILE_SIZE));
        int endRow = Math.min(MAP_ROWS - 1, (int)((camY + Constants.WINDOW_HEIGHT) / TILE_SIZE) + 1);

        gc.setImageSmoothing(false);

        for (int r = startRow; r <= endRow; r++) {
            for (int c = startCol; c <= endCol; c++) {
                int decoId = decoLayer[r][c];
                if (decoId >= 0 && decoId < tileImages.length && tileImages[decoId] != null) {
                    gc.drawImage(tileImages[decoId],
                        c * TILE_SIZE - camX,
                        r * TILE_SIZE - camY,
                        TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    /**
     * Kiểm tra tile tại (col, row) có solid không.
     */
    public boolean isSolid(int col, int row) {
        if (col < 0 || col >= MAP_COLS || row < 0 || row >= MAP_ROWS) {
            return true; // Out of bounds = solid
        }
        return Tile.fromId(groundLayer[row][col]).isSolid();
    }

    /**
     * Lấy tile tại vị trí (col, row).
     */
    public Tile getTile(int col, int row) {
        if (col < 0 || col >= MAP_COLS || row < 0 || row >= MAP_ROWS) {
            return Tile.WATER;
        }
        return Tile.fromId(groundLayer[row][col]);
    }

    public int getPixelWidth() { return MAP_COLS * TILE_SIZE; }
    public int getPixelHeight() { return MAP_ROWS * TILE_SIZE; }
}
