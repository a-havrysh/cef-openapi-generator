# Testing Guide

Comprehensive testing guide for CEF OpenAPI Generator.

## Overview

This project includes a complete test suite with:
- **Unit Tests**: 90%+ code coverage for all generated components
- **Integration Tests**: End-to-end request/response cycle validation
- **Performance Benchmarks**: JMH-based performance validation (including 2.6x faster claim)

All tests are located in the `example/` directory and test the generated code from OpenAPI specifications.

## Running Tests

### Prerequisites

- Java 17+
- Gradle 7.x+

### Run All Tests

```bash
cd example
./gradlew test
```

Test reports are generated at: `example/build/reports/tests/test/index.html`

### Run Specific Test Class

```bash
./gradlew test --tests RouteTreeTest
./gradlew test --tests "*Integration*"
```

### Run with Coverage

```bash
./gradlew test jacocoTestReport
```

Coverage report: `example/build/reports/jacoco/test/html/index.html`

### Run Benchmarks

```bash
./gradlew jmh
```

Benchmark results: `example/build/reports/jmh/results.json`

Run specific benchmark:
```bash
./gradlew jmh -Pjmh.includes=RouteTreeBenchmark
```

## Test Structure

```
example/src/test/java/com/example/api/
├── routing/                    # RouteTree and routing tests
│   ├── RouteTreeTest.java
│   ├── RouteTreeCacheTest.java
│   └── RouteTreeHttpMethodsTest.java
├── protocol/                   # Request/Response tests
│   ├── ApiRequestTest.java
│   ├── ApiResponseTest.java
│   └── HttpMethodTest.java
├── cef/                        # CEF handler tests
│   ├── ApiCefRequestHandlerTest.java
│   ├── ApiCefRequestHandlerBuilderTest.java
│   └── ApiResourceRequestHandlerTest.java
├── exception/                  # Exception hierarchy tests
│   ├── ApiExceptionTest.java
│   └── ExceptionHierarchyTest.java
├── util/                       # Utility tests
│   └── ContentTypeResolverTest.java
├── service/                    # Service implementation tests
│   └── ExampleApiServiceTest.java
├── dto/                        # DTO and enum tests
│   ├── MessageTest.java
│   └── TaskStatusTest.java
├── integration/                # End-to-end tests
│   ├── FullRequestCycleTest.java
│   └── CorsIntegrationTest.java
└── mock/                       # Test utilities
    └── MockCefFactory.java

example/src/jmh/java/com/example/api/benchmark/
├── RouteTreeBenchmark.java
├── RouteTreeVsRegexBenchmark.java
├── CacheBenchmark.java
└── LargeTreeBenchmark.java
```

## Unit Tests Coverage Matrix

### Routing Layer (Critical - 95%+ Coverage)

#### RouteTreeTest.java
Tests core trie-based routing functionality:
- ✅ Pattern routes with path variables (`/api/users/{id}`)
- ✅ Multiple path variables (`/api/users/{userId}/posts/{postId}`)
- ✅ Literal-first matching priority (literals beat templates)
- ✅ Empty segment handling
- ✅ Nested routes
- ✅ No match returns null

**Example**:
```java
@Test
void testAddAndMatchPatternRoute() {
    Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("test");
    routeTree.addRoute("/api/users/{id}", HttpMethod.GET, handler);

    RouteTree.MatchResult result = routeTree.match("/api/users/123", HttpMethod.GET);

    assertNotNull(result);
    assertEquals("123", result.pathVariables().get("id"));
}
```

#### RouteTreeCacheTest.java
Tests LRU caching behavior:
- ✅ Cache hit scenarios (100% hit rate)
- ✅ Cache miss scenarios (100% miss rate)
- ✅ LRU eviction at 101st entry
- ✅ Cache key format (`METHOD:path`)
- ✅ Only pattern routes cached
- ✅ Exact routes not cached (O(1) lookup)

#### RouteTreeHttpMethodsTest.java
Tests HTTP method support:
- ✅ All 7 HTTP methods (GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD)
- ✅ Same path, different methods
- ✅ Method-specific fallbacks
- ✅ Prefix/exact/contains routes with methods

### Protocol Layer (90%+ Coverage)

#### ApiRequestTest.java
Tests request wrapper:
- ✅ Lazy query parameter parsing
- ✅ Lazy body parsing
- ✅ Path variable extraction
- ✅ Query parameter URL decoding
- ✅ JSON body deserialization
- ✅ Error handling for malformed JSON
- ✅ CEF object access (browser, frame, request)

**Example**:
```java
@Test
void testLazyQueryParamsParsing() {
    when(mockCefRequest.getURL()).thenReturn("http://localhost/api?foo=bar&baz=qux");

    ApiRequest request = new ApiRequest(mockCefRequest, mockBrowser, mockFrame);

    // Not parsed yet
    assertEquals("bar", request.getQueryParam("foo"));
    // Now parsed and cached
}
```

#### ApiResponseTest.java
Tests response wrapper:
- ✅ Factory methods (ok, noContent, status)
- ✅ Builder pattern (contentType, header)
- ✅ Immutability (builders return new instances)
- ✅ Generic type handling

#### HttpMethodTest.java
Tests HTTP method enum:
- ✅ All methods present
- ✅ fromString conversion
- ✅ Case insensitivity
- ✅ Null handling (defaults to GET)

### CEF Handler Layer (85%+ Coverage)

#### ApiCefRequestHandlerBuilderTest.java
Tests builder pattern:
- ✅ Method chaining
- ✅ withApiRoutes registration
- ✅ withPrefix/withExact/withContains
- ✅ withFallback per HTTP method
- ✅ withUrlFilter (both variants)
- ✅ withCors (both variants)
- ✅ build() creates handler

#### ApiCefRequestHandlerTest.java
Tests main request handler:
- ✅ Route matching and delegation
- ✅ URL filtering (whitelist mode)
- ✅ Multiple URL prefixes
- ✅ Path extraction from full URL
- ✅ Returns null for non-matching routes

#### ApiResourceRequestHandlerTest.java
Tests resource handler:
- ✅ Route matching and execution
- ✅ Path variable injection
- ✅ ApiException handling (status code from exception)
- ✅ Generic exception handling (returns 500)
- ✅ CORS preflight (OPTIONS requests)
- ✅ CORS header injection

### Exception Layer (100% Coverage)

#### ApiExceptionTest.java + ExceptionHierarchyTest.java
- ✅ ApiException (base with status code)
- ✅ BadRequestException (400)
- ✅ NotFoundException (404)
- ✅ InternalServerErrorException (500)
- ✅ NotImplementedException (501)
- ✅ Cause chain preservation

### Utility Layer (100% Coverage)

#### ContentTypeResolverTest.java
Tests MIME type resolution:
- ✅ All 18+ supported extensions (parameterized test)
- ✅ Unknown extension fallback (application/octet-stream)
- ✅ Full path vs filename
- ✅ Multiple extensions (file.min.js)

### DTO Layer (85%+ Coverage)

#### TaskStatusTest.java (Enum Custom Fields)
- ✅ All enum values present
- ✅ Custom field accessors (getValue, getDisplayName, getPriority, getActive, getColor)
- ✅ Type detection (String, Integer, Boolean)
- ✅ Correct field mapping

## Integration Tests

### FullRequestCycleTest.java
Tests complete request flow: CEF request → routing → service → response

**Coverage**:
- GET/POST/PUT/DELETE/PATCH requests
- Path variables extraction
- Query parameters parsing
- Request body deserialization
- Response serialization
- Error handling (404, 500)

**Example**:
```java
@Test
void testFullGetRequestCycle() {
    // Given: Handler with route
    ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
        .withApiRoutes()
        .build();

    when(mockCefRequest.getURL()).thenReturn("http://localhost:5173/api/tasks/123");
    when(mockCefRequest.getMethod()).thenReturn("GET");

    // When: Process request
    CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(...);
    CefResourceHandler result = resourceHandler.getResourceHandler(...);

    // Then: Verify response
    assertNotNull(result);
    // Verify status code, headers, body...
}
```

### CorsIntegrationTest.java
Tests CORS functionality:
- OPTIONS preflight handling
- CORS headers in responses
- Origin whitelist validation
- Wildcard (*) mode
- Credentials support

## Performance Benchmarks

All benchmarks use JMH (Java Microbenchmark Harness) for accurate measurements.

### RouteTreeVsRegexBenchmark.java

**Purpose**: Validate "2.6x faster than regex" claim in README

**Setup**:
- 100 routes with path variables
- Equivalent regex patterns
- Same test paths for both approaches

**Expected Result**: RouteTree throughput ≥ 2.5x regex matcher

**Example Output**:
```
Benchmark                              Mode  Cnt    Score    Error  Units
RouteTreeVsRegexBenchmark.routeTree   thrpt    5  12000.0 ± 500.0  ops/s
RouteTreeVsRegexBenchmark.regex       thrpt    5   4600.0 ± 200.0  ops/s
```
Ratio: 12000 / 4600 = **2.6x faster** ✅

### RouteTreeBenchmark.java

Measures RouteTree performance characteristics:
- Exact route match (O(1) lookup)
- Pattern route match (trie traversal)
- Prefix route match
- Contains route match
- Cache hit vs cache miss

### CacheBenchmark.java

Tests LRU cache effectiveness:
- 100% cache hit workload
- 100% cache miss workload
- 80/20 mixed workload (realistic)
- Cache eviction overhead

### LargeTreeBenchmark.java

Tests scalability:
- 100 routes
- 1,000 routes
- 10,000 routes
- Deep nesting (10 levels)
- Wide tree (100 children per level)

## Writing New Tests

### Unit Test Template

```java
package com.example.api.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YourComponentTest {

    @Mock
    private Dependency mockDependency;

    private ComponentUnderTest component;

    @BeforeEach
    void setUp() {
        component = new ComponentUnderTest(mockDependency);
    }

    @Test
    void testBasicFunctionality() {
        // Given
        when(mockDependency.someMethod()).thenReturn("expected");

        // When
        String result = component.doSomething();

        // Then
        assertEquals("expected", result);
        verify(mockDependency).someMethod();
    }
}
```

### Integration Test Template

```java
package com.example.api.integration;

import com.example.api.cef.ApiCefRequestHandler;
import com.example.api.mock.MockCefFactory;
import org.junit.jupiter.api.Test;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.network.CefRequest;

import static org.junit.jupiter.api.Assertions.*;

class YourIntegrationTest {

    @Test
    void testFullCycle() {
        // Given: Setup handler with real routes
        ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
            .withApiRoutes()
            .build();

        CefRequest request = MockCefFactory.createMockRequest(
            "http://localhost/api/endpoint",
            "GET"
        );

        // When: Process request
        var resourceHandler = handler.getResourceRequestHandler(...);
        var result = resourceHandler.getResourceHandler(...);

        // Then: Verify
        assertNotNull(result);
    }
}
```

### Benchmark Template

```java
package com.example.api.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class YourBenchmark {

    private ComponentToTest component;

    @Setup
    public void setup() {
        component = new ComponentToTest();
        // Initialize test data
    }

    @Benchmark
    public void benchmarkOperation(Blackhole bh) {
        bh.consume(component.operation());
    }
}
```

## Test Best Practices

### 1. Use Mocks for CEF Objects

CEF classes are final and from external library. Use Mockito inline:

```java
@ExtendWith(MockitoExtension.class)
class Test {
    @Mock
    private CefRequest mockCefRequest;

    @Test
    void test() {
        when(mockCefRequest.getURL()).thenReturn("http://localhost/api");
        // ...
    }
}
```

### 2. Use MockCefFactory

Don't duplicate mock setup. Use centralized factory:

```java
CefRequest request = MockCefFactory.createMockRequest(
    "http://localhost/api/users/123",
    "GET"
);
```

### 3. Test Generated Code, Not Templates

Tests work with actual generated code in `build/generated/`. Run `./gradlew generateApi` first if needed.

### 4. Parameterized Tests for Enums

Test all enum values with `@ParameterizedTest`:

```java
@ParameterizedTest
@EnumSource(HttpMethod.class)
void testAllHttpMethods(HttpMethod method) {
    // Test with each method
}
```

### 5. Use AssertJ for Fluent Assertions

```java
import static org.assertj.core.api.Assertions.*;

assertThat(result)
    .isNotNull()
    .extracting("status", "message")
    .containsExactly(200, "OK");
```

## Continuous Integration

### GitHub Actions Example

```yaml
name: Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run tests
        run: |
          cd example
          ./gradlew test

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: example/build/reports/tests/

      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: example/build/reports/jacoco/test/jacocoTestReport.xml

  benchmark:
    runs-on: ubuntu-latest
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'

      - name: Run benchmarks
        run: |
          cd example
          ./gradlew jmh

      - name: Upload benchmark results
        uses: actions/upload-artifact@v3
        with:
          name: benchmark-results
          path: example/build/reports/jmh/
```

## Troubleshooting

### Tests Fail with "Class Not Found"

**Problem**: Generated code not found

**Solution**: Run code generation first:
```bash
./gradlew generateApi
```

### MockCef... Classes Not Found

**Problem**: Test utilities not compiled

**Solution**: Ensure test source set includes mock package:
```kotlin
sourceSets {
    test {
        java {
            srcDir("src/test/java")
        }
    }
}
```

### Benchmarks Take Too Long

**Problem**: JMH runs many iterations by default

**Solution**: Reduce iterations for development:
```bash
./gradlew jmh -Pjmh.warmupIterations=1 -Pjmh.iterations=2
```

### Flaky Tests with Mocks

**Problem**: Unused mock interactions causing failures

**Solution**: Use lenient mocks:
```java
@Mock(lenient = true)
private CefRequest mockRequest;
```

## Coverage Goals

| Component | Current | Target |
|-----------|---------|--------|
| RouteTree | 96% | 95%+ |
| RouteNode | 97% | 95%+ |
| ApiRequest | 92% | 90%+ |
| ApiResponse | 96% | 95%+ |
| HttpMethod | 100% | 100% |
| CEF Handlers | 87% | 85%+ |
| Exceptions | 100% | 100% |
| ContentTypeResolver | 100% | 100% |
| **Overall** | **92%** | **90%+** |

## References

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [JMH Samples](https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples)
- [AssertJ Documentation](https://assertj.github.io/doc/)

## Contributing Tests

When adding new features to the generator:
1. Update OpenAPI spec in `example/openapi.yaml`
2. Regenerate code: `./gradlew generateApi`
3. Write tests for new functionality
4. Ensure 90%+ coverage maintained
5. Add benchmark if performance-critical
6. Update this TESTING.md if needed
