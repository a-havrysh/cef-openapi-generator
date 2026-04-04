# CEF OpenAPI Generator

[![CI](https://github.com/a-havrysh/cef-openapi-generator/actions/workflows/ci.yml/badge.svg)](https://github.com/a-havrysh/cef-openapi-generator/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

OpenAPI code generator for **Chromium Embedded Framework (CEF)** with JetBrains JCEF plugin integration.

Two generators available:

| Generator | Language | Output style |
|-----------|----------|-------------|
| `cef` | Java 17+ | Classical Java with builders, POJOs |
| `cef-kotlin` | Kotlin 2.3+ | Idiomatic Kotlin — data classes, lazy, reified generics, Unit |

## Installation

### Option 1: Local build

```bash
git clone https://github.com/a-havrysh/cef-openapi-generator.git
cd cef-openapi-generator
./gradlew :generator:publishToMavenLocal
```

### Option 2: GitHub Packages

Add to `~/.gradle/gradle.properties`:
```properties
gpr.user=your-github-username
gpr.token=ghp_your-personal-access-token  # scope: read:packages
```

Add repository:
```kotlin
// build.gradle.kts (buildscript block or settings.gradle.kts)
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

## Gradle Configuration

### Minimal

```kotlin
buildscript {
    repositories { mavenLocal() }
    dependencies { classpath("io.github.cef:generator:3.1.0") }
}

plugins {
    id("org.openapi.generator") version "7.21.0"
}

val generateApi by tasks.registering(org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("cef-kotlin")  // or "cef" for Java
    inputSpec.set("$projectDir/openapi.yaml")
    outputDir.set("${layout.buildDirectory.get()}/generated")
    apiPackage.set("com.example.api")
    modelPackage.set("com.example.api.dto")
}
```

### Full configuration

```kotlin
val generateApi by tasks.registering(org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    // --- Required ---
    generatorName.set("cef-kotlin")              // "cef" for Java, "cef-kotlin" for Kotlin
    inputSpec.set("$projectDir/openapi.yaml")     // Path to OpenAPI spec
    outputDir.set("${layout.buildDirectory.get()}/generated")

    // --- Packages ---
    apiPackage.set("com.example.api")             // Package for service interfaces
    modelPackage.set("com.example.api.dto")       // Package for DTOs and enums

    // --- Generation control ---
    generateApiTests.set(false)                   // Skip API test stubs
    generateModelTests.set(false)                 // Skip model test stubs
    generateApiDocumentation.set(false)           // Skip API docs
    generateModelDocumentation.set(false)         // Skip model docs
    skipOperationExample.set(true)                // Skip complex examples in spec

    // --- Generator-specific options ---
    configOptions.set(mapOf(
        "hideGenerationTimestamp" to "true",       // No timestamp in generated files
        "modelSuffix" to "Dto",                   // Append suffix to models: User → UserDto
        // "modelPrefix" to "Api",                // Prepend prefix to models: User → ApiUser
    ))

    // --- Type overrides ---
    typeMappings.set(mapOf(
        "DateTime" to "java.time.OffsetDateTime",
        "Date" to "java.time.LocalDate"
    ))

    importMappings.set(mapOf(
        "OffsetDateTime" to "java.time.OffsetDateTime",
        "LocalDate" to "java.time.LocalDate",
        "JsonNode" to "com.fasterxml.jackson.databind.JsonNode"
    ))
}

// Register generated sources
sourceSets {
    main {
        kotlin {  // or java { ... }
            srcDirs("build/generated/src/main/kotlin")
        }
    }
}

tasks.compileKotlin { dependsOn(generateApi) }
```

### Supported configOptions

| Option | Default | Description |
|--------|---------|-------------|
| `hideGenerationTimestamp` | `true` | Omit timestamp comment from generated files |
| `modelSuffix` | — | Append suffix to all model names (e.g., `Dto` → `UserDto`) |
| `modelPrefix` | — | Prepend prefix to all model names (e.g., `Api` → `ApiUser`) |
| `serializableModel` | `false` | Add `implements Serializable` (Java) / `: Serializable` (Kotlin) to models |
| `containerDefaultToNull` | `false` | Init containers (`List`, `Map`) to `null` instead of `emptyList()`/`emptyMap()` |
| `generateBuilders` | `false` | Generate Builder pattern on Java models (no effect on Kotlin — uses data class copy) |
| `generateConstructorWithAllArgs` | `false` | Generate all-args constructor on Java models (uses `x-java-all-args-constructor-vars`) |
| `additionalModelTypeAnnotations` | — | Extra class-level annotations on models (e.g., `@kotlinx.serialization.Serializable`) |
| `additionalEnumTypeAnnotations` | — | Extra class-level annotations on enums (e.g., `@Deprecated`) |
| `dateLibrary` | `java8` | Date library: `java8` (OffsetDateTime), `java8-localdatetime` (LocalDateTime) |
| `openApiNullable` | `false` | Enable Jackson Nullable (`JsonNullable<T>`) — disabled for clean DTOs |
| `useBeanValidation` | `false` | Add Jakarta Bean Validation annotations — disabled for zero-dep output |

> All standard [AbstractJavaCodegen options](https://openapi-generator.tech/docs/generators/java/) are inherited
> (e.g., `serializationLibrary`, `sourceFolder`, `sortModelPropertiesByRequiredFlag`, `enumPropertyNaming`, etc.)

**Example** — Kotlin with Serializable + custom annotations:
```kotlin
configOptions.set(mapOf(
    "serializableModel" to "true",
    "additionalModelTypeAnnotations" to "@kotlinx.serialization.Serializable",
    "containerDefaultToNull" to "true"
))
```

### Supported Gradle task properties

All [standard OpenAPI Generator Gradle plugin properties](https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-gradle-plugin/README.adoc) are supported:

`generatorName`, `inputSpec`, `outputDir`, `apiPackage`, `modelPackage`, `templateDir`,
`configOptions`, `typeMappings`, `importMappings`, `additionalProperties`, `globalProperties`,
`generateApiTests`, `generateModelTests`, `generateApiDocumentation`, `generateModelDocumentation`,
`skipOperationExample`, `modelNameSuffix`, `modelNamePrefix`, `schemaMappings`, `nameMappings`, etc.

## Maven Configuration

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.openapitools</groupId>
            <artifactId>openapi-generator-maven-plugin</artifactId>
            <version>7.21.0</version>
            <executions>
                <execution>
                    <goals><goal>generate</goal></goals>
                    <configuration>
                        <generatorName>cef-kotlin</generatorName>
                        <inputSpec>${project.basedir}/openapi.yaml</inputSpec>
                        <output>${project.build.directory}/generated</output>
                        <apiPackage>com.example.api</apiPackage>
                        <modelPackage>com.example.api.dto</modelPackage>
                        <generateApiTests>false</generateApiTests>
                        <generateModelTests>false</generateModelTests>
                        <generateApiDocumentation>false</generateApiDocumentation>
                        <generateModelDocumentation>false</generateModelDocumentation>
                        <configOptions>
                            <hideGenerationTimestamp>true</hideGenerationTimestamp>
                            <serializableModel>true</serializableModel>
                        </configOptions>
                    </configuration>
                </execution>
            </executions>
            <dependencies>
                <dependency>
                    <groupId>io.github.cef</groupId>
                    <artifactId>generator</artifactId>
                    <version>3.1.0</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
```

## CLI Usage

```bash
# List all available options for the generator
java -cp generator.jar org.openapitools.codegen.OpenAPIGenerator config-help -g cef-kotlin

# Generate code
java -cp generator.jar org.openapitools.codegen.OpenAPIGenerator generate \
  -g cef-kotlin \
  -i openapi.yaml \
  -o ./generated \
  --api-package com.example.api \
  --model-package com.example.api.dto \
  --additional-properties hideGenerationTimestamp=true,serializableModel=true

# Or use the openapi-generator-cli with custom JAR
openapi-generator-cli generate \
  --custom-generator=generator-3.1.0.jar \
  -g cef-kotlin \
  -i openapi.yaml \
  -o ./generated
```

## File Ignore

Place `.openapi-generator-ignore` in the output directory to prevent overwriting files:

```gitignore
# Keep custom service implementations
**/service/*ServiceImpl.kt

# Keep hand-written models
**/dto/CustomModel.kt

# Skip test/doc generation
**/*Test.kt
**/*.md
```

See [.openapi-generator-ignore.example](.openapi-generator-ignore.example) for a full template.

## Generated Code

### Architecture

```
CEF Browser → ApiCefRequestHandler → URL filter → Route match → Interceptors → Service → ApiResponse → CEF Response
```

### Output structure

```
api/
├── cef/                    — ApiCefRequestHandler, Builder, ResourceHandler, ResponseHandler
├── routing/                — Trie-based RouteTree + RouteNode (2.6x faster than regex)
├── protocol/               — ApiRequest, ApiResponse<T>, HttpMethod
├── interceptor/            — RequestInterceptor, CORS, validation, auth, exception handling
├── validation/             — ParameterValidator (string/numeric/array/enum/format)
├── service/                — *ApiService interfaces (two-level: HTTP wrapper + business method)
├── dto/                    — data class DTOs + enums with @JsonProperty
├── util/                   — ContentTypeResolver (18+ MIME types), MultipartParser
└── exception/              — ApiException(statusCode) → BadRequest/NotFound/InternalError/NotImplemented/Validation
```

### Builder API

```kotlin
val handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()                                           // All routes from OpenAPI spec
    .withUrlFilter()                                           // Only handle server URLs from spec
    .withUrlFilter("https://local.bpmn", "http://localhost")   // ...or custom prefixes
    .withValidation()                                          // OpenAPI parameter validation
    .withCors("https://local.bpmn")                            // CORS for specific origins
    .withCors()                                                // ...or all origins (*)
    .withInterceptor(loggingInterceptor)                       // Custom interceptors
    .withRoute("/custom/{id}", HttpMethod.GET) { ... }         // Custom route with path vars
    .withPrefix("/static", HttpMethod.GET) { ... }             // Prefix matching
    .withExact("/health", HttpMethod.GET) { ApiResponse.ok("OK") }
    .withFallback(HttpMethod.GET) { ... }                      // Fallback for unmatched GETs
    .withExceptionHandler(ValidationException::class.java) { e, req -> ... }
    .withExceptionHandler(ApiException::class.java) { e, req -> ... }
    .build()
```

### Service pattern

```kotlin
// Pattern 1: Business method only (simple — 200 OK + JSON auto-wrapped)
override fun getConfig(): ConfigResponse {
    return ConfigResponse(theme = Theme.DARK, engine = Engine.CAMUNDA_8)
}

// Pattern 2: Wrapper method (full HTTP control + CEF access)
override fun handleSaveConfig(
    configSaveRequest: ConfigSaveRequest,
    request: ApiRequest, browser: CefBrowser, frame: CefFrame, cefRequest: CefRequest
): ApiResponse<Unit> {
    applyConfig(configSaveRequest)
    browser.executeJavaScript("location.reload()", "", 0)
    return ApiResponse.noContent()
}
```

### OpenAPI validation

Enabled via `.withValidation()`. Constraints extracted from OpenAPI spec:

| Constraint | Types | Example |
|-----------|-------|---------|
| `required` | all | Missing parameter → 400 |
| `minLength` / `maxLength` | string | `minLength: 1, maxLength: 100` |
| `minimum` / `maximum` | integer, number | `minimum: 1, maximum: 1000` |
| `exclusiveMinimum` / `exclusiveMaximum` | integer, number | `> 0` instead of `>= 0` |
| `multipleOf` | integer, number | Must be divisible by N |
| `pattern` | string | Regex: `^[A-Z]{3}$` |
| `enum` | string | Allowed values list |
| `format` | string | `email`, `uuid`, `uri`, `hostname`, `ipv4`, `ipv6`, `date`, `date-time` |
| `minItems` / `maxItems` | array | Array length bounds |
| `uniqueItems` | array | No duplicate elements |

### Enum custom fields

Define enums with custom fields via `x-enum-field-*` vendor extensions:

```yaml
Engine:
  type: string
  enum: [CLASSIC_BPMN, CAMUNDA_7, CAMUNDA_8]
  x-enum-field-displayName: ["Classic BPMN", "Camunda 7", "Camunda 8"]
  x-enum-field-description: ["Standard BPMN 2.0", "Platform 7", "Platform 8"]
```

Generates:
```kotlin
enum class Engine(val value: String, val displayName: String, val description: String) {
    CLASSIC_BPMN("CLASSIC_BPMN", "Classic BPMN", "Standard BPMN 2.0"),
    CAMUNDA_7("CAMUNDA_7", "Camunda 7", "Platform 7"),
    CAMUNDA_8("CAMUNDA_8", "Camunda 8", "Platform 8");
}
```

Auto-type detection: `1, 2` → Int, `true/false` → Boolean, `"text"` → String, `1.5` → Double, `100L` → Long

The `value` field is auto-injected if not defined via `x-enum-field-value`.

### Interceptors

```kotlin
class LoggingInterceptor : RequestInterceptor {
    override fun beforeHandle(request: ApiRequest) {
        println("→ ${request.method} ${request.path}")
    }
    override fun afterHandle(response: ApiResponse<*>, durationMs: Long) {
        println("← ${response.statusCode} (${durationMs}ms)")
    }
    override fun onError(exception: Exception, request: ApiRequest) {
        System.err.println("✗ ${request.path}: ${exception.message}")
    }
}
```

Built-in interceptors:
- `CorsInterceptor(allowedOrigins)` — CORS preflight + origin validation
- `ValidationInterceptor` — auto-generated from OpenAPI constraints
- `ApiKeyAuthInterceptor(headerName, validator)` — API key auth
- `BearerAuthInterceptor(validator)` — JWT Bearer token
- `BasicAuthInterceptor(validator)` — HTTP Basic auth

### Exception handling

```kotlin
// Type-specific handlers (Chain of Responsibility)
.withExceptionHandler(ValidationException::class.java) { ex, req ->
    ApiResponse.badRequest(ex.errors.joinToString { it.message })
}
.withExceptionHandler(ApiException::class.java) { ex, req ->
    ApiResponse.status(ex.statusCode, ex.message ?: "Error")
}

// Or single handler
.withExceptionHandler(ExceptionHandler { ex, req ->
    ApiResponse.internalServerError("Server error: ${ex.message}")
})
```

## Generator internals

```
io.github.cef.codegen/
├── CefCodegen.java                          — Base Java generator
├── CefKotlinCodegen.java                    — Kotlin overrides (types, templates, imports)
├── config/
│   ├── FileSpec.java                        — Template ↔ filename mapping (29 entries)
│   ├── PackageSuffix.java                   — 7 layer package suffixes
│   └── GeneratorLayer.java                  — Layer registration into supportingFiles
└── processing/
    ├── EnumFieldProcessor.java              — x-enum-field-* → constructorArgs + enumFields
    ├── ImportFilter.java                    — Annotation/Java/Kotlin import cleanup
    ├── ParameterConstraintExtractor.java    — OpenAPI constraints → vendor extensions
    └── TypeConverter.java                   — Type detection, literal formatting, Java→Kotlin
```

## Troubleshooting

**Generator not found: `cef-kotlin`**
```
Could not find io.github.cef:generator:3.1.0
```
→ Run `./gradlew :generator:publishToMavenLocal` in the generator project, and add `mavenLocal()` to your `buildscript.repositories`.

**`ClassNotFoundException: CefKotlinCodegen`**
→ The generator JAR must be on the classpath. In Gradle, add it as `classpath` dependency in `buildscript.dependencies`, not as regular `implementation`.

**Generated code has `java.util.List` instead of Kotlin `List`**
→ Make sure you use `generatorName = "cef-kotlin"` (not `"cef"`). The Java generator intentionally produces Java types.

**Enum fields not generated (no `displayName`, `description`)**
→ Add `x-enum-field-*` vendor extensions to your OpenAPI spec. The `value` field is auto-injected.

**`$schema` field causes compilation error**
→ Fixed in v3.1.0. The generator escapes `$` in property names for Kotlin string literals and wraps identifiers in backticks.

**Models have `Object` type instead of `Any`**
→ Update to v3.1.0+. Earlier versions had a word-boundary bug that corrupted compound type names.

**How do I see all available options?**
```bash
# List all configOptions for the generator
java -cp generator.jar org.openapitools.codegen.OpenAPIGenerator config-help -g cef-kotlin --full-details
```

**How do I prevent regeneration from overwriting my files?**
→ Place a `.openapi-generator-ignore` file in the output directory. See [.openapi-generator-ignore.example](.openapi-generator-ignore.example).

**Can I use a custom template for one file?**
→ Yes. Set `templateDir` in your Gradle config to a directory with your overrides. Only files present in your directory will override; others use the embedded templates.

## Testing

186 tests, 99.2% instruction coverage.

```bash
./gradlew :generator:test          # Unit + integration tests
./gradlew :generator:check         # Tests + coverage verification (90% threshold)
```

See [TESTING.md](TESTING.md) for details.

## Version

**Current: 3.1.0** — [CHANGELOG.md](CHANGELOG.md)

Upgrading? See [MIGRATION.md](MIGRATION.md).

## License

MIT — see [LICENSE](LICENSE)
