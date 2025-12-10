# FTBLoader (Fabric Mod)

This repository contains a Fabric mod built with Gradle. The project includes a GitHub Actions CI that builds the project and automatically creates a GitHub Release (with the built JARs) on every push to the `main` branch.

How the pipeline works:
- On `push` to `main`, CI runs on `ubuntu-latest`.
- Java 17 (Temurin) is used.
- `./gradlew build` runs and built jars in `build/libs/` are uploaded to a GitHub Release.
