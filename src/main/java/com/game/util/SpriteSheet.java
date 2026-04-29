package com.game.util;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

/**
 * SpriteSheet — Load và cắt sprite sheet thành các frame riêng lẻ.
 */
public class SpriteSheet {

    private final Image sheet;
    private final int frameWidth;
    private final int frameHeight;

    public SpriteSheet(Image sheet, int frameWidth, int frameHeight) {
        this.sheet = sheet;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

    /**
     * Lấy 1 frame từ sprite sheet tại vị trí (col, row).
     */
    public Image getFrame(int col, int row) {
        WritableImage frame = new WritableImage(
            sheet.getPixelReader(),
            col * frameWidth,
            row * frameHeight,
            frameWidth,
            frameHeight
        );
        return frame;
    }

    /**
     * Lấy frame và scale lên.
     */
    public Image getFrameScaled(int col, int row, int scale) {
        Image original = getFrame(col, row);
        int newWidth = frameWidth * scale;
        int newHeight = frameHeight * scale;
        WritableImage scaled = new WritableImage(newWidth, newHeight);
        PixelWriter pw = scaled.getPixelWriter();

        for (int y = 0; y < frameHeight; y++) {
            for (int x = 0; x < frameWidth; x++) {
                Color c = original.getPixelReader().getColor(x, y);
                for (int sy = 0; sy < scale; sy++) {
                    for (int sx = 0; sx < scale; sx++) {
                        pw.setColor(x * scale + sx, y * scale + sy, c);
                    }
                }
            }
        }
        return scaled;
    }

    public int getFrameWidth() { return frameWidth; }
    public int getFrameHeight() { return frameHeight; }
    public Image getSheet() { return sheet; }
}
