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
│  - Khởi tạo InputHandler + SaveSystem       │
│  - Load AudioSettingsStore + AudioManager   │
│  - Phát menu_theme.mp3 cho front screens    │
│  - Hiển thị Character Selection             │
│  - Chọn Girl / Boy                          │
│  - Sau đó mới hiện Continue / New Game      │
└─────────────────────────────────────────────┘
```

### Startup Branch

```
Character Selection Screen
   │
   └─ Chọn Girl / Boy
      └─ Continue / New Game Screen
         ├─ Continue -> tải slot save của giới tính đã chọn
         └─ New Game -> tạo phiên mới cho giới tính đã chọn
```

### Save Slots

```
%USERPROFILE%/.tiny-village-game/
   ├─ save-girl.properties
   ├─ save-boy.properties
   └─ settings.properties
```

Mỗi giới tính có một slot save riêng. Continue sẽ tải slot của nhân vật đã chọn trước đó, còn auto-save khi thoát sẽ ghi đè slot đúng với giới tính của world hiện tại. Audio settings dùng file chung `settings.properties`, không gắn với slot Girl/Boy.

---

## 1a. Continue / New Game Screen

### Giao Diện
- Background và title cùng style với màn hình chọn nhân vật.
- Menu nhỏ ở giữa màn hình gồm 2 lựa chọn: `Continue` và `New Game`.
- Subtitle hiển thị giới tính vừa chọn để người chơi biết mình đang thao tác với slot nào.
- Dòng trạng thái phía dưới hiển thị nhanh slot của giới tính đó đang có save hay chưa.

### Điều Khiển
```
↑ / ↓ hoặc W / S    : Chuyển giữa Continue và New Game
Enter / Space       : Xác nhận lựa chọn
Esc                 : Quay lại Character Selection
P                   : Mở / đóng Audio Settings
```

### Âm Thanh Front Screen
- Character Selection và Continue / New Game dùng chung BGM `menu_theme.mp3`.
- Di chuyển lựa chọn dùng `dialog_advance.wav` để giữ cảm giác tương tác nhẹ.
- Xác nhận bằng `Enter`/`Space` dùng `ui_confirm.wav`.
- `Esc` từ Continue / New Game quay lại Character Selection dùng `ui_back.wav`.
- `P` mở Audio Settings overlay trên front screen; khi overlay đang mở thì input menu bên dưới bị chặn cho tới khi đóng.

### Quy Trình
```
Continue / New Game Screen
   │
   ├─ Continue
   │  ├─ Nếu giới tính đã chọn chưa có save: báo lỗi và ở lại màn hình này
   │  └─ Nếu có save: load slot của Girl/Boy đã chọn rồi vào gameplay
   │
   └─ New Game
   └─ Khởi tạo phiên mới theo giới tính đã chọn rồi vào gameplay
```

---

## 2. Character Selection Screen

### Giao Diện
- **Background**: Gradient bầu trời xanh (#E0F7FA) → bãi cỏ (#B2EBF2)
- **Clouds**: Hoạt ảnh mây trôi nhẹ nhàng
- **Character Cards**:
   - Girl - mặc định option 0, preview ưu tiên lấy từ `src/main/resources/assets/player_preview.png`
   - Boy - option 1, preview ưu tiên lấy từ `src/main/resources/assets/player2_preview.png`
   - Nếu thiếu file preview, game fallback về frame idle nhìn xuống trong spritesheet `player.png` / `player2.png`, rồi scale lên trên card
   - Khi được chọn: Viền màu, hiệu ứng bouncing

### Điều Khiển
```
← / → Arrow Keys    : Chuyển giữa Girl/Boy
Enter / Space       : Xác nhận nhân vật và sang màn hình Continue / New Game
P                   : Mở / đóng Audio Settings
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
      └─ Chuyển sang Continue / New Game Screen
      └─ Giữ lại giới tính đã chọn để quyết định slot save tương ứng
```

Character Selection luôn xuất hiện trước để người chơi chốt giới tính, rồi mới quyết định `Continue` hay `New Game` cho đúng slot save.

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
             │   └─ Biên giới bao quanh map là nước biển (Water tiles).
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
             │   ├─ Fishing Rod (hidden, random 1 vị trí hợp lệ khắp map khi quest active)
             │   └─ Seeds (hidden, random 1 vị trí hợp lệ khắp map khi quest active)
             │
             ├─► InventorySystem
             │   └─ Được lưu theo save slot hiện tại, khôi phục khi Continue
             │
             ├─► CatFollower
             │   └─ State: IDLE (chưa theo)
             │
             └─► Camera
                 └─ Theo sau Player, target: viewport center
             │
             └─► Audio Transition
                └─ GameApplication chuyển BGM từ `menu_theme.mp3` sang `gameplay_loop.mp3` ngay trước khi `GameLoop.start()`
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
             │  ├─ Nếu Audio Settings Overlay ACTIVE:
             │  │  ├─ P/Esc đóng overlay
             │  │  ├─ ↑/↓ chọn dòng, ←/→ chỉnh volume, Enter bật/tắt mute
             │  │  └─ Chặn movement/dialog/pickup/fishing/inventory/cat care và tạm dừng timer tạm thời phía dưới
             │  │
             │  ├─ Nếu Inventory Overlay ACTIVE:
             │  │  ├─ I/Esc đóng kho
             │  │  └─ Chặn movement/dialog/pickup/cat care
             │  │
             │  ├─ Nếu Cat Care Overlay ACTIVE:
             │  │  ├─ C/Esc đóng menu chăm mèo
             │  │  ├─ ↑/↓ chọn cá trong inventory đã lưu
             │  │  ├─ Enter cho ăn vật phẩm đang chọn
             │  │  └─ Chặn movement/dialog/pickup/fishing
             │  │
             │  ├─ Nếu Dialog INACTIVE:
             │  │  ├─ Cập nhật Player (vị trí, animation)
             │  │  ├─ Kiểm tra collision (tile, NPC)
             │  │  ├─ Enter: interaction với NPC
             │  │  ├─ E: vuốt mèo khi ở gần
             │  │  ├─ C: gọi mèo hoặc mở Cat Care menu
             │  │  ├─ Nếu Player thực sự di chuyển trên cỏ: phát footsteps xen kẽ theo cadence
             │  │  └─ Kiểm tra item pickup
             │  │
             │  ├─ Nếu Dialog ACTIVE:
             │  │  ├─ DialogSystem xử lý input (Up/Down/Enter)
             │  │  └─ Render dialog text + choices
             │  │
             │  ├─ Cập nhật CatFollower
             │  │  └─ Nếu quest cần câu COMPLETED: Follow / Calling / cooldown chăm sóc
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
             │  ├─ Render DialogSystem (nếu active)
             │  ├─ Render Cat Care Overlay (nếu active)
             │  └─ Render Audio Settings Overlay (nếu active, luôn top-most)
             │
             └─ InputHandler.update() [clear justPressed keys]
```

---

## 4a. Save Slots & Auto Save

### Dữ Liệu Được Lưu
- Giới tính nhân vật đã chọn
- Vị trí player
- Hướng nhìn hiện tại của player
- Thời điểm save gần nhất để tính tiến trình quest khi offline
- Quest state theo `questId`
- Countdown mở lại của quest lặp `seeds`
- Danh sách quest item đã nhặt
- Vị trí spawn hiện tại của quest item đang dùng trong world
- Inventory persistent (hoa/cây/cá và quantity)
- Cat state dài hạn: unlocked, vị trí, mood, affection, state
- Audio volume/mute global không nằm trong save slot; chúng được lưu riêng trong `settings.properties`

### Dữ Liệu Không Lưu
- Dialog đang mở dở
- Fishing mini-game đang diễn ra
- Inventory overlay / Cat Care overlay / minimap đang mở
- Notification tạm thời
- Cooldown pet/feed/call đang đếm

Load luôn khôi phục tiến trình dài hạn, bao gồm cả vị trí quest item đang hoạt động, đồng thời trừ thời gian offline khỏi countdown quest lặp trước khi vào gameplay. Với quest `seeds`, timer được chuẩn hóa theo delay cấu hình hiện tại trước khi world mới áp dụng save, rồi reset các state tạm để tránh quay lại giữa một tương tác dở dang.

### Continue / New Game Flow
```
Character Selection
   └─ Continue / New Game Screen
      ├─ Continue
      │  └─ SaveSystem.load(selectedGender)
      │  └─ Dựng GameWorld mới theo giới tính đã chọn
      │  └─ Apply snapshot save vào world mới
      │
      └─ New Game
         └─ Tạo GameWorld mới trống theo giới tính đã chọn
```

### Auto Save / Continue Flow
```
Khi mở game:
   └─ Luôn vào Character Selection
      └─ Continue / New Game Screen -> load hoặc tạo world theo giới tính đã chọn

Khi đóng game:
   └─ Application.stop()
      └─ Nếu đang có GameWorld -> tự động save snapshot hiện tại vào slot Girl/Boy đúng với world đang chơi

Nếu game bị tắt trong lúc quest `seeds` đang countdown mở lại, khoảng thời gian ngoài game vẫn được trừ vào timer khi người chơi bấm `Continue`; nếu save cũ còn giữ timer lớn hơn delay cấu hình hiện tại, timer đó sẽ bị chặn về đúng mốc đang dùng.

---

## 4b. Audio System & Settings

### Mục Tiêu Thiết Kế
- Dùng ít file âm thanh nhất có thể và tái sử dụng theo ngữ nghĩa gần nhất.
- Không thêm SFX riêng cho những event không có asset phù hợp; các event đó giữ im lặng.
- Thiết lập âm thanh là global cho toàn bộ game, không phụ thuộc slot save giới tính.

### Runtime Audio
- `AudioManager` là singleton quản lý BGM loop và one-shot SFX.
- BGM front screen dùng `menu_theme.mp3`.
- BGM gameplay dùng `gameplay_loop.mp3`.
- SFX one-shot được cache và phát lại theo event key thay vì hard-code file path ở từng nơi.
- Nếu thiếu asset hoặc media lỗi, game chỉ log 1 lần và tiếp tục chạy, không crash flow chính.

### Audio Settings Overlay
- Có mặt ở cả front screen và gameplay, mở/tắt bằng `P`.
- Trong gameplay, overlay này có ưu tiên cao nhất trong `GameWorld.update()`, nên khi mở sẽ chặn input và tạm dừng các tương tác/timer tạm phía dưới.
- Điều khiển overlay:

```
↑ / ↓ hoặc W / S    : Chọn dòng thiết lập
← / → hoặc A / D    : Tăng / giảm volume cho hàng volume
Enter / Space       : Bật / tắt mute của mục đang chọn
Esc hoặc P          : Đóng overlay
```

- Hai hàng volume: `Nhạc nền`, `Hiệu ứng`.
- Hai hàng mute: `Tắt nhạc`, `Tắt hiệu ứng`.
- Giá trị mặc định: music `28%`, sfx `70%`, cả hai đều không mute.
- Khi người chơi thay đổi volume/mute, `AudioManager.applySettings(...)` được áp dụng ngay.
- Khi overlay đóng hoặc game thoát, settings được ghi vào `%USERPROFILE%/.tiny-village-game/settings.properties`.

### Minimal Audio Mapping

| Asset | Dùng cho |
| --- | --- |
| `menu_theme.mp3` | Character Selection + Continue / New Game |
| `gameplay_loop.mp3` | Gameplay loop |
| `footstep_grass_1.wav` | Footstep cadence trên cỏ |
| `footstep_grass_2.wav` | Footstep cadence xen kẽ trên cỏ |
| `dialog_open.wav` | Bắt đầu một dialog mới |
| `dialog_advance.wav` | Chuyển line thoại, đổi lựa chọn, và các tương tác nhẹ như đổi selection/menu navigation/call mèo/bắt đầu câu cá |
| `ui_confirm.wav` | Xác nhận lựa chọn, mở kho, mở Cat Care, pickup item, reward cá, xác nhận choice, cho mèo ăn thành công |
| `ui_back.wav` | Quay lại Character Selection, đóng dialog, đóng kho, đóng Cat Care, đóng Audio Settings, báo trượt câu cá |
| `quest_start.wav` | Quest chuyển sang `ACTIVE` |
| `quest_complete.wav` | Quest chuyển sang `COMPLETED` |

### Những Event Giữ Im Lặng
- Toggle minimap `M`.
- Continue thất bại vì slot chưa có save.
- Quest reset timer chạy nền.
- Notification text / badge prompt / cooldown lỗi.
- Auto-save lúc thoát game.
```

---

## 5. Player Movement & Interaction

### Điều Khiển
```
W / ↑                  : Di chuyển lên
S / ↓                  : Di chuyển xuống
A / ←                  : Di chuyển sang trái
D / →                  : Di chuyển sang phải
Enter                  : Tương tác với NPC
E                      : Vuốt mèo khi ở gần
C                      : Gọi mèo / mở menu chăm mèo
F                      : Câu cá khi đứng gần nước
M                      : Mở / đóng bản đồ nhỏ
I                      : Mở / đóng kho lưu trữ
Esc                    : Đóng kho lưu trữ hoặc Cat Care menu đang mở
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
   └─ Người chơi bấm Enter
      │
      └─► DialogSystem.startDialog(npc, dialogData)
         ├─ State: INACTIVE → SHOWING_TEXT
         ├─ Lấy dialog text của NPC
         ├─ Chạy typewriter effect
         └─ Khi text đủ: chuyển sang SHOWING_CHOICES (nếu có)
```

### Quy Trình Cat Care
```
Player đã hoàn thành quest `fishing_rod`
   │
   ├─ Bấm C khi mèo ở xa
   │  └─ CatFollower: WAITING → CALLING
   │  └─ Mèo chạy nhanh về gần Player, sau đó quay lại WAITING ở cạnh Player
   │
   ├─ Bấm E khi mèo ở gần
   │  └─ Nếu pet cooldown = 0:
   │     ├─ mood += 5
   │     ├─ affection += 2
   │     └─ Bắt đầu pet cooldown 6 giây
   │
   └─ Bấm C khi mèo đã ở gần
      └─ catCareOpen = true
         ├─ Hiển thị mood, heart level, affection, cooldown
         ├─ Hiển thị danh sách cá trong inventory đã lưu
         ├─ ↑/↓ chọn cá
         ├─ Enter: cho ăn cá đã chọn
         └─ C/Esc: đóng menu
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

### Audio Hooks Trong Dialog
- `startDialog()` phát `dialog_open.wav`.
- Khi người chơi skip typewriter hoặc sang line kế tiếp, game phát `dialog_advance.wav`.
- Khi di chuyển selection trong choice dialog, game cũng dùng `dialog_advance.wav`.
- Khi xác nhận choice bằng `Enter`/`Space`, game phát `ui_confirm.wav`.
- Khi dialog đóng và trả control về gameplay, game phát `ui_back.wav`.
- Quest callback từ dialog phát `quest_start.wav` hoặc `quest_complete.wav` tương ứng.

### Quest System: nhiều quest theo `questId`

`QuestSystem` quản lý state theo id:
- `fishing_rod`: quest tìm cần câu cho bạn nhỏ.
- `seeds`: quest tìm hạt giống cho bác làm vườn.

Mỗi quest có state `NOT_STARTED` → `ACTIVE` → `COMPLETED`, và item pickup được lưu bằng `QuestSystem.addItem(itemId)`.
Riêng quest `fishing_rod` là quest một lần: sau khi chuyển sang `COMPLETED`, quest đóng hẳn và NPC không thể kích hoạt lại quest này nữa.
Riêng quest `seeds` là quest lặp: sau khi `COMPLETED`, quest indicator giữ trạng thái thành công trong 10 giây đầu, sau đó đổi sang countdown mở lại quest cho tới khi tự reset về `NOT_STARTED`.

### Quest: "Tìm cần câu cho bạn nhỏ" (`fishing_rod`)

**Stage 1: NOT_STARTED**
- Nói chuyện "Bạn nhỏ" NPC
- Dialog yêu cầu tìm cần câu
- QuestSystem.startQuest("fishing_rod") → ACTIVE

**Stage 2: ACTIVE**
- Fishing Rod item được chọn ngẫu nhiên vào 1 ô hợp lệ bất kỳ trên map rồi trở thành visible
- Người chơi di chuyển đến vị trí item
- Khi va chạm với item → Auto pickup
- QuestSystem.addItem("fishing_rod")
- Phát `ui_confirm.wav` khi pickup thành công

**Stage 3: RETURN & COMPLETE**
- Quay lại "Bạn nhỏ" NPC
- Nói chuyện lần 2
- Dialog: "Cảm ơn! 🎣"
- QuestSystem.completeQuest("fishing_rod") → COMPLETED
- Quest đóng lại vĩnh viễn, không quay về `NOT_STARTED` hay `ACTIVE`

**Kết Quả:**
- CatFollower: IDLE → WAITING
- Mèo được mở khóa tương tác nhưng vẫn đứng tại chỗ cho tới khi Player bấm `C`
- Cat Care được mở khóa với mood ban đầu `60/100`
- Heart level ban đầu là `1`, suy ra từ affection hiện tại của mèo
- Player có thể dùng `C` để gọi mèo lại gần / mở Cat Care và `E` để vuốt mèo khi đã ở gần
- Những lần nói chuyện sau đó với NPC chỉ còn dialog hậu quest, không nhận lại quest `fishing_rod`
- Có thể tiếp tục explore hoặc nói chuyện NPC khác

### Quest: "Tìm hạt giống cho bác làm vườn" (`seeds`)

**Stage 1: NOT_STARTED**
- Nói chuyện "Bác làm vườn" ở góc dưới-trái map, tile `(5,25)`
- Dialog yêu cầu tìm túi hạt giống
- QuestSystem.startQuest("seeds") → ACTIVE

**Stage 2: ACTIVE**
- Seeds item được chọn ngẫu nhiên vào 1 ô hợp lệ bất kỳ trên map rồi trở thành visible
- Người chơi di chuyển đến vị trí item
- Khi va chạm với item → Auto pickup
- QuestSystem.addItem("seeds")
- Phát `ui_confirm.wav` khi pickup thành công

**Stage 3: RETURN & COMPLETE**
- Quay lại "Bác làm vườn"
- Dialog hiển thị lời cảm ơn trước
- QuestSystem.completeQuest("seeds") → COMPLETED
- Quest indicator hiển thị `✓ Hoàn thành!` trong 10 giây đầu
- Sau 10 giây, cùng quest đó đổi sang countdown phần còn lại của mốc 60 giây trước khi quest tự mở lại

**Kết Quả:**
- Random 1 reward từ pool `rose`, `sunflower`, `tulip`, `bonsai`
- Thêm reward vào InventorySystem persistent
- Hiện notification tên phần thưởng nhận được
- Quest indicator đổi từ trạng thái thành công sang countdown mở lại sau 10 giây
- Hết 60 giây: quest reset về `NOT_STARTED`, item Seeds được phép xuất hiện lại ở lần nhận mới
- Không kích hoạt CatFollower; quest `fishing_rod` chỉ mở khóa việc gọi và chăm mèo

---

## 7. Item Pickup System

```
Item (Fishing Rod / Seeds) Object
   │ (visible = false, tồn tại trên map nhưng không render)
   │
   ├─ Quest tương ứng ACTIVE
   │ ├─ Chọn ngẫu nhiên 1 tile hợp lệ trên toàn bộ map
   │ └─ visible = true (render sprite item)
   │
   ├─ Continue / Load khi quest vẫn ACTIVE
   │ └─ Khôi phục lại đúng tọa độ spawn đã save trước đó
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

## 8. Garden Reward, Fish Reward & Inventory

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
            ├─ Thêm vào kho đã lưu với quantity +1
            ├─ Hiện notification tên phần thưởng
            ├─ 10 giây đầu: quest indicator hiển thị `✓ Hoàn thành!`
            ├─ Sau đó: quest indicator đổi sang countdown mở lại
            └─ Hết 60 giây, quest `seeds` tự mở lại
```

   ### Inventory Persistent
```
InventorySystem
   ├─ Lưu theo save slot hiện tại
   ├─ Chứa cả reward cây/hoa và cá câu được
   ├─ Được ghi/đọc cùng dữ liệu save/load
   ├─ Continue → khôi phục lại đúng item và số lượng
   └─ New Game → bắt đầu với inventory trống
```

### Fish Reward Flow
```
Fishing Mini-game thắng
   │
   └─ InventorySystem.addRandomFishReward()
      ├─ Random 1 cá: Cá chép / Cá rô / Cá trê / Cá vàng
   ├─ Thêm vào inventory đã lưu với quantity +1
      └─ Có thể dùng lại trong Cat Care menu để cho mèo ăn
```

### Inventory Overlay
```
Người chơi nhấn I
   │
   └─ inventoryOpen = true
      │
      ├─ Render panel "Kho lưu trữ"
      ├─ Hiển thị icon, tên cây/hoa/cá, số lượng
      ├─ Nếu chưa có item: "Chưa có vật phẩm nào"
      └─ I hoặc Esc → đóng overlay
```

   - Mở inventory phát `ui_confirm.wav`.
   - Đóng inventory phát `ui_back.wav`.

Inventory overlay chỉ để xem vật phẩm đã lưu. Hành động cho mèo ăn diễn ra trong Cat Care menu riêng, không thực hiện trực tiếp trong panel kho.

Khi inventory overlay đang mở:
- Player không di chuyển
- Không tương tác NPC
- Không nhặt item
- Không mở Cat Care
- Esc sẽ đóng inventory ngay trong frame đó
- World vẫn render phía sau overlay

---

## 9. Cat Follower & Care Behavior

### States

**IDLE (Quest NOT COMPLETED)**
```
- Vị trí cố định trên map
- Hiệu ứng idle animation (chuyển động nhẹ, vẫy đuôi)
- Không tương tác được
```

**WAITING (Quest COMPLETED)**
```
- Đã mở khóa tương tác sau quest `fishing_rod`
- Đứng yên tại vị trí hiện tại cho tới khi Player chủ động gọi bằng `C`
- Khi Player lại gần: hiển thị 2 badge phím `E` và `C` trên đầu mèo
   theo cùng style với badge `F` khi đứng gần WATER
```

**CALLING (Player bấm `C` khi mèo ở xa)**
```
- Chỉ hoạt động sau khi quest `fishing_rod` đã COMPLETED
- Mèo tăng tốc chạy về gần Player
- Khi vào phạm vi gần: CALLING → WAITING
- Có call cooldown 2 giây để tránh spam
```

### Mood & Heart Level
```
Mood:
   ├─ Thang 0 → 100
   ├─ Mở khóa Cat Care: mood = 60
   ├─ Vuốt ve thành công: +5 mood
   └─ Cho ăn 1 cá: +15 mood

Heart level:
   ├─ Suy ra từ affection đã lưu trong file save
   ├─ Mở khóa Cat Care: heart level = 1
   ├─ Vuốt ve thành công: +2 affection
   ├─ Cho ăn 1 cá: +8 affection
   └─ Mốc heart:
      ├─ 0-19   → Heart 1
      ├─ 20-39  → Heart 2
      ├─ 40-69  → Heart 3
      ├─ 70-109 → Heart 4
      └─ 110+   → Heart 5
```

### Cooldown
```
- Pet cooldown : 6 giây sau mỗi lần vuốt ve thành công
- Feed cooldown: 10 giây sau mỗi lần cho ăn cá thành công
- Call cooldown: 2 giây sau mỗi lần gọi mèo bằng C
```

### Quy Trình Gọi Mèo
```
CatFollower.update(dt, player)
    │
    ├─ Nếu state = IDLE: Bỏ qua
    │
    ├─ Nếu state = WAITING:
    │  └─ Giữ vị trí hiện tại, chờ Player bấm `C`
    │
    └─ Nếu state = CALLING:
       │
       ├─ Lấy vị trí neo cạnh Player
       ├─ Tính direction → target
       │
       ├─ Di chuyển nhanh về gần Player
       │
       ├─ Cập nhật animation frame
       │
       └─ Khi đủ gần: state quay lại WAITING
```

   - Bấm `C` gọi mèo từ xa dùng `dialog_advance.wav`.
   - Mở Cat Care khi mèo đã ở gần dùng `ui_confirm.wav`.
   - Đóng Cat Care bằng `C`/`Esc` dùng `ui_back.wav`.
   - Di chuyển selection trong Cat Care dùng `dialog_advance.wav`.
   - Cho mèo ăn thành công dùng `ui_confirm.wav`.
   - Vuốt mèo thành công dùng `dialog_advance.wav`.

   ### Quy Trình Cho Ăn
   ```
   Player đứng gần mèo + bấm C
      │
      └─ Cat Care menu mở ra
         │
         ├─ InventorySystem.getFishItems()
         ├─ ↑/↓ chọn cá muốn dùng
         ├─ Enter xác nhận
         │   ├─ Nếu feed cooldown > 0: hiện notification chờ cooldown
         │   ├─ Nếu có cá: consume 1 item từ inventory
         │   ├─ CatFollower.feed()
         │   └─ Tăng mood + affection, có thể tăng heart level
         │
         └─ C/Esc đóng menu
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

```

---

## 11. Autotiling System

```
GrassTile.render(gc, groundLayer, col, row, x, y)
   │
   ├─ Tính bitmask 4-hướng (Lên, Phải, Xuống, Trái)
   │  └─ Kiểm tra tile xung quanh có phải là WATER / WATER_EDGE / PATH không (Out of bounds coi như Water)
   │  └─ Bitmask: (LEFT_WATER ? 1 : 0) | (DOWN_WATER ? 2 : 0) | (RIGHT_WATER ? 4 : 0) | (UP_WATER ? 8 : 0)
   │
   ├─ Lấy texture từ cache (16 biến thể)
   │  ├─ Nếu file `grass_autotile.png` / `dark_grass_autotile.png` tồn tại trong resource:
   │  │  └─ Load từ spritesheet ngoài theo mapping:
   │  │     1001 (9)  1000 (8)  1100 (12) 1101 (13)
   │  │     0001 (1)  0000 (0)  0100 (4)  0101 (5)
   │  │     0011 (3)  0010 (2)  0110 (6)  0111 (7)
   │  │     1011 (11) 1010 (10) 1110 (14) 1111 (15)
   │  │
   │  ├─ Nếu file `grass_autotile_extended.png` / `dark_grass_autotile_extended.png` tồn tại:
   │  │  └─ Chỉ dùng hàng đầu tiên của spritesheet để lấy 16 biến thể cardinal cơ bản.
   │  │  └─ Không còn dùng diagonal mask để bo góc tile cỏ quanh hồ nước hoặc PATH.
   │  │
   │  ├─ Nếu KHÔNG có file (Fallback):
   │  │  └─ Tự động sinh pixel art bằng code.
   │  │  └─ Thêm viền tối (Shadow) theo cạnh tiếp xúc, không bo góc chéo.
   │  │
   │  └─ Render tile cỏ với cạnh thẳng quanh WATER / WATER_EDGE / PATH thay vì cắt góc bo tròn
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
   └─ Sprite (trailing / calling / heart indicator)

6. DialogSystem (UI overlay)
   └─ Dialog box + Text + Choices
   └─ Quest indicators dạng stack ở góc phải, gồm cả countdown mở lại cho `seeds`
   └─ Cho animation typewriter effect

7. Inventory Overlay (nếu đang mở)
   └─ Dim background + panel "Kho lưu trữ"
   └─ Hiển thị reward cây/hoa/cá trong phiên chơi

8. Cat Care Overlay (nếu đang mở)
   └─ Dim background + panel "Chăm sóc mèo"
   └─ Hiển thị mood, heart level, affection, cooldown
   └─ Hiển thị danh sách cá để cho ăn
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
   │  └─ If overlap + Enter pressed:
   │     └─ startDialog(npc)

// 3. Player vs Cat Care Interaction
Player.update():
   ├─ Check distance Player ↔ CatFollower
   ├─ Nếu gần + E pressed:
   │  ├─ Nếu pet cooldown = 0: CatFollower.pet()
   │  └─ Nếu cooldown > 0: Hiện notification chờ
   └─ Nếu gần + C pressed:
      └─ Mở Cat Care menu

// 4. Player vs Item Pickup
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
   - Hiện Character Selection

2. [CHARACTER SELECTION]
   User: Chọn Girl hoặc Boy trước

3. [CONTINUE / NEW GAME SCREEN]
   - Nếu chọn Continue: Load slot save của giới tính vừa chọn
   - Nếu chọn New Game: Bắt đầu game mới với giới tính vừa chọn

3. [INIT]
   Create GameWorld:
   - TileMap with grass, water, trees, benches
   - Player at position (400, 300)
   - 4 NPCs scattered, gồm Bác làm vườn ở góc dưới-trái
   - Fishing Rod item (hidden, sẽ random ở bất kỳ ô hợp lệ nào trên map khi quest bắt đầu)
   - Seeds item (hidden, sẽ random ở bất kỳ ô hợp lệ nào trên map khi quest bắt đầu)
   - InventorySystem empty nếu New Game, hoặc được khôi phục từ save nếu Continue
   - CatFollower at (300, 300) - IDLE
   - Start GameLoop

4. [EXPLORATION]
   GameLoop 60 FPS:
   - Player presses W → moves up
   - Player sees NPC marker "!"
   - Player presses Enter → Dialog starts
   - Dialog: "Tìm cần câu cho em" (typewriter effect)
   - User: Press Space/Enter
   - QuestSystem.startQuest("fishing_rod") → ACTIVE
   - Fishing Rod random 1 vị trí hợp lệ khắp map rồi becomes visible on map

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
   - Quest `fishing_rod` đóng hẳn, không thể nhận hoặc làm lại
   - CatFollower state: IDLE → WAITING
   - Cat Care được mở khóa nhưng mèo chưa tự chạy theo Player

7. [GARDENER SIDE QUEST]
   Player visits "Bác làm vườn" at tile (5,25)
   - Dialog asks for seeds
   - QuestSystem.startQuest("seeds") → ACTIVE
   - Seeds random 1 vị trí hợp lệ khắp map rồi becomes visible
   - Auto pickup → QuestSystem.addItem("seeds")
   - Return to gardener → QuestSystem.completeQuest("seeds") → COMPLETED
   - Random reward: rose / sunflower / tulip / bonsai
   - InventorySystem stores reward with quantity x1
   - Notification shows reward name
   - Sau 60 giây, quest `seeds` quay lại trạng thái chưa nhận để có thể làm vòng mới

8. [FISHING & INVENTORY]
   Player đứng gần hồ sau khi có cần câu:
   - Nhấn F → vào fishing mini-game
   - Thắng mini-game → random 1 cá vào InventorySystem

9. [INVENTORY]
   User presses I:
   - Inventory overlay opens
   - Gameplay pauses while overlay is open
   - Reward cây/hoa/cá xuất hiện với icon, tên, quantity
   - User presses I or Esc to close

10. [CAT CALL & CARE]
   Player explores with cat:
   - Cat đứng tại vị trí hiện tại cho tới khi được gọi
   - Khi lại gần mèo, hiện 2 badge `E` và `C` trên đầu mèo
     giống kiểu badge `F` của fishing prompt
   - Bấm C khi mèo ở xa → mèo chạy lại gần Player
   - Khi Player đi xa trở lại, phải bấm C lần nữa nếu muốn mèo quay lại gần
   - Bấm E khi mèo ở gần → mood +5, affection +2
   - Bấm C khi mèo ở gần → mở Cat Care menu
   - Chọn 1 con cá và bấm Enter → mèo ăn, mood +15, affection +8
   - Heart level tăng dần theo affection đã lưu
   - Continue playing until close game

11. [SAVE / LOAD]
   - Close game → app tự động save lại lần cuối vào đúng slot giới tính
   - Mở lại game → Character Selection → Continue / New Game Screen → chọn Continue để load slot tương ứng
   - Nếu quest item đang active nhưng chưa nhặt, game restore lại đúng vị trí spawn trước khi thoát
   - Nếu countdown mở lại của quest `seeds` đã trôi bớt trong lúc game tắt, timer sau khi load sẽ giảm tương ứng; nếu save cũ còn timer dài hơn mốc hiện tại thì game sẽ chuẩn hóa xuống delay cấu hình mới; nếu đã hết giờ thì quest tự mở lại ngay

12. [END]
   User closes window → Application.stop()
   → Auto save nếu đang có phiên gameplay
   → Game ends
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
      │   ├─ InventorySystem (persistent rewards + fish)
      │   └─ Cat Care Overlay state (menu chọn cá cho mèo)
      │
      ├─► SaveSystem
      │   ├─ Properties files `save-girl.properties` / `save-boy.properties`
      │   ├─ save(snapshot)
      │   └─ load(isGirl) -> SaveData
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
   └─ Enter
      └─► [CONTINUE / NEW GAME]
         ├─ Continue -> load selected gender slot -> [GAMEPLAY]
         ├─ New Game -> create new selected gender world -> [GAMEPLAY]
         └─ Esc -> [CHARACTER SELECT]

[GAMEPLAY]
      │
      ├─ Player moves (Input: WASD)
      │
      ├─ Encounter NPC (Input: Enter)
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
      │                   ├─ Quest `fishing_rod` closed permanently
      │                   │
      │                   ├─ If fishing_rod: [CAT CARE UNLOCKED]
      │                   │    ├─ Cat state: WAITING
      │                   │    ├─ Press C when far → [CAT CALLING] → WAITING
      │                   │    ├─ Press E when near → mood/affection tăng
      │                   │    └─ Press C when near → [CAT CARE MENU]
      │                   │         ├─ ↑/↓ chọn cá
      │                   │         ├─ Enter feed fish
      │                   │         └─ C/Esc close
      │                   │
      │                   ├─ If seeds: random garden reward
      │                   │    ├─ InventorySystem.addRandomGardenReward()
      │                   │    └─ Sau 60 giây -> [QUEST AVAILABLE AGAIN]
      │                   │
      │                   └─ Continue gameplay
      │
      ├─ Press I
      │    └─► [INVENTORY OVERLAY]
      │         ├─ Gameplay paused
      │         ├─ Show saved reward inventory
      │         └─ I/Esc closes overlay
      │
      ├─ Press C near cat
      │    └─► [CAT CARE OVERLAY]
      │         ├─ Gameplay paused
      │         ├─ Show mood / heart / cooldown
      │         ├─ Show fish inventory for feeding
      │         └─ C/Esc closes overlay
      │
      └─ Exit game
            └─ auto save current gender slot
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
│   ├─ Global audio settings bootstrap
│   └─ GameLoop init
│
├── audio/
│   ├── AudioManager.java
│   │   ├─ Menu/gameplay BGM switching
│   │   ├─ Shared SFX event mapping + clip cache
│   │   └─ Missing asset safe-fail logging
│   │
│   ├── AudioSettings.java
│   │   └─ Music/SFX volume + mute state
│   │
│   └── AudioSettingsStore.java
│       └─ Global settings persistence ở `%USERPROFILE%/.tiny-village-game/settings.properties`
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
│   │   ├─ Choice selection
│   │   └─ Dialog audio hooks
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
│       ├─ Inventory persistent cho reward cây/hoa và cá
│       ├─ Random reward pool (rose, sunflower, tulip, bonsai, fish)
│       ├─ Item quantities for inventory overlay
│       └─ Consume fish items khi cho mèo ăn
│
├── save/
│   ├── SaveData.java
│   │   └─ Snapshot immutable cho long-lived progress, gồm cả tọa độ quest item và thời điểm save
│   └── SaveSystem.java
│       ├─ Save theo slot giới tính bằng java.util.Properties
│       ├─ Đường dẫn `%USERPROFILE%/.tiny-village-game/save-girl.properties` / `save-boy.properties`
│       └─ Parse / normalize dữ liệu save, gồm cả tọa độ quest item và offline countdown, khi load
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
│       ├─ State (IDLE, WAITING, CALLING)
│       ├─ Mood, affection, heart level
│       ├─ Cooldown pet/feed/call
│       └─ Unlock + call-near + care behavior
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
├── ui/
│   └── AudioSettingsOverlay.java
│       └─ Shared settings overlay cho front screen và gameplay
│
└── world/
    ├── GameWorld.java (Manager)
    │   ├─ TileMap, Player, NPCs, Items, Cat, Camera, InventorySystem
    │   ├─ update(dt, inputHandler)
    │   ├─ render(gc)
    │   ├─ renderInventoryOverlay(gc)
   │   ├─ renderCatCareOverlay(gc)
   │   └─ Audio overlay + footstep gating
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
   ├─► Player asset có logic riêng theo giới tính đã chọn
   │   ├─ Girl: kiểm tra src/main/resources/assets/player.png
   │   ├─ Boy: kiểm tra src/main/resources/assets/player2.png
   │   ├─ Nếu file tương ứng không tồn tại: fallback gọi PixelArtGenerator.generatePlayerSheet(isGirl)
   │
   ├─► Character Selection Screen gọi AssetManager.getPlayerPreview(isGirl)
   │   ├─ Ưu tiên load ảnh preview riêng từ player_preview.png / player2_preview.png
   │   ├─ Nếu thiếu preview asset: fallback về frame idle nhìn xuống từ player.png / player2.png
   │   └─ Không đánh dấu loadAll() đã hoàn tất, nên vào game vẫn load đúng spritesheet theo lựa chọn
   │
   ├─► Các asset khác kiểm tra file ảnh trong src/main/resources/assets/ (vd: tiles.png, cat.png)
   │   ├─ Nếu TỒN TẠI: Load Image từ file (Cho phép thay đổi giao diện không cần code)
   │   └─ Nếu KHÔNG TỒN TẠI: Fallback gọi PixelArtGenerator tạo hình mặc định
   │
   ├─► Grass autotile hỗ trợ 2 mức custom asset
   │   ├─ `grass_autotile.png` / `dark_grass_autotile.png`: spritesheet 4x4 cho 16 biến thể cardinal cơ bản
   │   └─ `grass_autotile_extended.png` / `dark_grass_autotile_extended.png`: spritesheet lớn hơn, hiện chỉ đọc hàng đầu tiên cho 16 biến thể cardinal cơ bản
   │
   └─► Đưa Image/SpriteSheet vào bộ nhớ (HashMap) để sử dụng
```
Game hỗ trợ thay đổi toàn bộ visual (nhân vật, map, item, reward icon) chỉ bằng cách copy file `.png` vào thư mục `assets`. Riêng player có thể dùng hai spritesheet `player.png` cho Girl và `player2.png` cho Boy, đồng thời có thể cung cấp preview riêng bằng `player_preview.png` và `player2_preview.png` cho màn hình chọn nhân vật.

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

- Khi bắt đầu fishing mini-game bằng `F`, game dùng `dialog_advance.wav` như một cue tương tác nhẹ.

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
   ├─ Cá được lưu trong inventory persistent
   ├─ Có thể dùng lại trong Cat Care menu để cho mèo ăn
   ├─ Phát `ui_confirm.wav` vì đây cũng là một reward pickup
   └─ PlayerState: FISHING → NORMAL

Thua:
   ├─ Nếu hết 7 giây mà currentValue < 100
   ├─ Không thêm item vào inventory
   ├─ Phát `ui_back.wav` để báo fail và thoát nhịp câu cá
   └─ PlayerState: FISHING → NORMAL
```

Reward cá được lưu persistent giống garden reward; Continue sẽ khôi phục inventory theo save slot hiện tại.

---

## Tóm Tắt
- **Engine**: Java 17 + JavaFX 21 Canvas 2D
- **FPS**: 60 frames per second (60 Hz)
- **World**: Procedural tile-based map (40x30 tiles)
- **Core Gameplay**: Explore → Dialog → Quest → Collect → Reward/Completion → Fishing mini-game → Cat Care (quest `fishing_rod` mở khóa mèo; nhấn `C` để gọi mèo lại gần; garden/fish rewards đi vào inventory; cá có thể dùng để tăng thân thiết với mèo)
- **Input**: WASD movement, Enter NPC interaction, E pet cat, C call/care cat, F fishing, I inventory, M map, P audio settings, Arrow keys for menus
- **Rendering**: Layer-based (tiles → items → NPCs → player → cat → UI → fishing UI → inventory overlay → Cat Care overlay → Audio Settings overlay)
- **Cat Prompt UI**: Khi đứng gần mèo, game hiển thị badge `E` và `C` trên đầu mèo theo cùng phong cách với badge `F`; mood chỉ hiển thị trong Cat Care overlay, không hiển thị trực tiếp trên mèo.
- **Audio**: 2 BGM loop + 8 reusable SFX/footstep files, settings global lưu riêng trong `settings.properties`
- **State Management**: Character select → Continue/New Game screen → Gameplay loop → Dialog/Multi-quest → Inventory overlay / Cat Care overlay / Audio Settings overlay / Cat calling / Fishing → Auto-save on exit vào slot giới tính tương ứng
- **Extensibility**: Hỗ trợ Custom Asset thông qua thư mục resources.

Game được thiết kế để mở rộng dễ dàng với Inventory, Save/Load, Minimap, và các quest phụ.
