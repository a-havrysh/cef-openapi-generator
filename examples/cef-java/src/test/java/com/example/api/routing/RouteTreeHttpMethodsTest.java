package com.example.api.routing;

import com.example.api.protocol.ApiRequest;
import com.example.api.protocol.ApiResponse;
import com.example.api.protocol.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RouteTree HTTP method support.
 * All 7 HTTP methods should be supported: GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD
 */
class RouteTreeHttpMethodsTest {

    private RouteTree routeTree;

    @BeforeEach
    void setUp() {
        routeTree = new RouteTree();
    }

    @ParameterizedTest
    @EnumSource(HttpMethod.class)
    void testAllHttpMethodsSupported(HttpMethod method) {
        // Given: Route registered for each method
        Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("method: " + method);
        routeTree.addRoute("/api/resource", method, handler);

        // When: Matching with that method
        RouteTree.MatchResult result = routeTree.match("/api/resource", method);

        // Then: Route should match
        assertNotNull(result, method + " should be supported");
        assertEquals(handler, result.handler());
    }

    @Test
    void testSamePathDifferentMethods() {
        Function<ApiRequest, ApiResponse<?>> getHandler = req -> ApiResponse.ok("GET");
        Function<ApiRequest, ApiResponse<?>> postHandler = req -> ApiResponse.ok("POST");
        Function<ApiRequest, ApiResponse<?>> putHandler = req -> ApiResponse.ok("PUT");
        Function<ApiRequest, ApiResponse<?>> deleteHandler = req -> ApiResponse.ok("DELETE");

        routeTree.addRoute("/api/resource", HttpMethod.GET, getHandler);
        routeTree.addRoute("/api/resource", HttpMethod.POST, postHandler);
        routeTree.addRoute("/api/resource", HttpMethod.PUT, putHandler);
        routeTree.addRoute("/api/resource", HttpMethod.DELETE, deleteHandler);

        // Each method should route to its own handler
        assertEquals(getHandler, routeTree.match("/api/resource", HttpMethod.GET).handler());
        assertEquals(postHandler, routeTree.match("/api/resource", HttpMethod.POST).handler());
        assertEquals(putHandler, routeTree.match("/api/resource", HttpMethod.PUT).handler());
        assertEquals(deleteHandler, routeTree.match("/api/resource", HttpMethod.DELETE).handler());
    }

    @Test
    void testMethodSpecificFallback() {
        Function<ApiRequest, ApiResponse<?>> getHandler = req -> ApiResponse.ok("GET");
        Function<ApiRequest, ApiResponse<?>> getFallback = req -> ApiResponse.notFound("GET not found");
        Function<ApiRequest, ApiResponse<?>> postFallback = req -> ApiResponse.notFound("POST not found");

        routeTree.addRoute("/api/known", HttpMethod.GET, getHandler);
        routeTree.setFallback(HttpMethod.GET, getFallback);
        routeTree.setFallback(HttpMethod.POST, postFallback);

        // Known route uses handler
        RouteTree.MatchResult known = routeTree.match("/api/known", HttpMethod.GET);
        assertEquals(getHandler, known.handler());

        // Unknown GET uses GET fallback
        RouteTree.MatchResult unknownGet = routeTree.match("/api/unknown", HttpMethod.GET);
        assertEquals(getFallback, unknownGet.handler());

        // Unknown POST uses POST fallback
        RouteTree.MatchResult unknownPost = routeTree.match("/api/unknown", HttpMethod.POST);
        assertEquals(postFallback, unknownPost.handler());
    }

    @Test
    void testMethodNotMatching() {
        Function<ApiRequest, ApiResponse<?>> getHandler = req -> ApiResponse.ok("GET");
        routeTree.addRoute("/api/resource", HttpMethod.GET, getHandler);

        // POST to GET-only route should not match
        RouteTree.MatchResult result = routeTree.match("/api/resource", HttpMethod.POST);
        assertNull(result, "Different method should not match");
    }

    @Test
    void testPrefixWithMethod() {
        Function<ApiRequest, ApiResponse<?>> getHandler = req -> ApiResponse.ok("GET static");
        Function<ApiRequest, ApiResponse<?>> postHandler = req -> ApiResponse.ok("POST static");

        routeTree.addPrefixRoute("/static", HttpMethod.GET, getHandler);
        routeTree.addPrefixRoute("/static", HttpMethod.POST, postHandler);

        // GET to /static/...
        RouteTree.MatchResult getResult = routeTree.match("/static/css/style.css", HttpMethod.GET);
        assertEquals(getHandler, getResult.handler());

        // POST to /static/... (different handler)
        RouteTree.MatchResult postResult = routeTree.match("/static/upload", HttpMethod.POST);
        assertEquals(postHandler, postResult.handler());

        // PUT should not match (no handler registered)
        assertNull(routeTree.match("/static/file", HttpMethod.PUT));
    }

    @Test
    void testExactWithMethod() {
        Function<ApiRequest, ApiResponse<?>> getHandler = req -> ApiResponse.ok("GET");
        Function<ApiRequest, ApiResponse<?>> postHandler = req -> ApiResponse.ok("POST");

        routeTree.addExactRoute("/health", HttpMethod.GET, getHandler);
        routeTree.addExactRoute("/health", HttpMethod.POST, postHandler);

        assertEquals(getHandler, routeTree.match("/health", HttpMethod.GET).handler());
        assertEquals(postHandler, routeTree.match("/health", HttpMethod.POST).handler());

        // Different method not registered
        assertNull(routeTree.match("/health", HttpMethod.DELETE));
    }

    @Test
    void testContainsWithMethod() {
        Function<ApiRequest, ApiResponse<?>> getHandler = req -> ApiResponse.ok("GET min");
        Function<ApiRequest, ApiResponse<?>> deleteHandler = req -> ApiResponse.ok("DELETE min");

        routeTree.addContainsRoute(".min.", HttpMethod.GET, getHandler);
        routeTree.addContainsRoute(".min.", HttpMethod.DELETE, deleteHandler);

        assertEquals(getHandler, routeTree.match("/js/app.min.js", HttpMethod.GET).handler());
        assertEquals(deleteHandler, routeTree.match("/js/app.min.js", HttpMethod.DELETE).handler());

        assertNull(routeTree.match("/js/app.min.js", HttpMethod.POST));
    }

    @Test
    void testOptionsMethod() {
        // OPTIONS is commonly used for CORS preflight
        Function<ApiRequest, ApiResponse<?>> optionsHandler = req -> ApiResponse.ok("CORS preflight");

        routeTree.addRoute("/api/resource", HttpMethod.OPTIONS, optionsHandler);

        RouteTree.MatchResult result = routeTree.match("/api/resource", HttpMethod.OPTIONS);
        assertNotNull(result);
        assertEquals(optionsHandler, result.handler());
    }

    @Test
    void testHeadMethod() {
        // HEAD should work like GET but without body
        Function<ApiRequest, ApiResponse<?>> headHandler = req -> ApiResponse.ok("HEAD");

        routeTree.addRoute("/api/resource", HttpMethod.HEAD, headHandler);

        RouteTree.MatchResult result = routeTree.match("/api/resource", HttpMethod.HEAD);
        assertNotNull(result);
        assertEquals(headHandler, result.handler());
    }

    @Test
    void testPatchMethod() {
        // PATCH for partial updates
        Function<ApiRequest, ApiResponse<?>> patchHandler = req -> ApiResponse.ok("PATCH");

        routeTree.addRoute("/api/resource/{id}", HttpMethod.PATCH, patchHandler);

        RouteTree.MatchResult result = routeTree.match("/api/resource/123", HttpMethod.PATCH);
        assertNotNull(result);
        assertEquals(patchHandler, result.handler());
        assertEquals("123", result.pathVariables().get("id"));
    }

    @Test
    void testMultipleRoutesMultipleMethods() {
        // Realistic API setup
        Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("test");

        // CRUD operations on /api/users
        routeTree.addRoute("/api/users", HttpMethod.GET, handler);        // List
        routeTree.addRoute("/api/users", HttpMethod.POST, handler);       // Create
        routeTree.addRoute("/api/users/{id}", HttpMethod.GET, handler);   // Get
        routeTree.addRoute("/api/users/{id}", HttpMethod.PUT, handler);   // Update
        routeTree.addRoute("/api/users/{id}", HttpMethod.DELETE, handler); // Delete
        routeTree.addRoute("/api/users/{id}", HttpMethod.PATCH, handler); // Partial update

        // All operations should work
        assertNotNull(routeTree.match("/api/users", HttpMethod.GET));
        assertNotNull(routeTree.match("/api/users", HttpMethod.POST));
        assertNotNull(routeTree.match("/api/users/123", HttpMethod.GET));
        assertNotNull(routeTree.match("/api/users/123", HttpMethod.PUT));
        assertNotNull(routeTree.match("/api/users/123", HttpMethod.DELETE));
        assertNotNull(routeTree.match("/api/users/123", HttpMethod.PATCH));
    }

    @Test
    void testMethodPriorityDoesNotMatter() {
        Function<ApiRequest, ApiResponse<?>> getHandler = req -> ApiResponse.ok("GET");
        Function<ApiRequest, ApiResponse<?>> postHandler = req -> ApiResponse.ok("POST");

        // Register POST first, then GET
        routeTree.addRoute("/api/data", HttpMethod.POST, postHandler);
        routeTree.addRoute("/api/data", HttpMethod.GET, getHandler);

        // Both should work regardless of registration order
        assertEquals(getHandler, routeTree.match("/api/data", HttpMethod.GET).handler());
        assertEquals(postHandler, routeTree.match("/api/data", HttpMethod.POST).handler());
    }

    @Test
    void testFallbackWithNoRoutes() {
        Function<ApiRequest, ApiResponse<?>> getFallback = req -> ApiResponse.notFound("Not found");

        routeTree.setFallback(HttpMethod.GET, getFallback);

        // Any GET request uses fallback
        RouteTree.MatchResult result = routeTree.match("/any/path", HttpMethod.GET);
        assertEquals(getFallback, result.handler());

        // POST has no fallback, returns null
        assertNull(routeTree.match("/any/path", HttpMethod.POST));
    }
}
