# Схема базы данных

## Таблица subscriptions
- id (Long, PK, autoGenerate)
- name (String)
- price (Double)
- previousPrice (Double?)
- currency (String)
- cycle (String)
- nextPaymentDate (String)
- ownerId (String)
- familyId (String?)
- colorHex (String)
- note (String)

## Таблица price_history
- id (Long, PK, autoGenerate)
- subscriptionId (Long, FK → subscriptions.id, CASCADE)
- price (Double)
- date (String)