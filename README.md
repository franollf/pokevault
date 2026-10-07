# PokéVault

PokéVault is a learning-focused Spring Boot application for tracking Pokémon cards and, over time, their collection and market value. V0.1 implements one complete request flow: create a card, store it in PostgreSQL, and retrieve it by ID.

## What V0.1 teaches

```text
HTTP request
  -> CardController      translates HTTP to Java
  -> CardService         applies application behavior
  -> CardRepository      reads and writes entities
  -> PostgreSQL          stores the data
```

Request and response records keep the public JSON contract separate from the JPA entity. Flyway creates the database schema, and Hibernate validates that the entity still matches it.

## Prerequisites

- JDK 21 (`java --version`)
- Docker Desktop with the Docker engine running (`docker info`)
- PowerShell

Maven does not need to be installed globally. The repository includes Maven Wrapper.

## Start PostgreSQL

From the repository root:

```powershell
docker compose up -d
docker compose ps
```

Wait until the `postgres` service reports `healthy`. The default local credentials are all `pokevault`; copy `.env.example` to `.env` only if you want to override them.

If port `5432` is already used by another PostgreSQL installation, choose another host port for the current PowerShell session:

```powershell
$env:POSTGRES_PORT = '5433'
$env:DATABASE_URL = 'jdbc:postgresql://localhost:5433/pokevault'
docker compose up -d
```

Run the application from that same terminal so it inherits `DATABASE_URL`.

## Run the application

```powershell
.\mvnw.cmd spring-boot:run
```

The API starts at `http://localhost:8080`. On startup, Flyway applies `V1__create_cards_table.sql`, then Hibernate validates the `Card` mapping.

If Maven reports Java 8 after you installed Java 21, restart VS Code or refresh the terminal environment before running Maven.

## Create a card

Open a second PowerShell terminal in the repository root:

```powershell
$request = @{
    externalId = 'sv8-161'
    name = 'Umbreon ex'
    setName = 'Prismatic Evolutions'
    setCode = 'PRE'
    cardNumber = '161'
    rarity = 'Special Illustration Rare'
    imageUrl = 'https://example.com/umbreon.png'
} | ConvertTo-Json

$card = Invoke-RestMethod `
    -Method Post `
    -Uri 'http://localhost:8080/api/cards' `
    -ContentType 'application/json' `
    -Body $request

$card
```

The server responds with `201 Created` and the persisted card. The response contains the generated `id`, `createdAt`, and `updatedAt` values.

## Retrieve the card

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/cards/$($card.id)"
```

A positive ID that does not exist returns `404`. Zero, negative, and non-numeric IDs return `400`.

## Inspect PostgreSQL directly

```powershell
docker compose exec postgres psql -U pokevault -d pokevault `
    -c 'SELECT id, external_id, name, set_name, card_number, created_at FROM cards;'
```

This is the same row returned through the API, viewed at the final persistence layer.

## Run the tests

Keep Docker Desktop running, then execute:

```powershell
.\mvnw.cmd clean verify
```

The suite contains:

- Service unit tests using Mockito
- Controller contract tests using MockMvc
- Flyway/JPA integration tests using a temporary PostgreSQL Testcontainer
- A complete Spring context smoke test

Testcontainers creates and removes its own temporary databases. It does not use or erase the database created by `docker compose`.

## Stop the local database

Stop the container while preserving your data:

```powershell
docker compose stop
```

Start it again later with `docker compose up -d`.

`docker compose down -v` also deletes the named PostgreSQL volume and all locally stored PokéVault data, so use it only when you intentionally want a fresh database.

## Learning exercise

Trace one `POST /api/cards` request through these files in order:

1. `CardController` receives and validates `CreateCardRequest`.
2. `CardService` checks the external ID and creates a `Card` entity.
3. `CardRepository` asks JPA to persist the entity.
4. PostgreSQL generates the ID and stores the row.
5. `CardService` maps the saved entity to `CardResponse`.
6. `CardController` returns `201 Created` with a `Location` header.

Then try changing `externalId` and creating a second card. Predict the response before sending it. Finally, resend the original external ID and inspect the structured `409 Conflict` response.
