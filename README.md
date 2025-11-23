# Cinema Ticket System - Microservices Architecture Style

This is the implementation of the Cinema Ticket System using a Microservices Architecture Style. Each microservice
applies a rich domain model to structure its business logic.

This respostory contains the implementation of the API Gateway plus all the microservices required for the Cinema Ticket
System. There are also two additional repositories that implement an API Composition framework and smoke tests for the
entire system. All instructions to start up the system are provided below.

## Starting up the Cinema Ticket System Microservices

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
    - http://localhost:8091/swagger-ui/index.html

### Shows Microservice

- `cd shows`
- To compile and install all dependencies: `./mvnw install`
- To run all service tests: `./mvnw test`
- To start the service: `./mvnw exec:java`. A sample shows data is loaded at startup.
- Once started, you can open swagger UI:
    - http://localhost:8092/swagger-ui/index.html

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

## API Composer

Additionally, there is another repository that implements an API Composition framework that composes microservice
responses for clients:

- `git clone https://github.com/enriquemolinari/book-apicomposition.git api-composer`
- `cd api-composer`
- To compile and install all dependencies: `./mvnw install`
- To run all tests: `./mvnw test`
- To start the service: `./mvnw exec:java`.

## Smoke Tests

Finally, there is another repository that implements smoke tests for the entire system:

- `git clone https://github.com/enriquemolinari/book-ms-smoketests.git smoke-tests`
- cd `smoke-tests`
- To compile and install all dependencies: `./mvnw install -DskipTests`
- To run tests: `./mvnw test`

## URIs for each Microservice

The following tables summarize the URIs exposed by each microservice, along with the HTTP verb, whether authentication
is required, and any relevant notes. All endpoints will be exposed through the API Gateway at port `8080`.

### Movies Endpoints

| URI                                                                                                                                                                            | Notes                                      |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------|
| `GET /movies/{id}`                                                                                                                                                             |                                            |
| `GET /movies/sorted/rate`                                                                                                                                                      | Paginated.                                 |
| `GET /movies/sorted/releasedate`                                                                                                                                               | Paginated.                                 |
| `GET /movies`                                                                                                                                                                  | Paginated. Sorted list by name.            |
| `GET /movies/search/{fullOrPartialName}`                                                                                                                                       | Name search with pagination.               |
| `GET /movies/by/{ids}`                                                                                                                                                         | Accepts a list of IDs.                     |
| `GET /movies/{id}/rate`                                                                                                                                                        | Ratings ordered by date.                   |
| `GET /composed/movies/{id}/rate`                                                                                                                                               | Same as before but composed with userName. |
| `POST /movies/private/{movieId}/rate`<br>Example body: `{"rateValue":5,"comment":"Loved it"}`                                                                                  | Auth Required.                             |
| `POST /movies/private/new`<br>Example body: `{"name":"Movie name","duration":148,"releaseDate":"2010-07-16","plot":"A mind-bending thriller.","genres":["ACTION","THRILLER"]}` | Auth Required.                             |

### Shows Endpoints

| URI                                                                                                                                                                                  | Notes                                                           |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------|
| `GET /shows`                                                                                                                                                                         | Shows available in the next 10 days.                            |
| `GET /composed/shows`                                                                                                                                                                | Shows available in the next 10 days, composed with movie data.  |
| `GET /shows/{id}`                                                                                                                                                                    | Show detail.                                                    |
| `GET /shows/movie/{id}`                                                                                                                                                              | Shows grouped by movie.                                         |
| `GET /shows/buyer`                                                                                                                                                                   | Used by the API Composer to build user profile.                 |
| `GET /shows/sale/{salesIdentifier}`                                                                                                                                                  | Used by API Composer to create the notification after purchase. |
| `POST /shows/private/{showId}/reserve`<br>Example body: `[1,2,3]`                                                                                                                    | Reserve seats for a show.                                       |
| `POST /shows/private/{showId}/pay`<br>Example body: `{"selectedSeats":[4,5],"creditCardNumber":"4111111111111111","secturityCode":"123","expirationYear":2026,"expirationMonth":12}` | Pay seats for a show.                                           |
| `GET /composed/shows/sales/{salesId}`                                                                                                                                                | Sale data composed with user and movie data.                    |

### Users Endpoints

| URI                                                                                                                                                                                          | Notes                                               |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------|
| `POST /users/register`<br>Example body: `{"name":"Malcom","surname":"Joven","email":"my@codingarchstyle.com","username":"malcom","password":"abcdi1234567","repeatPassword":"abcdi1234567"}` | Public registration.                                |
| `POST /users/login`<br>Example body: `{"username":"nico","password":"123456789012"}`                                                                                                         | Returns HTTP-only `token` cookie and user profile.  |
| `POST /users/token`<br>Example body: `"eyJhbGciOi..."`                                                                                                                                       | Validates token and returns the associated userId.  |
| `GET /users/profile/by/{ids}`                                                                                                                                                                | Retrieves multiple profiles by IDs.                 |
| `GET /users/private/profile`                                                                                                                                                                 | Retrieves user profile.                             |
| `GET /composed/users/private/profile`                                                                                                                                                        | Retrieves user profile composed with points earned. |
| `POST /users/private/changepassword`<br>Example body: `{"currentPassword":"old","newPassword1":"new","newPassword2":"new"}`                                                                  | Changes password for the authenticated user.        |
| `POST /users/private/logout`                                                                                                                                                                 | Clears the `token` cookie and signs out.            |

Routes prefixed with `/private` pass through the `TokenVerificationFilter`, which enforces the `token` cookie and
forwards `userId` via the `fw-gateway-user-id` header. The composed endpoints (`/composed/*`) route to the API
Composition service.
