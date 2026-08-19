# Notification Service

A Kafka-driven, multi-channel notification service built with Spring Boot 4.1. The service decouples the HTTP request that creates a notification from the actual delivery work: an API call writes the record to Postgres and publishes an event to Kafka, while a consumer running in the same process handles template rendering, Redis-based deduplication, user-preference checks, and email dispatch — all without blocking the original caller.

---

## Tech Stack

- **Java 17** · **Spring Boot 4.1.0**
- **Spring MVC** — REST API layer
- **Spring Data JPA / Hibernate** — ORM, PostgreSQL dialect
- **PostgreSQL** — primary datastore
- **Apache Kafka** — event bus for decoupled notification dispatch
- **Spring Data Redis** — deduplication and preference caching
- **Spring Security + JJWT 0.12.5** — stateless JWT authentication
- **Spring Mail (SMTP)** — HTML email delivery via Jakarta Mail / Gmail SMTP
- **Spring Validation (Jakarta Bean Validation)** — request-level validation
- **Lombok** — boilerplate reduction
- **springdoc-openapi 3.1.0** — OpenAPI 3 / Swagger UI

---

## Architecture Overview

```
HTTP Client
    │
    ▼
[REST Controller]
    │  1. Validate request
    │  2. Resolve recipient user from Postgres
    │  3. Save Notification record (status = PENDING)
    │  4. Publish NotificationAddRequest → Kafka topic: notification-events
    │  5. Return 200 immediately
    │
    ▼
[Kafka Consumer — NotificationDispatcher]
    │
    ├─ 1. Deduplication check (Redis key: dedup:<idempotencyKey>:<channel>:<eventType>)
    │       If key exists → ack & skip
    │       If key absent → set key with 60-second TTL and proceed
    │
    ├─ 2. Load user notification preference
    │       Redis key: prefs:<userId>:<channel>:<eventType>
    │       Cache miss → fetch from Postgres, write to Redis (TTL 10 min)
    │
    ├─ 3. If channel = EMAIL and preference.enabled = true:
    │       a. Fetch Notification record from Postgres
    │       b. Render HTML template (EmailTemplateUtil) — replaces {{placeholders}}
    │       c. Send email (EmailSenderUtil → JavaMailSender)
    │       d. On success → set status = SENT, ack Kafka message
    │       e. On EmailSendingException → increment attemptCount, save error message
    │             If attemptCount >= max-retry-count (3) → set status = FAILED, ack
    │             Otherwise → rethrow (triggers @RetryableTopic backoff)
    │
    └─ @RetryableTopic: up to 3 attempts, 60 s initial delay, 2× backoff, no DLT
```

---

## Key Design Decisions

**Why Kafka decouples the API from delivery latency**
Email delivery involves SMTP round-trips, template rendering, and possible retries. Doing this synchronously in the HTTP request would significantly inflate p99 latency and couple delivery failure to API availability. Publishing to Kafka makes the API response instant and lets the consumer retry independently.

**Redis — deduplication**
The idempotency key embedded in each `NotificationAddRequest` is used to build a Redis key with a 60-second TTL. If a duplicate message arrives within that window (e.g. a Kafka re-delivery before ACK), the consumer detects the key, acknowledges the message, and discards it — preventing double delivery without a DB query.

**Redis — preference caching**
Every Kafka message triggers a preference lookup (is this user opted-in for this channel/event type?). Querying Postgres for every message at scale would be expensive. Preferences are written to Redis on first access (TTL 10 minutes) and served from cache on subsequent messages, keeping the hot path off the database.

**No DLQ / no indefinite backoff — intentional**
The retry strategy (`@RetryableTopic`, 3 attempts, 2× backoff, `DltStrategy.NO_DLT`) is intentionally simple: attempt delivery up to three times, then mark the notification as FAILED and stop. There is no dead-letter queue and no external alerting. This is a known trade-off: it avoids operational overhead at the cost of silent failure after three attempts. A full DLQ / backoff system is out of scope for the current implementation.

**Templates use `multipart/form-data`**
The HTML body template is an uploaded `.html` file rather than a JSON string, making it easier to author and version outside the application. Placeholders follow `{{key}}` syntax; the service extracts and stores them at upload time, then validates them at render time.

---

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/user/register` | Public | Register a new user; returns JWT |
| `POST` | `/api/user/login` | Public | Authenticate; returns JWT |
| `POST` | `/api/notifications` | JWT | Queue a notification (writes to DB + Kafka) |
| `GET` | `/api/notifications` | JWT | List paginated notifications for current user; filter by `status` |
| `GET` | `/api/notifications/{id}` | JWT | Fetch a single notification by ID |
| `POST` | `/api/notification/preferences/` | JWT | Create a channel/event preference for current user |
| `PUT` | `/api/notification/preferences/{id}` | JWT | Partially update a preference |
| `GET` | `/api/notification/preferences` | JWT | List all preferences for current user |
| `POST` | `/api/notification/templates` | JWT + ADMIN | Upload a new HTML email template (multipart) |
| `PUT` | `/api/notification/templates/{id}` | JWT + ADMIN | Update an existing template (multipart) |
| `GET` | `/api/notification/templates` | JWT + ADMIN | List templates; optional `channel` / `eventType` filters |

Valid enum values:
- **Channel:** `EMAIL`, `IN_APP`, `PUSH`
- **EventType:** `DAILY_UPDATE`, `WEEKLY_UPDATE`, `COMMENT_UPDATE`, `MEET_TRANSCRIPT`, `ORDER_UPDATE`
- **NotificationStatus:** `PENDING`, `SENT`, `FAILED`
- **Role:** `CLIENT`, `ADMIN`

---

## Getting Started

### Prerequisites

- Java 17+
- PostgreSQL (local or remote)
- Redis (local or remote)
- Apache Kafka (local — default `localhost:9092`)
- Maven (or use the `./mvnw` wrapper included in the repo)

### Running locally

1. **Configure the application**

   Edit `src/main/resources/application.properties` and fill in real values for all credentials (see [Environment Variables](#environment-variables--config) below).

2. **Start infrastructure**

   Start PostgreSQL, Redis, and Kafka. If you are using local Docker containers:
   ```bash
   # PostgreSQL
   docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=root -e POSTGRES_DB=my-test-db postgres

   # Redis
   docker run -d -p 6379:6379 redis

   # Kafka (with ZooKeeper)
   docker run -d -p 9092:9092 apache/kafka
   ```

3. **Run the application**

   ```bash
   ./mvnw spring-boot:run
   ```

4. **Open Swagger UI**

   ```
   http://localhost:8080/swagger-ui/index.html
   ```

   The OpenAPI JSON spec is at:
   ```
   http://localhost:8080/v3/api-docs
   ```

   Click **Authorize** in Swagger UI, paste a JWT obtained from `/api/user/login`, and all protected endpoints will be authenticated.

---

## Environment Variables / Config

All configuration lives in `src/main/resources/application.properties`. The following values must be set before running:

| Property | Description |
|----------|-------------|
| `spring.datasource.url` | JDBC URL for your PostgreSQL instance |
| `spring.datasource.username` | Postgres username |
| `spring.datasource.password` | Postgres password |
| `jwt.secret` | HS256 secret key (min 32 characters recommended) |
| `jwt.expiration` | JWT expiry in milliseconds (e.g. `86400000` = 24 h) |
| `spring.mail.username` | Gmail address used as the sender |
| `spring.mail.password` | Gmail app password (not your Google account password) |
| `spring.data.redis.host` | Redis host |
| `spring.data.redis.port` | Redis port |
| `spring.data.redis.password` | Redis password (leave blank if none) |
| `spring.kafka.bootstrap-servers` | Kafka broker address (e.g. `localhost:9092`) |
| `notification.max-retry-count` | Max delivery attempts before marking FAILED (default `3`) |

> There is no `application.properties.example` file in the repository yet. Copy the properties above from the table as a starting template.

---

## Running Tests

```bash
./mvnw test
```

The test suite covers service-layer unit tests using Mockito (no Spring context required for most tests):

- `UserServiceTest` — register success, duplicate email, login success (3 tests)
- `NotificationPreferenceServiceTest` — create, update, get preferences, not-found case (4 tests)
- `NotificationTemplateServiceTest` — create, update, get with/without filters, not-found case (5 tests)
- `NotificationServiceTest` — add notification, user-not-found, get list (with and without status filter), get by ID, not-found case (6 tests)
- `NotificationServiceApplicationTests` — Spring context load smoke test

---

## Not Yet Implemented / Future Improvements

- **IN_APP and PUSH channels** — the `Channel` enum defines `IN_APP` and `PUSH`, but `NotificationDispatcher` currently only handles `Channel.EMAIL`. Other channels are silently acknowledged without dispatch.
- **Dead-letter queue (DLQ)** — after 3 failed delivery attempts the notification is marked FAILED with no further action. A DLQ with alerting is not configured.
- **Scheduled / batch notifications** — all notifications are event-triggered. Scheduled or digest-style delivery is not implemented.
- **Docker Compose / CI-CD** — no `Dockerfile`, `docker-compose.yml`, or `.github/workflows/` are present in the repository. Deployment is currently manual.
