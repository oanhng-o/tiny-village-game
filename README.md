# Tiny Village

Tiny Village là một game phiêu lưu 2D phong cách pixel art chibi, được phát triển bằng Java 17, JavaFX 21 và Maven. Người chơi sẽ khám phá một ngôi làng nhỏ, tương tác với NPC, thực hiện quest, câu cá, thu thập vật phẩm, chăm sóc mèo và quản lý tiến trình thông qua hệ thống save/load theo từng nhân vật.

## Mục lục

- [Giới thiệu](#giới-thiệu)
- [Thể loại và định hướng](#thể-loại-và-định-hướng)
- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Tính năng chính](#tính-năng-chính)
- [Luồng chơi chính](#luồng-chơi-chính)
- [Điều khiển](#điều-khiển)
- [Cấu trúc dự án](#cấu-trúc-dự-án)
- [Yêu cầu cài đặt](#yêu-cầu-cài-đặt)
- [Hướng dẫn cài đặt và chạy game](#hướng-dẫn-cài-đặt-và-chạy-game)
- [Lưu dữ liệu](#lưu-dữ-liệu)
- [Tài nguyên và tuỳ biến](#tài-nguyên-và-tuỳ-biến)
- [Lệnh Maven hữu ích](#lệnh-maven-hữu-ích)
- [Định hướng mở rộng](#định-hướng-mở-rộng)

## Giới thiệu

Tiny Village tập trung vào trải nghiệm thư giãn, khám phá và tương tác. Thay vì chiến đấu, game xây dựng vòng lặp chơi xoay quanh:

- Đi bộ khám phá bản đồ
- Nói chuyện với NPC
- Nhận và hoàn thành quest
- Nhặt vật phẩm ngẫu nhiên trên map
- Câu cá qua mini-game
- Nuôi dưỡng và chăm sóc mèo đồng hành
- Quản lý inventory và save tiến trình

Game sử dụng JavaFX Canvas 2D để render thế giới tile-based, kết hợp hệ thống audio, overlay UI, quest state và save/load theo slot nhân vật.

## Thể loại và định hướng

- Tên game: Tiny Village
- Thể loại: Phiêu lưu 2D, casual RPG nhẹ, exploration, mini-game
- Phong cách đồ hoạ: Pixel Art, Chibi
- Góc nhìn: Top-down 2D
- Đối tượng trải nghiệm: người chơi thích game nhẹ nhàng, khám phá, tương tác NPC và sưu tầm vật phẩm

## Công nghệ sử dụng

| Thành phần | Công nghệ |
| --- | --- |
| Ngôn ngữ | Java 17 |
| UI/Game rendering | JavaFX 21.0.2 |
| Build tool | Maven |
| Kiến trúc | Java Module System |
| Audio | JavaFX Media |

Thông tin build hiện tại:

- `groupId`: `com.game`
- `artifactId`: `tiny-village`
- `mainClass`: `com.game/com.game.Main`

## Tính năng chính

### 1. Character Selection và Save Slot theo giới tính

- Game luôn bắt đầu ở màn hình chọn nhân vật Girl hoặc Boy.
- Sau khi chọn nhân vật, người chơi quyết định `Continue` hoặc `New Game`.
- Mỗi giới tính có một slot save riêng, giúp tách tiến trình chơi rõ ràng.

### 2. Khám phá thế giới tile-based

- Bản đồ 2D dạng tile với cỏ, nước, đường đi, cây, ghế và các khu vực tương tác.
- Camera bám theo nhân vật để giữ trải nghiệm di chuyển mượt mà.
- Hệ thống va chạm với tile solid và các thực thể trên bản đồ.

### 3. Tương tác NPC và hệ thống đối thoại

- NPC có lời thoại riêng và có thể kích hoạt quest.
- Dialog có hiệu ứng typewriter.
- Một số NPC hỗ trợ choice-based interaction.
- Game có audio hook cho mở thoại, chuyển dòng, xác nhận và đóng thoại.

### 4. Hệ thống quest nhiều trạng thái

- Quest vận hành theo các trạng thái `NOT_STARTED`, `ACTIVE`, `COMPLETED`.
- Quest `fishing_rod`: tìm cần câu cho NPC để mở khoá tương tác với mèo.
- Quest `seeds`: tìm hạt giống cho bác làm vườn, nhận thưởng ngẫu nhiên và có cơ chế lặp lại theo thời gian.

### 5. Item pickup và phần thưởng ngẫu nhiên

- Vật phẩm quest xuất hiện ngẫu nhiên ở vị trí hợp lệ trên map.
- Người chơi tự động nhặt item khi va chạm.
- Phần thưởng có thể là cây, hoa hoặc cá tuỳ theo hoạt động.

### 6. Inventory persistent

- Lưu trữ vật phẩm người chơi đã thu thập.
- Giữ được dữ liệu qua các lần `Continue`.
- Hiển thị item, icon và số lượng trong overlay inventory.

### 7. Fishing mini-game

- Mở khoá sau khi hoàn thành quest cần câu.
- Kích hoạt khi đứng gần nước và nhấn `F`.
- Người chơi cần canh thanh lực trong thời gian giới hạn để nhận cá.
- Cá thu được có thể dùng trong hệ thống chăm sóc mèo.

### 8. Cat follower và Cat Care

- Sau quest `fishing_rod`, mèo được mở khoá tương tác.
- Người chơi có thể gọi mèo lại gần, vuốt mèo hoặc cho mèo ăn.
- Hệ thống mood, affection, heart level và cooldown giúp tạo tiến trình gắn bó dài hạn.

### 9. Audio system và Audio Settings Overlay

- Tách riêng BGM menu và BGM gameplay.
- SFX được tái sử dụng theo ngữ nghĩa để tối ưu asset.
- Overlay cài đặt âm thanh cho phép chỉnh volume và mute cho music/sfx.
- Thiết lập âm thanh được lưu global, không gắn với save slot.

### 10. Save/Load và Auto Save

- Tự động lưu khi thoát game.
- Khôi phục player, inventory, quest state, cat state và vị trí item quest khi tiếp tục chơi.
- Có xử lý thời gian offline cho quest lặp.

## Luồng chơi chính

Luồng trải nghiệm cốt lõi của Tiny Village:

1. Mở game.
2. Chọn nhân vật Girl hoặc Boy.
3. Chọn `Continue` hoặc `New Game`.
4. Vào thế giới game và khám phá bản đồ.
5. Tương tác với NPC để mở quest.
6. Tìm vật phẩm, hoàn thành nhiệm vụ và nhận thưởng.
7. Mở khoá câu cá và mèo đồng hành.
8. Quản lý inventory, chăm sóc mèo và tiếp tục khám phá.
9. Thoát game và auto-save tiến trình vào đúng slot nhân vật.

## Điều khiển

| Phím | Chức năng |
| --- | --- |
| `W/A/S/D` hoặc mũi tên | Di chuyển nhân vật / điều hướng menu |
| `Enter` | Tương tác với NPC / xác nhận |
| `Space` | Xác nhận trong một số UI / fishing mini-game |
| `E` | Vuốt mèo khi ở gần |
| `C` | Gọi mèo / mở hoặc đóng Cat Care |
| `F` | Bắt đầu fishing mini-game khi đứng gần nước |
| `I` | Mở hoặc đóng inventory |
| `M` | Mở hoặc đóng minimap |
| `P` | Mở hoặc đóng Audio Settings overlay |
| `Esc` | Quay lại / đóng overlay đang mở |

## Cấu trúc dự án

```text
tiny-village-game/
├── pom.xml
├── README.md
├── FLOW.md
├── PLAN.md
├── implementation_plan.md
└── src/
    └── main/
        ├── java/
        │   ├── module-info.java
        │   └── com/game/
        │       ├── Main.java
        │       ├── GameApplication.java
        │       ├── audio/
        │       ├── core/
        │       ├── dialog/
        │       ├── entity/
        │       ├── inventory/
        │       ├── save/
        │       ├── ui/
        │       ├── util/
        │       └── world/
        └── resources/
            └── assets/
```

### Một số module quan trọng

- `GameApplication`: khởi tạo stage, canvas, front screen và gameplay.
- `GameLoop`: vòng lặp game 60 FPS.
- `GameWorld`: quản lý player, NPC, item, map, camera, quest, inventory, cat care và overlay.
- `DialogSystem`: quản lý hội thoại và choice dialog.
- `QuestSystem`: quản lý state quest theo `questId`.
- `InventorySystem`: lưu và thao tác với reward, fish và item quantity.
- `SaveSystem`: đọc và ghi save file theo slot nhân vật.
- `AudioManager`: xử lý BGM, SFX và áp dụng audio settings.

## Yêu cầu cài đặt

Trước khi chạy dự án, cần chuẩn bị:

- JDK 17 hoặc mới hơn tương thích với Java 17
- Apache Maven 3.8+ hoặc mới hơn
- Git để clone repository
- Hệ điều hành Windows, macOS hoặc Linux có hỗ trợ JavaFX runtime

Khuyến nghị:

- Dùng IntelliJ IDEA hoặc VS Code để thuận tiện chạy và debug Maven project.
- Kiểm tra biến môi trường `JAVA_HOME` đã trỏ đúng tới JDK 17.

## Hướng dẫn cài đặt và chạy game

### 1. Clone repository

```bash
git clone <repository-url>
cd tiny-village-game
```

### 2. Kiểm tra môi trường Java và Maven

```bash
java -version
mvn -version
```

Bạn nên thấy Java 17 trong kết quả trả về.

### 3. Build dự án

```bash
mvn clean install
```

Lệnh này sẽ:

- Xoá build cũ
- Tải dependency cần thiết
- Biên dịch source code
- Đóng gói artifact Maven

### 4. Chạy game bằng Maven

```bash
mvn javafx:run
```

Đây là cách chạy khuyến nghị vì dự án đã cấu hình sẵn `javafx-maven-plugin` với entrypoint:

```text
com.game/com.game.Main
```

### 5. Chạy từ IDE

Nếu chạy từ IDE:

- Import project như một Maven project
- Đảm bảo IDE dùng JDK 17
- Chạy class `com.game.Main` hoặc dùng goal `javafx:run`

## Lưu dữ liệu

Game lưu dữ liệu tại thư mục người dùng:

```text
%USERPROFILE%/.tiny-village-game/
```

Bao gồm:

- `save-girl.properties`: save slot của nhân vật Girl
- `save-boy.properties`: save slot của nhân vật Boy
- `settings.properties`: cấu hình âm thanh toàn cục

### Dữ liệu được lưu

- Giới tính nhân vật
- Vị trí và hướng nhìn của player
- Quest state
- Inventory
- Cat state
- Vị trí item quest đang hoạt động
- Mốc thời gian save gần nhất để xử lý countdown offline

### Dữ liệu không lưu

- Dialog đang mở dở
- Fishing mini-game đang diễn ra
- Overlay inventory hoặc cat care đang mở
- Các cooldown ngắn hạn đang đếm

## Tài nguyên và tuỳ biến

Dự án hỗ trợ thay đổi asset bằng cách thêm file vào thư mục resources:

```text
src/main/resources/assets/
```

Các điểm đáng chú ý:

- Có thể thay spritesheet nhân vật bằng `player.png` và `player2.png`.
- Có thể thêm ảnh preview riêng cho màn hình chọn nhân vật bằng `player_preview.png` và `player2_preview.png`.
- Nếu thiếu asset, game có cơ chế fallback bằng pixel art generator để tránh crash.
- Audio asset được đặt trong resources và được ánh xạ theo từng event ngữ nghĩa.

Điều này giúp dự án phù hợp cho cả mục đích học tập, mở rộng gameplay lẫn thay đổi giao diện nhanh mà không cần sửa nhiều code.

## Lệnh Maven hữu ích

```bash
# Biên dịch và đóng gói
mvn clean package

# Cài artifact vào local repository
mvn clean install

# Chạy game
mvn javafx:run
```

## Tài liệu liên quan

| Tài liệu | Mô tả | Liên kết |
| --- | --- | --- |
| Flow tài liệu | Mô tả chi tiết luồng hoạt động, gameplay loop, quest flow, save/load, audio và kiến trúc tổng quan | [FLOW.md](./FLOW.md) |
| Test case | Tài liệu kiểm thử | [Test case](https://docs.google.com/spreadsheets/d/1xIlT1jzPOxu9lnSPFEMAB69a8XZO6PQyvwmUJVAmfdU/edit?usp=sharing) |
| SRS | Tài liệu đặc tả yêu cầu phần mềm | [SRS](https://docs.google.com/document/d/1HzM9H-J6lBm8cWf4Fc2M6EOVEeR2lYyb/edit?usp=sharing&ouid=114131250450428532536&rtpof=true&sd=true) |

## Định hướng mở rộng

Tiny Village có cấu trúc đủ rõ để tiếp tục phát triển các tính năng mới như:

- Thêm NPC và nhánh hội thoại mới
- Bổ sung quest chain nhiều bước
- Mở rộng bản đồ và tileset
- Thêm item, crafting hoặc farming
- Thêm UI polish, animation và hiệu ứng môi trường
- Đóng gói game thành bản phát hành desktop hoàn chỉnh

## Tác giả

### Nhóm thực hiện

| Thành viên | Mã sinh viên |
| --- | --- |
| Nguyễn Oanh Oanh | 24020262 |
| Nguyễn Thị Phương Ngọc | 24020253 |
| Nguyễn Thị Lan Anh | 24020019 |

### Thông tin học phần

| Mục | Thông tin |
| --- | --- |
| Môn học | Công nghệ phần mềm |
| Giảng viên | TS. Nguyễn Đức Anh |
| Học kỳ | 2 |
| Năm học | 2025-2026 |