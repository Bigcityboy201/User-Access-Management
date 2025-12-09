## Test Report - Duplicate Empty List Scenario

- **Context**: Trong `user-service` unit test (`UserServiceTest`), có hai test cùng mục tiêu kiểm tra danh sách rỗng khi không có user.
- **Issue**: Trùng lặp test case làm tăng noise, không thêm giá trị coverage.
- **Vị trí**: `user-service/src/test/java/com/r2s/user/UserServiceTest.java`
  - Test giữ lại: `getAllUsers_shouldReturnEmptyList_whenNoUsers`
  - Test đề nghị bỏ/gộp: `getAllUsers_shouldReturnEmptyListWhenNoUsers` (alias cùng logic)

### Hành động đề xuất
1) Giữ lại duy nhất test `getAllUsers_shouldReturnEmptyList_whenNoUsers`.
2) Xoá test alias trùng lặp hoặc gộp nội dung nếu có khác biệt (hiện tại không khác biệt).
3) Đảm bảo đặt @DisplayName rõ ràng cho test được giữ lại (đã có).

### Trạng thái hiện tại
- Đã gộp: test trùng lặp đã được xoá, chỉ còn một test cho scenario “empty list”.
- Không còn test trùng mục đích trong file.

### Việc Fresher cần xác nhận
- Review lại để chắc chắn không có thêm test trùng logic khác.
- Chạy lại bộ test để bảo đảm không ảnh hưởng hành vi.


