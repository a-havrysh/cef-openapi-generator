# Testing Guide

## Overview

The project has two levels of testing:

1. **Generator tests** (`generator/src/test/`) — test the codegen itself (195 tests, 99.2% coverage)
2. **Example project tests** (`examples/cef-java/`) — test the generated output (186 tests)

## Generator Tests

### Run

```bash
./gradlew :generator:test          # All tests
./gradlew :generator:check         # Tests + JaCoCo coverage verification (90% minimum)
```

### Structure

```
generator/src/test/java/io/github/cef/codegen/
├── CefCodegenTest.java                      — Base generator: config, params, enums, server URLs
├── CefKotlinCodegenTest.java                — Kotlin: types, templates, property conversion
├── IntegrationTest.java                     — Full generation against test-openapi.yaml
└── processing/
    ├── TypeConverterTest.java               — Type detection, formatting, Java→Kotlin conversion
    ├── ImportFilterTest.java                — Java/Kotlin import filtering
    ├── EnumFieldProcessorTest.java          — Vendor extension enum processing
    └── ParameterConstraintExtractorTest.java — OpenAPI validation constraint extraction
```

### What's tested

| Class | Tests | Coverage |
|-------|-------|----------|
| CefCodegen | ~45 | Parameter extraction, template config, enums, server URLs |
| CefKotlinCodegen | ~28 | Type mappings, templates, property kotlinification, $-escaping |
| IntegrationTest | ~21 | Full Java + Kotlin generation, configOptions, output verification |
| TypeConverter | ~36 | detectType, formatLiteral, kotlinify, kotlinifyDefaultValue |
| ImportFilter | ~28 | Java cleanup, Kotlin cleanup, self-package filtering |
| EnumFieldProcessor | ~10 | Vendor extensions, value injection, type detection |
| ParameterConstraintExtractor | ~30 | String/numeric/array/enum/nullable constraints |

### Integration tests

`IntegrationTest.java` generates code from `test-openapi.yaml` into a temp directory and verifies:
- All expected files exist (DTOs, services, infrastructure)
- Kotlin files have `data class`, `by lazy`, `Unit`, no `java.util`
- Java files have Builder, Serializable when configured
- Enums have vendor extension fields
- configOptions work: `serializableModel`, `containerDefaultToNull`, `generateBuilders`, `additionalModelTypeAnnotations`, `modelSuffix`

### Adding tests

- **New configOption** → add integration test in `IntegrationTest.ConfigOptions`
- **New processing logic** → add unit test in `processing/` package
- **New template feature** → integration test verifying generated output content
- Run `./gradlew :generator:check` — JaCoCo enforces 90% minimum

## Example Project Tests

The `examples/cef-java/` directory contains a complete generated project with 186 tests:

```bash
cd examples/cef-java
./gradlew test              # Unit + integration tests
./gradlew jmh              # Performance benchmarks
./gradlew jacocoTestReport  # Coverage report
```

### Categories

- **Routing** — RouteTree, caching, HTTP methods, pattern matching
- **Protocol** — ApiRequest, ApiResponse, HttpMethod
- **CEF handlers** — Handler, Builder, ResourceHandler
- **Exceptions** — Hierarchy, status codes, cause chains
- **Interceptors** — CORS, auth (API key, Bearer, Basic), validation
- **DTOs/Enums** — Serialization, custom fields, type detection
- **Integration** — Full request/response cycle
- **Benchmarks** — RouteTree vs regex (2.6x faster), cache effectiveness, scalability

## CI

Tests run automatically on push/PR via GitHub Actions (`.github/workflows/ci.yml`).

Coverage report uploaded as artifact on every CI run.
