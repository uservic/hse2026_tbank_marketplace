workspace "Marketplace" "Проектирование архитектуры маркетплейса по модели C4" {

    model {
        # --- Действующие лица (Actors) ---
        buyer = person "Покупатель" "Ищет товары в ленте, оформляет заказы, оплачивает покупки."
        seller = person "Продавец" "Управляет товарным каталогом, остатками и видит заказы."

        # --- Внешние системы (External Systems) ---
        paymentGateway = softwareSystem "Платежный шлюз" "Внешняя платежная система." "External"
        notificationProvider = softwareSystem "Сервис отправки сообщений" "Внешний провайдер SMS/Email/Push уведомлений." "External"

        # --- Система Маркетплейса (Ядро) ---
        marketplace = softwareSystem "Маркетплейс" "Цифровая платформа для торговли." {

            # Входные интерфейсы (Frontend)
            webAppBuyer = container "Web-приложение для покупателей" "Интерфейс для покупателей (Личный кабинет)." "Browser, React/TypeScript"
            webAppSeller = container "Web-приложение для продавцов" "Интерфейс для продавцов (Личный кабинет)." "Browser, React/TypeScript"

            # API Gateway
            gateway = container "API Gateway" "Единая точка входа, аутентификация JWT, маршрутизация и балансировка." "Java, Spring Cloud Gateway"

            # Микросервисы (Бизнес-логика)
	        authService = container "Auth Service" "Сервис авторизации" "Java, Spring Boot" {
                tags "Microservice"
            }
            userService = container "User Service" "Управление пользователями, профилями покупателей/продавцов и ролями." "Java, Spring Boot" {
                tags "Microservice"
            }
            productService = container "Product Service" "Управление товарным каталогом, категориями и остатками." "Java, Spring Boot" {
                tags "Microservice"
            }
            orderService = container "Order Service" "Создание, расчет стоимости, валидация и ведение жизненного цикла заказов." "Java, Spring Boot" {
                tags "Microservice"
            }
            paymentService = container "Payment Service" "Интеграция с платежным шлюзом, фиксация и учет транзакций." "Java, Spring Boot" {
                tags "Microservice"
            }
            recommendationService = container "Recommendation Service" "Анализ поведения пользователей и генерация персонализированной ленты." "Python, FastAPI, ML models" {
                tags "Microservice"
            }
            notificationService = container "Notification Service" "Обработка и подготовка уведомлений о статусах заказов." "Java, Spring Boot" {
                tags "Microservice"
            }

            # Базы данных и инфраструктура внутри системы
            authDb = container "Auth DB" "Хранит данные сессий и токенов." "Redis" {
                tags "Database"
            }
            userDb = container "User DB" "Хранит данные учетных записей." "PostgreSQL" {
                tags "Database"
            }
            productDb = container "Product DB" "Хранит информацию о товарах и категориях." "MongoDB" {
                tags "Database"
            }
            orderDb = container "Order DB" "Хранит информацию о заказах и истории их изменений." "PostgreSQL" {
                tags "Database"
            }
            recommendationDb = container "Recommendation Cache" "Быстрое хранилище предрассчитанных персональных лент." "Redis" {
                tags "Database"
            }
            paymentDb = container "Payment DB" "Хранит историю транзакций, статусы платежей и ключи идемпотентности." "PostgreSQL" {
                tags "Database"
            }
            kafka = container "Message Broker (Kafka)" "Асинхронный обмен событиями между микросервисами." "Apache Kafka" {
                tags "Broker"
            }
        }

        # --- Взаимодействия Уровня 1: System Context View ---

        buyer -> marketplace "Использует"
        seller -> marketplace "Использует"
        marketplace -> paymentGateway "Интеграция"
        marketplace -> notificationProvider "Интеграция"

        # --- Взаимодействия Уровня 2: Container View ---

        # Пользователи -> WebApp
        buyer -> webAppBuyer "Использует"
        seller -> webAppSeller "Использует"

        # WebApp -> Gateway
        webAppBuyer -> gateway "Вызов API" "HTTPS / REST"
        webAppSeller -> gateway "Вызов API" "HTTPS / REST"

        # Gateway -> Микросервисы
        gateway -> userService "Маршрутизирует запросы профилей" "HTTP / REST"
        gateway -> authService "Аутентифицирует пользователей и валидирует токены" "HTTP / REST"
        gateway -> productService "Маршрутизирует запросы каталога" "HTTP / REST"
        gateway -> orderService "Маршрутизирует запросы заказов" "HTTP / REST"
        gateway -> paymentService "Маршрутизирует запросы платежей и истории транзакций" "HTTP / REST"
        gateway -> recommendationService "Запрашивает главную ленту товаров" "HTTP / REST"

        # Сервисы -> Базы данных
	    authService -> authDb "Управляет кэшем сессий и токенами" "Redis Protocol"
        userService -> userDb "Читает и пишет данные пользователей" "SQL / JDBC"
        productService -> productDb "Читает и пишет данные каталога" "BSON / Native"
        orderService -> orderDb "Читает и пишет данные заказов" "SQL / JDBC"
        recommendationService -> recommendationDb "Читает и пишет кэш ленты" "Redis Protocol"
        paymentService -> paymentDb "Читает и пишет логи транзакций и статусы оплат" "SQL / JDBC"

        # Синхронное межсервисное взаимодействие (gRPC)
        orderService -> productService "Проверяет остатки товаров и актуальные цены" "gRPC / Protobuf"
        orderService -> userService "Проверяет статус и адрес покупателя" "gRPC / Protobuf"

        # Асинхронные события (Event-Driven с Kafka)
        orderService -> kafka "Публикует события (Заказ создан / Изменился статус)" "TCP"
        paymentService -> kafka "Публикует события (Транзакция успешна / Ошибка оплаты)" "TCP"

        kafka -> paymentService "Слушает события создания заказов для инициации оплаты" "TCP"
        kafka -> notificationService "Слушает любые изменения статусов для отправки уведомлений" "TCP"
        kafka -> recommendationService "Слушает события заказов для обучения ML моделей" "TCP"

        # Интеграция с внешним миром из контейнеров
        paymentService -> paymentGateway "Инициирует транзакции списания / возврата" "HTTPS / REST"
        notificationService -> notificationProvider "Передает сформированные сообщения для отправки" "HTTPS / REST"
    }

    views {
        systemContext marketplace "ContextView" "Диаграмма контекста системы маркетплейса" {
            include *
        }

        container marketplace "ContainerView" "Диаграмма контейнеров маркетплейса" {
            include *
        }

        styles {
            element "Element" {
                background #1168bd
                color #ffffff
            }
            element "Microservice" {
                shape Component
                background #1168bd
                color #ffffff
            }
            element "External" {
                background #999999
                color #ffffff
            }
            element "Broker" {
                background #ef7a00
                color #ffffff
                shape Pipe
            }
            element "Database" {
                shape Cylinder
                background #22aa22
                color #ffffff
            }
        }
    }
}
