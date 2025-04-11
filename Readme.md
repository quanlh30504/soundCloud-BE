## 🔧 Cài đặt & Cấu hình

### 1. Tạo Firebase Service Account và file `.env`

#### 📂 Cấu hình Firebase
- Tạo file `firebase-service-account.json` và đặt trong thư mục `src/main/resources`.
- File này chứa thông tin xác thực Firebase Service Account.

🔗 **Tài liệu hướng dẫn chi tiết**:  
[Firebase Config Guide](https://docs.google.com/document/d/1jktDAk06mgWVppShp_ybc0SthYn_nSGnK5Mp23hmd4k/edit?usp=sharing)

#### 📝 File `.env`
Tạo file `.env` ở thư mục gốc với nội dung tối thiểu như sau:

```env
YTDLP_EXECUTABLE_PATH=C:/yt-dlp/yt-dlp.exe
```

---

### 2. Cài đặt `yt-dlp` để tải nhạc từ YouTube

- Truy cập link tải chính thức:  
  🔗 [https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe](https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe)

- Tạo thư mục mới, ví dụ: `C:\yt-dlp`, và lưu file `yt-dlp.exe` vào đó.

- Cập nhật giá trị `YTDLP_EXECUTABLE_PATH` trong file `.env` trỏ đến file `.exe`:

```env
YTDLP_EXECUTABLE_PATH=C:/yt-dlp/yt-dlp.exe
```

---

### 3. Tích hợp API lời bài hát

Dự án sử dụng **public API miễn phí** từ [Lyrics.ovh](https://lyrics.ovh/) để lấy lời bài hát.

#### 📌 Cách sử dụng:
Gửi `GET` request đến:

```
https://api.lyrics.ovh/v1/{artist}/{title}
```

#### Ví dụ:

```
GET https://api.lyrics.ovh/v1/Coldplay/Yellow
```

Response:
```json
{
  "lyrics": "Look at the stars, look how they shine for you..."
}
```

---

## Database
- Data schema: [soundcloud_db_schema](Data/soundcloud_db_schema.sql)

## ✍️ Ghi chú

- Cần Java 17+ và Maven 3.8+ để build ứng dụng.
- Đảm bảo quyền truy cập mạng cho `yt-dlp` nếu dùng trong môi trường bị giới hạn.
- Nếu API lời bài hát trả về null, hãy kiểm tra lại tên nghệ sĩ và tiêu đề bài hát.

