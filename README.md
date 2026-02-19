# ShareIt

Сервис **шеринга вещей**: пользователи добавляют вещи, находят подходящие по поиску, бронируют их на период времени, оставляют комментарии к использованным вещам, а также создают запросы на нужные вещи.

---

## Архитектура проекта

Проект построен по принципу **Gateway + Server** и состоит из трёх контейнеров (см. `docker-compose.yaml`):

1. **shareit-gateway** — порт **8080**
    - Принимает входящие HTTP-запросы от клиентов
    - Выполняет валидацию входных данных (в том числе query-параметров и дат бронирования)
    - Проксирует запросы в основной сервис по URL `SHAREIT_SERVER_URL`

2. **shareit-server** — порт **9090**
    - Основная бизнес-логика: пользователи, вещи, бронирования, комментарии, запросы вещей
    - Работает с базой данных PostgreSQL
    - Инициализирует схему из `server/src/main/resources/schema.sql`

3. **PostgreSQL** — порт подключения с хоста **6541**
    - БД: `shareit`, пользователь: `dbuser`, пароль: `12345`

Схема взаимодействия:

`client -> gateway (8080) -> server (9090) -> postgres (5432)`

---

## Технологии

- **Java 21**
- **Spring Boot 3.3.2**
- **Spring Web / Validation**
- **Spring Data JPA (Hibernate)**
- **PostgreSQL 16.1**
- **Maven** (многомодульная сборка: `server`, `gateway`)
- **Lombok**
- **MapStruct**
- **Docker / Docker Compose**
- **Тесты:** Spring Boot Test, JUnit; для тестового профиля используется **H2** (in-memory)

---

## База данных

Основные таблицы (см. `schema.sql`):

- `users` — пользователи
- `items` — вещи (владельцы, доступность, связь с запросом)
- `bookings` — бронирования вещей (статус, период)
- `comments` — комментарии к вещам
- `item_requests` — запросы на вещи

---

## Запуск проекта

### 1) Запуск через Docker Compose (рекомендуется)

В корне проекта:

```bash
docker compose up -d
```

Поднимутся контейнеры:

- `postgres` (порт 6541 → 5432)
- `shareit-server` (порт 9090)
- `shareit-gateway` (порт 8080)

После запуска API доступно через gateway:

- `http://localhost:8080`

### 2) Локальный запуск без Docker (с базой в Docker)

1. Поднимите только PostgreSQL:

```bash
docker compose up -d db
```

2. Соберите проект:

```bash
mvn clean install
```

3. Запустите **server** (подставляем настройки БД, т.к. в docker-compose порт проброшен на `6541`):

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:6541/shareit SPRING_DATASOURCE_USERNAME=dbuser SPRING_DATASOURCE_PASSWORD=12345 mvn spring-boot:run -pl server
```

4. Запустите **gateway**:

```bash
SHAREIT_SERVER_URL=http://localhost:9090 mvn spring-boot:run -pl gateway
```

---

## Краткое описание API

### Важно про заголовок пользователя

Большинство эндпоинтов требует заголовок:

- `X-Sharer-User-Id: <id пользователя>`

Gateway прокидывает этот заголовок в server.

### Users (`/users`)

- **GET** `/users` — список пользователей
- **GET** `/users/{id}` — получить пользователя
- **POST** `/users` — создать пользователя
- **PATCH** `/users/{userId}` — обновить пользователя
- **DELETE** `/users/{id}` — удалить пользователя

### Items (`/items`)

- **POST** `/items` — добавить вещь
- **PATCH** `/items/{itemId}` — обновить вещь
- **GET** `/items/{itemId}` — получить вещь (для владельца — с бронированиями)
- **GET** `/items?from=0&size=10` — список вещей владельца (постранично)
- **GET** `/items/search?text=дрель` — поиск доступных вещей по тексту
- **POST** `/items/{itemId}/comment` — добавить комментарий к вещи (после завершённого бронирования)

### Bookings (`/bookings`)

- **POST** `/bookings` — создать бронирование
- **PATCH** `/bookings/{bookingId}?approved=true|false` — подтверждение/отклонение бронирования владельцем
- **GET** `/bookings/{bookingId}` — получить бронирование
- **GET** `/bookings?state=ALL&from=0&size=10` — бронирования текущего пользователя (booker)
- **GET** `/bookings/owner?state=ALL&from=0&size=10` — бронирования вещей текущего пользователя (owner)

Поддерживаемые значения `state`:
`ALL`, `CURRENT`, `PAST`, `FUTURE`, `WAITING`, `REJECTED`.

### Requests (`/requests`)

- **POST** `/requests` — создать запрос на вещь
- **GET** `/requests` — свои запросы
- **GET** `/requests/all?from=0&size=10` — все запросы других пользователей (постранично)
- **GET** `/requests/{requestId}` — получить запрос по id

---

## Тесты

### Postman

Коллекция для проверки API находится в папке **postman**:

- `postman/sprint.json`

### Unit/Integration tests

- Тесты располагаются в `gateway/src/test` и `server/src/test`
- Для тестов server используется профиль `test` с H2 (`jdbc:h2:mem:shareit`)
