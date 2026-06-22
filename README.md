# Kafka Order Service

Pet-проект на Spring Boot, созданный для практики работы с Kafka, Redis и различными подходами к кэшированию.

Проект-consumer для Kafka: https://github.com/irishsea/kafka-consumer-service

## Возможности

* REST API для работы с товарами
* Отправка событий в Kafka
* Хранение данных в PostgreSQL
* Кэширование с использованием Redis
* Пример ручного кэширования
* Пример кэширования через Spring Cache
* Ограничение количества запросов (Rate Limiting)
* Распределённые блокировки через Redis
* Unit-тесты
* Запуск инфраструктуры через Docker Compose

## Стек технологий

* Java 21
* Spring Boot 3
* Spring Web
* Spring Data JPA
* PostgreSQL
* Redis
* Apache Kafka
* Maven
* JUnit 5
* Mockito
* Docker Compose

## Запуск проекта

Поднять инфраструктуру:

```bash
docker compose up -d
```

Запустить приложение:

```bash
./mvnw spring-boot:run
```

После запуска приложение будет доступно по адресу:

```
http://localhost:8080
```

## Структура проекта

```
controller/    REST API
service/       Бизнес-логика
repository/    Работа с базой данных
cache/         Реализации кэширования
kafka/         Kafka producer
ratelimit/     Ограничение количества запросов
lock/          Redis distributed lock
config/        Конфигурация приложения
```

## Цель проекта

Проект создан в целях для практики следующих технологий и подходов:

* Spring Boot
* Kafka
* Redis
* Кэширование
* Rate Limiting
* Распределённые блокировки
* Unit-тестирование
* Docker Compose

## Планы по развитию

* Добавить интеграционные тесты с Testcontainers
* Подключить Swagger/OpenAPI
* Настроить GitHub Actions
* Добавить метрики и мониторинг
