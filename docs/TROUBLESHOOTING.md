# Troubleshooting Guide

## GitLab Runner Shell Executor Issues

### Error: `pwsh: executable file not found in %PATH%`

**Triệu chứng:**
```
ERROR: Job failed (system failure): prepare environment: failed to start process: 
starting OS command: exec: "pwsh": executable file not found in %PATH%
```

**Nguyên nhân:**
GitLab runner đang được cấu hình để sử dụng PowerShell (`pwsh`) nhưng PowerShell không được cài đặt hoặc không có trong PATH trên máy chạy runner.

**Giải pháp:**

#### Giải pháp 1: Cài đặt PowerShell (Khuyến nghị)

1. Tải PowerShell Core từ: https://github.com/PowerShell/PowerShell/releases
2. Cài đặt PowerShell
3. Đảm bảo `pwsh` có trong PATH:
   ```powershell
   pwsh --version
   ```
4. Restart GitLab runner:
   ```powershell
   # Chạy với quyền Administrator
   gitlab-runner restart
   ```

#### Giải pháp 2: Đổi sang Windows CMD (Nhanh hơn)

Vì file `.gitlab-ci.yml` đã sử dụng cú pháp Windows CMD (`%VARIABLE%`, `^`), bạn có thể đổi runner sang sử dụng `cmd`:

1. Mở file cấu hình GitLab runner (thường ở `C:\GitLab-Runner\config.toml`)
2. Tìm section `[runners]` hoặc `[[runners]]` có tag `local`
3. Thay đổi hoặc thêm dòng:
   ```toml
   [[runners]]
     name = "local"
     url = "https://gitlab.com/"
     token = "..."
     executor = "shell"
     shell = "cmd"  # Thay đổi từ "pwsh" thành "cmd"
   ```
4. **Restart GitLab runner** để áp dụng thay đổi:
   
   **Cách 1:** Sử dụng lệnh GitLab runner (chạy với quyền Administrator):
   ```powershell
   gitlab-runner restart
   ```
   
   **Cách 2:** Nếu runner chạy như Windows Service:
   ```powershell
   Restart-Service gitlab-runner
   ```
   
   **Cách 3:** Restart thủ công qua Services:
   - Mở `services.msc`
   - Tìm service `gitlab-runner`
   - Click chuột phải → Restart

5. **Kiểm tra runner đã hoạt động:**
   ```powershell
   gitlab-runner verify
   ```
   
   Hoặc xem trạng thái:
   ```powershell
   gitlab-runner status
   ```

**Lưu ý:** File `.gitlab-ci.yml` hiện tại sử dụng cú pháp Windows CMD, nên giải pháp 2 (dùng `cmd`) sẽ phù hợp hơn và không cần cài thêm phần mềm.

**Sau khi restart:** Thử push code lại để kiểm tra pipeline có chạy được không.

### Xác nhận runner đã hoạt động đúng

Sau khi restart, bạn có thể kiểm tra:

1. **Xem danh sách runner đã đăng ký:**
   ```powershell
   gitlab-runner list
   ```
   
2. **Xem trạng thái runner:**
   ```powershell
   gitlab-runner status
   ```

3. **Kiểm tra runner có đang chạy:**
   - Mở GitLab web interface
   - Vào Settings → CI/CD → Runners
   - Kiểm tra runner "khong dien gi" có status là "Online" (màu xanh)

4. **Test pipeline:**
   - Push một commit mới hoặc tạo Merge Request
   - Xem pipeline logs trong GitLab để đảm bảo không còn lỗi `pwsh: executable file not found`

### Kiểm tra cấu hình runner hiện tại

Để xem runner đang sử dụng shell nào:

```powershell
gitlab-runner list
```

Hoặc xem file config:
```powershell
type C:\GitLab-Runner\config.toml
```

**Lưu ý về vị trí file config:**

- File config mặc định thường ở: `C:\GitLab-Runner\config.toml`
- Khi chạy lệnh `gitlab-runner` từ command line, nó sẽ tìm config ở thư mục hiện tại
- Để chỉ định đường dẫn config cụ thể:
  ```powershell
  gitlab-runner list --config C:\GitLab-Runner\config.toml
  ```
- Service GitLab runner tự động sử dụng file config đúng khi chạy, không cần lo lắng về việc này khi chạy pipeline

### Error: `shell cmd not found`

**Triệu chứng:**
```
ERROR: Preparation failed: shell cmd not found
Will be retried in 3s ...
ERROR: Job failed (system failure): shell cmd not found
```

**Nguyên nhân:**
GitLab runner không tìm thấy `cmd.exe` trong PATH khi chạy. Mặc dù `cmd.exe` luôn có sẵn trong Windows, runner có thể không tìm thấy nó do vấn đề về PATH hoặc cách chỉ định shell.

**Giải pháp:**

1. **Chỉ định đường dẫn đầy đủ đến cmd.exe trong config.toml:**

   Mở file `C:\GitLab-Runner\config.toml` và thay đổi:
   ```toml
   shell = "cmd"
   ```
   
   Thành:
   ```toml
   shell = "C:\\Windows\\System32\\cmd.exe"
   ```

2. **Hoặc sử dụng script tự động:**

   Chạy script `fix-runner-config.ps1` (với quyền Administrator):
   ```powershell
   powershell -ExecutionPolicy Bypass -File fix-runner-config.ps1
   ```

3. **Restart GitLab runner** (với quyền Administrator):
   ```powershell
   gitlab-runner restart
   ```
   
   Hoặc restart qua Services:
   - Mở `services.msc`
   - Tìm service `gitlab-runner`
   - Click chuột phải → Restart

4. **Kiểm tra cấu hình đã được cập nhật:**
   ```powershell
   Get-Content C:\GitLab-Runner\config.toml | Select-String -Pattern "shell"
   ```
   
   Kết quả mong đợi:
   ```
   shell = "C:\\Windows\\System32\\cmd.exe"
   ```

**Lưu ý:** Sau khi sửa và restart, pipeline sẽ tự động sử dụng đường dẫn đầy đủ đến cmd.exe và lỗi sẽ được khắc phục.

