FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
COPY shared/ ./shared/
COPY services/notification-service/ ./services/notification-service/
COPY services/customer-service/pom.xml services/customer-service/pom.xml
COPY services/account-service/pom.xml services/account-service/pom.xml
COPY services/transaction-service/pom.xml services/transaction-service/pom.xml
COPY services/payment-service/pom.xml services/payment-service/pom.xml

RUN mvn install -pl services/notification-service -am -Dmaven.test.skip=true -B --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/services/notification-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
