# Архитектура приложения

## Стек технологий
- **Kotlin** + **Coroutines / Flow** (асинхронность и реактивные потоки данных)
- **Jetpack Compose** (Material 3 UI)
- **Navigation Compose** (навигация между экранами и `NavigationBar`)
- **Room** (локальная база данных для личных подписок и истории цен)
- **Firebase Authentication** (авторизация и регистрация по Email/Password)
- **Cloud Firestore** (облачная NoSQL БД для синхронизации семьи и семейных подписок в реальном времени)
- **WorkManager** (фоновый мониторинг цен и напоминания об оплате)
- **MVVM + Repository** (архитектурный паттерн)

## Слои приложения
- `data/` — работа с данными:
  - `local/` — Room DAO, сущности (`SubscriptionEntity`, `PriceHistoryEntity`), `AppDatabase`
  - `repository/` — репозитории:
    - `SubscriptionRepository` — CRUD для локальных подписок и истории цен (Room)
    - `AuthRepository` — вход, регистрация, выход и получение `currentUserId` (Firebase Auth)
    - `FamilyRepository` — создание семьи, подключение по 6-значному инвайт-коду, real-time синхронизация семейных подписок через `SnapshotListener` (Cloud Firestore)
  - `price/` — сервис проверки изменения цен (`PriceSource`)
- `domain/` — бизнес-модели (`Subscription`, `BillingCycle`, `PriceHistory`)
- `feature/` — UI-слой (экраны Compose и ViewModel):
  - `auth/` — `LoginScreen`, `RegisterScreen`
  - `subscriptions/` — `SubscriptionsListScreen`, `AddEditSubscriptionScreen`, `SubscriptionDetailScreen`, `SubscriptionViewModel`
  - `family/` — `FamilyScreen`
  - `stats/` — `StatsScreen`
- `worker/` — фоновые задачи (`PriceCheckWorker`, `PaymentReminderWorker`), проверяющие как локальные (Room), так и семейные (Firestore) подписки
- `core/` — навигация (`AppNavHost`), тема оформления

## Схема потоков данных (Гибридное хранилище)
1. **Личные подписки** сохраняются локально в **Room** и доступны офлайн.
2. **Семейные подписки** сохраняются в **Cloud Firestore** (`family_subscriptions`) и синхронизируются между всеми участниками семьи в реальном времени.
3. `SubscriptionViewModel` объединяет потоки из Room и Firestore с помощью оператора `combine`, предоставляя UI единый список подписок для отображения и расчёта статистики.