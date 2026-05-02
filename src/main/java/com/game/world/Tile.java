package com.game.world;

/**
 * Tile — Enum các loại tile trên map.
 * Mỗi tile có ID, có thể đi qua hay không (solid), và vị trí trên tileset.
 */
public enum Tile {

    GRASS(0, false, 0, 0),
    WATER(1, true, 1, 0),
    PATH(2, false, 2, 0),
    TREE(3, true, 3, 0),
    FLOWER(4, false, 4, 0),
    BENCH(5, true, 5, 0),
    FENCE(6, true, 6, 0),
    BRIDGE(7, false, 7, 0),
    TREE_TOP(8, false, 0, 1), // Decoration layer, not solid
    DARK_GRASS(9, false, 1, 1),
    WATER_EDGE(10, true, 2, 1);

    private final int id;
    private final boolean solid;
    private final int tilesetCol;
    private final int tilesetRow;

    Tile(int id, boolean solid, int tilesetCol, int tilesetRow) {
        this.id = id;
        this.solid = solid;
        this.tilesetCol = tilesetCol;
        this.tilesetRow = tilesetRow;
    }

    public int getId() {
        return id;
    }

    public boolean isSolid() {
        return solid;
    }

    public int getTilesetCol() {
        return tilesetCol;
    }

    public int getTilesetRow() {
        return tilesetRow;
    }

    /**
     * Lấy Tile từ ID.
     */
    public static Tile fromId(int id) {
        for (Tile t : values()) {
            if (t.id == id)
                return t;
        }
        return GRASS;
    }
}
