# ===== Giai đoạn 1: build file .jar bằng Maven =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
# Bỏ qua test vì test cần kết nối database thật
RUN mvn -q clean package -DskipTests

# ===== Giai đoạn 2: chạy ứng dụng =====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Gói Render miễn phí chỉ có 512 MB RAM
ENV JAVA_OPTS="-Xmx350m"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
