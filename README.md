# CEF OpenAPI Generator

OpenAPI Code Generator for Chromium Embedded Framework (CEF) with JetBrains Platform integration.

Generates **Java** and **idiomatic Kotlin** code from OpenAPI specs for JCEF-based IntelliJ plugins.

## Features

- **RouteTree Routing** — Trie-based, 2.6x faster than regex, LRU-cached
- **Type-Safe Parameters** — query params, path variables, body auto-extracted and typed
- **Two-Level Services** — HTTP wrapper + pure business method per operation
- **Builder Pattern** — fluent API for routes, interceptors, CORS, validation
- **Zero Dependencies** — only Jackson for JSON (no Lombok, Spring, Jakarta in generated code)
- **Java 17+ / Kotlin 2.3+** — modern language features, idiomatic output
- **Direct CEF Access** — CefBrowser, CefFrame, CefRequest in wrapper methods

### Kotlin Generator (v3.1.0+)

The `cef-kotlin` generator produces idiomatic Kotlin:
- `data class` models with `val` properties and default values
- `by lazy` for deferred initialization
- `inline fun <reified T>` for type-safe body deserialization
- `fun interface`, `companion object`, expression bodies
- `Unit` instead of `Void?`, `runCatching` instead of try-catch
- `typealias RouteHandler`, `getOrPut`, Kotlin collections

### Interceptors & Security

- Request/Response interceptors (logging, metrics, auth)
- CORS with origin whitelisting
- API Key / Bearer / Basic authentication
- OpenAPI parameter validation (minLength, maximum, pattern, enum, etc.)
- Type-specific exception handlers (Chain of Responsibility)

### Advanced

- Multipart file upload (RFC 7578)
- Mock service generation from OpenAPI examples
- Enum custom fields via `x-enum-field-*` with auto-type detection
- Model naming options (`modelSuffix`, `modelPrefix`)

## Installation

### Local Build

```bash
git clone https://github.com/a-havrysh/cef-openapi-generator.git
cd cef-openapi-generator
./gradlew :generator:publishToMavenLocal
```

```kotlin
// build.gradle.kts
buildscript {
    repositories { mavenLocal() }
    dependencies { classpath("io.github.cef:generator:3.1.0") }
}

plugins {
    id("org.openapi.generator") version "7.21.0"
}
```

### GitHub Packages

```kotlin
// settings.gradle.kts or build.gradle.kts
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/a-havrysh/cef-openapi-generator")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

## Quick Start

### Configure Generation

```kotlin
val generateApi by tasks.registering(org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("cef-kotlin")  // or "cef" for Java
    inputSpec.set("$projectDir/openapi.yaml")

    modelPackage.set("com.example.api.dto")
    apiPackage.set("com.example.api")

    generateApiTests.set(false)
    generateModelTests.set(false)
    generateApiDocumentation.set(false)
    generateModelDocumentation.set(false)
}
```

### Use Generated Code

**Kotlin:**

```kotlin
val handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withUrlFilter()
    .withValidation()
    .withCors("https://local.bpmn")
    .withExact("/health", HttpMethod.GET) { ApiResponse.ok("OK") }
    .withFallback(HttpMethod.GET) { request ->
        val data = readResource(request.path.substringAfter("/"))
        ApiResponse.ok(data, ContentTypeResolver.resolve(request.path))
    }
    .build()
```

**Java:**

```java
var handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withCors("http://localhost:3000")
    .withValidation()
    .withInterceptor(new LoggingInterceptor())
    .withExceptionHandler(ValidationException.class, (ex, req) ->
        ApiResponse.badRequest(ex.getMessage()))
    .build();
```

### Implement Services

```kotlin
@Service(Service.Level.PROJECT)
class ConfigService(private val project: Project) : ConfigApiService {

    // Pattern 1: Override business method (simple)
    override fun getConfig(): ConfigResponse {
        return ConfigResponse(theme = Theme.DARK, engine = Engine.CAMUNDA_8)
    }

    // Pattern 2: Override wrapper method (custom HTTP response)
    override fun handleSaveConfig(
        configSaveRequest: ConfigSaveRequest,
        request: ApiRequest, browser: CefBrowser, frame: CefFrame, cefRequest: CefRequest
    ): ApiResponse<Unit> {
        applyConfig(configSaveRequest)
        browser.executeJavaScript("location.reload()", "", 0)
        return ApiResponse.noContent()
    }
}
```

## Generated Structure

```
api/
├── cef/                    — CEF integration (handler, builder, response)
├── routing/                — Trie-based RouteTree + RouteNode
├── protocol/               — ApiRequest, ApiResponse, HttpMethod
├── interceptor/            — CORS, auth, validation, exception handlers
├── validation/             — ParameterValidator
├── service/                — *ApiService interfaces (two-level)
├── dto/                    — data classes / enums
├── util/                   — ContentTypeResolver, MultipartParser
└── exception/              — ApiException hierarchy (400, 404, 500, 501)
```

## Generator Architecture

```
io.github.cef.codegen/
├── CefCodegen.java                     — Base generator (Java)
├── CefKotlinCodegen.java               — Kotlin overrides
├── config/
│   ├── FileSpec.java                   — Template ↔ filename mapping
│   ├── PackageSuffix.java              — Layer package suffixes
│   └── GeneratorLayer.java             — Layer registration
└── processing/
    ├── EnumFieldProcessor.java         — x-enum-field-* vendor extensions
    ├── ImportFilter.java               — Import cleanup (Java + Kotlin)
    ├── ParameterConstraintExtractor.java — OpenAPI validation constraints
    └── TypeConverter.java              — Type detection, formatting, Java→Kotlin
```

## Enum Custom Fields

```yaml
Engine:
  type: string
  enum: [CLASSIC_BPMN, CAMUNDA_7, CAMUNDA_8]
  x-enum-field-displayName: ["Classic BPMN", "Camunda 7", "Camunda 8"]
  x-enum-field-description: ["Standard BPMN 2.0", "Platform 7", "Platform 8"]
```

Generates (Kotlin):

```kotlin
enum class Engine(val value: String, val displayName: String, val description: String) {
    CLASSIC_BPMN("CLASSIC_BPMN", "Classic BPMN", "Standard BPMN 2.0"),
    CAMUNDA_7("CAMUNDA_7", "Camunda 7", "Platform 7"),
    CAMUNDA_8("CAMUNDA_8", "Camunda 8", "Platform 8");
}
```

Type detection: `1, 2` → Int, `true/false` → Boolean, `"text"` → String, `1.5` → Double

## Version

**Current: 3.1.0**

See [CHANGELOG.md](CHANGELOG.md) for history. Upgrading from 1.x/2.x? See [MIGRATION.md](MIGRATION.md).

## License

MIT
