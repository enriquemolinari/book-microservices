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

| URI                                      | Notes                                      |
|------------------------------------------|--------------------------------------------|
| `GET /movies/{id}`                       |                                            |
| `GET /movies/sorted/rate`                | Paginated.                                 |
| `GET /movies/sorted/releasedate`         | Paginated.                                 |
| `GET /movies`                            | Paginated. Sorted list by name.            |
| `GET /movies/search/{fullOrPartialName}` | Name search with pagination.               |
| `GET /movies/by/{ids}`                   | Accepts a list of IDs.                     |
| `GET /movies/{id}/rate`                  | Ratings ordered by date.                   |
| `GET /composed/movies/{id}/rate`         | Same as before but composed with userName. |
| `POST /movies/private/{movieId}/rate`    | Auth Required.                             |
| `POST /movies/private/new`               | Auth Required.                             |

### Shows Endpoints

| URI                                    | Notes                                                           |
|----------------------------------------|-----------------------------------------------------------------|
| `GET /shows`                           | Shows available in the next 10 days.                            |
| `GET /composed/shows`                  | Shows available in the next 10 days, composed with movie data.  |
| `GET /shows/{id}`                      | Show detail.                                                    |
| `GET /shows/movie/{id}`                | Shows grouped by movie.                                         |
| `GET /shows/buyer`                     | Used by the API Composer to build user profile.                 |
| `GET /shows/sale/{salesIdentifier}`    | Used by API Composer to create the notification after purchase. |
| `POST /shows/private/{showId}/reserve` | Reserve seats for a show.                                       |
| `POST /shows/private/{showId}/pay`     | Pay seats for a show.                                           |
| `GET /composed/shows/sales/{salesId}`  | Sale data composed with user and movie data.                    |

### Users Endpoints

| URI                                   | Notes                                               |
|---------------------------------------|-----------------------------------------------------|
| `POST /users/register`                | Public registration.                                |
| `POST /users/login`                   | Returns HTTP-only `token` cookie and user profile.  |
| `POST /users/token`                   | Validates token and returns the associated userId.  |
| `GET /users/profile/by/{ids}`         | Retrieves multiple profiles by IDs.                 |
| `GET /users/private/profile`          | Retrieves user profile.                             |
| `GET /composed/users/private/profile` | Retrieves user profile composed with points earned. |
| `POST /users/private/changepassword`  | Changes password for the authenticated user.        |
| `POST /users/private/logout`          | Clears the `token` cookie and signs out.            |

#### Examples

##### Movies

###### GET /movies/{id}

Request:
`GET /movies/1`

###### GET /movies/sorted/rate, `/movies/sorted/releasedate`, `/movies`, `/movies/search/{fullOrPartialName}`

Request (paginated):
`GET /movies/search/moviename?page=1`

###### GET /movies/by/{ids}

Request:
`GET /movies/by/1,3`

###### GET /movies/{id}/rate

Request:
`GET /movies/1/rate?page=1`

###### GET /composed/movies/{id}/rate

Request:
`GET /composed/movies/1/rate`

###### POST /movies/private/{movieId}/rate

```json
{
  "rateValue": 5,
  "comment": "Loved it"
}
```

###### POST /movies/private/new

```json
{
  "name": "Movie name",
  "duration": 148,
  "releaseDate": "2010-07-16",
  "plot": "A mind-bending thriller.",
  "genres": [
    "ACTION",
    "THRILLER"
  ]
}
```

##### Shows

###### GET /shows

Request:
`GET /shows`

###### GET /composed/shows

Request:
`GET /composed/shows`

###### GET /shows/{id}

Request:
`GET /shows/11`

###### GET /shows/movie/{id}

Request:
`GET /shows/movie/2`

###### GET /shows/buyer

Request (auth header required):

```
GET /shows/buyer
fw-gateway-user-id: 4
```

###### GET /shows/sale/{salesIdentifier}

Request:
`GET /shows/sale/SALE-123`

###### GET /composed/shows/sales/{salesId}

Request:
`GET /composed/shows/sales/SALE-123`

###### POST /shows/private/{showId}/reserve

```json
[
  1,
  2,
  3
]
```

###### POST /shows/private/{showId}/pay

```json
{
  "selectedSeats": [
    4,
    5
  ],
  "creditCardNumber": "4111111111111111",
  "secturityCode": "123",
  "expirationYear": 2026,
  "expirationMonth": 12
}
```

##### Users

###### GET /users/profile/by/{ids}

Request:
`GET /users/profile/by/4,5`

###### GET /users/private/profile

Request (auth header required):

```
GET /users/private/profile
fw-gateway-user-id: 4
```

###### GET /composed/users/private/profile

Request (auth header required):

```
GET /composed/users/private/profile
fw-gateway-user-id: 4
```

###### POST /users/register

```json
{
  "name": "Malcom",
  "surname": "Joven",
  "email": "my@codingarchstyle.com",
  "username": "malcom",
  "password": "abcdi1234567",
  "repeatPassword": "abcdi1234567"
}
```

###### POST /users/login

```json
{
  "username": "nico",
  "password": "123456789012"
}
```

###### POST /users/token

```json
"eyJhbGciOi..."
```

###### POST /users/private/changepassword

```json
{
  "currentPassword": "old",
  "newPassword1": "new",
  "newPassword2": "new"
}
```

###### POST /users/private/logout

_No request body; the endpoint clears the `token` cookie and returns `204 No Content`._

Routes prefixed with `/private` pass through the `TokenVerificationFilter`, which enforces the `token` cookie and
forwards `userId` via the `fw-gateway-user-id` header. The composed endpoints (`/composed/*`) route to the API
Composition service.
