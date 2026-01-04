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
        classpath("io.github.cef:generator:1.0.0")
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
        classpath("io.github.cef:generator:1.0.0")
    }
}
```

## Quick Start

### Configure Generation

```gradle
val generateApi by tasks.registering(org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("cef")
    inputSpec.set("$projectDir/openapi.yaml")

    // Templates are included in JAR, no need to specify templateDir

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

// With custom routes
var handler = ApiCefRequestHandler.builder(project)
    .withApiRoutes()
    .withPrefix("/static", request -> {
        byte[] data = readStaticFile(request.getPath());
        return ApiResponse.ok(data, ContentTypeResolver.resolve(request.getPath()));
    })
    .withExact("/health", request -> ApiResponse.ok("OK"))
    .withFallback(request -> {
        // Fallback for unmatched routes (e.g., static files)
        String path = request.getPath().substring(1);
        byte[] data = readResource(path);
        return ApiResponse.ok(data, ContentTypeResolver.resolve(path));
    })
    .build();
```

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
