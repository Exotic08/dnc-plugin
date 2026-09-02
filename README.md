# DisplayNameColor (DNC)

Plugin quản lý màu tên gradient tùy chỉnh cho người chơi — tự viết theo yêu cầu.

## 1. Cách build ra file .jar

Cần cài sẵn trên máy: **JDK 17+** và **Maven**.

```bash
cd dnc-plugin
mvn clean package
```

File jar sẽ nằm ở: `target/DisplayNameColor.jar` — copy vào `plugins/` của server.

## 2. Yêu cầu

- Paper 1.21.11 (hoặc tương thích 1.21.x)
- PlaceholderAPI (bắt buộc để tích hợp TAB — nếu không cài, chat vẫn có màu nhưng TAB/nametag sẽ không đổi)
- LuckPerms (dùng cho lệnh `/dnc setpermission`, tự động chạy lệnh `/lp group ... permission set ...`)

## 3. Các lệnh

```
/dnc create <ten> <material> <mau1,mau2,...>   - tạo DNC mới (ví dụ: /dnc create vip NAME_TAG "#fa1505,#fa6b05,#fad905")
/dnc remove <ten>                               - xóa DNC
/dnc test <ten>                                 - xem thử màu áp lên tên mình, in ra chat
/dnc setpermission <ten> <rank_lp> <true/false> - cấp/thu quyền dùng DNC cho 1 rank LuckPerms
/dnc list                                       - liệt kê toàn bộ DNC
/dnc info <ten>                                 - xem chi tiết 1 DNC
/dnc edit name/material/color <ten> <giá_trị>   - sửa DNC đã tạo
/dnc reload                                     - reload lại config.yml/data.yml
/dnc                                            - mở GUI chọn màu (cho người chơi thường)
```

Permission quản lý: `dnc.admin` (mặc định chỉ OP).
Permission dùng từng DNC: `dnc.use.<tên>` (mặc định false, cấp qua `/dnc setpermission`).

## 4. Lưu trữ

- `plugins/DisplayNameColor/config.yml` — danh sách DNC đã tạo (tên, material, mã màu)
- `plugins/DisplayNameColor/data.yml` — DNC đang bật của từng người chơi (theo UUID)

## 5. Tích hợp TAB (bắt buộc để hiện màu ở TAB + nametag)

Trong `plugins/TAB/config.yml`, sửa các dòng liên quan tên hiển thị để dùng placeholder `%dnc_color%` thay vì `%player%` trực tiếp:

```yaml
tablist-name-formatting: '%luckperms_prefix%%dnc_color%'
```

Với nametag (tên nổi trên đầu), tìm đúng mục tương ứng trong TAB (`above-name` hoặc `nametag`, tùy bản) và áp dụng tương tự.

**Lưu ý:** `%dnc_color%` tự động trả về tên gốc không màu (`%player%`) nếu người chơi không bật DNC nào — an toàn để dùng thay thế hoàn toàn `%player%` trong các dòng liên quan tên hiển thị.

Chạy `/tab reload` sau khi sửa.

## 6. Cách gradient hoạt động

- Nếu tạo DNC với 1 màu duy nhất → cả tên 1 màu đó.
- Nếu tạo với nhiều màu (vd 3 màu) → gradient trải đều từ đầu đến cuối tên, đi qua các màu trung gian theo đúng thứ tự nhập.
- Nếu số màu nhập vào NHIỀU HƠN số ký tự trong tên người chơi → tự động chỉ lấy màu ĐẦU và màu CUỐI, bỏ qua các màu giữa (đúng theo yêu cầu đã chốt).

## 7. Hành vi tự động

- Khi admin `setpermission ... false` cho 1 rank: nếu có người chơi ONLINE đang bật đúng DNC đó và không còn quyền (do rank/permission thay đổi), plugin tự động tắt về mặc định ngay lập tức và thông báo cho họ.
- Chỉ 1 DNC được bật cùng lúc cho mỗi người chơi — bật cái mới tự động tắt cái cũ.
- Thay đổi áp dụng real-time, không cần relog (chat áp dụng ngay theo listener; TAB/nametag áp dụng theo chu kỳ refresh của TAB, thường 1-2 giây).

## 8. Ghi chú quan trọng

Mình (Claude) không thể build sẵn file `.jar` chạy được vì môi trường không có quyền truy cập Maven Central / kho của PaperMC/PlaceholderAPI để tải các thư viện cần thiết lúc biên dịch. Code đã viết đầy đủ, đúng cấu trúc chuẩn — em chỉ cần chạy `mvn clean package` trên máy có mạng bình thường là ra file `.jar` dùng ngay.
