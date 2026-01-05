# CEF OpenAPI Generator

OpenAPI Code Generator for Chromium Embedded Framework (CEF) with JetBrains Platform integration.

## Features

- **RouteTree Routing**: Trie-based routing, 2.6x faster than regex
- **ApiResponse<T>**: Generic response wrapper (like Spring ResponseEntity)
- **Builder Pattern**: Fluent API for custom routes
- **Enum Custom Fields**: Auto-detect types (Integer, String, Boolean, etc.) from YAML values
- **Type-Safe**: No unchecked casts
- **Zero Dependencies**: No Lombok, Swagger, or Spring
- **Java 8+ Compatible**: Works with legacy codebases
- **Extensible**: Add prefix/exact/contains routes alongside API routes

## Installation

### Option 1: GitHub Packages (Gradle)

**1. Add GitHub Packages repository:**

```gradle
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

**2. Add credentials to `~/.gradle/gradle.properties`:**

```properties
gpr.user=your-github-username
gpr.token=your-github-personal-access-token
```

To create a GitHub Personal Access Token:
- Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
- Click "Generate new token"
- Select scopes: `read:packages`
- Copy the token

**3. Add dependency:**

```gradle
buildscript {
    dependencies {
        classpath("io.github.cef:generator:1.0.6")
    }
}

plugins {
    id("org.openapi.generator") version "7.18.0"
}
```

### Option 2: Local Build

```bash
git clone https://github.com/a-havrysh/cef-openapi-generator.git
cd cef-openapi-generator
./gradlew :generator:publishToMavenLocal

# In your project:
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath("io.github.cef:generator:1.0.6")
    }
}
```

## Quick Start

### Configure Generation

```gradle
val generateApi by tasks.registering(org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("cef")
    inputSpec.set("$projectDir/openapi.yaml")

    modelPackage.set("com.example.api.dto")
    apiPackage.set("com.example.api")

    // Disable unnecessary generation
    generateApiTests.set(false)
    generateModelTests.set(false)
    generateApiDocumentation.set(false)
    generateModelDocumentation.set(false)
}

tasks.compileJava {
    dependsOn(generateApi)
}

sourceSets {
    main {
        java {
            srcDir("$buildDir/generated/src/main/java")
        }
    }
}
```

### Use Generated Code

```java
// Simple usage
var handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .build();

browser.addRequestHandler(handler, browser.getCefBrowser());

// With custom routes (HTTP method-specific)
var handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withPrefix("/static", HttpMethod.GET, request -> {
        byte[] data = readStaticFile(request.getPath());
        return ApiResponse.ok(data, ContentTypeResolver.resolve(request.getPath()));
    })
    .withExact("/health", HttpMethod.GET, request -> ApiResponse.ok("OK"))
    .withContains(".json", HttpMethod.POST, request -> {
        // Handle all POST requests containing ".json" in path
        return processJsonRequest(request);
    })
    .withFallback(HttpMethod.GET, request -> {
        // Fallback for unmatched GET requests (e.g., static files)
        String path = request.getPath().substring(1);
        byte[] data = readResource(path);
        return ApiResponse.ok(data, ContentTypeResolver.resolve(path));
    })
    .withFallback(HttpMethod.POST, request -> {
        // Different fallback for POST requests
        return ApiResponse.notFound("POST endpoint not found");
    })
    .build();

// With URL filtering (auto from OpenAPI servers)
var handler = ApiCefRequestHandler.builder(project)
    .withUrlFilter()  // Only handles URLs from 'servers' section
    .withApiRoutes()
    .build();

// With URL filtering (manual prefixes)
var handler = ApiCefRequestHandler.builder(project)
    .withUrlFilter("http://localhost:5173")
    .withApiRoutes()
    .withPrefix("/static", HttpMethod.GET, staticFileHandler)
    .build();
```

**HTTP Method Support:**

All custom route methods (`withPrefix`, `withExact`, `withContains`, `withFallback`) support HTTP method specification:
- Allows different handlers for GET vs POST on the same path
- Enables method-specific fallback behavior
- Example: Serve static files only for GET, reject POST with 404

```java
// Different handlers for different methods
builder
    .withPrefix("/api", HttpMethod.GET, getHandler)
    .withPrefix("/api", HttpMethod.POST, postHandler)
    .withFallback(HttpMethod.GET, serveStaticFile)
    .withFallback(HttpMethod.POST, notFoundHandler);
```

**URL Filtering:**
- `withUrlFilter()` - automatically uses URLs from OpenAPI `servers` section
- `withUrlFilter(String... prefixes)` - manually specify allowed URL prefixes
- Without filter - handler processes all requests (default)
- With filter - handler only processes matching URLs, returns `null` for others (browser handles them)

### Implement Services

```java
@Service(Service.Level.PROJECT)
public final class ExampleServiceImpl implements ExampleApiService {

    @Override
    public Message getHello() {
        return new Message("Hello World!");
    }

    // Or override wrapper for custom response:
    @Override
    public ApiResponse<Message> handleGetHello(ApiRequest request) {
        return ApiResponse.ok(getHello())
            .contentType("application/json")
            .header("X-Custom-Header", "value");
    }
}
```

## Enum Custom Fields

Define enums with custom fields in your OpenAPI spec using `x-enum-field-*` extensions. The generator automatically detects field types from YAML values:

```yaml
TaskStatus:
  type: string
  enum:
    - PENDING
    - IN_PROGRESS
    - COMPLETED
  x-enum-field-displayName:
    - Pending
    - In Progress
    - Completed
  x-enum-field-value:
    - pending
    - in_progress
    - completed
  x-enum-field-priority:
    - 1        # Integer (no quotes)
    - 2
    - 3
  x-enum-field-active:
    - true     # Boolean
    - true
    - false
```

Generates:

```java
public enum TaskStatus {
    PENDING("pending", "Pending", 1, true),
    IN_PROGRESS("in_progress", "In Progress", 2, true),
    COMPLETED("completed", "Completed", 3, false);

    private final String value;
    private final String displayName;
    private final Integer priority;
    private final Boolean active;

    TaskStatus(String value, String displayName, Integer priority, Boolean active) {
        this.value = value;
        this.displayName = displayName;
        this.priority = priority;
        this.active = active;
    }

    public String getValue() { return value; }
    public String getDisplayName() { return displayName; }
    public Integer getPriority() { return priority; }
    public Boolean getActive() { return active; }
}
```

Type detection rules:
- `1, 2, 3` (no quotes) → `Integer`
- `"1", "2", "3"` (with quotes) → `String`
- `true, false` → `Boolean`
- `1.5, 2.3` → `Double`

Supported types: `Integer`, `Long`, `Double`, `Float`, `Boolean`, `String`, `BigDecimal`, `BigInteger`

## Generated Structure

```
api/
├── cef/
│   ├── ApiCefRequestHandler.java
│   ├── ApiCefRequestHandlerBuilder.java
│   ├── ApiResourceRequestHandler.java
│   └── ApiResponseHandler.java
├── routing/
│   ├── RouteTree.java (Trie with extensions)
│   └── RouteNode.java
├── protocol/
│   ├── ApiRequest.java
│   ├── ApiResponse.java
│   └── HttpMethod.java
├── service/
│   └── *Service.java (two-level: wrapper + business)
├── dto/
│   └── *.java (with manual builders)
└── exception/
    └── *.java (hierarchy)
```

## Performance

- **RouteTree**: 2.6x faster than regex matching
- **Optimizations**: Exact routes O(1), LRU cache, selective caching
- **Tested**: Benchmark validated

## Architecture

```
Browser → ApiCefRequestHandler (RouteTree check)
  → ApiResourceRequestHandler (RouteTree match)
    → Service.handleXxx() → ApiResponse<T>
      → ApiResponseHandler → CEF
```

## Testing

For comprehensive testing examples and best practices, see [TESTING.md](TESTING.md).

The testing guide covers:
- Unit tests for HTTP method support
- Route matching priority
- CRUD operations testing
- Performance testing
- Manual testing examples

## License

MIT License

## Contributing

Contributions welcome! Please open an issue or PR.
