# Tiny Village Game - Flow Documentation

## Tổng Quan
Tiny Village là một game 2D pixel art được phát triển bằng **Java 17 + JavaFX 21** (Maven). Người chơi điều khiển nhân vật chibi khám phá công viên, tương tác với NPC, hoàn thành các quest, và kết bạn với một chú mèo.

---

## 1. Startup Flow

```
┌─────────────────────────────────────────────┐
│         Program Start                       │
│  java -m com.game/com.game.Main             │
└────────────┬────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────┐
│  Main.java                                  │
│  - Gọi Application.launch()                 │
│  - Khởi tạo GameApplication                 │
└────────────┬────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────┐
│  GameApplication.start(Stage)               │
│  - Tạo Canvas 800x600 px                    │
│  - Hiển thị Character Selection Screen      │
│  - Setup InputHandler + GameLoop            │
└─────────────────────────────────────────────┘
```

---

## 2. Character Selection Screen

### Giao Diện
- **Background**: Gradient bầu trời xanh (#E0F7FA) → bãi cỏ (#B2EBF2)
- **Clouds**: Hoạt ảnh mây trôi nhẹ nhàng
- **Character Cards**: 
  - Girl (áo hồng) - mặc định option 0
  - Boy (áo xanh) - option 1
  - Khi được chọn: Viền màu, hiệu ứng bouncing

### Điều Khiển
```
← / → Arrow Keys    : Chuyển giữa Girl/Boy
Enter / Space       : Xác nhận lựa chọn
```

### Quy Trình
```
Character Selection Screen
        │
        ├─ Người chơi bấm ← hoặc →
        │  └─ Cập nhật selectedOption (0 hoặc 1)
        │  └─ Render card được chọn với hiệu ứng
        │
        └─ Người chơi bấm Enter/Space
           └─ characterSelected = true
           └─ Khởi tạo Player với giới tính đã chọn
           └─ Chuyển sang Game Loop
```

---

## 3. Game Initialization (GameWorld Setup)

Khi Character Selection hoàn tất:

```
┌─────────────────────────────────────────────┐
│  GameWorld Initialization                   │
└────────────┬────────────────────────────────┘
             │
             ├─► TileMap (40x30 tiles, 32px each)
             │   └─ Grass, Water, Paths, Trees, Benches...
             │
             ├─► Player (dựa trên character đã chọn)
             │   └─ Position: Gần center map
             │   └─ Animation: Idle + 4-direction walk
             │
             ├─► NPC (4 NPCs)
             │   ├─ NPC 1: "Bà cụ bán rau" (simple dialog)
             │   ├─ NPC 2: "Ông chú bán nước" (choice dialog)
             │   ├─ NPC 3: "Bạn nhỏ câu cá" (quest cần câu)
             │   └─ NPC 4: "Bác làm vườn" (quest hạt giống, tile 5,25)
             │
             ├─► Items
             │   ├─ Fishing Rod (hidden, spawns sau quest cần câu)
             │   └─ Seeds (hidden, spawns sau quest hạt giống ở tile 32,5)
             │
             ├─► InventorySystem
             │   └─ Runtime-only, reset mỗi lần chơi mới
             │
             ├─► CatFollower
             │   └─ State: IDLE (chưa theo)
             │
             └─► Camera
                 └─ Theo sau Player, target: viewport center
```

---

## 4. Main Game Loop (60 FPS)

```
┌──────────────────────────────────────────────────────┐
│  GameLoop.handle(now)  [AnimationTimer]              │
│  Chạy 60 lần/giây                                    │
└────────────┬─────────────────────────────────────────┘
             │
             ├─ Tính deltaTime (frame elapsed time)
             │
             ├─ GameWorld.update(dt, inputHandler)
             │  │
             │  ├─ Nếu Inventory Overlay ACTIVE:
             │  │  ├─ I/Esc đóng kho
             │  │  └─ Chặn movement/dialog/pickup
             │  │
             │  ├─ Nếu Dialog INACTIVE:
             │  │  ├─ Cập nhật Player (vị trí, animation)
             │  │  ├─ Kiểm tra collision (tile, NPC)
             │  │  ├─ Kiểm tra interaction (E key)
             │  │  └─ Kiểm tra item pickup
             │  │
             │  ├─ Nếu Dialog ACTIVE:
             │  │  ├─ DialogSystem xử lý input (Up/Down/Enter)
             │  │  └─ Render dialog text + choices
             │  │
             │  ├─ Cập nhật CatFollower
             │  │  └─ Nếu quest cần câu COMPLETED: Follow player
             │  │
             │  └─ Cập nhật Camera
             │
             ├─ Clear Canvas (dark background #1a1a2e)
             │
             ├─ GameWorld.render(gc)
             │  ├─ Render TileMap
             │  ├─ Render Items
             │  ├─ Render NPCs (+ name tags)
             │  ├─ Render Player
             │  ├─ Render CatFollower
             │  └─ Render DialogSystem (nếu active)
             │
             └─ InputHandler.update() [clear justPressed keys]
```

---

## 5. Player Movement & Interaction

### Điều Khiển
```
W / ↑                  : Di chuyển lên
S / ↓                  : Di chuyển xuống
A / ←                  : Di chuyển sang trái
D / →                  : Di chuyển sang phải
E / Enter              : Tương tác với NPC
I                      : Mở / đóng kho lưu trữ
Esc                    : Đóng kho lưu trữ khi đang mở
```

### Quy Trình Chuyển Động
```
Input (W/A/S/D)
    │
    ├─ InputHandler ghi lại keyPressed
    │
    └─► Player.update(dt, inputHandler)
        ├─ Tính vị trí mới (x, y)
        ├─ Kiểm tra collision với Tile solid
        │  └─ Nếu va chạm: Revert movement
        ├─ Cập nhật direction (UP, DOWN, LEFT, RIGHT)
        ├─ Cập nhật animation frame
        │  └─ Walk animation: 4 frames, loop
        │  └─ Idle frame: khi không di chuyển
        └─ Render sprite tại vị trí mới
```

### Quy Trình Tương Tác NPC
```
Player gần NPC (trong interactionRadius)
    │
    ├─ Hiển thị visual indicator ("!" hoặc "...")
    │
    └─ Người chơi bấm E/Enter
       │
       └─► DialogSystem.startDialog(npc, dialogData)
           ├─ State: INACTIVE → SHOWING_TEXT
           ├─ Lấy dialog text của NPC
           ├─ Chạy typewriter effect
           └─ Khi text đủ: chuyển sang SHOWING_CHOICES (nếu có)
```

---

## 6. Dialog & Quest Flow

### Dialog System States
```
┌────────────────────────────────────────┐
│   INACTIVE                             │
│ (Dialog không hiển thị)                │
└─────────────┬──────────────────────────┘
              │ startDialog()
              ▼
┌────────────────────────────────────────┐
│   SHOWING_TEXT                         │
│ (Typewriter effect, ~35 chars/s)       │
│ Space/Enter tới line tiếp theo         │
└─────────────┬──────────────────────────┘
              │ Hết tất cả lines
              ▼
┌────────────────────────────────────────┐
│   SHOWING_CHOICES                      │
│ ↑/↓: Chọn option                       │
│ Enter: Confirm                         │
└─────────────┬──────────────────────────┘
              │ Confirm choice
              ▼
┌────────────────────────────────────────┐
│   WAITING_QUEST_RESPONSE               │
│ (Nếu choice có quest effect)           │
│ Hiển thị response text, sau đó close   │
└─────────────┬──────────────────────────┘
              │ Kết thúc
              ▼
┌────────────────────────────────────────┐
│   INACTIVE                             │
│ (Quay lại gameplay)                    │
└────────────────────────────────────────┘
```

### Quest System: nhiều quest theo `questId`

`QuestSystem` quản lý state theo id:
- `fishing_rod`: quest tìm cần câu cho bạn nhỏ.
- `seeds`: quest tìm hạt giống cho bác làm vườn.

Mỗi quest có state `NOT_STARTED` → `ACTIVE` → `COMPLETED`, và item pickup được lưu bằng `QuestSystem.addItem(itemId)`.

### Quest: "Tìm cần câu cho bạn nhỏ" (`fishing_rod`)

**Stage 1: NOT_STARTED**
- Nói chuyện "Bạn nhỏ" NPC
- Dialog yêu cầu tìm cần câu
- QuestSystem.startQuest("fishing_rod") → ACTIVE

**Stage 2: ACTIVE**
- Fishing Rod item trở thành visible trên map
- Người chơi di chuyển đến vị trí item
- Khi va chạm với item → Auto pickup
- QuestSystem.addItem("fishing_rod")

**Stage 3: RETURN & COMPLETE**
- Quay lại "Bạn nhỏ" NPC
- Nói chuyện lần 2
- Dialog: "Cảm ơn! 🎣"
- QuestSystem.completeQuest("fishing_rod") → COMPLETED

**Kết Quả:**
- CatFollower: IDLE → FOLLOWING
- Mèo bắt đầu theo sát Player
- Có thể tiếp tục explore hoặc nói chuyện NPC khác

### Quest: "Tìm hạt giống cho bác làm vườn" (`seeds`)

**Stage 1: NOT_STARTED**
- Nói chuyện "Bác làm vườn" ở góc dưới-trái map, tile `(5,25)`
- Dialog yêu cầu tìm túi hạt giống
- QuestSystem.startQuest("seeds") → ACTIVE

**Stage 2: ACTIVE**
- Seeds item trở thành visible ở khu hoa phía trên-phải, tile `(32,5)`
- Người chơi di chuyển đến vị trí item
- Khi va chạm với item → Auto pickup
- QuestSystem.addItem("seeds")

**Stage 3: RETURN & COMPLETE**
- Quay lại "Bác làm vườn"
- Dialog hiển thị lời cảm ơn trước
- QuestSystem.completeQuest("seeds") → COMPLETED

**Kết Quả:**
- Random 1 reward từ pool `rose`, `sunflower`, `tulip`, `bonsai`
- Thêm reward vào InventorySystem runtime
- Hiện notification tên phần thưởng nhận được
- Không kích hoạt CatFollower; mèo chỉ follow sau quest `fishing_rod`

---

## 7. Item Pickup System

```
Item (Fishing Rod / Seeds) Object
    │ (visible = false, tồn tại trên map nhưng không render)
    │
    ├─ Quest tương ứng ACTIVE
    │ └─ visible = true (render sprite item)
    │
    └─► Player.update() → Check collision với Item.getBounds()
        │
        ├─ Va chạm: Item.onPickup()
        │  ├─ Phát hiệu ứng pickup (sparkle)
        │  ├─ item.setCollected(true)
        │  ├─ QuestSystem.addItem(itemId)
        │  └─ Render notification nhặt vật phẩm
        │
        └─ Không va chạm: Tiếp tục loop
```

---

## 8. Garden Reward & Inventory

### Reward Flow
```
Quest `seeds` ACTIVE + player đã nhặt Seeds
    │
    └─ Người chơi quay lại nói chuyện Bác làm vườn
       │
       ├─ DialogSystem hiển thị lời cảm ơn
       │
       └─ QuestSystem.completeQuest("seeds")
          │
          └─ GameWorld.onQuestComplete("seeds")
             ├─ InventorySystem.addRandomGardenReward()
             ├─ Random 1 item: rose / sunflower / tulip / bonsai
             ├─ Thêm vào kho runtime với quantity +1
             └─ Hiện notification tên phần thưởng
```

### Inventory Runtime
```
InventorySystem
    ├─ Lưu trong memory theo phiên chơi hiện tại
    ├─ Không ghi file save/load
    ├─ Restart game/app → inventory trống lại
    └─ Hoàn thành quest hạt giống trong lần chơi mới → reward random lại
```

### Inventory Overlay
```
Người chơi nhấn I
    │
    └─ inventoryOpen = true
       │
       ├─ Render panel "Kho lưu trữ"
       ├─ Hiển thị icon, tên cây/hoa, số lượng
       ├─ Nếu chưa có item: "Chưa có vật phẩm nào"
       └─ I hoặc Esc → đóng overlay
```

Khi inventory overlay đang mở:
- Player không di chuyển
- Không tương tác NPC
- Không nhặt item
- World vẫn render phía sau overlay

---

## 9. Cat Follower Behavior

### States

**IDLE (Quest NOT COMPLETED)**
```
- Vị trí cố định trên map
- Hiệu ứng idle animation (chuyển động nhẹ, vẫy đuôi)
- Không tương tác được
```

**FOLLOWING (Quest COMPLETED)**
```
- Theo sát Player ở phía sau
- Cách Player: ~40-60 pixels
- Sử dụng position history queue cho smooth trailing
- Walk animation giống Player
```

### Quy Trình Following
```
CatFollower.update(dt, player)
    │
    ├─ Nếu state = IDLE: Bỏ qua
    │
    └─ Nếu state = FOLLOWING:
       │
       ├─ Lấy target position từ player history
       │ (position từ vài frame trước)
       │
       ├─ Tính direction → target
       │
       ├─ Di chuyển với speed ~80-100 px/s
       │
       ├─ Cập nhật animation frame
       │
       └─ Render sprite tại vị trí mới
           (luôn ở phía sau Player)
```

---

## 10. Camera System

```
Camera.update(player, gameWorld)
    │
    └─ Target: Giữ Player ở center viewport
       │
       ├─ cameraX = player.x - (VIEWPORT_WIDTH / 2)
       ├─ cameraY = player.y - (VIEWPORT_HEIGHT / 2)
       │
       ├─ Clamp để không vượt quá map bounds
       │ ├─ cameraX: [0, worldWidth - VIEWPORT_WIDTH]
       │ └─ cameraY: [0, worldHeight - VIEWPORT_HEIGHT]
       │
       └─► TileMap.render(gc, camera)
           ├─ Chỉ render tiles trong viewport
           ├─ Offset bằng camera position
           └─ Smooth scrolling
```

---

## 11. Render Order (Z-depth)

```
1. TileMap (Background + Ground layer)
   └─ Grass, Water, Paths, Decoration (Trees on top)

2. Items
   └─ Fishing Rod / Seeds (nếu visible)
      └─ Sparkle effect

3. NPCs
   └─ Sprite + Name tag

4. Player
   └─ Sprite + Direction indicator

5. CatFollower
   └─ Sprite (trailing behind player)

6. DialogSystem (UI overlay)
   └─ Dialog box + Text + Choices
   └─ Quest indicators dạng stack ở góc phải
   └─ Cho animation typewriter effect

7. Inventory Overlay (nếu đang mở)
   └─ Dim background + panel "Kho lưu trữ"
   └─ Hiển thị reward cây/hoa trong phiên chơi
```

---

## 12. Input Handler

### Key Tracking
```
InputHandler
    ├─ keyPressed: Set<KeyCode> (keys held down)
    ├─ justPressed: Set<KeyCode> (keys pressed this frame)
    └─ justReleased: Set<KeyCode> (keys released this frame)
```

### Update Cycle
```
Frame 1:
    ├─ User presses W
    ├─ keyPressed.add(W)
    ├─ justPressed.add(W)
    └─ End of frame: justPressed stays W

Frame 2:
    ├─ User still holds W
    ├─ keyPressed.contains(W) = true
    ├─ justPressed.clear() [called in GameLoop.handle()]
    └─ justPressed.contains(W) = false

Frame 3:
    ├─ User releases W
    ├─ keyPressed.remove(W)
    ├─ justReleased.add(W)
    └─ End of frame: justReleased cleared next frame
```

### Usage in Code
```
// Movement (continuous while held)
if (inputHandler.isKeyPressed(KeyCode.W)) {
    player.moveUp();
}

// Dialog interaction (one-time per press)
if (inputHandler.isKeyJustPressed(KeyCode.ENTER)) {
    dialogSystem.advanceDialog();
}
```

---

## 13. Collision Detection

### AABB (Axis-Aligned Bounding Box)
```
Entity
    ├─ Position: (x, y) - world coordinates
    ├─ Size: (width, height) - pixels
    └─ getBounds() → Rectangle2D(x, y, width, height)
```

### Collision Checks
```
// 1. Player vs Tile Collision
Player.update():
    ├─ newX = x + velocity.x * dt
    ├─ newY = y + velocity.y * dt
    │
    ├─ For each corner of player bounds:
    │  └─ Check if TileMap.isSolid(worldX, worldY)
    │
    └─ If collision: Revert to old position

// 2. Player vs NPC Interaction
Player.update():
    ├─ For each NPC:
    │  ├─ Check if Player.interactionBounds
    │     overlaps with NPC.bounds
    │  │
    │  └─ If overlap + E pressed:
    │     └─ startDialog(npc)

// 3. Player vs Item Pickup
Player.update():
    ├─ For each Item:
    │  ├─ Check if Player.bounds overlaps Item.bounds
    │  │
    │  └─ If overlap:
    │     ├─ Item.setCollected(true)
    │     ├─ QuestSystem.addItem(itemId)
    │     └─ Show pickup notification
```

---

## 14. Complete Game Session Example

```
1. [STARTUP]
   Start game → Main.java → GameApplication.start()

2. [CHARACTER SELECTION]
   Show selection screen (Girl/Boy)
   User: Press → to Boy, Press Enter
   → characterSelected = true

3. [INIT]
   Create GameWorld:
   - TileMap with grass, water, trees, benches
   - Player at position (400, 300)
   - 4 NPCs scattered, gồm Bác làm vườn ở góc dưới-trái
   - Fishing Rod item (hidden)
   - Seeds item (hidden ở khu hoa phía trên-phải)
   - InventorySystem empty (runtime-only)
   - CatFollower at (300, 300) - IDLE
   - Start GameLoop

4. [EXPLORATION]
   GameLoop 60 FPS:
   - Player presses W → moves up
   - Player sees NPC marker "!"
   - Player presses E → Dialog starts
   - Dialog: "Tìm cần câu cho em" (typewriter effect)
   - User: Press Space/Enter
   - QuestSystem.startQuest("fishing_rod") → ACTIVE
   - Fishing Rod becomes visible on map

5. [QUEST ACTIVE]
   Player explores:
   - Sees Fishing Rod sprite with sparkle
   - Player moves to rod position
   - Auto pickup → QuestSystem.addItem("fishing_rod")
   - Rod disappears from map

6. [RETURN & COMPLETE]
   Player returns to "Bạn nhỏ"
   - Dialog: "Cảm ơn! 🎣"
   - QuestSystem.completeQuest("fishing_rod") → COMPLETED
   - CatFollower state: IDLE → FOLLOWING
   - Cat starts trailing Player

7. [GARDENER SIDE QUEST]
   Player visits "Bác làm vườn" at tile (5,25)
   - Dialog asks for seeds
   - QuestSystem.startQuest("seeds") → ACTIVE
   - Seeds become visible at tile (32,5)
   - Auto pickup → QuestSystem.addItem("seeds")
   - Return to gardener → QuestSystem.completeQuest("seeds") → COMPLETED
   - Random reward: rose / sunflower / tulip / bonsai
   - InventorySystem stores reward with quantity x1
   - Notification shows reward name

8. [INVENTORY]
   User presses I:
   - Inventory overlay opens
   - Gameplay pauses while overlay is open
   - Reward item appears with icon, name, quantity
   - User presses I or Esc to close

9. [CAT FOLLOWING]
   Player explores with cat:
   - Cat follows at ~50px distance
   - Cat animate walk when player moves
   - Cat animate idle when player stops
   - Continue playing until close game

10. [END]
   User closes window → Game ends
   Next launch creates a new empty inventory
```

---

## 15. Architecture Overview

```
Main.java
    │
    └─► GameApplication (extends Application)
        │
        ├─► Canvas (800x600)
        │
        ├─► GameLoop (AnimationTimer)
        │   └─ handle() chạy 60 FPS
        │
        ├─► GameWorld (Manager)
        │   ├─ TileMap (40x30 tiles)
        │   ├─ Player
        │   ├─ List<NPC> (4 NPCs)
        │   ├─ List<Item> (Fishing Rod, Seeds, etc.)
        │   ├─ CatFollower
        │   ├─ Camera
        │   ├─ DialogSystem
        │   ├─ QuestSystem
        │   └─ InventorySystem (runtime rewards)
        │
        ├─► InputHandler (Input tracking)
        │   ├─ keyPressed
        │   ├─ justPressed
        │   └─ justReleased
        │
        └─► Entities (Base classes)
            ├─ Entity (abstract)
            │  ├─ Player (extends Entity)
            │  ├─ NPC (extends Entity)
            │  ├─ Item (extends Entity)
            │  └─ CatFollower (extends Entity)
            │
            └─ World Components
               ├─ Tile (enum)
               └─ TileMap (2D array)
```

---

## 16. Performance Considerations

```
Optimization Techniques:

1. Camera Culling
   └─ Chỉ render tiles/entities visible trong viewport
   └─ Skip rendering offscreen objects

2. TileMap Lazy Evaluation
   └─ Không tạo object cho mỗi tile
   └─ 2D int array reference → Tile enum

3. Animation Caching
   └─ Reuse sprite frames
   └─ Tính frame index, không tạo new texture

4. Delta Time Capping
   └─ MAX_DELTA = 0.05s (50ms)
   └─ Prevent physics glitches nếu frame drop

5. Input Clearing
   └─ Clear justPressed mỗi frame
   └─ Avoid input lag từ frame trước
```

---

## 17. State Diagram

```
[CHARACTER SELECT]
       │
       ├─ Girl selected
       │
       └─► [GAMEPLAY]
            │
            ├─ Player moves (Input: WASD)
            │
            ├─ Encounter NPC (Input: E)
            │
            ├─► [DIALOG]
            │    ├─ Show text (typewriter)
            │    │
            │    ├─ Show choices (↑↓ select, Enter confirm)
            │    │
            │    └─ Quest triggered (startQuest(questId))
            │         │
            │         └─► [QUEST ACTIVE]
            │              ├─ Quest item visible
            │              │
            │              ├─ Player pickup item
            │              │
            │              └─► [RETURN TO NPC]
            │                   ├─ Dialog quest complete
            │                   │
            │                   ├─ If fishing_rod: [CAT FOLLOWING]
            │                   │    └─ Cat state: FOLLOWING
            │                   │
            │                   ├─ If seeds: random garden reward
            │                   │    └─ InventorySystem.addRandomGardenReward()
            │                   │
            │                   └─ Continue gameplay
            │
            ├─ Press I
            │    └─► [INVENTORY OVERLAY]
            │         ├─ Gameplay paused
            │         ├─ Show runtime reward inventory
            │         └─ I/Esc closes overlay
            │
            └─ Exit game
```

---

## 18. File Structure & Key Classes

```
src/main/java/com/game/
│
├── Main.java
│   └─ Entry point: Application.launch()
│
├── GameApplication.java
│   ├─ Stage setup
│   ├─ Canvas creation
│   ├─ Character selection UI
│   └─ GameLoop init
│
├── core/
│   ├── GameLoop.java (AnimationTimer)
│   │   └─ 60 FPS update + render
│   │
│   ├── InputHandler.java
│   │   ├─ Key tracking (pressed/justPressed/justReleased)
│   │   └─ isKeyPressed(), isKeyJustPressed()
│   │
│   └── Camera.java
│       └─ Follow player, viewport culling
│
├── dialog/
│   ├── DialogSystem.java
│   │   ├─ State machine (INACTIVE, SHOWING_TEXT, SHOWING_CHOICES)
│   │   ├─ Typewriter effect
│   │   └─ Choice selection
│   │
│   ├── DialogData.java
│   │   ├─ NPC dialogs (text, choices)
│   │   └─ Effect triggers (quest, relationship)
│   │
│   └── QuestSystem.java
│       ├─ Quest state (NOT_STARTED, ACTIVE, COMPLETED)
│       └─ Multi-quest logic by questId (`fishing_rod`, `seeds`)
│
├── inventory/
│   └── InventorySystem.java
│       ├─ Runtime-only garden reward inventory
│       ├─ Random reward pool (rose, sunflower, tulip, bonsai)
│       └─ Item quantities for inventory overlay
│
├── entity/
│   ├── Entity.java (abstract)
│   │   ├─ x, y, width, height
│   │   ├─ getBounds(), render(), update()
│   │   └─ Animation support
│   │
│   ├── Player.java
│   │   ├─ Movement (WASD)
│   │   ├─ 4-direction walk animation
│   │   ├─ Collision detection
│   │   └─ Interaction radius
│   │
│   ├── NPC.java
│   │   ├─ DialogData
│   │   ├─ Name tag render
│   │   └─ Interaction range
│   │
│   ├── Item.java
│   │   ├─ visible flag
│   │   ├─ Auto pickup on collision
│   │   └─ Sparkle animation
│   │
│   └── CatFollower.java
│       ├─ State (IDLE, FOLLOWING)
│       ├─ Position history queue
│       └─ Trailing behavior
│
├── util/
│   ├── AssetManager.java
│   │   └─ Sprite/texture management
│   │
│   ├── PixelArtGenerator.java
│   │   └─ Generate sprites procedurally
│   │
│   └── SpriteSheet.java
│       └─ Sprite frame management
│
└── world/
    ├── GameWorld.java (Manager)
    │   ├─ TileMap, Player, NPCs, Items, Cat, Camera, InventorySystem
    │   ├─ update(dt, inputHandler)
    │   ├─ render(gc)
    │   └─ renderInventoryOverlay(gc)
    │
    ├── Tile.java (enum)
    │   ├─ GRASS, WATER, PATH, TREE, BENCH, FENCE, BRIDGE
    │   └─ solid, sprite coords
    │
    └── TileMap.java
        ├─ 2D int array map (40x30)
        ├─ getTile(), isSolid()
        └─ render(gc, camera) with culling
```

---

## 19. Custom Asset Loading

```
AssetManager.loadAll()
    │
    ├─► Kiểm tra file ảnh trong src/main/resources/assets/ (vd: player.png)
    │   ├─ Nếu TỒN TẠI: Load Image từ file (Cho phép thay đổi giao diện không cần code)
    │   └─ Nếu KHÔNG TỒN TẠI: Fallback gọi PixelArtGenerator tạo hình mặc định
    │
    └─► Đưa Image/SpriteSheet vào bộ nhớ (HashMap) để sử dụng
```
Game hỗ trợ thay đổi toàn bộ visual (nhân vật, map, item, reward icon) chỉ bằng cách copy file `.png` (khung hình 32x32px) vào thư mục `assets`.

---

## 20. Fishing Mini-game

### Điều kiện kích hoạt
```
Player đã hoàn thành quest `fishing_rod`
    │
    ├─ Đứng gần tile WATER (full bounds + 16px chạm nước)
    │  └─ Hiển thị biểu tượng 'F' nổi trên đầu Player
    │
    └─ Nhấn F
       └─ PlayerState: NORMAL → FISHING
          └─ Khóa movement / dialog / pickup / inventory / map cho tới khi kết thúc
```

### Waiting Phase
```
FishingMiniGame.start()
    ├─ phase = WAITING
    ├─ waitDuration = Random.Range(2.0, 5.0)
    ├─ Hiển thị hình ảnh Player đang cầm cần câu hướng ra nước (sử dụng sprite sheet fishing_rod_action.png tương ứng 4 hướng)
    └─ Player đứng yên chờ cá cắn

Hết waitDuration
    └─ phase = POWER_BAR
       └─ Hiển thị "!" trên đầu Player
```

### Mini-game Phase: Power Bar
```
currentValue = 20
maxValue = 100
duration = 7 giây

Mỗi frame:
    ├─ currentValue -= 15 * deltaTime
    ├─ Clamp currentValue không thấp hơn 0
    └─ Vẽ hiệu ứng bọt khí nổi lên cách Player 40px theo hướng nhìn của Player

Mỗi lần Space justPressed:
    └─ currentValue += 8, clamp không quá 100
```

### Kết quả
```
Thắng:
    ├─ Nếu currentValue >= 100 trong vòng 7 giây
    ├─ InventorySystem.addRandomFishReward()
    ├─ Random 1 cá: Cá chép / Cá rô / Cá trê / Cá vàng
    └─ PlayerState: FISHING → NORMAL

Thua:
    ├─ Nếu hết 7 giây mà currentValue < 100
    ├─ Không thêm item vào inventory
    └─ PlayerState: FISHING → NORMAL
```

Reward cá là runtime-only giống garden reward; restart game/app sẽ tạo inventory mới.

---

## Tóm Tắt
- **Engine**: Java 17 + JavaFX 21 Canvas 2D
- **FPS**: 60 frames per second (60 Hz)
- **World**: Procedural tile-based map (40x30 tiles)
- **Core Gameplay**: Explore → Dialog → Quest → Collect → Reward/Completion → Fishing mini-game (cat follows only after fishing quest; garden/fish rewards go to inventory)
- **Input**: WASD movement, E interaction, F fishing, I inventory, M map, Arrow keys for menus
- **Rendering**: Layer-based (tiles → items → NPCs → player → cat → UI → fishing UI → inventory overlay)
- **State Management**: Character select → Gameplay loop → Dialog/Multi-quest → Inventory overlay/Cat following/Fishing
- **Extensibility**: Hỗ trợ Custom Asset thông qua thư mục resources.

Game được thiết kế để mở rộng dễ dàng với Inventory, Save/Load, Minimap, và các quest phụ theo roadmap trong PLAN.md.
