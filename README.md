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
* [UI](ui) with Vite/Svelte/TailwindCSS - get more components from [Svelte Sample Repo](https://github.com/keksworks/svelte-sample)

Follows [The Pure Code Manifesto](https://github.com/keksworks/manifesto) principles.

Check [Klite Tutorial](https://github.com/keksworks/klite/blob/main/TUTORIAL.md) if you want to start from scratch with Klite.

## Prerequisites

* JDK 25+ (the Kotlin Toolchain provisions one automatically if missing)
* Node 24+
* Docker

## Building

See [Dockerfile](Dockerfile) for more information

### Build server

`./kotlin do jar`

This produces a runnable `build/libs/app.jar` with dependencies in `build/libs/deps`.

### Build UI

Inside of `ui` directory:

Install the dependencies using: `npm install` and

Build UI using: `npm run build`

## IDE

The project is imported using the [Kotlin Toolchain](https://kotlin-toolchain.org/) wrapper.
Use the latest IntelliJ IDEA with the
[Kotlin Toolchain plugin](https://plugins.jetbrains.com/plugin/31850-kotlin-toolchain) installed;
the IDE reads `project.yaml` and `module.yaml` and provisions the configured JDK automatically.
The committed `kotlin`/`kotlin.bat` wrappers are the source of truth for the Toolchain version.

If the project was previously imported by Gradle, close it and delete the stale `.idea/gradle.xml`
and `.idea/kotlinc.xml` files before reopening, so that IDEA does not try to sync a Gradle project
that no longer exists.

## Development

Start API by running the [Launcher](src/Launcher.kt) or `./kotlin run`.

It will automatically try to start the database using `docker compose up -d db`
To access DB via IDE use credentials from [.env](.env) file.

Start UI using: `cd ui && npm start`

After changing backend API types, regenerate TypeScript types for the UI with `./kotlin do types.ts`.

## Running tests

Server tests:
`./kotlin test`

Repository tests are integration tests, connecting to the real database, which runs in Docker.
The test database name ends with `_test`.

UI tests:
`cd ui && npm test`

## Deployment instructions

The easiest way to deploy is via Docker compose:

`docker compose up -d`

This will pull/build and start the application and the database.
