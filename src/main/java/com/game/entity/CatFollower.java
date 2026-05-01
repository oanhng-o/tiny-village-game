package com.game.entity;

import com.game.util.AssetManager;
import com.game.util.SpriteSheet;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * CatFollower — Mèo con theo chân player sau khi quest hoàn thành.
 * Sau quest chính, mèo có thể được chăm sóc với mood, heart level và hành vi gọi về gần player.
 */
public class CatFollower extends Entity {

    private static final double FOLLOW_DISTANCE = 40.0;
    private static final double FOLLOW_SPEED = 100.0;
    private static final double CALL_SPEED = 170.0;
    private static final double INTERACTION_RANGE = 56.0;
    private static final double CARE_MENU_RANGE = 72.0;

    private static final int MAX_MOOD = 100;
    private static final int UNLOCK_MOOD = 60;
    private static final int PET_MOOD_GAIN = 5;
    private static final int PET_AFFECTION_GAIN = 2;
    private static final int FEED_MOOD_GAIN = 15;
    private static final int FEED_AFFECTION_GAIN = 8;

    private static final double PET_COOLDOWN = 6.0;
    private static final double FEED_COOLDOWN = 10.0;
    private static final double CALL_COOLDOWN = 2.0;
    private static final double INTERACTION_EFFECT_DURATION = 1.2;

    public enum State {
        IDLE, FOLLOWING, CALLING
    }

    private State state = State.IDLE;
    private SpriteSheet spriteSheet;
    private Player targetPlayer;

    private int mood = 0;
    private int affectionPoints = 0;
    private double petCooldownRemaining = 0;
    private double feedCooldownRemaining = 0;
    private double callCooldownRemaining = 0;
    private double interactionEffectRemaining = 0;

    // Animation
    private boolean isWalking = false;
    private int animFrame = 0;
    private double animTimer = 0;

    // Idle animation (tail wag)
    private double idleTimer = 0;

    public CatFollower(double x, double y) {
        super(x, y, 32, 32);
        this.spriteSheet = AssetManager.getInstance().getSpriteSheet("cat");
        this.animationSpeed = 0.25;
        updateSprite();
    }

    @Override
    public void update(double dt) {
        updateCooldowns(dt);
        updateMovement(dt);

        // Update animation
        if (isWalking) {
            animTimer += dt;
            if (animTimer >= animationSpeed) {
                animTimer = 0;
                animFrame = (animFrame + 1) % 4;
            }
        } else {
            idleTimer += dt;
            if (idleTimer >= 0.8) {
                idleTimer = 0;
                animFrame = (animFrame + 1) % 4;
            }
        }
        updateSprite();
    }

    private void updateSprite() {
        if (spriteSheet != null) {
            int row = isWalking ? 1 : 0;
            currentSprite = spriteSheet.getFrame(animFrame, row);
        }
    }

    @Override
    public void render(GraphicsContext gc, double camX, double camY) {
        double renderX = x - camX;
        double renderY = y - camY;

        if (currentSprite != null) {
            gc.setImageSmoothing(false);
            gc.drawImage(currentSprite, renderX, renderY, width, height);
        }

        if (isCareUnlocked()) {
            double heartBounce = Math.sin(idleTimer * 4) * 2;
            gc.setFill(Color.web("#FF69B4", 0.7));
            gc.setFont(javafx.scene.text.Font.font("Monospaced", 10));
            gc.fillText("♥" + getHeartLevel(), renderX + width / 2 - 8, renderY - 6 + heartBounce);

            if (interactionEffectRemaining > 0) {
                gc.setFill(Color.web("#FFD1E8", Math.min(1.0, interactionEffectRemaining)));
                gc.setFont(javafx.scene.text.Font.font("Monospaced", 12));
                gc.fillText("♥", renderX + width / 2.0 - 4, renderY - 20 - heartBounce);
            }
        }
    }

    private void updateCooldowns(double dt) {
        petCooldownRemaining = Math.max(0, petCooldownRemaining - dt);
        feedCooldownRemaining = Math.max(0, feedCooldownRemaining - dt);
        callCooldownRemaining = Math.max(0, callCooldownRemaining - dt);
        interactionEffectRemaining = Math.max(0, interactionEffectRemaining - dt);
    }

    private void updateMovement(double dt) {
        if (targetPlayer == null || state == State.IDLE) {
            isWalking = false;
            return;
        }

        if (state == State.CALLING) {
            double anchorX = targetPlayer.getX() - width * 0.75;
            double anchorY = targetPlayer.getY() + targetPlayer.getHeight() * 0.25;
            boolean arrived = moveToward(anchorX, anchorY, CALL_SPEED, dt, 10.0);
            if (arrived) {
                state = State.FOLLOWING;
            }
            return;
        }

        double targetX = targetPlayer.getHistoryX(30);
        double targetY = targetPlayer.getHistoryY(30);
        moveToward(targetX, targetY, FOLLOW_SPEED, dt, FOLLOW_DISTANCE);
    }

    private boolean moveToward(double targetX, double targetY, double speed, double dt, double stopDistance) {
        double dx = targetX - x;
        double dy = targetY - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist <= stopDistance) {
            isWalking = false;
            return true;
        }

        double step = Math.min(dist - stopDistance, speed * dt);
        if (step <= 0) {
            isWalking = false;
            return true;
        }

        x += (dx / dist) * step;
        y += (dy / dist) * step;
        isWalking = true;
        return false;
    }

    private void boostCare(int moodGain, int affectionGain) {
        mood = Math.min(MAX_MOOD, mood + moodGain);
        affectionPoints += affectionGain;
        interactionEffectRemaining = INTERACTION_EFFECT_DURATION;
    }

    /**
     * Bắt đầu follow player.
     */
    public void startFollowing(Player player) {
        boolean wasLocked = !isCareUnlocked();
        this.state = State.FOLLOWING;
        this.targetPlayer = player;
        if (wasLocked) {
            this.mood = UNLOCK_MOOD;
            this.affectionPoints = 0;
        }
    }

    public boolean callToPlayer(Player player) {
        if (!isCareUnlocked() || callCooldownRemaining > 0) {
            return false;
        }

        this.targetPlayer = player;
        this.state = State.CALLING;
        this.callCooldownRemaining = CALL_COOLDOWN;
        return true;
    }

    public boolean pet(Player player) {
        if (!canPet(player)) {
            return false;
        }

        boostCare(PET_MOOD_GAIN, PET_AFFECTION_GAIN);
        petCooldownRemaining = PET_COOLDOWN;
        return true;
    }

    public boolean feed() {
        if (!canFeed()) {
            return false;
        }

        boostCare(FEED_MOOD_GAIN, FEED_AFFECTION_GAIN);
        feedCooldownRemaining = FEED_COOLDOWN;
        return true;
    }

    public boolean canPet(Player player) {
        return isPlayerInInteractionRange(player)
                && petCooldownRemaining <= 0
                && state != State.CALLING;
    }

    public boolean canFeed() {
        return isCareUnlocked() && feedCooldownRemaining <= 0;
    }

    public boolean isPlayerInInteractionRange(Player player) {
        if (!isCareUnlocked()) {
            return false;
        }

        double dx = getCenterX() - player.getCenterX();
        double dy = getCenterY() - player.getCenterY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        return dist <= INTERACTION_RANGE || getFullBounds().intersects(player.getInteractionBounds());
    }

    public boolean isPlayerNearForCare(Player player) {
        if (!isCareUnlocked()) {
            return false;
        }

        double dx = getCenterX() - player.getCenterX();
        double dy = getCenterY() - player.getCenterY();
        return Math.sqrt(dx * dx + dy * dy) <= CARE_MENU_RANGE;
    }

    public boolean isCareUnlocked() {
        return state != State.IDLE || targetPlayer != null;
    }

    public int getMood() {
        return mood;
    }

    public int getAffectionPoints() {
        return affectionPoints;
    }

    public int getHeartLevel() {
        if (!isCareUnlocked()) {
            return 0;
        }
        if (affectionPoints >= 110) {
            return 5;
        }
        if (affectionPoints >= 70) {
            return 4;
        }
        if (affectionPoints >= 40) {
            return 3;
        }
        if (affectionPoints >= 20) {
            return 2;
        }
        return 1;
    }

    public String getMoodLabel() {
        if (!isCareUnlocked()) {
            return "Chưa mở";
        }
        if (mood < 30) {
            return "Buồn";
        }
        if (mood < 70) {
            return "Bình thường";
        }
        return "Vui vẻ";
    }

    public double getPetCooldownRemaining() {
        return petCooldownRemaining;
    }

    public double getFeedCooldownRemaining() {
        return feedCooldownRemaining;
    }

    public double getCallCooldownRemaining() {
        return callCooldownRemaining;
    }

    // Getters
    public State getState() { return state; }
}
