# Multi-stage build
# Этап 1: Сборка приложения через оригинальный Liberica OpenJDK 21 и Gradle
FROM bellsoft/liberica-openjdk-alpine:21 AS builder
WORKDIR /app

# Устанавливаем актуальный Gradle внутри контейнера для сборки
RUN apk add --no-cache gradle

# Копируем файлы конфигурации сборки
COPY build.gradle settings.gradle ./

# Копируем исходный код проекта
COPY src ./src

# Запускаем сборку jar-архива силами Gradle без прогона тестов
RUN gradle bootJar -x test

# Этап 2: Финальный ультра-компактный контейнер для запуска приложения
FROM bellsoft/liberica-openjre-alpine-musl:21
WORKDIR /app

# Переносим скомпилированный jar-пакет из предыдущего этапа
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]