FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
COPY shared/ ./shared/
COPY services/account-service/ ./services/account-service/
COPY services/customer-service/pom.xml services/customer-service/pom.xml
COPY services/transaction-service/pom.xml services/transaction-service/pom.xml
COPY services/notification-service/pom.xml services/notification-service/pom.xml
COPY services/payment-service/pom.xml services/payment-service/pom.xml

RUN mvn install -pl services/account-service -am -Dmaven.test.skip=true -B --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/services/account-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
