# ==========================================
# ЭТАП 1: Единая сборка всего монорепозитория
# ==========================================
FROM bellsoft/liberica-openjdk-alpine:21 AS builder
WORKDIR /app

# Устанавливаем Gradle внутри контейнера
RUN apk add --no-cache gradle

# Копируем КОРНЕВЫЕ файлы управления сборкой
COPY settings.gradle build.gradle ./

# Копируем файлы настроек и исходники ВСЕХ подпроектов сразу
COPY auth-service/build.gradle ./auth-service/
COPY auth-service/src ./auth-service/src

COPY product-service/build.gradle ./product-service/
COPY product-service/src ./product-service/src

# Запускаем сборку всех JAR-пакетов без прогона тестов
RUN gradle bootJar -x test

# ==========================================
# ЭТАП 2: Компактный образ для AUTH
# ==========================================
FROM bellsoft/liberica-openjre-alpine-musl:21 AS auth-service
WORKDIR /app
COPY --from=builder /app/auth-service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

# ==========================================
# ЭТАП 3: Компактный образ для PRODUCT
# ==========================================
FROM bellsoft/liberica-openjre-alpine-musl:21 AS product-service
WORKDIR /app
COPY --from=builder /app/product-service/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
