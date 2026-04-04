# CEF Kotlin Example

Example project demonstrating the `cef-kotlin` generator output.

## Prerequisites

- Java 21+
- Generator published to mavenLocal:
  ```bash
  cd ../..
  ./gradlew :generator:publishToMavenLocal
  ```

## Run

```bash
./gradlew generateApi      # Generate Kotlin code from openapi.yaml
./gradlew test             # Run tests
./gradlew jacocoTestReport # Coverage report
```

## What's Generated

The generator produces idiomatic Kotlin:
- `data class` DTOs with `val` properties and defaults
- Enum classes with `value` + custom vendor extension fields
- Service interfaces with `Unit` return (not `Void?`)
- `ApiRequest` with `by lazy`, `inline reified`, `runCatching`
- `ApiResponse` with expression-body factory methods
- `fun interface ExceptionHandler` with `DEFAULT` companion
- `RouteTree` with `typealias RouteHandler` and `getOrPut`

See the [main README](../../README.md) for full documentation.
