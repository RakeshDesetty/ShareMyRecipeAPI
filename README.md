Recipe Publishing Platform - Complete Starter
This archive contains two projects:
- api-app (port 8080): Spring Boot API with JWT auth, recipe endpoints, follow/unfollow, queueing.
- worker-app (port 8090): Spring Boot worker that consumes queue and processes images.

Run RabbitMQ:
docker run -d --name rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management

Run services:
cd api-app
mvn spring-boot:run

cd worker-app
mvn spring-boot:run

Test:
1) Signup:
POST /api/auth/signup {email,password,handle,role:"chef"}
2) Login: POST /api/auth/login {email,password} -> get accessToken
3) Create recipe (multipart) with Authorization: Bearer <accessToken>
