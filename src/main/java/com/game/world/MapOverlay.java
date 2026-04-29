package com.game.world;

import com.game.GameApplication;
import com.game.entity.CatFollower;
import com.game.entity.Entity;
import com.game.entity.Item;
import com.game.entity.NPC;
import com.game.entity.Player;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

/**
 * MapOverlay — Minimap/Map Overlay hiển thị khi phím M được nhấn.
 * Vẽ bản đồ nhỏ, player, NPCs, items (quest active), và cat.
 */
public class MapOverlay {

    private boolean visible = false;

    // Minimap dimensions & position
    private static final int MINIMAP_WIDTH = 200;  // pixels
    private static final int MINIMAP_HEIGHT = 150; // pixels
    private static final int MINIMAP_X = 10;
    private static final int MINIMAP_Y = 10;

    // Scale factor: world pixels → minimap pixels
    private float scaleX;
    private float scaleY;

    public MapOverlay(TileMap tileMap) {
        // Calculate scale: map world size → minimap size
        this.scaleX = (float) MINIMAP_WIDTH / tileMap.getPixelWidth();
        this.scaleY = (float) MINIMAP_HEIGHT / tileMap.getPixelHeight();
    }

    /**
     * Toggle minimap visibility.
     */
    public void toggle() {
        visible = !visible;
    }

    /**
     * Set minimap visibility.
     */
    public void setVisible(boolean v) {
        visible = v;
    }

    /**
     * Check if minimap is visible.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Render minimap overlay.
     */
    public void render(GraphicsContext gc, TileMap tileMap, Player player, 
                       List<NPC> npcs, List<Item> items, CatFollower cat) {
        if (!visible) return;

        // Background box
        gc.setFill(Color.web("#1a1a2e", 0.95));
        gc.fillRect(MINIMAP_X, MINIMAP_Y, MINIMAP_WIDTH, MINIMAP_HEIGHT);

        // Border
        gc.setStroke(Color.web("#FFD700", 1.0));
        gc.setLineWidth(2);
        gc.strokeRect(MINIMAP_X, MINIMAP_Y, MINIMAP_WIDTH, MINIMAP_HEIGHT);

        // Title
        gc.setFill(Color.web("#FFD700", 1.0));
        gc.setFont(Font.font("Monospaced", 11));
        gc.fillText("MAP", MINIMAP_X + 7, MINIMAP_Y + 13);

        // Draw tile map (background)
        renderTileMap(gc, tileMap);

        // Draw NPCs (cyan/light blue)
        gc.setFill(Color.web("#00CED1", 0.85));
        for (NPC npc : npcs) {
            drawEntityOnMinimap(gc, npc);
        }

        // Draw items (active quest items in yellow)
        gc.setFill(Color.web("#FFD700", 0.85));
        for (Item item : items) {
            if (item.isVisible() && !item.isCollected()) {
                drawEntityOnMinimap(gc, item);
            }
        }

        // Draw cat (magenta/pink)
        gc.setFill(Color.web("#FF69B4", 0.85));
        drawEntityOnMinimap(gc, cat);

        // Draw player (red) - last so it's on top
        gc.setFill(Color.web("#FF0000", 1.0));
        drawEntityOnMinimap(gc, player);

        // Draw legend at bottom
        renderLegend(gc);
    }

    /**
     * Render tile map background on minimap.
     */
    private void renderTileMap(GraphicsContext gc, TileMap tileMap) {
        // Simple representation: darker for solid tiles
        int tileSize = TileMap.TILE_SIZE;
        
        for (int row = 0; row < TileMap.MAP_ROWS; row++) {
            for (int col = 0; col < TileMap.MAP_COLS; col++) {
                Tile tile = tileMap.getTile(col, row);
                
                // Convert world tile position to minimap position
                int worldX = col * tileSize;
                int worldY = row * tileSize;
                
                int minimapX = MINIMAP_X + (int)(worldX * scaleX);
                int minimapY = MINIMAP_Y + (int)(worldY * scaleY);
                int minimapTileW = Math.max(1, (int)(tileSize * scaleX));
                int minimapTileH = Math.max(1, (int)(tileSize * scaleY));
                
                // Color based on tile type
                if (tile.isSolid()) {
                    // Solid tiles (trees, benches, water, etc.) - darker
                    if (tile == Tile.WATER) {
                        gc.setFill(Color.web("#1E90FF", 0.6)); // Blue for water
                    } else {
                        gc.setFill(Color.web("#3a3a4a", 0.8)); // Dark gray for obstacles
                    }
                } else {
                    // Walkable tiles - lighter
                    gc.setFill(Color.web("#2a5a2a", 0.5)); // Dark green for grass
                }
                
                gc.fillRect(minimapX, minimapY, minimapTileW, minimapTileH);
            }
        }
    }

    /**
     * Draw a single entity on minimap as a dot.
     */
    private void drawEntityOnMinimap(GraphicsContext gc, Entity entity) {
        int minimapX = MINIMAP_X + (int)(entity.getX() * scaleX);
        int minimapY = MINIMAP_Y + (int)(entity.getY() * scaleY);
        int dotSize = 4; // Small dot
        
        gc.fillRect(minimapX - dotSize / 2, minimapY - dotSize / 2, dotSize, dotSize);
    }

    /**
     * Render legend explaining minimap symbols.
     */
    private void renderLegend(GraphicsContext gc) {
        int startY = MINIMAP_Y + MINIMAP_HEIGHT + 8;
        int startX = MINIMAP_X;
        
        gc.setFont(Font.font("Monospaced", 9));
        
        // Red = Player
        gc.setFill(Color.web("#FF0000", 0.85));
        gc.fillRect(startX, startY - 8, 6, 6);
        gc.setFill(Color.web("#FFFFFF", 0.7));
        gc.fillText("Player", startX + 10, startY - 2);
        
        // Cyan = NPC
        gc.setFill(Color.web("#00CED1", 0.85));
        gc.fillRect(startX + 60, startY - 8, 6, 6);
        gc.setFill(Color.web("#FFFFFF", 0.7));
        gc.fillText("NPC", startX + 70, startY - 2);
        
        // Yellow = Item
        gc.setFill(Color.web("#FFD700", 0.85));
        gc.fillRect(startX + 110, startY - 8, 6, 6);
        gc.setFill(Color.web("#FFFFFF", 0.7));
        gc.fillText("Item", startX + 120, startY - 2);
        
        // Pink = Cat
        gc.setFill(Color.web("#FF69B4", 0.85));
        gc.fillRect(startX + 150, startY - 8, 6, 6);
        gc.setFill(Color.web("#FFFFFF", 0.7));
        gc.fillText("Cat", startX + 160, startY - 2);
    }
}
