# CEF OpenAPI Generator

OpenAPI Code Generator for Chromium Embedded Framework (CEF) with JetBrains Platform integration.

## Features

### Core Framework
- **RouteTree Routing**: Trie-based routing, 2.6x faster than regex
- **Type-Safe Parameters**: Query params, path variables, body automatically extracted and typed
- **ApiResponse<T>**: Generic response wrapper (like Spring ResponseEntity)
- **Builder Pattern**: Fluent API for custom routes and configuration
- **Zero Dependencies**: No Lombok, Swagger, Jakarta Validation, or Spring (only Jackson for JSON)
- **Java 17+ Compatible**: Modern Java with records, switch expressions
- **Direct CEF Access**: CefBrowser, CefFrame, CefRequest in wrapper methods

### OpenAPI Validation (v2.0.0+)
- **String Validation**: minLength, maxLength, pattern (regex), enum
- **Numeric Validation**: minimum, maximum, exclusiveMinimum, exclusiveMaximum, multipleOf
- **Array Validation**: minItems, maxItems, uniqueItems, item enum validation
- **Format Validation**: email, uuid, uri, hostname, ipv4, ipv6
- **Date/Time Parsing**: format: date → LocalDate, format: date-time → OffsetDateTime
- **Boolean Parsing**: Accepts true/false, 1/0, yes/no, on/off (case-insensitive)
- **Default Values**: Automatically applied from OpenAPI schema
- **Nullable Handling**: Proper null handling per OpenAPI specification

### Interceptors & Middleware
- **Request/Response Interceptors**: Logging, metrics, custom processing
- **Type-Specific Exception Handlers**: Different handlers for different exception types
- **CORS Support**: Cross-origin resource sharing with origin whitelisting
- **Validation Interceptor**: Automatic OpenAPI constraint validation (enable with `.withValidation()`)
- **API Key Authentication**: Header, query parameter, or cookie-based auth
- **Bearer Token (JWT)**: Authorization: Bearer <token> authentication
- **Basic Authentication**: Authorization: Basic <base64> authentication

### Advanced Features
- **File Upload**: multipart/form-data parsing with MultipartFile support
- **Mock Service Generator**: Auto-generated mock implementations from OpenAPI examples
- **Enhanced JavaDoc**: Rich documentation from OpenAPI descriptions and constraints
- **Deprecated Annotations**: @Deprecated for deprecated operations
- **Enum Custom Fields**: Auto-detect types (Integer, String, Boolean, etc.) from YAML values
- **Model Naming Options**: Add suffix/prefix to generated models (modelSuffix, modelPrefix)

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
        classpath("io.github.cef:generator:2.0.0")
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
        classpath("io.github.cef:generator:2.0.0")
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

    // Optional: Model naming configuration
    configOptions.set(mapOf(
        "hideGenerationTimestamp" to "true",
        "modelSuffix" to "Dto"     // Append Dto to all models (Task -> TaskDto)
        // "modelPrefix" to "Api"  // Prepend prefix to all models (Task -> ApiTask)
    ))

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

// With interceptors (logging, metrics, auth)
var handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withInterceptor(new LoggingInterceptor())
    .withInterceptor(new MetricsInterceptor())
    .withExceptionHandler((ex, req) -> {
        logger.error("API error", ex);
        return ApiResponse.internalServerError("Server error");
    })
    .build();

// With custom routes (HTTP method-specific)
var handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withRoute("/custom/{id}", HttpMethod.GET, request -> {
        String id = request.getPathVariable("id");
        return ApiResponse.ok("Custom: " + id);
    })
    .withPrefix("/static", HttpMethod.GET, request -> {
        byte[] data = readStaticFile(request.getPath());
        return ApiResponse.ok(data, ContentTypeResolver.resolve(request.getPath()));
    })
    .withExact("/health", HttpMethod.GET, request -> ApiResponse.ok("OK"))
    .withFallback(HttpMethod.GET, request -> {
        // Fallback for unmatched GET requests
        String path = request.getPath().substring(1);
        byte[] data = readResource(path);
        return ApiResponse.ok(data, ContentTypeResolver.resolve(path));
    })
    .build();

// With URL filtering + CORS + interceptors
var handler = ApiCefRequestHandler.builder(project)
    .withUrlFilter("http://localhost:5173")
    .withApiRoutes()
    .withCors("http://localhost:3000", "https://example.com")
    .withInterceptor(new AuthInterceptor())
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

**CORS Support:**

Enable Cross-Origin Resource Sharing for browser-based clients:

```java
// Allow all origins (Access-Control-Allow-Origin: *)
var handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withCors()
    .build();

// Allow specific origins only
var handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withCors("http://localhost:3000", "https://example.com")
    .build();
```

CORS features:
- Automatic OPTIONS preflight handling
- `Access-Control-Allow-Origin`, `Access-Control-Allow-Credentials` headers
- `Access-Control-Allow-Methods`, `Access-Control-Allow-Headers`, `Access-Control-Max-Age`
- Empty list = allow all origins (`*`)
- Specific origins = whitelist mode with credentials support

**OpenAPI Parameter Validation:**

Automatically validate request parameters against OpenAPI constraints (v2.0+):

```java
// Enable validation (recommended for production)
var handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withValidation()
    .build();
```

Supported constraints from OpenAPI specification:
- **required**: Parameter must be present
- **minLength** / **maxLength**: String length bounds
- **minimum** / **maximum**: Numeric value bounds
- **pattern**: Regex pattern matching
- **enum**: Allowed values list

Example OpenAPI specification with constraints:

```yaml
/api/tasks:
  get:
    parameters:
      - name: status
        in: query
        schema:
          type: string
          enum: [pending, in_progress, completed]
      - name: page
        in: query
        schema:
          type: integer
          minimum: 1
          maximum: 1000
```

When validation fails, `ValidationException` is thrown (HTTP 400) with detailed error information:

```json
{
  "statusCode": 400,
  "message": "Validation failed with 2 error(s)",
  "errors": [
    {
      "parameter": "page",
      "value": "0",
      "constraint": "minimum",
      "message": "page must be at least 1 (got 0)"
    },
    {
      "parameter": "status",
      "value": "invalid",
      "constraint": "enum",
      "message": "status must be one of: pending, in_progress, completed"
    }
  ]
}
```

Validation features:
- Validates before service layer - early error detection
- Collects all errors in single response - better UX
- Thread-safe regex pattern caching - high performance
- No external dependencies - pure Java validation
- Automatically generated from OpenAPI spec - no manual code

**Request/Response Interceptors:**

Add cross-cutting concerns (logging, metrics, authentication):

```java
// Logging interceptor
public class LoggingInterceptor implements RequestInterceptor {
    @Override
    public void beforeHandle(ApiRequest request) {  // v2.0: simplified signature
        System.out.println("→ " + request.getMethod() + " " + request.getPath());
        String userId = request.getPathVariable("userId");  // Access path variables from request
    }

    @Override
    public void afterHandle(ApiResponse<?> response, long durationMs) {
        System.out.println("← " + response.getStatusCode() + " (" + durationMs + "ms)");
    }

    @Override
    public void onError(Exception e, ApiRequest request) {  // v2.0: ApiRequest instead of CefRequest
        System.err.println("✗ Error: " + e.getMessage());
    }
}

// Authentication interceptor
public class AuthInterceptor implements RequestInterceptor {
    @Override
    public void beforeHandle(ApiRequest request) throws Exception {  // v2.0: no pathVars parameter
        String token = request.getHeader("Authorization");  // v2.0: getHeader instead of getHeaderByName
        if (token == null || !isValidToken(token)) {
            throw new ApiException(401, "Unauthorized");
        }
    }
}

// Register interceptors
var handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withInterceptor(new LoggingInterceptor())
    .withInterceptor(new AuthInterceptor())
    .build();
```

Interceptor execution order:
1. `beforeHandle()` - before route handler (can abort via exception)
2. Route handler executes
3. `afterHandle()` - after successful handling
4. `onError()` - if exception occurs

**Exception Handler:**

Centralized exception handling for custom error responses.

**Option 1: Type-Specific Handlers (v2.0+, recommended):**

```java
var handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withExceptionHandler(ValidationException.class, (ex, req) -> {
        // Custom handling for validation errors
        ErrorResponse err = new ErrorResponse();
        err.setError("Validation");
        err.setMessage(ex.getMessage());
        err.setDetails(ex.getErrors());  // Include validation error list
        return ApiResponse.badRequest(err);
    })
    .withExceptionHandler(ApiException.class, (ex, req) -> {
        // Custom handling for API exceptions
        logger.warn("API error: {}", ex.getMessage());
        return ApiResponse.status(ex.getStatusCode(), ex.getMessage());
    })
    .withExceptionHandler(Exception.class, (ex, req) -> {
        // Fallback for unexpected errors
        logger.error("Unexpected error", ex);
        return ApiResponse.internalServerError("Internal server error");
    })
    .build();
```

Features:
- Chain of Responsibility - handlers checked in registration order
- Type-safe with generics
- First matching handler processes the exception
- Clean separation of error handling logic

**Option 2: Single Handler:**

```java
var handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withExceptionHandler((exception, request) -> {
        // Manual instanceof checks
        if (exception instanceof ValidationException) {
            return ApiResponse.badRequest(exception.getMessage());
        }
        if (exception instanceof ApiException) {
            ApiException apiEx = (ApiException) exception;
            return ApiResponse.status(apiEx.getStatusCode(), apiEx.getMessage());
        }
        return ApiResponse.internalServerError("Internal server error");
    })
    .build();
```

### Implement Services

Generated service interfaces use a two-level architecture with typed parameters:

```java
@Service(Service.Level.PROJECT)
public final class TasksServiceImpl implements TasksApiService {

    // Pattern 1: Simple business logic (query parameters auto-extracted!)
    @Override
    public TaskListResponse listTasks(String status, Integer page, Integer size) {
        // All parameters typed and extracted automatically from query string
        int p = page != null ? page : 0;
        int s = size != null ? size : 20;

        return repository.findAll(status, p, s);
    }

    // Pattern 2: Path variables auto-extracted
    @Override
    public Task getTask(String taskId) {
        // taskId extracted from /api/tasks/{taskId}
        return repository.findById(taskId)
            .orElseThrow(() -> new NotFoundException("Task not found"));
    }

    // Pattern 3: Custom HTTP response (override wrapper)
    @Override
    public ApiResponse<Task> handleCreateTask(
        CreateTaskRequest body,      // Request body (typed!)
        CefBrowser browser,          // Direct CEF access
        CefFrame frame,
        CefRequest cefRequest
    ) {
        Task task = createTask(body);
        repository.save(task);

        // Custom status code + headers
        return ApiResponse.created(task)
            .header("Location", "/api/tasks/" + task.getId());
    }

    // Pattern 4: CEF browser interaction
    @Override
    public ApiResponse<Void> handleNotifyBrowser(
        NotifyBrowserRequest body,
        CefBrowser browser,          // Direct browser access!
        CefFrame frame,
        CefRequest cefRequest
    ) {
        // Execute JavaScript in browser
        browser.executeJavaScript(
            "showNotification('" + body.getMessage() + "')",
            "",
            0
        );

        return ApiResponse.ok();
    }
}
```

**Key Benefits:**
- ✅ **Type Safety**: All parameters typed (String, Integer, BodyType)
- ✅ **Auto Extraction**: Query params, path variables, body automatically extracted
- ✅ **Auto Parsing**: String → Integer conversion for query params
- ✅ **Direct CEF Access**: CefBrowser, CefFrame, CefRequest available
- ✅ **No Manual Work**: No `request.getQueryParam()` or `request.getPathVariable()` needed

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
│   ├── RouteTree.java (Trie with LRU cache)
│   └── RouteNode.java
├── protocol/
│   ├── ApiRequest.java (for custom routes)
│   ├── ApiResponse.java (with convenience methods)
│   └── HttpMethod.java
├── interceptor/
│   ├── RequestInterceptor.java (logging, metrics, auth)
│   ├── ExceptionHandler.java (centralized error handling)
│   ├── CompositeExceptionHandler.java (type-specific handlers)
│   ├── ValidationInterceptor.java (OpenAPI validation)
│   ├── CorsInterceptor.java (CORS implementation)
│   └── UrlFilterInterceptor.java (URL filtering)
├── validation/
│   └── ParameterValidator.java (OpenAPI constraint validation)
├── service/
│   └── *Service.java (two-level with typed params)
├── dto/
│   └── *.java (with @JsonProperty, builders)
├── util/
│   └── ContentTypeResolver.java (18+ MIME types)
└── exception/
    ├── ApiException.java (base with status code)
    ├── BadRequestException.java (400)
    ├── NotFoundException.java (404)
    ├── InternalServerErrorException.java (500)
    ├── ValidationException.java (400, with error list)
    └── NotImplementedException.java (501)
```

## Performance

- **RouteTree**: 2.6x faster than regex matching
- **Optimizations**: Exact routes O(1), LRU cache, selective caching
- **Tested**: Benchmark validated

## Architecture

```
CEF Browser Request
    ↓
ApiCefRequestHandler
  ├─ URL Filtering (return null if not whitelisted)
  ├─ Route Matching (return null if no match)
  └─ Matched? → ApiResourceRequestHandler
                    ↓
        beforeHandle Interceptors (logging, auth, metrics)
                    ↓
        Route Handler
          ├─ Extract typed parameters (query, path, body)
          ├─ Call service.handleXxx(params..., browser, frame, cefRequest)
          └─ Service returns ApiResponse<T>
                    ↓
        afterHandle Interceptors (logging, metrics)
                    ↓
        Exception? → ExceptionHandler → ApiResponse
                    ↓
        ApiResponseHandler → CEF Response
```

**Key Principles:**
- ✅ **return null** = browser handles URL (external sites, unmapped routes)
- ✅ **Interceptors** = only for matched routes (logging, auth, metrics)
- ✅ **Exception Handler** = converts all exceptions to HTTP responses
- ✅ **Type Safety** = all parameters typed and extracted automatically

## Testing

For comprehensive testing examples and best practices, see [TESTING.md](TESTING.md).

The testing guide covers:
- Unit tests for all components (186 tests, 100% pass rate)
- Integration tests for full request/response cycle
- JMH benchmarks for performance validation
- MockCefFactory for easy test setup
- 90%+ code coverage

## Documentation

- **[README.md](README.md)** - Main documentation (this file)
- **[TESTING.md](TESTING.md)** - Comprehensive testing guide
- **[CHANGELOG.md](CHANGELOG.md)** - Version history and release notes
- **[MIGRATION.md](MIGRATION.md)** - Migration guides between versions
- **[examples/cef-java/README.md](examples/cef-java/README.md)** - Java example project
- **[examples/cef-kotlin/README.md](examples/cef-kotlin/README.md)** - Kotlin example project

## Version

**Current version: 3.0.0** (2026-01-12)

**Key Features:**
- Type-safe parameters (query, path, body)
- Request/Response interceptors
- Exception handler
- CORS support
- 186 tests with 100% pass rate

See [CHANGELOG.md](CHANGELOG.md) for detailed version history.

**Upgrading from 1.0.x?** See [MIGRATION.md](MIGRATION.md).

## License

MIT License

## Contributing

Contributions welcome! Please open an issue or PR.

For detailed changelog, see [CHANGELOG.md](CHANGELOG.md).
