# Klite starter project

**Don't forget to update this file with your own project description.**

This is a preconfigured sample/starter project with Kotlin/Klite with some best-practices:

* Simple src/test directory structure for backend code
* Committed `.env` file for dev configuration
* PostgreSQL in Docker Compose
* [DB migrations](db) creating:
  * Less privileged app user
  * change_history table and triggers
* Unit tests
* Internationalization
* TypeScript types generation for frontend
* [UI](ui) with Vite/Svelte/TailwindCSS - get more components from [Svelte Sample Repo](https://github.com/codeborne/svelte-sample)

Follows [The Pure Code Manifesto](https://github.com/keksworks/manifesto) principles.

Check [Klite Tutorial](https://github.com/keksworks/klite/blob/main/TUTORIAL.md) if you want to start from scratch with Klite.

## Prerequisites

* JDK 25+
* Node 24+
* Docker

## Building

See [Dockerfile](Dockerfile) for more information

### Build server

`./gradlew jar`

### Build UI

Inside of `ui` directory:

Install the dependencies using: `npm install` and

Build UI using: `npm run build`

## Development

Start API by running the [Launcher](src/Launcher.kt).

It will automatically try to start the database using `docker compose up -d db`
To access DB via IDE use credentials from [.env](.env) file.

Start UI using: `cd ui && npm start`

## Running tests

Server tests:
`./gradlew test`

Repository tests are integration tests, connecting to the real database, which runs in Docker.
The test database name ends with `_test`.

UI tests:
`cd ui && npm test`

## Deployment instructions

The easiest way to deploy is via Docker compose:

`docker compose up -d`

This will pull/build and start the application and the database.
