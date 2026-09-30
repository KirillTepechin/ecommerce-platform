# Ecommerce Platform

**Spring Boot микросервисная платформа** онлайн-магазина. Координация распределённых транзакций через Saga-паттерн, надёжная доставка событий через Kafka (Outbox pattern), аутентификация через Keycloak, обнаружение сервисов через Netflix Eureka.

---

##  Архитектура

```
                    ┌─────────────┐
                    │   Ingress   │
                    │  (nginx)    │
                    └──────┬──────┘
                           │
              ┌────────────┴────────────┐
              │                         │
    ┌─────────▼─────────┐   ┌──────────▼──────────┐
    │   Frontend (SPA)  │   │   API Gateway       │
    │   :80             │   │ (WebFlux + Gateway) │
    └───────────────────┘   └──────────┬──────────┘
                                       │
                    ┌──────────────────┼──────────────────┐
                    │                  │                  │
          ┌─────────▼─────────┐ ┌─────▼──────┐ ┌────────▼────────┐
          │  Order Service    │ │Payment Svc │ │Inventory Service│
          │  (PostgreSQL)     ││(PostgreSQL) │ │ (PostgreSQL)    │
          └───────────────────┘└────────────┘└─────────────────┘
                    │                  │                  │
                    └────────┬─────────┴─────────┬────────┘
                             │                   │
                      ┌──────▼──────┐    ┌───────▼──────┐
                      │  Kafka 3×   │    │   Eureka     │
                      │  (KRaft)    │    │  (Service    │
                      └─────────────┘    │   Discovery) │
                                         └──────────────┘
                             │
                      ┌──────▼──────┐
                      │  MongoDB    │
                      │ (Analytics) │
                      └─────────────┘
```
## Event Flow (Saga Pattern):
```text
1. Client
   │
   │  POST /api/orders
   ▼

2. Order Service
   │
   │  публикует событие
   ▼
   [ order-created topic ]
   │
   ▼

3. Inventory Service
   │  резервирует товары
   ├─ ✅ Успех  → [ inventory-reserved topic ]
   └─ ❌ Нет товара → [ order-rejected topic ]
   │
   ▼

4. Payment Service
   │  (слушает inventory-reserved)
   ├─ ✅ Успех  → [ payment-completed topic ]
   └─ ❌ Ошибка → [ payment-failed topic ]
   │
   ▼

5. Order Service
   │  обновляет статус заказа:
   │
   • inventory-reserved  → CONFIRMED
   • payment-completed   → PAID
   • payment-failed      → CANCELLED + компенсация
   • order-rejected      → CANCELLED + компенсация
```
### Модули

| Модуль | Порт (K8s) | Стек | Описание |
|--------|------------|------|----------|
| **api-gateway** | 9000 | Spring Cloud Gateway, WebFlux | Маршрутизация, OAuth2 Resource Server, Token Relay |
| **service-discovery** | 8761 | Netflix Eureka | Обнаружение сервисов |
| **order-service** | 8081 | Spring Boot, PostgreSQL, Kafka | Управление заказами, Saga orchestrator |
| **payment-service** | 8083 | Spring Boot, PostgreSQL, Kafka | Обработка платежей |
| **inventory-service** | 8082 | Spring Boot, PostgreSQL, Kafka | Управление складом |
| **analytics-service** | 8084 | Spring Boot, MongoDB, Kafka Streams | Аналитика событий |
| **common-events** | — | Plain Java | Общие DTO событий |
| **frontend** | 80 | nginx + SPA | JS фронтенд |

### Ключевые паттерны

- **Saga** — координация транзакций Order → Inventory → Payment
- **Outbox** — `OrderOutboxService` для надёжной публикации событий в Kafka
- **Circuit Breaker** — Resilience4j на уровне Gateway
- **OAuth2 / OIDC** — Keycloak → JWT → Gateway → TokenRelay

---

##  Быстрый старт (локально)

### 1. Инфраструктура

```powershell
# Kafka + MongoDB + Keycloak
docker-compose -f docker-compose/docker-compose.yml up -d
```

### 2. Запуск сервисов

```powershell
# Сборка
./gradlew build

# Запуск конкретного сервиса
./gradlew :order-service:bootRun
./gradlew :payment-service:bootRun
./gradlew :inventory-service:bootRun
./gradlew :api-gateway:bootRun
./gradlew :service-discovery:bootRun
```

Локально каждый сервис биндится на случайный порт (`port: 0`) и регистрируется в Eureka на `localhost:8761`. API Gateway маршрутизирует запросы на `localhost:8080`.

---

##  Деплой в Kubernetes

### Требования

- Kubernetes cluster (1.25+)
- `kubectl` настроен на подключение к кластеру
- `helm` v3+
- nginx ingress controller
- StorageClass по умолчанию (для PVC)

### Вариант 1: Helm chart (рекомендуется)

Helm chart устанавливает **всё**: namespace, секреты, БД (PostgreSQL, MongoDB), Kafka (3 реплики), Keycloak, все микросервисы, Ingress.

```powershell
helm install ecommerce k8s/helm/ecommerce/
```

### Вариант 2: Манифесты (plain YAML)

```powershell
# 1. Namespace и секреты
kubectl apply -f k8s/deployments/namespace.yaml
kubectl apply -f k8s/secrets/

# 2. StatefulSet (БД и Kafka)
kubectl apply -f k8s/statefulsets/

# 3. Deployments (сервисы)
kubectl apply -f k8s/deployments/

# 4. Ingress
kubectl apply -f k8s/ingress/

```

### Образы

```powershell
# Сборка образов
docker build -t ecommerce/api-gateway:v3 ./api-gateway
...

# Загрузить в minikube
minikube image load ecommerce/api-gateway:v3
# ... и так далее
```

### Доступы

| Сервис | Порт | Описание |
|--------|------|----------|
| Keycloak Admin | `http://keycloak.ecommerce.local:8080` | admin / admin123 |
| Frontend | `http://ecommerce.local` | SPA + OAuth |
| API | `http://api.ecommerce.local` | REST Gateway |
| Kafka UI | `http://localhost:8085` (port-forward) | provectuslabs/kafka-ui |

---

##  Конфигурация

Каждый сервис имеет два профиля:

| Профиль | Назначение |
|---------|-----------|
| `local` | Локальная разработка (docker-compose) |
| `kubernetes` | Деплой в K8s |
---

##  Структура

```
ecommerce-platform/
├── api-gateway/              # Spring Cloud Gateway
├── service-discovery/        # Netflix Eureka
├── order-service/            # Orders (PostgreSQL, Kafka)
├── payment-service/          # Payments (PostgreSQL, Kafka)
├── inventory-service/        # Stock (PostgreSQL, Kafka)
├── analytics-service/        # Events (MongoDB, Kafka Streams)
├── common-events/            # Shared event DTOs
├── frontend/                 # SPA (nginx)
├── k8s/                      # Kubernetes manifests + Helm
│   ├── deployments/          # Deployment + Service YAML
│   ├── statefulsets/         # PostgreSQL, MongoDB, Kafka
│   ├── secrets/              # K8s Secrets
│   ├── ingress/              # Ingress rules
│   └── helm/ecommerce/       # Helm chart
├── docker-compose/           # Local infra (Kafka, MongoDB, Keycloak)
└── docker-compose-kafka/     # Kafka only
```
