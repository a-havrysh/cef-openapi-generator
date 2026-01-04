# CEF OpenAPI Generator

OpenAPI Code Generator for Chromium Embedded Framework (CEF) with JetBrains Platform integration.

## Features

- **RouteTree Routing**: Trie-based routing, 2.6x faster than regex
- **ApiResponse<T>**: Generic response wrapper (like Spring ResponseEntity)
- **Builder Pattern**: Fluent API for custom routes
- **Type-Safe**: No unchecked casts
- **Zero Dependencies**: No Lombok, Swagger, or Spring
- **Java 8+ Compatible**: Works with legacy codebases
- **Extensible**: Add prefix/exact/contains routes alongside API routes

## Quick Start

### 1. Add Generator Dependency

```gradle
buildscript {
    dependencies {
        classpath("io.github.cef:cef-openapi-generator:1.0.0")
    }
}

plugins {
    id("org.openapi.generator") version "7.18.0"
}
```

### 2. Configure Generation

```gradle
val generateApi by tasks.registering(org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("cef")
    inputSpec.set("$projectDir/openapi.yaml")
    templateDir.set("path/to/templates")  // Or use packaged templates

    modelPackage.set("com.example.api.dto")
    apiPackage.set("com.example.api")
}
```

### 3. Use Generated Code

```java
// Simple usage
var handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .build();

browser.addRequestHandler(handler, browser.getCefBrowser());

// With custom routes
var handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withPrefix("/static", request -> {
        byte[] data = readStaticFile(request.getPath());
        return ApiResponse.ok(data, ContentTypeResolver.resolve(request.getPath()));
    })
    .withExact("/health", request -> ApiResponse.ok("OK"))
    .build();
```

### 4. Implement Services

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

## License

MIT License

## Contributing

Contributions welcome! Please open an issue or PR.
