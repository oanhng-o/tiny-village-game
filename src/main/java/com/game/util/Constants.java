package com.game.util;

/**
 * Constants — Tập trung tất cả các hằng số, kích thước, tên file, thư mục và
 * tham số game.
 * Dễ dàng quản lý và tùy chỉnh các giá trị này từ một chỗ duy nhất.
 */
public class Constants {

    // ==================== WINDOW / DISPLAY ====================
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;

    // ==================== SPRITE / ENTITY SIZES ====================
    public static final int SPRITE_SIZE = 32;
    public static final int TILE_SIZE = 32;

    // ==================== MAP DIMENSIONS ====================
    public static final int MAP_COLS = 40;
    public static final int MAP_ROWS = 30;

    // ==================== PLAYER CONSTANTS ====================
    public static final double PLAYER_SPEED = 120.0; // pixels per second
    public static final double PLAYER_ANIMATION_SPEED = 0.2; // seconds per frame
    public static final double PLAYER_POSITION_HISTORY_RECORD_INTERVAL = 0.05; // seconds

    // ==================== CAMERA CONSTANTS ====================
    public static final double CAMERA_LERP_SPEED = 5.0; // 0.0 = no follow, 1.0 = instant
    public static final double MAX_DELTA_TIME = 0.05; // seconds (cap delta time to prevent physics glitches)

    // ==================== CAT FOLLOWER CONSTANTS ====================
    public static final double CAT_SPEED = 100.0; // pixels per second (slightly slower than player)
    public static final double CAT_ANIMATION_SPEED = 0.25; // seconds per frame
    public static final double CAT_FOLLOW_DISTANCE = 40.0; // pixels
    public static final int CAT_HISTORY_FRAMES = 30; // frames back to follow
    public static final double CAT_IDLE_ANIMATION_INTERVAL = 0.8; // seconds between idle animation frames

    // ==================== ENTITY CONSTANTS ====================
    public static final double DEFAULT_ANIMATION_SPEED = 0.2; // seconds per frame

    // ==================== COLLISION CONSTANTS ====================
    public static final int ENTITY_COLLISION_PADDING_X = 4; // pixels on each side
    public static final int ENTITY_COLLISION_PADDING_Y = 0; // pixels from top

    // ==================== ASSET PATHS ====================
    public static final String ASSET_DIR = "assets/";

    // Asset file names
    public static final String ASSET_PLAYER_GIRL = "player.png";
    public static final String ASSET_PLAYER_BOY = "player2.png";
    public static final String ASSET_PLAYER_PREVIEW_GIRL = "player_preview.png";
    public static final String ASSET_PLAYER_PREVIEW_BOY = "player2_preview.png";
    public static final String ASSET_NPC_GRANDMA = "npc_grandma.png";
    public static final String ASSET_NPC_DRINK_SELLER = "npc_drink_seller.png";
    public static final String ASSET_NPC_FISHER_KID = "npc_fisher_kid.png";
    public static final String ASSET_NPC_GARDENER = "npc_gardener.png";
    public static final String ASSET_CAT = "cat.png";
    public static final String ASSET_TILES = "tiles.png";
    public static final String ASSET_GRASS_AUTOTILE = "grass_autotile.png";
    public static final String ASSET_DARK_GRASS_AUTOTILE = "dark_grass_autotile.png";
    public static final String ASSET_GRASS_AUTOTILE_EXTENDED = "grass_autotile_extended.png";
    public static final String ASSET_DARK_GRASS_AUTOTILE_EXTENDED = "dark_grass_autotile_extended.png";
    public static final String ASSET_FISHING_ROD = "fishing_rod.png";
    public static final String ASSET_SEEDS = "seeds.png";
    public static final String ASSET_REWARD_ROSE = "rose.png";
    public static final String ASSET_REWARD_SUNFLOWER = "sunflower.png";
    public static final String ASSET_REWARD_TULIP = "tulip.png";
    public static final String ASSET_REWARD_BONSAI = "bonsai.png";
    public static final String ASSET_FISH_CARP = "fish_carp.png";
    public static final String ASSET_FISH_PERCH = "fish_perch.png";
    public static final String ASSET_FISH_CATFISH = "fish_catfish.png";
    public static final String ASSET_FISH_GOLDFISH = "fish_goldfish.png";
    public static final String ASSET_INTERACT_ICON = "interact_icon.png";
    public static final String ASSET_FISHING_ROD_ACTION = "fishing_rod_action.png";

    // Audio asset file names
    public static final String ASSET_AUDIO_MENU_THEME = "menu_theme.mp3";
    public static final String ASSET_AUDIO_GAMEPLAY_LOOP = "gameplay_loop.mp3";
    public static final String ASSET_AUDIO_FOOTSTEP_GRASS_1 = "footstep_grass_1.wav";
    public static final String ASSET_AUDIO_FOOTSTEP_GRASS_2 = "footstep_grass_2.wav";
    public static final String ASSET_AUDIO_DIALOG_OPEN = "dialog_open.wav";
    public static final String ASSET_AUDIO_DIALOG_ADVANCE = "dialog_advance.wav";
    public static final String ASSET_AUDIO_UI_CONFIRM = "ui_confirm.wav";
    public static final String ASSET_AUDIO_UI_BACK = "ui_back.wav";
    public static final String ASSET_AUDIO_QUEST_START = "quest_start.wav";
    public static final String ASSET_AUDIO_QUEST_COMPLETE = "quest_complete.wav";

    // ==================== ASSET KEYS (cho AssetManager) ====================
    public static final String KEY_PLAYER = "player";
    public static final String KEY_NPC_GRANDMA = "npc_grandma";
    public static final String KEY_NPC_DRINK_SELLER = "npc_drink_seller";
    public static final String KEY_NPC_FISHER_KID = "npc_fisher_kid";
    public static final String KEY_NPC_GARDENER = "npc_gardener";
    public static final String KEY_CAT = "cat";
    public static final String KEY_TILES = "tiles";
    public static final String KEY_FISHING_ROD = "fishing_rod";
    public static final String KEY_SEEDS = "seeds";
    public static final String KEY_REWARD_ROSE = "rose";
    public static final String KEY_REWARD_SUNFLOWER = "sunflower";
    public static final String KEY_REWARD_TULIP = "tulip";
    public static final String KEY_REWARD_BONSAI = "bonsai";
    public static final String KEY_FISH_CARP = "fish_carp";
    public static final String KEY_FISH_PERCH = "fish_perch";
    public static final String KEY_FISH_CATFISH = "fish_catfish";
    public static final String KEY_FISH_GOLDFISH = "fish_goldfish";
    public static final String KEY_INTERACT_ICON = "interact_icon";
    public static final String KEY_FISHING_ROD_ACTION = "fishing_rod_action";

    // Audio keys
    public static final String KEY_AUDIO_MENU_THEME = "audio_menu_theme";
    public static final String KEY_AUDIO_GAMEPLAY_LOOP = "audio_gameplay_loop";
    public static final String KEY_AUDIO_FOOTSTEP_GRASS_1 = "audio_footstep_grass_1";
    public static final String KEY_AUDIO_FOOTSTEP_GRASS_2 = "audio_footstep_grass_2";
    public static final String KEY_AUDIO_DIALOG_OPEN = "audio_dialog_open";
    public static final String KEY_AUDIO_DIALOG_ADVANCE = "audio_dialog_advance";
    public static final String KEY_AUDIO_UI_CONFIRM = "audio_ui_confirm";
    public static final String KEY_AUDIO_UI_BACK = "audio_ui_back";
    public static final String KEY_AUDIO_QUEST_START = "audio_quest_start";
    public static final String KEY_AUDIO_QUEST_COMPLETE = "audio_quest_complete";

    // Audio event keys
    public static final String AUDIO_EVENT_MENU_MUSIC = "menu_music";
    public static final String AUDIO_EVENT_GAMEPLAY_MUSIC = "gameplay_music";
    public static final String AUDIO_EVENT_FOOTSTEP = "footstep";
    public static final String AUDIO_EVENT_DIALOG_OPEN = "dialog_open";
    public static final String AUDIO_EVENT_DIALOG_ADVANCE = "dialog_advance";
    public static final String AUDIO_EVENT_CONFIRM = "confirm";
    public static final String AUDIO_EVENT_BACK = "back";
    public static final String AUDIO_EVENT_QUEST_START = "quest_start";
    public static final String AUDIO_EVENT_QUEST_COMPLETE = "quest_complete";

    // Audio defaults
    public static final double DEFAULT_MUSIC_VOLUME = 0.28;
    public static final double DEFAULT_SFX_VOLUME = 0.70;
    public static final double FOOTSTEP_INTERVAL_SECONDS = 0.34;

    // ==================== ANIMATION FRAMES ====================
    public static final int PLAYER_ANIMATION_FRAMES = 4; // frames per direction
    public static final int NPC_ANIMATION_FRAMES = 4;
    public static final int CAT_ANIMATION_FRAMES = 4;
    public static final int PLAYER_DIRECTIONS = 4; // 0=down, 1=left, 2=right, 3=up

    // ==================== MAP CONSTANTS ====================
    public static final int MAP_BORDER_WIDTH = 2; // tiles
    public static final int ENTRANCE_PATH_START_ROW = 2;
    public static final int ENTRANCE_PATH_END_ROW = 8;
    public static final int ENTRANCE_PATH_COL = 19;

    // ==================== UI COLORS ====================
    public static final String COLOR_BACKGROUND = "#1a1a2e";
    public static final String COLOR_HEART = "#FF69B4";

    // ==================== TEXT / FONT ====================
    public static final String FONT_DEFAULT = "Monospaced";
    public static final int FONT_SIZE_DEFAULT = 10;

    // ==================== CHARACTER SELECT ====================
    public static final int CHARACTER_SELECT_OPTIONS = 2; // girl (0), boy (1)

    // ==================== DEBUG / LOGGING ====================
    public static final boolean DEBUG_MODE = false;

    // ==================== PRIVATE CONSTRUCTOR ====================
    private Constants() {
        // Prevent instantiation
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Lấy full path của asset từ filename.
     */
    public static String getAssetPath(String filename) {
        return "/" + ASSET_DIR + filename;
    }

    /**
     * Lấy resource URL của asset.
     */
    public static String getAssetResource(String filename) {
        return getAssetPath(filename);
    }
}
