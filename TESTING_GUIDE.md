## Testing overview (unit, integration, e2e)

File này giúp bạn (và reviewer) nhanh chóng biết:
- **Class / method nào là unit test**
- **Class / method nào là integration test**
- **Class / method nào là system / e2e test**
- **Cách tự nhận biết khi viết test mới**

---

## 1. Unit test

### 1.1. Đặc điểm nhận biết (rule chung)

- **Annotation / setup**
  - Thường dùng: `@ExtendWith(MockitoExtension.class)`
  - **Không** khởi chạy full Spring context (không có `@SpringBootTest`).
  - Dùng **Mockito** (`@Mock`, `@InjectMocks`, `when(..)`, `verify(..)`).
- **Scope**
  - Test **một class** cụ thể (service, util, handler, entity…).
  - Mọi dependency được **mock**, không dùng DB thật.
- **Tín hiệu khác**
  - Tên class test thường dạng: `*Test` trong `src/test/java`.

### 1.2. Các class unit test chính trong project

- **Module `core`**
  - `com.r2s.core.util.JwtUtilsTest`
  - `com.r2s.core.security.CustomUserDetailsServiceTest`
  - `com.r2s.core.security.CustomUserDetailsTest`
  - `com.r2s.core.security.JwtAuthenticationFilterTest`
  - `com.r2s.core.security.CustomAccessDeniedHandlerTest`
  - `com.r2s.core.security.CustomAuthenticationEntryPointTest`
  - `com.r2s.core.entity.UserTest`
  - `com.r2s.core.entity.RoleTest`
  - `com.r2s.core.handler.GlobalExceptionHandlerTest`

- **Module `auth-service`**
  - `com.r2s.auth.UserServiceTest`  
    - Dùng `@ExtendWith(MockitoExtension.class)`, mock `UserRepository`, `RoleRepository`, `PasswordEncoder`, `AuthenticationManager`, `JwtUtils`, `UserKafkaProducer`, inject vào `UserServiceIMPL`.

- **Module `user-service`**
  - `com.r2s.user.UserServiceTest`
  - `com.r2s.user.UserProfileServiceIMPLTest`

### 1.3. Cách tự phân loại một test là unit test

Một test class mới có thể xem là **unit test** nếu thỏa:

- Có `@ExtendWith(MockitoExtension.class)` (hoặc dùng thuần JUnit, không bật Spring).
- Không dùng `@SpringBootTest`, `@AutoConfigureMockMvc`, `@DataJpaTest`, `@SpringBootTest(webEnvironment = ...)`.
- Toàn bộ dependency là **mock** (không kết nối DB, không container, không Kafka thật).

---

## 2. Integration test

Ở đây hiểu là: **test một service / API với Spring context thật**, có thể:
- Mock một số bean (service layer), **nhưng chạy MVC layer, security, validation, exception handler… thật**.
- Hoặc chạy full service với DB (H2 / Postgres Testcontainers).

### 2.1. Đặc điểm nhận biết

- **Annotation chính**
  - `@SpringBootTest` (có thể kèm `webEnvironment = RANDOM_PORT`).
  - Hay đi kèm:
    - `@AutoConfigureMockMvc`
    - `@AutoConfigureTestDatabase`
    - `@ActiveProfiles("test")`
    - `@MockBean` để fake một số dependency.
- **Scope**
  - Gọi **HTTP endpoint** thật qua `MockMvc` hoặc RestTemplate/WebTestClient.
  - Spring Security, validation, exception handler… được bật thật.
  - Có thể dùng **DB thật** (Postgres qua Testcontainers) hoặc H2.

### 2.2. Các class integration test chính trong project

- **Module `auth-service`**
  - `com.r2s.auth.AuthControllerIntegrationTest`
    - `@SpringBootTest`, `@AutoConfigureMockMvc`, dùng `MockMvc` để gọi các endpoint `/auth/...` với context thật.

- **Module `user-service`**
  - `com.r2s.user.integration.UserControllerIntegrationTest`
    - `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`
    - `@AutoConfigureMockMvc`
    - `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)`
    - Dùng **PostgreSQL Testcontainers** (nếu start được), gọi trực tiếp `/users/...` qua `MockMvc`, đi qua security + repository thật.

### 2.3. Các test “giữa chừng” (controller test nhưng vẫn là integration nhỏ)

Một số test controller dùng `@SpringBootTest` + `@AutoConfigureMockMvc` nhưng **mock service layer**, ví dụ:

- `auth-service`: `com.r2s.auth.AuthControllerTest`
- `user-service`: `com.r2s.user.UserControllerTest`

Đây vẫn là **integration test ở mức web/controller** (vì:
- Chạy Spring context thật (MVC, security, exception handler).
- Dùng `MockMvc` gọi HTTP endpoint.
), nhưng không đi sâu tới DB vì service được mock.

Khi mô tả trong báo cáo, bạn có thể gọi chúng là:
- **Controller integration tests** hoặc **Web layer integration tests**.

### 2.4. Cách tự phân loại một test là integration test

Một test class mới có thể xem là **integration test** nếu:

- Có `@SpringBootTest` (hoặc `@DataJpaTest`, `@WebMvcTest`, …) và bật Spring context.
- Dùng `MockMvc` / RestTemplate để gọi endpoint, hoặc repository/service thật với DB (H2/Postgres).
- Có thể mock một số bean bằng `@MockBean` nhưng **vẫn có Spring context**.

---

## 3. System / E2E test

Trong project này, “E2E” được hiện thực dưới dạng **system / cross-service test** trong module `system-tests`.

### 3.1. Đặc điểm nhận biết

- Nằm trong **module riêng**: `system-tests`.
- Dùng `@SpringBootTest(classes = TestConfiguration.class)` để khởi tạo 1 app test riêng.
- Kết hợp nhiều thành phần cross-service:
  - Entity + Repository từ `core`
  - `PasswordEncoder`
  - `JwtUtils` với cấu hình thật
- Có thể dùng:
  - H2 in-memory (cấu hình qua `@DynamicPropertySource`).
  - Hoặc DB cấu hình ngoài qua `application-test-no-docker.yaml`.

### 3.2. Các class system / e2e test trong project

- `com.r2s.system.CrossServiceIntegrationTest`
  - Dùng H2 in-memory (config qua `@DynamicPropertySource`).
  - Kiểm tra:
    - Tạo user + role, generate & validate JWT.
    - Data consistency (lưu & đọc lại user).
    - Role-based authentication (nhiều role).
    - Concurrent user creation.
    - Token expiration.

- `com.r2s.system.CrossServiceIntegrationTestNoDocker`
  - Dùng DB theo cấu hình `application-test-no-docker.yaml`.
  - Cũng test JWT, data consistency, role-based authentication… nhưng trong bối cảnh **không dùng Docker/Testcontainers**.

- `com.r2s.system.TestConfiguration`
  - Spring Boot app test, scan `com.r2s.core` + cấu hình `PasswordEncoder`, `JwtUtils`.

### 3.3. Cách tự phân loại một test là system / e2e test

Một test class có xu hướng là **system / e2e** nếu:

- Nằm trong module **riêng cho system test / e2e** (ở đây là `system-tests`).
- Khởi chạy context bao trùm nhiều module (core, auth, user…).
- Mô phỏng **luồng nghiệp vụ end-to-end** (VD: tạo user, gán role, sinh JWT, dùng JWT để xác thực).
- Có thể dùng real DB, nhiều bean thật, ít hoặc không mock.

---

## 4. Tóm tắt nhanh để nhận biết

- **Unit test**
  - `@ExtendWith(MockitoExtension.class)` hoặc JUnit thuần.
  - Không dùng `@SpringBootTest`.
  - Mock toàn bộ dependency, test 1 class.

- **Integration test (service / web / repository)**
  - Có `@SpringBootTest` / `@AutoConfigureMockMvc` / `@DataJpaTest`…
  - Gọi HTTP endpoint hoặc dùng bean thật (service/repository) với DB (H2/Postgres).
  - Có thể mock một số bean bằng `@MockBean`.

- **System / E2E test**
  - Module riêng (`system-tests`), context bao nhiều module.
  - Mô phỏng luồng nghiệp vụ “từ đầu tới cuối”.
  - Ít mock, dùng gần như toàn bộ stack thật (entity, repo, security, JWT, DB…).

Khi thêm test mới, bạn có thể mở file này và:
- Tham chiếu class tương tự để copy cấu trúc.
- Ghi chú trong Javadoc / comment đầu file: **"This is a unit test"**, **"This is a web-layer integration test"**, **"This is a system/E2E test"** để người đọc dễ phân loại.


