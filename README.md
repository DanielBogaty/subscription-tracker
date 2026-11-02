# 📱 Subscription Tracker

Мобильное Android-приложение для учёта личных и семейных подписок, контроля ежемесячных расходов и автоматического отслеживания изменения цен. Приложение сочетает локальное хранение данных в Room с облачной синхронизацией семейных подписок в реальном времени через Firebase.

---

## 🛠 Стек технологий

- **Язык:** Kotlin 2.0 (Coroutines, StateFlow)
- **UI:** Jetpack Compose (Material 3), Navigation Compose
- **Архитектура:** MVVM + Repository
- **Локальная БД:** Room (`subscriptions`, `price_history`)
- **Облако и авторизация:** Firebase Authentication (Email/Password), Cloud Firestore (Real-time синхронизация)
- **Фоновые задачи:** Android WorkManager (проверка цен + напоминания об оплате)

---

## 🚀 Инструкция по запуску

1. **Клонируйте репозиторий:**
   ```bash
   git clone <ссылка_на_репозиторий>
   cd subscription-tracker