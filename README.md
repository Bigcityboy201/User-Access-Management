
# User Access Management Platform

Multi-module Spring Boot system for managing authentication, authorization, and user lifecycle events. The code in this repository powers the `ngoquangtruong2012004-project` GitLab project and serves as the basis for experiments with CI/CD, integration tests, and containerized deployment.

## Architecture

| Module | Purpose |
| ------ | ------- |
| `core` | Shared domain objects (users, roles, permissions), Spring Security glue code, and reusable utilities. |
| `user-service` | Exposes CRUD APIs for tenants/users, persists data, and emits domain events. |
| `auth-service` | Handles login, token issuance, and cross-service authentication. |
| `system-tests` | End-to-end verification suite that boots the stack and exercises the most important user journeys. |

Support scripts such as `docker-compose.yaml`, `start-all.bat`, and database bootstrap scripts live in the repository root.

## Prerequisites

- JDK 17+
- Docker & Docker Compose (for local infra and CI parity)
- Maven Wrapper (`./mvnw`) – already committed, no global Maven install required

## Quick Start

```bash
git clone https://gitlab.com/ngoquangtruong2012004-group/ngoquangtruong2012004-project.git
cd user-access-management
./mvnw clean package
docker-compose up --build
```

The compose stack wires together Postgres, `user-service`, and `auth-service`. Adjust exposed ports inside `docker-compose.yaml` if you already have services running on 8080/8081.

## Testing Strategy

- **Unit / component tests:** `./mvnw test` (runs inside CI as part of the `test-job` stage).
- **Integration tests:** each module owns integration suites under `src/test/java`.
- **System tests:** `cd system-tests && ../mvnw verify -Psystem` boots the entire topology and validates authentication flows.
- Helper script `run-tests.bat` chains the most common checks for Windows developers.

## CI/CD

`.gitlab-ci.yml` defines three stages (`build`, `test`, `deploy`). Artifacts produced by `./mvnw clean package -DskipTests` feed subsequent stages. The deploy step uses Docker-in-Docker to restart the `myapp` container with the latest jar (`target/demo-1.0.jar` by default); adjust `APP_NAME` and `JAR_PATH` variables to match your desired image or module output.

Pushes to `main` automatically trigger all stages in GitLab. Merge Request pipelines run the build and test stages to block regressions before merging.

## Local Development Tips

1. Use `./mvnw spring-boot:run` within `auth-service` or `user-service` to iterate quickly.
2. Profiles (`application-local`, `application-dev`, `application-docker`) are pre-configured; export `SPRING_PROFILES_ACTIVE` to switch contexts.
3. `postgres/init.sql` provisions baseline roles and demo accounts—run it once or let Docker compose load it at startup.

## Contributing

1. Create a feature branch from `main`.
2. Ensure `./mvnw clean verify` succeeds.
3. Update this README or `docs/` when behavior changes.
4. Open a Merge Request in GitLab and wait for the `build-job` + `test-job` pipeline to pass.

## License

Internal learning project. Contact the maintainers before reusing code outside the `ngoquangtruong2012004-project` boundaries.