Recipe API - Complete Starter
Run prerequisites:
- Java 17+
- Maven
- RabbitMQ (docker recommended: docker run -d --name rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management)

Run:
cd api-app
mvn spring-boot:run

API docs: http://localhost:8080/swagger-ui.html
