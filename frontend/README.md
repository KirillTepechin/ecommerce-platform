# 🐾 Pet Shop Frontend

Супер простой фронтенд для ecommerce-platform. Один HTML файл + nginx для проксирования внутри Kubernetes.

## 📁 Структура

```
frontend/
├── index.html    # Весь UI + JS (SPA)
├── nginx.conf    # Прокси: /auth → Keycloak, /api → API Gateway
├── Dockerfile
└── README.md
```

## 🚀 Быстрый старт (локально)

### 1. Запустить порт-форвард на API Gateway и Keycloak

```powershell
# Terminal 1
kubectl port-forward svc/api-gateway 9000:9000 -n ecommerce

# Terminal 2
kubectl port-forward svc/keycloak 8080:8080 -n ecommerce
```

### 2. Открыть index.html в браузере

Просто открой файл `frontend/index.html` в браузере.

### 3. Войти и создать заказ

- Логин/пароль — из твоего Keycloak (realm: `ecommerce-realm`)
- Client ID: `ecommerce-frontend`
- Создать заказ → отследить статус → отменить

---

## 🐳 Сборка и деплой в Kubernetes

### 1. Собрать и запушить образ

```powershell
docker build -t ecommerce/frontend:v1 ./frontend
docker push ecommerce/frontend:v1
```

### 2. Применить манифесты

```powershell
kubectl apply -f k8s/deployments/frontend.yaml
```

### 3. Порт-форвард на frontend (для проверки)

```powershell
kubectl port-forward svc/frontend 8080:80 -n ecommerce
```

Открыть: http://localhost:8080

### 4. Или через Ingress (если настроен)

Добавить в hosts:
```
127.0.0.1 ecommerce.local
```

Открыть: http://ecommerce.local

---

## 🌐 Как работает

```
Браузер
  │
  ├─ /auth/realms/...    → nginx → Keycloak (keycloak.ecommerce.svc:8080)
  │
  └─ /api/orders/...     → nginx → API Gateway (api-gateway.ecommerce.svc:9000)
                              └──→ Order Service
```

Всё работает через один порт — nginx внутри pod проксирует запросы.

---

## 🔧 Кастомизация

### Изменить client_id

В `index.html`:
```javascript
client_id: 'ecommerce-frontend',  // ← поменяй здесь
```

### Изменить URL сервисов

В `nginx.conf`:
```nginx
proxy_pass http://keycloak.NAMESPACE.svc.cluster.local:8080/...
proxy_pass http://api-gateway.NAMESPACE.svc.cluster.local:9000/...
```
