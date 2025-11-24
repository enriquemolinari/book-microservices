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

| #  | URI                                      | Notes                                      |
|----|------------------------------------------|--------------------------------------------|
| 1  | `GET /movies/{id}`                       | Movie detail by ID.                        |
| 2  | `GET /movies/sorted/rate`                | Sorted by rate, paginated.                 |
| 3  | `GET /movies/sorted/releasedate`         | Sorted by release date, paginated.         |
| 4  | `GET /movies`                            | Sorted list by name, paginated.            |
| 5  | `GET /movies/search/{fullOrPartialName}` | Name search with pagination.               |
| 6  | `GET /movies/by/{ids}`                   | Movie details by comma-separated IDs       |
| 7  | `GET /movies/{id}/rate`                  | Ratings ordered by date.                   |
| 8  | `GET /composed/movies/{id}/rate`         | Same as before but composed with userName. |
| 9  | `POST /movies/private/{movieId}/rate`    | Create a new rate for a movie.             |
| 10 | `POST /movies/private/new`               | Create a new movie.                        |

Example for API #1:

```bash
curl -X GET "http://localhost:8080/movies/1" \
     -H "Accept: application/json"
```

Example for API #2:

```bash
curl -X GET "http://localhost:8080/movies/sorted/rate?page=1" \
     -H "Accept: application/json"
```

Example for API #3:

```bash
curl -X GET "http://localhost:8080/movies/sorted/releasedate?page=1" \
     -H "Accept: application/json"
```

Example for API #4:

```bash
curl -X GET "http://localhost:8080/movies?page=1" \
     -H "Accept: application/json"
```

Example for API #5:

```bash
curl -X GET "http://localhost:8080/movies/search/fish?page=1" \
     -H "Accept: application/json"
```

Example for API #6:

```bash
curl -X GET "http://localhost:8080/movies/by/1,2" \
     -H "Accept: application/json"
```

Example for API #7:

```bash
curl -X GET "http://localhost:8080/movies/1/rate" \
     -H "Accept: application/json"
```

Example for API #8:

```bash
curl -X GET "http://localhost:8080/composed/movies/1/rate" \
     -H "Accept: application/json"
```

Example for API #9:

```bash
curl -X POST "http://localhost:8080/movies/private/2/rate" \
     -H "Content-Type: application/json" \
     -H "Cookie: token={A_TOKEN}" \
     -d '{"rateValue": 5, "comment": "Loved it"}'
```

Example for API #10:

```bash
curl -X POST "http://localhost:8080/movies/private/new" \
     -H "Content-Type: application/json" \
     -H "Cookie: token={A_TOKEN}" \
     -d '{"name": "Movie name", "duration": 148, "releaseDate": "2010-07-16", "plot": "A mind-bending thriller.", "genres": ["ACTION", "THRILLER"]}'
```

### Shows Endpoints

| # | URI                                    | Notes                                                          |
|---|----------------------------------------|----------------------------------------------------------------|
| 1 | `GET /shows`                           | Shows available in the next 10 days.                           |
| 2 | `GET /composed/shows`                  | Shows available in the next 10 days, composed with movie data. |
| 3 | `GET /shows/{id}`                      | Show detail.                                                   |
| 4 | `GET /shows/movie/{id}`                | Shows grouped by movie.                                        |
| 5 | `GET /shows/buyer`                     | Obtain buyer's points earned.                                  |
| 6 | `GET /shows/sale/{salesIdentifier}`    | Sale details.                                                  |
| 7 | `POST /shows/private/{showId}/reserve` | Reserve seats for a show.                                      |
| 8 | `POST /shows/private/{showId}/pay`     | Pay seats for a show.                                          |
| 9 | `GET /composed/shows/sales/{salesId}`  | Sale details composed with user and movie data.                |

Example for API #1:

```bash
curl -X GET "http://localhost:8080/shows" \
     -H "Accept: application/json"
```

Example for API #2:

```bash
curl -X GET "http://localhost:8080/composed/shows" \
     -H "Accept: application/json"
```

Example for API #3:

```bash
curl -X GET "http://localhost:8080/shows/1" \
     -H "Accept: application/json"
```

Example for API #4:

```bash
curl -X GET "http://localhost:8080/shows/movie/1" \
     -H "Accept: application/json"
```

Example for API #5:

```bash
curl -X GET "http://localhost:8080/shows/buyer" \
     -H "Accept: application/json" \
     -H "fw-gateway-user-id: 1"
```

Example for API #6:

```bash
curl -X GET "http://localhost:8080/shows/sale/sale-123" \
     -H "Accept: application/json"
```

Example for API #7:

```bash
curl -X POST "http://localhost:8080/shows/private/1/reserve" \
     -H "Content-Type: application/json" \
     -H "Cookie: token={A_TOKEN}" \
     -d '[1, 2, 3]'
```

Example for API #8:

```bash
curl -X POST "http://localhost:8080/shows/private/1/pay" \
     -H "Content-Type: application/json" \
     -H "Cookie: token={A_TOKEN}" \
     -d '{"selectedSeats": [1, 2, 3], "creditCardNumber": "4111111111111111", "secturityCode": "123", "expirationYear": 2026, "expirationMonth": 12}'
```

Example for API #9:

```bash
curl -X GET "http://localhost:8080/composed/shows/sales/{AN_UUID}" \
     -H "Accept: application/json"
```

### Users Endpoints

| # | URI                                   | Notes                                               |
|---|---------------------------------------|-----------------------------------------------------|
| 1 | `POST /users/register`                | User registration.                                  |
| 2 | `POST /users/login`                   | Returns HTTP-only `token` cookie and user profile.  |
| 3 | `POST /users/token`                   | Validates token and returns the associated userId.  |
| 4 | `GET /users/profile/by/{ids}`         | Retrieves multiple profiles by comma-separated IDs. |
| 5 | `GET /users/private/profile`          | Retrieves user profile.                             |
| 6 | `GET /composed/users/private/profile` | Retrieves user profile composed with points earned. |
| 7 | `POST /users/private/changepassword`  | Changes password for an authenticated user.         |
| 8 | `POST /users/private/logout`          | Clears the `token` cookie and signs out.            |

Example for API #1:

```bash
curl -X POST "http://localhost:8080/users/register" \
     -H "Content-Type: application/json" \
     -d '{"name": "Malcom", "surname": "Joven", "email": "my@codingarchstyle.com", "username": "malcom", "password": "abcdi1234567", "repeatPassword": "abcdi1234567"}'
```

Example for API #2:

```bash
curl -X POST "http://localhost:8080/users/login" \
     -H "Content-Type: application/json" \
     -d '{"username": "nico", "password": "123456789012"}'
```

Example for API #3:

```bash
curl -X POST "http://localhost:8080/users/token" \
     -H "Content-Type: application/json" \
     -d '"eyJhbGciOi..."'
```

Example for API #4:

```bash
curl -X GET "http://localhost:8080/users/profile/by/1,2" \
     -H "Accept: application/json"
```

Example for API #5:

```bash
curl -X GET "http://localhost:8080/users/private/profile" \
     -H "Accept: application/json" \
     -H "Cookie: token={A_TOKEN}"
```

Example for API #6:

```bash
curl -X GET "http://localhost:8080/composed/users/private/profile" \
     -H "Accept: application/json" \
     -H "Cookie: token={A_TOKEN}"
```

Example for API #7:

```bash
curl -X POST "http://localhost:8080/users/private/changepassword" \
     -H "Content-Type: application/json" \
     -H "Cookie: token={A_TOKEN}" \
     -d '{"currentPassword": "old", "newPassword1": "new", "newPassword2": "new"}'
```

Example for API #8:

```bash
curl -X POST "http://localhost:8080/users/private/logout" \
     -H "Accept: application/json" \
     -H "Cookie: token={A_TOKEN}"
```