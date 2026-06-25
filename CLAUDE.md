# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What Baba is

Baba is Entur's **Organisation Registry** — a Spring Boot service that manages organisations,
authorities, users, machine-to-machine (M2M) clients, administrative zones, roles, and
responsibility sets. It is the backing store for Ninkasi users and serves role assignments used
for authorization across other Entur systems.

Despite the `no.rutebanken` Java package, the Maven artifact is `no.rutebanken:baba` and the
parent POM is Entur's `org.entur.ror:superpom`.

## Build, test, run

Java 21. Maven build. The project inherits a **Prettier (prettier-java)** plugin bound to the
`validate` phase that reformats (or in CI, checks) all Java — printWidth 100, 2-space indent.

- **Build:** `mvn clean install`
- **Run tests fast (skip Prettier reformat):** `mvn test -P prettierSkip`
- **Single test:** `mvn test -P prettierSkip -Dtest=UserResourceIntegrationTest`
- **Single method:** `mvn test -P prettierSkip -Dtest=UserResourceIntegrationTest#someMethod`
- **Format code (run only when finalizing work):** `mvn prettier:write` (or a normal `mvn` build
  with no profile, since `write` is the default goal)
- **CI parity check (fails if unformatted):** `mvn verify -PprettierCheck`

Note: per global instructions, run Maven commands via a subagent, and use `-P prettierSkip` during
iteration; only run Prettier when finalizing.

## Architecture

### Layered REST stack

Each domain entity flows through a consistent vertical slice. To add or change an endpoint you
typically touch all of these for the entity:

```
Resource (JAX-RS) → Validator → Mapper (DTO⇄entity) → Repository (Spring Data JPA) → Entity (JPA)
```

- **`organisation.rest`** — JAX-RS resources (`*Resource`). Most CRUD logic lives in the generic
  `BaseResource<E, D>`, which orchestrates validate → map → persist. Concrete resources wire in
  their repository/mapper/validator/entity-class and add custom endpoints (e.g. `UserResource`
  has `/roleAssignments`, `/authenticatedUser`).
- **`organisation.rest.validation`** — `DTOValidator` implementations; `validateCreate` /
  `validateUpdate` / `validateDelete` hooks.
- **`organisation.rest.mapper`** — `DTOMapper` implementations converting between DTOs and entities.
- **`organisation.rest.dto`** — request/response DTOs.
- **`organisation.repository`** — Spring Data repositories. Custom base behavior is in
  `BaseRepositoryImpl` (registered as `repositoryBaseClass`), exposing `getOneByPublicId` etc.
- **`organisation.model`** — JPA entities. All persistent entities extend `VersionedEntity`
  (surrogate `pk`, optimistic-lock `lockVersion`, public `entityVersion`, and a `privateCode`
  that doubles as the public `id`). Public IDs are `CODESPACE:TYPE:privateCode` (see the `Id`
  record).

### Jersey / JAX-RS, not Spring MVC

REST endpoints are **JAX-RS resources served by Jersey**, not Spring `@RestController`s. Resources
are registered explicitly in `config/JerseyConfig.java` under the servlet mapping
`/services/organisations/*`. **A new `*Resource` must be added to `JerseyConfig` to be exposed.**
Exception mappers (`organisation.rest.exception`) translate exceptions to HTTP responses and are
likewise registered there. OpenAPI/Swagger is exposed at
`/services/organisations/openapi.json`.

### Security & authorization

- `security.oauth2.BabaWebSecurityConfiguration` (active when profile != `test`) is an OAuth2
  resource server using Entur's `MultiIssuerAuthenticationManagerResolver`. All requests require
  authentication except the OpenAPI doc and selected Actuator endpoints.
- Method-level authz uses Spring `@PreAuthorize` with a bean named `authorizationService`
  (e.g. `@PreAuthorize("@authorizationService.isOrganisationAdmin()")`).
- `config.AuthorizationConfig` selects beans by property:
  - `baba.security.authorization-service` = `token-based` | `full-access`
  - `baba.security.role.assignment.extractor` = `jwt` (default) | `baba` (local lookup via
    `LocalBabaRoleAssignmentExtractor`)
  - Tests use `full-access` (see `src/test/resources/application.properties`).
- The `permission-store` profile + `PermissionStoreConfig` wire an OAuth2 client (`WebClient`) to
  Entur's permission store. IAM provisioning currently routes through `NoopIamService` (permission
  store migration in progress — IAM changes are not yet forwarded).

The `no.entur.uttu.ext.entur.security` package is currently empty (placeholder dirs only).

### Persistence & migrations

- PostgreSQL + PostGIS in production (Hibernate Spatial for `AdministrativeZone` polygons); H2 for
  tests.
- Schema is managed by **Flyway** migrations in `src/main/resources/db/migration` (`V<n>__*.sql`).
  Add a new versioned file for any schema change and commit it with the code that needs it. Flyway
  is disabled in tests (`spring.flyway.enabled=false`); the H2 test schema comes from Hibernate
  `ddl-auto` + `import.sql`.

### Other notable pieces

- **Email:** `organisation.email` sends new-user emails via FreeMarker templates
  (`resources/templates/*.ftl`) + Spring Mail; i18n strings in `messages*.properties`.
- **Caching:** Spring Cache + Caffeine (`config.CachingConfig`).
- **Observability:** Micrometer/Prometheus at `/actuator/prometheus`; health at
  `/actuator/health` (+ liveness/readiness).

## Testing conventions

- `BabaTestApp` is a test-only Spring Boot app that excludes the real `App` and
  `SecurityAutoConfiguration`.
- Repository/integration tests extend `BaseIntegrationTest` — `@SpringBootTest` on a random port,
  `@Transactional` (rolled back per test), with an all-permit security filter chain and a stub
  `PermissionStoreClient`. It seeds a default `CodeSpace` and `Authority` in `@BeforeEach`.
- `*ResourceIntegrationTest` use REST Assured against the running port.
- Tests run on the `test` Spring profile via `src/test/resources/application.properties`.

## Deployment

Containerized via the multi-stage `Dockerfile` (Spring Boot layered jar on Liberica JRE 21) and
deployed with the Helm chart under `helm/baba`. CI is GitHub Actions (`.github/workflows/push.yml`):
Maven verify with `prettierCheck`, SonarCloud scan, then Docker build/push on `master`.
