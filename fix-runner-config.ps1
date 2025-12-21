# Script để sửa file config.toml của GitLab runner
# Chạy với quyền Administrator

$configPath = "C:\GitLab-Runner\config.toml"

if (-not (Test-Path $configPath)) {
    Write-Host "File config.toml không tìm thấy tại: $configPath" -ForegroundColor Red
    exit 1
}

# Đọc nội dung file
$content = Get-Content $configPath -Raw

# Thay thế shell = "cmd" thành đường dẫn đầy đủ
$content = $content -replace 'shell = "cmd"', 'shell = "C:\\Windows\\System32\\cmd.exe"'

# Ghi lại file
Set-Content -Path $configPath -Value $content -NoNewline

Write-Host "Đã cập nhật config.toml thành công!" -ForegroundColor Green
Write-Host "Đường dẫn cmd.exe đã được chỉ định đầy đủ." -ForegroundColor Green
Write-Host ""
Write-Host "Bước tiếp theo: Restart GitLab runner:" -ForegroundColor Yellow
Write-Host "  gitlab-runner restart" -ForegroundColor Cyan

