# Script để đổi GitLab runner sang PowerShell
# Chạy với quyền Administrator

$configPath = "C:\GitLab-Runner\config.toml"

if (-not (Test-Path $configPath)) {
    Write-Host "File config.toml không tìm thấy tại: $configPath" -ForegroundColor Red
    exit 1
}

# Đọc nội dung file
$content = Get-Content $configPath -Raw

# Thay thế shell thành PowerShell
$content = $content -replace 'shell = "C:\\\\Windows\\\\System32\\\\cmd\.exe"', 'shell = "powershell"'
$content = $content -replace 'shell = "cmd"', 'shell = "powershell"'

# Ghi lại file
Set-Content -Path $configPath -Value $content -NoNewline

Write-Host "Đã cập nhật config.toml để sử dụng PowerShell!" -ForegroundColor Green
Write-Host ""
Write-Host "Bước tiếp theo:" -ForegroundColor Yellow
Write-Host "1. Restart GitLab runner: gitlab-runner restart" -ForegroundColor Cyan
Write-Host "2. File .gitlab-ci.yml sẽ được tự động chuyển đổi sang PowerShell syntax" -ForegroundColor Cyan

