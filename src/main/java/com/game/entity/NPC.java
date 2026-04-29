package com.game.entity;

import com.game.dialog.DialogData;
import com.game.util.AssetManager;
import com.game.util.SpriteSheet;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * NPC — Nhân vật không điều khiển, có dialog và interaction.
 * Hiển thị tên, indicator khi player ở gần.
 */
public class NPC extends Entity {

    public enum DialogType {
        SIMPLE, CHOICE, QUEST
    }

    private final String name;
    private final DialogType dialogType;
    private DialogData dialogData;
    private SpriteSheet spriteSheet;
    private boolean playerInRange = false;
    private double indicatorBounce = 0;

    // Interaction range (pixel distance)
    private static final double INTERACTION_RANGE = 48;

    // Idle animation
    private double idleTimer = 0;
    private int idleFrame = 0;

    public NPC(String name, double x, double y, DialogType dialogType, String spriteKey) {
        super(x, y, 32, 32);
        this.name = name;
        this.dialogType = dialogType;
        this.spriteSheet = AssetManager.getInstance().getSpriteSheet(spriteKey);
        updateSprite();
    }

    @Override
    public void update(double dt) {
        // Idle animation
        idleTimer += dt;
        if (idleTimer >= 1.5) { // Toggle every 1.5 seconds
            idleTimer = 0;
            idleFrame = (idleFrame + 1) % 2;
            updateSprite();
        }

        // Indicator bounce
        indicatorBounce += dt * 3;
    }

    /**
     * Kiểm tra player có trong phạm vi tương tác không.
     */
    public boolean isPlayerInRange(Player player) {
        double dx = getCenterX() - player.getCenterX();
        double dy = getCenterY() - player.getCenterY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        playerInRange = dist < INTERACTION_RANGE;
        return playerInRange;
    }

    /**
     * Kiểm tra interaction bounds của player có chạm NPC không.
     */
    public boolean canInteract(Player player) {
        Rectangle2D interactArea = player.getInteractionBounds();
        return isPlayerInRange(player) || getFullBounds().intersects(interactArea);
    }

    private void updateSprite() {
        if (spriteSheet != null) {
            currentSprite = spriteSheet.getFrame(idleFrame, 0);
        }
    }

    @Override
    public void render(GraphicsContext gc, double camX, double camY) {
        double renderX = x - camX;
        double renderY = y - camY;

        // Draw sprite
        if (currentSprite != null) {
            gc.setImageSmoothing(false);
            gc.drawImage(currentSprite, renderX, renderY, width, height);
        }

        // Draw name tag above NPC
        gc.setFont(Font.font("Monospaced", 10));
        gc.setFill(Color.web("#000000", 0.5));
        double nameWidth = name.length() * 6;
        gc.fillRoundRect(renderX + width/2.0 - nameWidth/2 - 4, renderY - 16, nameWidth + 8, 14, 4, 4);
        gc.setFill(Color.WHITE);
        gc.fillText(name, renderX + width/2.0 - nameWidth/2, renderY - 5);

        // Draw interaction indicator when player is in range
        if (playerInRange) {
            double bounceOffset = Math.sin(indicatorBounce) * 4;
            gc.setFill(Color.web("#FFD700"));
            gc.setFont(Font.font("Monospaced", 16));
            gc.fillText("!", renderX + width/2.0 - 4, renderY - 22 + bounceOffset);

            // "Nhấn E" hint
            gc.setFont(Font.font("Monospaced", 8));
            gc.setFill(Color.web("#FFFFFF", 0.7));
            gc.fillText("[E]", renderX + width/2.0 - 8, renderY - 30 + bounceOffset);
        }
    }

    // Getters/Setters
    public String getName() { return name; }
    public DialogType getDialogType() { return dialogType; }
    public DialogData getDialogData() { return dialogData; }
    public void setDialogData(DialogData data) { this.dialogData = data; }
}
