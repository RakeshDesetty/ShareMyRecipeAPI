Recipe Worker
Run prerequisites:
- Java 17+, Maven, RabbitMQ

Run:
cd worker-app
mvn spring-boot:run

The worker resizes images and writes them to ${java.io.tmpdir}/recipe-images/{recipeId}/
