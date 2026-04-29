# CrackHash
## 1. Описание архитектуры
 Архитектура проекта состоит из четырёх приложений, [Manager](./Manager), [Worker](./Worker), база данных [MongoDB](https://www.mongodb.com/) и брокер [RabbitMQ](https://www.rabbitmq.com/).
 Первый отвечает за обработку запросов клиента, делегирование задач воркерам и агрегирование результатов.
 Второй отвечает за исполнение делегированных задач. В базе данных сохраняются текущие и исполненные задачи, брокер осуществляет коммуникацию между мменеджером и воркерами.
 
 Проект написан на Kotlin с фреймворком Spring.

## 2. Схема взаимодействия компонентов
 ![scheme.png](./scheme.png)
## 3. Инструкция по запуску
```cmd
docker-compose up -d --scale worker=3
```
## 4. API и примеры запросов
> POST http://manager/api/hash/crack

Ручка для клиента, применяется для создания основной задачи по взлому хэша.
```json
{
 "hash": "5f4dcc3b5aa765d61d8327deb882cf99",
 "maxLength": 4
}
```
> GET http://manager/api/hash/status?requestId=...

Ручка для клиента, по заданному uuid позволяет посмотреть статус выполнения задачи.
## 5. Используемые конфигурационные параметры.

>MANAGER_PORT=8080

Порт, на котором менеджер слушает воркеров
>HASH_ALPHABET=0123456789abcdefjhijklmnopqrstuvwxyz

Алфавит для поиска слова
>USER_STATUS_ENDPOINT=/api/hash/status
>USER_REQUEST_ENDPOINT=/api/hash/crack

Эндпоинты в приложении

>TASK_SUBDIVISION_SIZE=10

Количество подзадач, генерируемых из задачи
>SPRING_RABBITMQ_HOST=rabbitmq
>SPRING_RABBITMQ_PORT=5672
>SPRING_RABBITMQ_USERNAME=guest
>SPRING_RABBITMQ_PASSWORD=guest

Настройки брокера
>SPRING_MONGODB_URI=mongodb://mongo-primary:27017,mongo-replica-1:27017,mongo-replica-2:27017/crackhash?replicaSet=rs0
>MONGO_INITDB_ROOT_USERNAME=admin
>MONGO_INITDB_ROOT_PASSWORD=admin

Настройки кластера БД