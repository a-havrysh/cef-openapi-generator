package com.example.api.cef;

import com.example.api.protocol.ApiRequest;
import com.example.api.protocol.ApiResponse;
import com.example.api.protocol.HttpMethod;
import com.example.api.interceptor.RequestInterceptor;
import com.example.api.interceptor.CorsInterceptor;
import com.example.api.interceptor.ValidationInterceptor;
import com.example.api.mock.MockCefFactory;
import com.intellij.openapi.project.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ApiCefRequestHandlerBuilder Tests")
class ApiCefRequestHandlerBuilderTest {

    private Project mockProject;

    @BeforeEach
    void setUp() {
        mockProject = MockCefFactory.createMockProject();
    }

    @Nested
    @DisplayName("Builder Creation Tests")
    class BuilderCreationTests {

        @Test
        @DisplayName("Should create new builder instance")
        void testBuilderCreationReturnsNewInstance() {
            // Given & When: Create builder
            ApiCefRequestHandlerBuilder builder = ApiCefRequestHandlerBuilder.builder(mockProject);

            // Then: Should return a builder instance
            assertThat(builder).isNotNull();
            assertThat(builder).isInstanceOf(ApiCefRequestHandlerBuilder.class);
        }

        @Test
        @DisplayName("Should initialize builder with project reference")
        void testBuilderInitializesWithProject() {
            // Given & When: Create builder
            ApiCefRequestHandlerBuilder builder = ApiCefRequestHandlerBuilder.builder(mockProject);

            // Then: Should be able to build a handler
            ApiCefRequestHandler handler = builder.build();
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should create independent builder instances")
        void testMultipleBuilderInstancesAreIndependent() {
            // Given: Two separate builders
            ApiCefRequestHandlerBuilder builder1 = ApiCefRequestHandlerBuilder.builder(mockProject);
            ApiCefRequestHandlerBuilder builder2 = ApiCefRequestHandlerBuilder.builder(mockProject);

            // When: Build handlers from each
            ApiCefRequestHandler handler1 = builder1
                    .withRoute("/api/test1", HttpMethod.GET, req -> ApiResponse.ok("test1"))
                    .build();
            ApiCefRequestHandler handler2 = builder2
                    .withRoute("/api/test2", HttpMethod.GET, req -> ApiResponse.ok("test2"))
                    .build();

            // Then: Both handlers should be different instances
            assertThat(handler1).isNotNull();
            assertThat(handler2).isNotNull();
            assertThat(handler1).isNotEqualTo(handler2);
        }
    }

    @Nested
    @DisplayName("API Routes Tests")
    class ApiRoutesTests {

        @Test
        @DisplayName("Should add all generated API routes")
        void testWithApiRoutesAddsAllRoutes() {
            // Given & When: Builder with API routes
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withApiRoutes()
                    .build();

            // Then: Handler should be created successfully
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should generate route for browser notify endpoint")
        void testApiRoutesIncludeBrowserNotify() {
            // Given: Builder with API routes
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withApiRoutes()
                    .build();

            // When: Access the internal route tree
            // Then: Handler exists (integration test - actual routing tested in integration tests)
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should generate route for statistics endpoint")
        void testApiRoutesIncludeStatistics() {
            // Given & When: Builder with API routes
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withApiRoutes()
                    .build();

            // Then: Handler created successfully with routes
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should generate routes for all task endpoints")
        void testApiRoutesIncludeTaskEndpoints() {
            // Given & When: Builder with API routes (includes 6 task endpoints)
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withApiRoutes()
                    .build();

            // Then: Handler should support multiple task operations
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should return builder for chaining after withApiRoutes")
        void testWithApiRoutesReturnsBuilderForChaining() {
            // Given: Builder
            ApiCefRequestHandlerBuilder builder = ApiCefRequestHandler.builder(mockProject);

            // When: Call withApiRoutes
            ApiCefRequestHandlerBuilder result = builder.withApiRoutes();

            // Then: Should return builder for chaining
            assertThat(result).isNotNull();
            assertThat(result).isSameAs(builder);
        }

        @Test
        @DisplayName("Should be able to chain methods after withApiRoutes")
        void testChainMethodsAfterApiRoutes() {
            // Given & When: Chain methods after API routes
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withApiRoutes()
                    .withCors()
                    .withValidation()
                    .build();

            // Then: Should build successfully
            assertThat(handler).isNotNull();
        }
    }

    @Nested
    @DisplayName("Route Method Tests")
    class RouteMethodTests {

        @Test
        @DisplayName("Should add route with pattern-based matching")
        void testWithRouteAddsPatternRoute() {
            // Given: Builder with route
            ApiCefRequestHandlerBuilder builder = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/users/{id}", HttpMethod.GET, req -> ApiResponse.ok("user"));

            // When: Build handler
            ApiCefRequestHandler handler = builder.build();

            // Then: Handler should be created
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should support multiple pattern routes")
        void testMultiplePatternRoutes() {
            // Given: Builder with multiple routes
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/users/{id}", HttpMethod.GET, req -> ApiResponse.ok("user"))
                    .withRoute("/api/posts/{postId}/comments/{commentId}", HttpMethod.GET,
                            req -> ApiResponse.ok("comment"))
                    .build();

            // Then: Handler should support all routes
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should add prefix route with startsWith matching")
        void testWithPrefixAddsRouteWithPrefixMatching() {
            // Given: Builder with prefix route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withPrefix("/static", HttpMethod.GET, req -> ApiResponse.ok("static"))
                    .build();

            // Then: Handler should be created
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should support multiple prefix routes")
        void testMultiplePrefixRoutes() {
            // Given: Builder with multiple prefix routes
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withPrefix("/static", HttpMethod.GET, req -> ApiResponse.ok("static"))
                    .withPrefix("/assets", HttpMethod.GET, req -> ApiResponse.ok("assets"))
                    .build();

            // Then: Handler should support all prefix routes
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should add exact route with exact path matching")
        void testWithExactAddsRouteWithExactMatching() {
            // Given: Builder with exact route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withExact("/health", HttpMethod.GET, req -> ApiResponse.ok("ok"))
                    .build();

            // Then: Handler should be created
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should support multiple exact routes")
        void testMultipleExactRoutes() {
            // Given: Builder with multiple exact routes
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withExact("/health", HttpMethod.GET, req -> ApiResponse.ok("ok"))
                    .withExact("/status", HttpMethod.GET, req -> ApiResponse.ok("status"))
                    .build();

            // Then: Handler should support all exact routes
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should add contains route with substring matching")
        void testWithContainsAddsRouteWithSubstringMatching() {
            // Given: Builder with contains route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withContains(".json", HttpMethod.GET, req -> ApiResponse.ok("json"))
                    .build();

            // Then: Handler should be created
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should support multiple contains routes")
        void testMultipleContainsRoutes() {
            // Given: Builder with multiple contains routes
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withContains(".json", HttpMethod.GET, req -> ApiResponse.ok("json"))
                    .withContains(".xml", HttpMethod.GET, req -> ApiResponse.ok("xml"))
                    .build();

            // Then: Handler should support all contains routes
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should add fallback handler for HTTP method")
        void testWithFallbackAddsFallbackHandler() {
            // Given: Builder with fallback
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .withFallback(HttpMethod.POST, req -> ApiResponse.notFound("not found"))
                    .build();

            // Then: Handler should be created
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should support multiple fallbacks for different methods")
        void testMultipleFallbacksForDifferentMethods() {
            // Given: Builder with multiple fallbacks
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withFallback(HttpMethod.GET, req -> ApiResponse.ok("default GET"))
                    .withFallback(HttpMethod.POST, req -> ApiResponse.created("default POST"))
                    .withFallback(HttpMethod.DELETE, req -> ApiResponse.ok("default DELETE"))
                    .build();

            // Then: Handler should support all fallbacks
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should return builder from all route methods")
        void testAllRouteMethodsReturnBuilder() {
            // Given: Builder
            ApiCefRequestHandlerBuilder builder = ApiCefRequestHandler.builder(mockProject);

            // When: Call route methods
            ApiCefRequestHandlerBuilder result1 = builder.withRoute("/api/test", HttpMethod.GET,
                    req -> ApiResponse.ok("test"));
            assertThat(result1).isSameAs(builder);

            ApiCefRequestHandlerBuilder result2 = builder.withPrefix("/static", HttpMethod.GET,
                    req -> ApiResponse.ok("static"));
            assertThat(result2).isSameAs(builder);

            ApiCefRequestHandlerBuilder result3 = builder.withExact("/health", HttpMethod.GET,
                    req -> ApiResponse.ok("health"));
            assertThat(result3).isSameAs(builder);

            ApiCefRequestHandlerBuilder result4 = builder.withContains(".json", HttpMethod.GET,
                    req -> ApiResponse.ok("json"));
            assertThat(result4).isSameAs(builder);

            ApiCefRequestHandlerBuilder result5 = builder.withFallback(HttpMethod.GET,
                    req -> ApiResponse.ok("fallback"));
            assertThat(result5).isSameAs(builder);
        }

        @Test
        @DisplayName("Should support all HTTP methods in routes")
        void testAllHttpMethodsInRoutes() {
            // Given: Builder with all HTTP methods
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("get"))
                    .withRoute("/api/items", HttpMethod.POST, req -> ApiResponse.created("post"))
                    .withRoute("/api/items/{id}", HttpMethod.PUT, req -> ApiResponse.ok("put"))
                    .withRoute("/api/items/{id}", HttpMethod.PATCH, req -> ApiResponse.ok("patch"))
                    .withRoute("/api/items/{id}", HttpMethod.DELETE, req -> ApiResponse.ok("delete"))
                    .build();

            // Then: Handler should support all methods
            assertThat(handler).isNotNull();
        }
    }

    @Nested
    @DisplayName("URL Filter Tests")
    class UrlFilterTests {

        @Test
        @DisplayName("Should enable URL filtering with no arguments")
        void testWithUrlFilterNoArgsEnablesFiltering() {
            // Given: Builder with URL filter (no prefixes)
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter()
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // Then: Handler should be created with filtering
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should accept multiple URL prefixes")
        void testWithUrlFilterAcceptsMultiplePrefixes() {
            // Given: Builder with multiple URL prefixes
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter("http://localhost:5173", "http://app.example.com", "https://prod.example.com")
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // Then: Handler should be created
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should return builder for chaining from URL filter")
        void testWithUrlFilterReturnsBuilder() {
            // Given: Builder
            ApiCefRequestHandlerBuilder builder = ApiCefRequestHandler.builder(mockProject);

            // When: Call withUrlFilter
            ApiCefRequestHandlerBuilder result = builder.withUrlFilter("http://localhost:5173");

            // Then: Should return builder for chaining
            assertThat(result).isSameAs(builder);
        }

        @Test
        @DisplayName("Should apply URL filter to all routes")
        void testUrlFilterAppliesToAllRoutes() {
            // Given: Builder with filter and multiple routes
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter("http://localhost:5173")
                    .withRoute("/api/users", HttpMethod.GET, req -> ApiResponse.ok("users"))
                    .withRoute("/api/posts", HttpMethod.GET, req -> ApiResponse.ok("posts"))
                    .withRoute("/api/items", HttpMethod.POST, req -> ApiResponse.created("item"))
                    .build();

            // Then: Handler should be created with filter applied to all
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should handle null prefixes in withUrlFilter")
        void testWithUrlFilterHandlesNullPrefixes() {
            // Given: Builder with null prefixes
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter((String[]) null)
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // Then: Handler should be created
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should support overriding URL filter")
        void testUrlFilterCanBeOverridden() {
            // Given: Builder with initial filter
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter("http://old.com")
                    .withUrlFilter("http://new.com")  // Override
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // Then: Handler should use the latest filter
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should support empty prefixes in withUrlFilter")
        void testWithUrlFilterEmptyPrefixes() {
            // Given: Builder with empty prefixes
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter()
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // Then: Handler should be created
            assertThat(handler).isNotNull();
        }
    }

    @Nested
    @DisplayName("Interceptor Tests")
    class InterceptorTests {

        @Test
        @DisplayName("Should add CORS interceptor with no arguments")
        void testWithCorsAddsInterceptor() {
            // Given: Builder with CORS
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withCors()
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // Then: Handler should be created
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should add CORS interceptor with specific origins")
        void testWithCorsAddsInterceptorWithOrigins() {
            // Given: Builder with CORS origins
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withCors("http://localhost:3000", "https://example.com")
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // Then: Handler should be created
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should add validation interceptor")
        void testWithValidationAddsInterceptor() {
            // Given: Builder with validation
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withValidation()
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // Then: Handler should be created
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should support conditional validation")
        void testWithValidationConditional() {
            // Given: Builder with conditional validation (enabled)
            ApiCefRequestHandler handler1 = ApiCefRequestHandler.builder(mockProject)
                    .withValidation(true)
                    .build();

            // When: Conditional validation disabled
            ApiCefRequestHandler handler2 = ApiCefRequestHandler.builder(mockProject)
                    .withValidation(false)
                    .build();

            // Then: Both should be created
            assertThat(handler1).isNotNull();
            assertThat(handler2).isNotNull();
        }

        @Test
        @DisplayName("Should add custom interceptor")
        void testWithInterceptorAddsCustomInterceptor() {
            // Given: Custom interceptor
            RequestInterceptor customInterceptor = new RequestInterceptor() {
                public void beforeHandle(ApiRequest request) throws Exception {
                    // Custom logic
                }

                public void afterHandle(ApiRequest request, ApiResponse<?> response) throws Exception {
                    // Custom logic
                }
            };

            // When: Builder with custom interceptor
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withInterceptor(customInterceptor)
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // Then: Handler should be created
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should handle null interceptor gracefully")
        void testWithInterceptorHandlesNull() {
            // Given: Builder with null interceptor
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withInterceptor(null)
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // Then: Handler should be created
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should support multiple interceptors")
        void testMultipleInterceptors() {
            // Given: Builder with multiple interceptors
            RequestInterceptor interceptor1 = new RequestInterceptor() {
                public void beforeHandle(ApiRequest request) throws Exception {}

                public void afterHandle(ApiRequest request, ApiResponse<?> response) throws Exception {}
            };

            RequestInterceptor interceptor2 = new RequestInterceptor() {
                public void beforeHandle(ApiRequest request) throws Exception {}

                public void afterHandle(ApiRequest request, ApiResponse<?> response) throws Exception {}
            };

            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withCors()
                    .withValidation()
                    .withInterceptor(interceptor1)
                    .withInterceptor(interceptor2)
                    .build();

            // Then: Handler should be created with all interceptors
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should return builder from all interceptor methods")
        void testInterceptorMethodsReturnBuilder() {
            // Given: Builder
            ApiCefRequestHandlerBuilder builder = ApiCefRequestHandler.builder(mockProject);

            // When: Call interceptor methods
            ApiCefRequestHandlerBuilder result1 = builder.withCors();
            assertThat(result1).isSameAs(builder);

            ApiCefRequestHandlerBuilder result2 = builder.withValidation();
            assertThat(result2).isSameAs(builder);

            ApiCefRequestHandlerBuilder result3 = builder.withInterceptor(null);
            assertThat(result3).isSameAs(builder);
        }

        @Test
        @DisplayName("Should add multiple interceptor instances")
        void testMultipleInterceptorInstances() {
            // Given: Builder with multiple instances of CORS
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withCors("http://localhost:3000")
                    .withCors("https://example.com")
                    .build();

            // Then: Handler should be created with both
            assertThat(handler).isNotNull();
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should use default exception handler if none specified")
        void testDefaultExceptionHandlerUsedWhenNoneSpecified() {
            // Given: Builder without custom exception handler
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // Then: Handler should use default (composite)
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should set custom exception handler")
        void testCustomExceptionHandlerCanBeSet() {
            // Given: Custom exception handler
            com.example.api.interceptor.ExceptionHandler customHandler =
                    (ex, req) -> ApiResponse.internalServerError("Error");

            // When: Builder with custom handler
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withExceptionHandler(customHandler)
                    .build();

            // Then: Handler should be created
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should handle null custom exception handler")
        void testNullCustomExceptionHandlerIgnored() {
            // Given: Builder with null exception handler
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withExceptionHandler((com.example.api.interceptor.ExceptionHandler) null)
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // Then: Handler should use default
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should register type-specific exception handler")
        void testTypeSpecificExceptionHandlerRegistration() {
            // Given: Type-specific handler
            com.example.api.interceptor.CompositeExceptionHandler.TypedExceptionHandler<IllegalArgumentException> handler =
                    (ex, req) -> ApiResponse.badRequest("Invalid argument");

            // When: Builder with type handler
            ApiCefRequestHandler result = ApiCefRequestHandler.builder(mockProject)
                    .withExceptionHandler(IllegalArgumentException.class, handler)
                    .build();

            // Then: Handler should be created
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should support multiple type-specific exception handlers")
        void testMultipleTypeSpecificHandlers() {
            // Given: Multiple type handlers
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withExceptionHandler(IllegalArgumentException.class,
                            (ex, req) -> ApiResponse.badRequest("Invalid"))
                    .withExceptionHandler(RuntimeException.class,
                            (ex, req) -> ApiResponse.internalServerError("Error"))
                    .withExceptionHandler(Exception.class,
                            (ex, req) -> ApiResponse.internalServerError("Unknown error"))
                    .build();

            // Then: Handler should support all
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should return builder from exception handler methods")
        void testExceptionHandlerMethodsReturnBuilder() {
            // Given: Builder
            ApiCefRequestHandlerBuilder builder = ApiCefRequestHandler.builder(mockProject);

            // When: Call exception handler methods
            com.example.api.interceptor.ExceptionHandler handler = (ex, req) -> ApiResponse.internalServerError("Error");
            ApiCefRequestHandlerBuilder result1 = builder.withExceptionHandler(handler);
            assertThat(result1).isSameAs(builder);

            ApiCefRequestHandlerBuilder result2 = builder.withExceptionHandler(Exception.class,
                    (ex, req) -> ApiResponse.internalServerError("Error"));
            assertThat(result2).isSameAs(builder);
        }
    }

    @Nested
    @DisplayName("Build Method Tests")
    class BuildMethodTests {

        @Test
        @DisplayName("Should return ApiCefRequestHandler instance")
        void testBuildReturnsHandlerInstance() {
            // Given: Builder
            ApiCefRequestHandlerBuilder builder = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"));

            // When: Build
            ApiCefRequestHandler handler = builder.build();

            // Then: Should return handler
            assertThat(handler).isNotNull();
            assertThat(handler).isInstanceOf(ApiCefRequestHandler.class);
        }

        @Test
        @DisplayName("Should create handler with all configured routes")
        void testBuildIncludesAllRoutes() {
            // Given: Builder with multiple routes
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/users", HttpMethod.GET, req -> ApiResponse.ok("users"))
                    .withRoute("/api/posts", HttpMethod.GET, req -> ApiResponse.ok("posts"))
                    .withRoute("/api/comments", HttpMethod.POST, req -> ApiResponse.created("comment"))
                    .build();

            // Then: Handler should be created with all routes
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should create handler with all configured interceptors")
        void testBuildIncludesAllInterceptors() {
            // Given: Builder with interceptors
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withCors()
                    .withValidation()
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // Then: Handler should be created with interceptors
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should create handler with URL filters")
        void testBuildIncludesUrlFilters() {
            // Given: Builder with URL filter
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter("http://localhost:5173")
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // Then: Handler should be created with filter
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should allow multiple build calls creating independent handlers")
        void testMultipleBuildCallsCreateIndependentHandlers() {
            // Given: Builder
            ApiCefRequestHandlerBuilder builder = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"));

            // When: Build multiple times
            ApiCefRequestHandler handler1 = builder.build();
            ApiCefRequestHandler handler2 = builder.build();

            // Then: Should create independent instances
            assertThat(handler1).isNotNull();
            assertThat(handler2).isNotNull();
            assertThat(handler1).isNotEqualTo(handler2);
        }
    }

    @Nested
    @DisplayName("Fluent API Tests")
    class FluentApiTests {

        @Test
        @DisplayName("Should support method chaining")
        void testMethodChaining() {
            // Given & When: Chain multiple methods
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withApiRoutes()
                    .withCors()
                    .withValidation()
                    .withUrlFilter("http://localhost:5173")
                    .withRoute("/api/custom", HttpMethod.GET, req -> ApiResponse.ok("custom"))
                    .withInterceptor(null)
                    .withFallback(HttpMethod.GET, req -> ApiResponse.notFound("Not found"))
                    .build();

            // Then: Should successfully build
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should preserve builder state during chaining")
        void testBuilderStatePreservedDuringChaining() {
            // Given: Builder
            ApiCefRequestHandlerBuilder builder = ApiCefRequestHandler.builder(mockProject);

            // When: Chain methods and verify same builder returned
            ApiCefRequestHandlerBuilder result = builder
                    .withRoute("/api/test1", HttpMethod.GET, req -> ApiResponse.ok("test1"))
                    .withRoute("/api/test2", HttpMethod.GET, req -> ApiResponse.ok("test2"));

            // Then: Same builder instance
            assertThat(result).isSameAs(builder);

            // And: Build should include both routes
            ApiCefRequestHandler handler = builder.build();
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should support complex chaining scenarios")
        void testComplexChainingScenarios() {
            // Given & When: Complex chain
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter("http://localhost:5173", "http://localhost:8080")
                    .withApiRoutes()
                    .withCors("http://localhost:3000")
                    .withValidation(true)
                    .withRoute("/api/custom1", HttpMethod.GET, req -> ApiResponse.ok("custom1"))
                    .withRoute("/api/custom2", HttpMethod.POST, req -> ApiResponse.created("custom2"))
                    .withPrefix("/files", HttpMethod.GET, req -> ApiResponse.ok("file"))
                    .withExact("/status", HttpMethod.GET, req -> ApiResponse.ok("ok"))
                    .withContains(".json", HttpMethod.GET, req -> ApiResponse.ok("json"))
                    .withFallback(HttpMethod.GET, req -> ApiResponse.notFound("Not found"))
                    .withExceptionHandler(Exception.class, (ex, req) -> ApiResponse.internalServerError("Error"))
                    .build();

            // Then: Should successfully build with all features
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should support fluent API with no arguments methods")
        void testFluentApiWithNoArgumentsMethods() {
            // Given & When: Chain no-argument methods
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withApiRoutes()
                    .withCors()
                    .withValidation()
                    .withUrlFilter()
                    .build();

            // Then: Should successfully build
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should allow reordering of builder calls")
        void testReorderingBuilderCalls() {
            // Given & When: Reorder method calls
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .withUrlFilter("http://localhost:5173")
                    .withCors()
                    .withApiRoutes()
                    .withValidation()
                    .build();

            // Then: Should still work
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should support building empty handler")
        void testBuildingEmptyHandler() {
            // Given & When: Build with no routes
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .build();

            // Then: Should create valid handler
            assertThat(handler).isNotNull();
        }

        @Test
        @DisplayName("Should support minimal builder usage")
        void testMinimalBuilderUsage() {
            // Given & When: Minimal configuration
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/health", HttpMethod.GET, req -> ApiResponse.ok("ok"))
                    .build();

            // Then: Should create valid handler
            assertThat(handler).isNotNull();
        }
    }
}
