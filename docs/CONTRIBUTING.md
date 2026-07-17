# Contributing to CEF OpenAPI Generator

Thank you for your interest in contributing!

## Getting Started

```bash
git clone https://github.com/a-havrysh/cef-openapi-generator.git
cd cef-openapi-generator
./gradlew :generator:test   # Run all tests (195+)
./gradlew :generator:check  # Tests + coverage verification
```

## How to Contribute

### Bug Reports

Open an [issue](https://github.com/a-havrysh/cef-openapi-generator/issues) with:
- OpenAPI spec (minimal reproducer) that triggers the bug
- Expected vs actual generated code
- Generator version and `generatorName` (`cef` or `cef-kotlin`)

### Feature Requests

Open an issue describing:
- What you want to generate
- Why the current output doesn't work
- Example of desired generated code

### Pull Requests

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Make changes
4. Add tests (we require 90%+ coverage)
5. Run: `./gradlew :generator:check`
6. Commit with descriptive message
7. Push and open a PR

## Project Structure

```
generator/
├── src/main/java/io/github/cef/codegen/
│   ├── CefCodegen.java              — Base Java generator
│   ├── CefKotlinCodegen.java        — Kotlin overrides
│   ├── config/                      — FileSpec, PackageSuffix, GeneratorLayer
│   └── processing/                  — EnumFieldProcessor, ImportFilter, TypeConverter, etc.
├── src/main/resources/
│   ├── cef-java/                    — Java Mustache templates (37 files)
│   ├── cef-kotlin/                  — Kotlin Mustache templates (39 files)
│   └── META-INF/services/           — SPI registration
└── src/test/
    ├── java/                        — Unit + integration tests
    └── resources/test-openapi.yaml  — Test OpenAPI spec
```

## Development Guidelines

### Code
- Java 17+ for generator code (switch expressions, `var`, records where appropriate)
- Follow existing code style — look at `TypeConverter.java` as a reference
- Utility classes: `final class` + `private` constructor + `static` methods
- Use `Set.of()`, `Map.of()`, `List.of()` for immutable collections

### Templates
- Java templates in `cef-java/`, Kotlin templates in `cef-kotlin/`
- Kotlin templates must produce idiomatic Kotlin (no `Void?`, no `getX()` getters, use `by lazy`, `runCatching`, etc.)
- Template directory must match output package (e.g., `cef/` templates → `cef/` output)

### Tests
- All new features must have tests
- New configOptions → add integration test in `IntegrationTest.java`
- New processing logic → add unit test in `processing/` package
- Run `./gradlew :generator:check` — JaCoCo enforces 90% minimum

### Commit Messages

Follow conventional format:
```
Add support for X configOption

- Template changes for Java and Kotlin
- Integration test verifying generated output
- README updated with new option
```

## Testing Locally with Your Project

```bash
# Build and publish to local Maven
./gradlew :generator:publishToMavenLocal

# In your project, use mavenLocal():
buildscript {
    repositories { mavenLocal() }
    dependencies { classpath("io.github.cef:generator:3.1.2") }
}
```

## Questions?

Open a [discussion](https://github.com/a-havrysh/cef-openapi-generator/issues) or reach out via issues.
