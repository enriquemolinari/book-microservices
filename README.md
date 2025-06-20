# Cinema Ticket System - Microservices

## Microservices Architecture Style

- `git clone https://github.com/enriquemolinari/book-microservices.git`
- `cd book-microservices`

### API Gateway (with Spring Cloud)

- `cd gateway`
- To compile and install all dependencies: `./mvnw install`
- To run all tests: `./mvnw test`
    - It tests routing and authentication logic
    - It uses [MockServer](https://www.mock-server.com/) to mock services
- To start the service: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`

### Rabbit MQ

- `cd rabbitmq-docker`
- To start it: `docker compose -f rabbitmq-container.yml up -d`
- Management tool: `http://localhost:15672`
    - user/pwd: admin/1234
- To stop it: `docker compose -f rabbitmq-container.yml down`

### Movies Microservice

- `cd movies`
- To compile and install all dependencies: `./mvnw install`
- To run all service tests: `./mvnw test`
- To start the service: `./mvnw exec:java`. A sample movie data is loaded at startup.
- Once started, you can open swagger UI:
    - http://localhost:8090/swagger-ui/index.html

### Shows Microservice

- `cd shows`
- To compile and install all dependencies: `./mvnw install`
- To run all service tests: `./mvnw test`
- To start the service: `./mvnw exec:java`. A sample shows data is loaded at startup.
- Once started, you can open swagger UI:
    - http://localhost:8091/swagger-ui/index.html

### Users Microservice

- `cd users`
- To compile and install all dependencies: `./mvnw install`
- To run all service tests: `./mvnw test`
- To start the service: `./mvnw exec:java`. A sample users data is loaded at startup.
- Once started, you can open swagger UI:
    - http://localhost:8093/swagger-ui/index.html

### Notifications Microservice

- `cd notifications`
- To compile and install all dependencies: `./mvnw install`
- To run all service tests: `./mvnw test`
- To start the service:
    - Start mailpit server: `docker compose -f mailpit/docker-compose.yml up -d`
        - Email Web Console will be available at: http://localhost:8025.
    - Then `./mvnw exec:java`.
        - It will start waiting for new events in `notifications.shows.events` queue.
