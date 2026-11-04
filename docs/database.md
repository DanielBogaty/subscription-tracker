# Схема базы данных

Приложение использует гибридную схему хранения данных: **Room (SQLite)** для локальных данных и **Cloud Firestore (NoSQL)** для облачной синхронизации семейных данных.

---

## 1. Локальная база данных (Room)

### Таблица `subscriptions`
- `id` (Long, PK, autoGenerate) — уникальный идентификатор
- `name` (String) — название сервиса
- `price` (Double) — текущая стоимость
- `previousPrice` (Double?) — предыдущая стоимость (до изменения цены)
- `currency` (String) — валюта (по умолчанию `"RUB"`)
- `cycle` (String) — расчётный период (`WEEKLY`, `MONTHLY`, `QUARTERLY`, `YEARLY`)
- `nextPaymentDate` (String) — дата следующего списания (`YYYY-MM-DD`)
- `ownerId` (String) — UID владельца подписки
- `familyId` (String?) — ID семьи (`null` для личных подписок)
- `colorHex` (String) — цвет карточки
- `note` (String) — заметка

### Таблица `price_history`
- `id` (Long, PK, autoGenerate) — уникальный идентификатор записи
- `subscriptionId` (Long, FK → `subscriptions.id`, CASCADE) — ссылка на подписку
- `price` (Double) — старая цена до изменения
- `date` (String) — дата фиксации изменения (`YYYY-MM-DD`)

---

## 2. Облачная база данных (Cloud Firestore)

### Коллекция `families`
Хранит данные о семейных группах.
- `familyId` (String, Document ID) — уникальный идентификатор семьи
- `name` (String) — название семьи
- `ownerId` (String) — Firebase UID создателя семьи
- `members` (Array<String>) — массив `userId` всех участников семьи
- `inviteCode` (String) — 6-значный код для приглашения новых участников

### Коллекция `family_subscriptions`
Хранит общие подписки участников семьи (синхронизируются в реальном времени).
- `id` (Number, Document ID) — уникальный идентификатор подписки
- `name` (String) — название подписки
- `price` (Number) — текущая стоимость
- `previousPrice` (Number?) — предыдущая стоимость
- `currency` (String) — валюта (`"RUB"`)
- `cycle` (String) — период оплаты (`WEEKLY`, `MONTHLY`, `QUARTERLY`, `YEARLY`)
- `nextPaymentDate` (String) — дата следующей оплаты (`YYYY-MM-DD`)
- `ownerId` (String) — UID участника, добавившего подписку
- `familyId` (String, FK → `families.familyId`) — идентификатор семьи

---

## 3. ER-диаграмма

```mermaid
erDiagram
    USERS ||--o{ SUBSCRIPTIONS : "owns (local)"
    SUBSCRIPTIONS ||--o{ PRICE_HISTORY : "has"
    USERS }|--o{ FAMILIES : "members (Firestore)"
    FAMILIES ||--o{ FAMILY_SUBSCRIPTIONS : "shares (Firestore)"