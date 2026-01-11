# Migration Guide

Guide for migrating between major versions of CEF OpenAPI Generator.

---

## Migrating from 1.0.x to 1.1.0

Version 1.1.0 introduces significant improvements with breaking changes to service method signatures.

### Breaking Changes

#### 1. Service Wrapper Method Signatures

Wrapper methods now receive typed parameters instead of ApiRequest.

**Before (v1.0.x):**
```java
@Override
public ApiResponse<Task> handleGetTask(ApiRequest request) {
    String taskId = request.getPathVariable("taskId");
    Task task = getTask(taskId);
    return ApiResponse.ok(task);
}

@Override
public ApiResponse<TaskListResponse> handleListTasks(ApiRequest request) {
    String status = request.getQueryParam("status");
    Integer page = request.getQueryParam("page") != null
        ? Integer.parseInt(request.getQueryParam("page"))
        : null;

    TaskListResponse result = listTasks(status, page, size);
    return ApiResponse.ok(result);
}
```

**After (v1.1.0):**
```java
@Override
public ApiResponse<Task> handleGetTask(
    String taskId,         // ✅ Already extracted and typed!
    CefBrowser browser,    // Direct CEF access
    CefFrame frame,
    CefRequest cefRequest
) {
    Task task = getTask(taskId);
    return ApiResponse.ok(task);
}

@Override
public ApiResponse<TaskListResponse> handleListTasks(
    String status,        // ✅ Query param (already extracted!)
    Integer page,         // ✅ Query param (auto-parsed to Integer!)
    Integer size,         // ✅ Query param (auto-parsed to Integer!)
    CefBrowser browser,
    CefFrame frame,
    CefRequest cefRequest
) {
    TaskListResponse result = listTasks(status, page, size);
    return ApiResponse.ok(result);
}
```

**Benefits:**
- ✅ No manual extraction needed
- ✅ Type safety (Integer vs String)
- ✅ Auto-parsing for numeric types
- ✅ Cleaner code
- ✅ Direct CEF object access

#### 2. Builder Constructor

Direct constructor usage changed (use builder pattern instead).

**Before:**
```java
new ApiCefRequestHandler(project, routeTree, urlPrefixes, corsAllowedOrigins);
```

**After:**
```java
new ApiCefRequestHandler(project, routeTree, urlPrefixes, interceptors, exceptionHandler);
```

**Recommended:** Use builder pattern (no breaking changes):
```java
// Works in both versions
ApiCefRequestHandler handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withCors()
    .build();
```

#### 3. CORS Implementation

CORS now implemented via CorsInterceptor (internal change, API unchanged).

**No code changes needed** - `.withCors()` works the same way.

### Migration Steps

#### Step 1: Update Dependency

**build.gradle.kts:**
```kotlin
buildscript {
    dependencies {
        classpath("io.github.cef:generator:1.1.0")  // Update version
    }
}
```

Or if using mavenLocal:
```bash
cd cef-openapi-generator
./gradlew :generator:publishToMavenLocal
```

#### Step 2: Regenerate Code

```bash
./gradlew clean generateApi
```

This will regenerate all service interfaces with new typed parameter signatures.

#### Step 3: Update Service Implementations

For each overridden wrapper method, update the signature:

**Example 1: Simple parameter extraction**
```java
// OLD
public ApiResponse<Task> handleGetTask(ApiRequest request) {
    String taskId = request.getPathVariable("taskId");
    // ...
}

// NEW
public ApiResponse<Task> handleGetTask(
    String taskId,  // Parameter now in signature
    CefBrowser browser, CefFrame frame, CefRequest cefRequest
) {
    // taskId already available, no extraction needed!
    // ...
}
```

**Example 2: Query parameters**
```java
// OLD
public ApiResponse<TaskListResponse> handleListTasks(ApiRequest request) {
    String status = request.getQueryParam("status");
    Integer page = request.getQueryParam("page") != null
        ? Integer.parseInt(request.getQueryParam("page")) : null;
    // ...
}

// NEW
public ApiResponse<TaskListResponse> handleListTasks(
    String status,   // Already extracted
    Integer page,    // Already parsed to Integer!
    Integer size,    // Already parsed to Integer!
    CefBrowser browser, CefFrame frame, CefRequest cefRequest
) {
    // All parameters ready to use, no extraction or parsing needed!
    // ...
}
```

**Example 3: Request body**
```java
// OLD
public ApiResponse<Task> handleCreateTask(ApiRequest request) {
    CreateTaskRequest body = request.getBody(CreateTaskRequest.class);
    // ...
}

// NEW
public ApiResponse<Task> handleCreateTask(
    CreateTaskRequest body,  // Already deserialized!
    CefBrowser browser, CefFrame frame, CefRequest cefRequest
) {
    // body already typed and ready to use
    // ...
}
```

**Example 4: CEF browser access**
```java
// OLD
public ApiResponse<Void> handleNotify(ApiRequest request) {
    CefBrowser browser = request.getCefBrowser();
    NotifyRequest body = request.getBody(NotifyRequest.class);
    browser.executeJavaScript(...);
    // ...
}

// NEW
public ApiResponse<Void> handleNotify(
    NotifyRequest body,      // Typed body
    CefBrowser browser,      // Direct parameter!
    CefFrame frame,
    CefRequest cefRequest
) {
    // Direct browser access, no request.getCefBrowser() needed!
    browser.executeJavaScript(...);
    // ...
}
```

#### Step 4: Update Custom Routes (if any)

Custom routes still use `Function<ApiRequest, ApiResponse<?>>` - **no changes needed**.

```java
// Still works the same
.withRoute("/custom/{id}", HttpMethod.GET, request -> {
    String id = request.getPathVariable("id");
    return ApiResponse.ok("Custom: " + id);
})
```

#### Step 5: Test Your Implementation

```bash
./gradlew test
```

Ensure all service implementations compile and tests pass.

### Optional: Use New Features

#### Add Logging Interceptor

```java
public class LoggingInterceptor implements RequestInterceptor {
    @Override
    public void beforeHandle(CefRequest request, Map<String, String> pathVars) {
        System.out.println("→ " + request.getMethod() + " " + request.getURL());
    }

    @Override
    public void afterHandle(ApiResponse<?> response, long durationMs) {
        System.out.println("← " + response.getStatusCode() + " (" + durationMs + "ms)");
    }

    @Override
    public void onError(Exception e, CefRequest request) {
        System.err.println("✗ Error: " + e.getMessage());
    }
}

// Register
ApiCefRequestHandler handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withInterceptor(new LoggingInterceptor())
    .build();
```

#### Add Custom Exception Handler

```java
ApiCefRequestHandler handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withExceptionHandler((exception, request) -> {
        // Log to monitoring service
        logger.error("API error for " + request.getURL(), exception);

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

### Common Issues & Solutions

#### Issue 1: Compilation Error - Method Signature Mismatch

**Error:**
```
method handleXxx in interface XxxApiService cannot be applied to given types
```

**Solution:** Update wrapper method signature to include typed parameters:
```java
// Add parameters before CefBrowser, CefFrame, CefRequest
public ApiResponse<T> handleXxx(
    String pathParam,
    Integer queryParam,
    BodyType body,
    CefBrowser browser,
    CefFrame frame,
    CefRequest cefRequest
)
```

#### Issue 2: Cannot Access ApiRequest

**Error:**
```
request.getPathVariable("id") not accessible
```

**Solution:** Use method parameters directly:
```java
// OLD
String id = request.getPathVariable("id");

// NEW
// id is already a method parameter!
public ApiResponse<Task> handleGetTask(String id, ...) {
    // Use 'id' directly
}
```

#### Issue 3: Query Parameter Parsing

**Error:**
```
Cannot parse query parameter to Integer
```

**Solution:** Generator now auto-parses. Just use the typed parameter:
```java
// OLD
String pageStr = request.getQueryParam("page");
Integer page = pageStr != null ? Integer.parseInt(pageStr) : null;

// NEW
public TaskListResponse listTasks(Integer page, ...) {
    // page is already Integer, no parsing needed!
    int p = page != null ? page : 0;
}
```

### Deprecated Features

- **ApiRequest in service methods**: Now only used internally for routing
  - Still available for custom routes
  - Service methods receive typed parameters directly

### New Recommended Patterns

#### Pattern 1: Simple Business Logic
```java
@Override
public Task getTask(String taskId) {
    return repository.findById(taskId);
}
```

#### Pattern 2: Custom Response
```java
@Override
public ApiResponse<Task> handleCreateTask(
    CreateTaskRequest body,
    CefBrowser browser,
    CefFrame frame,
    CefRequest cefRequest
) {
    Task task = createTask(body);
    return ApiResponse.created(task)
        .header("Location", "/api/tasks/" + task.getId());
}
```

#### Pattern 3: CEF Browser Interaction
```java
@Override
public ApiResponse<Void> handleNotify(
    NotifyRequest body,
    CefBrowser browser,  // Direct access!
    CefFrame frame,
    CefRequest cefRequest
) {
    browser.executeJavaScript("alert('" + body.getMessage() + "')", "", 0);
    return ApiResponse.ok();
}
```

### Rollback Plan

If migration issues occur:

1. **Revert to 1.0.5:**
   ```kotlin
   classpath("io.github.cef:generator:1.0.5")
   ```

2. **Regenerate:**
   ```bash
   ./gradlew clean generateApi
   ```

3. **Report issue:**
   https://github.com/a-havrysh/cef-openapi-generator/issues

### Support

- **Documentation**: [README.md](README.md)
- **Testing Guide**: [TESTING.md](TESTING.md)
- **Changelog**: [CHANGELOG.md](CHANGELOG.md)
- **Issues**: https://github.com/a-havrysh/cef-openapi-generator/issues

---

## Migrating from 1.x to 2.0.0

Version 2.0.0 introduces OpenAPI parameter validation, type-specific exception handling, and API improvements with breaking changes.

### Breaking Changes

#### 1. RequestInterceptor Interface Changes

**beforeHandle() and onError() signatures changed:**

**Before (v1.x):**
```java
public class MyInterceptor implements RequestInterceptor {
    @Override
    public void beforeHandle(CefRequest request, Map<String, String> pathVariables) {
        String url = request.getURL();
        String userId = pathVariables.get("userId");
    }

    @Override
    public void onError(Exception e, CefRequest request) {
        System.err.println("Error: " + e.getMessage());
    }
}
```

**After (v2.0.0):**
```java
public class MyInterceptor implements RequestInterceptor {
    @Override
    public void beforeHandle(ApiRequest request) {  // Simplified signature
        String url = request.getPath();
        String userId = request.getPathVariable("userId");  // Access from request
        String queryParam = request.getQueryParam("name");   // Better access
    }

    @Override
    public void onError(Exception e, ApiRequest request) {  // ApiRequest instead of CefRequest
        System.err.println("Error: " + e.getMessage());
    }
}
```

**Changes:**
- `CefRequest` → `ApiRequest` (better access to query params, headers, body, path variables)
- Removed `pathVariables` parameter from `beforeHandle()` - use `request.getPathVariable(name)` instead
- `onError()` also uses `ApiRequest`

**Benefits:**
- Cleaner API - one source for all request data
- Better access to query params and body in interceptors
- No redundant pathVariables parameter

#### 2. Exception Classes No Longer Final

**Before (v1.x):**
```java
public final class BadRequestException extends ApiException { }
public final class NotFoundException extends ApiException { }
public final class InternalServerErrorException extends ApiException { }
```

**After (v2.0.0):**
```java
public class BadRequestException extends ApiException { }  // Can extend
public class NotFoundException extends ApiException { }     // Can extend
public class InternalServerErrorException extends ApiException { }  // Can extend
```

**Impact:** Can now create custom exception types (e.g., ValidationException extends BadRequestException).

**No action required** - backward compatible change.

#### 3. Exception Handling Behavior

**beforeHandle() exceptions now trigger onError() callbacks:**

**Before (v1.x):**
- beforeHandle exception → immediately handled, onError NOT called

**After (v2.0.0):**
- beforeHandle exception → onError called for all interceptors → ExceptionHandler processes

**Impact:** onError() now called for ALL exceptions (beforeHandle, handler, afterHandle).

**Action:** If your interceptor relies on onError() NOT being called for beforeHandle exceptions, update logic.

### New Features (Optional)

#### OpenAPI Parameter Validation

Enable automatic validation:

```java
ApiCefRequestHandler handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withValidation()  // NEW: Enable OpenAPI validation
    .build();
```

Add constraints to openapi.yaml:
```yaml
parameters:
  - name: page
    in: query
    schema:
      type: integer
      minimum: 1
      maximum: 1000
```

#### Type-Specific Exception Handlers

Register different handlers for different exception types:

```java
ApiCefRequestHandler handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withExceptionHandler(ValidationException.class, (ex, req) -> {
        ErrorResponse err = new ErrorResponse();
        err.setError("Validation");
        err.setDetails(ex.getErrors());
        return ApiResponse.badRequest(err);
    })
    .withExceptionHandler(ApiException.class, (ex, req) -> {
        return ApiResponse.status(ex.getStatusCode(), ex.getMessage());
    })
    .withExceptionHandler(Exception.class, (ex, req) -> {
        logger.error("Unexpected error", ex);
        return ApiResponse.internalServerError("Server error");
    })
    .build();
```

#### Jackson @JsonProperty Annotations

DTO fields now annotated for proper JSON mapping:

```java
public final class NotifyRequest {
    @JsonProperty("user-id")    // Original OpenAPI name
    private String userId;       // Java-safe field name
}
```

Handles special characters in field names automatically.

#### Model Naming Options

Add suffix or prefix to all model names:

```gradle
configOptions.set(mapOf(
    "modelSuffix" to "Dto"  // Task → TaskDto
))
```

### Migration Steps

#### Step 1: Update Dependency

```kotlin
buildscript {
    dependencies {
        classpath("io.github.cef:generator:2.0.0")  // Update to 2.0.0
    }
}
```

#### Step 2: Regenerate Code

```bash
./gradlew clean generateApi
```

This regenerates with new signatures and validation support.

#### Step 3: Update Custom Interceptors

If you have custom interceptors, update signatures:

```java
// Update method signature
public void beforeHandle(ApiRequest request) {  // Remove pathVariables param
    String id = request.getPathVariable("userId");  // Access from request
}

public void onError(Exception e, ApiRequest request) {  // CefRequest → ApiRequest
    String path = request.getPath();  // Use ApiRequest methods
}
```

#### Step 4: Test

```bash
./gradlew compileJava test
```

### Rollback Plan

If migration issues occur:

1. **Revert to 1.1.0:**
   ```kotlin
   classpath("io.github.cef:generator:1.1.0")
   ```

2. **Regenerate:**
   ```bash
   ./gradlew clean generateApi
   ```

3. **Report issue:**
   https://github.com/a-havrysh/cef-openapi-generator/issues

---

## Future Migrations

This section will be updated with guides for future version migrations.
