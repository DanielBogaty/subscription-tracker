# Архитектура приложения

## Стек технологий
- Kotlin
- Jetpack Compose (UI)
- Room (локальная БД)
- WorkManager (фоновые задачи)
- MVVM архитектура

## Слои
- data/ — работа с данными (Room DAO, Repository)
- domain/ — бизнес-модели (Subscription, PriceHistory)
- feature/ — экраны и ViewModel
- worker/ — фоновые задачи
- core/ — навигация, тема, утилиты