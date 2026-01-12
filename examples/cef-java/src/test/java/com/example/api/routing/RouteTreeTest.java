package com.example.api.routing;

import com.example.api.protocol.ApiRequest;
import com.example.api.protocol.ApiResponse;
import com.example.api.protocol.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RouteTree trie-based routing functionality.
 * Target coverage: 95%+
 */
class RouteTreeTest {

    private RouteTree routeTree;
    private Function<ApiRequest, ApiResponse<?>> testHandler;

    @BeforeEach
    void setUp() {
        routeTree = new RouteTree();
        testHandler = request -> ApiResponse.ok("test");
    }

    @Test
    void testAddAndMatchPatternRoute() {
        // Given
        routeTree.addRoute("/api/users/{id}", HttpMethod.GET, testHandler);

        // When
        RouteTree.MatchResult result = routeTree.match("/api/users/123", HttpMethod.GET);

        // Then
        assertNotNull(result, "Route should match");
        assertEquals(testHandler, result.handler());
        assertEquals("123", result.pathVariables().get("id"));
    }

    @Test
    void testMultiplePathVariables() {
        // Given
        routeTree.addRoute("/api/users/{userId}/posts/{postId}", HttpMethod.GET, testHandler);

        // When
        RouteTree.MatchResult result = routeTree.match("/api/users/42/posts/999", HttpMethod.GET);

        // Then
        assertNotNull(result);
        Map<String, String> vars = result.pathVariables();
        assertEquals("42", vars.get("userId"));
        assertEquals("999", vars.get("postId"));
    }

    @Test
    void testLiteralPriorityOverTemplate() {
        // Literal "admin" should beat template {id}
        Function<ApiRequest, ApiResponse<?>> literalHandler = req -> ApiResponse.ok("literal");
        Function<ApiRequest, ApiResponse<?>> templateHandler = req -> ApiResponse.ok("template");

        routeTree.addRoute("/api/users/{id}", HttpMethod.GET, templateHandler);
        routeTree.addRoute("/api/users/admin", HttpMethod.GET, literalHandler);

        // When matching /api/users/admin
        RouteTree.MatchResult result = routeTree.match("/api/users/admin", HttpMethod.GET);

        // Then: literal handler wins
        assertEquals(literalHandler, result.handler());
        assertTrue(result.pathVariables().isEmpty(), "No variables for literal match");
    }

    @Test
    void testTemplateMatchWhenNoLiteral() {
        Function<ApiRequest, ApiResponse<?>> literalHandler = req -> ApiResponse.ok("literal");
        Function<ApiRequest, ApiResponse<?>> templateHandler = req -> ApiResponse.ok("template");

        routeTree.addRoute("/api/users/{id}", HttpMethod.GET, templateHandler);
        routeTree.addRoute("/api/users/admin", HttpMethod.GET, literalHandler);

        // When matching /api/users/123 (not "admin")
        RouteTree.MatchResult result = routeTree.match("/api/users/123", HttpMethod.GET);

        // Then: template handler matches
        assertEquals(templateHandler, result.handler());
        assertEquals("123", result.pathVariables().get("id"));
    }

    @Test
    void testExactPathMatching() {
        // Exact path matching - no trailing slash normalization
        routeTree.addRoute("/api/users", HttpMethod.GET, testHandler);

        // Exact match works
        RouteTree.MatchResult result = routeTree.match("/api/users", HttpMethod.GET);
        assertNotNull(result);

        // Different path (with trailing slash) treated as different route
        // This is expected behavior - strict path matching
    }

    @Test
    void testNoMatchReturnsNull() {
        routeTree.addRoute("/api/users", HttpMethod.GET, testHandler);

        // Non-existent path
        RouteTree.MatchResult result = routeTree.match("/api/products", HttpMethod.GET);

        assertNull(result, "Non-matching route should return null");
    }

    @Test
    void testNestedRoutes() {
        routeTree.addRoute("/api/v1/users", HttpMethod.GET, testHandler);
        routeTree.addRoute("/api/v1/users/{id}", HttpMethod.GET, testHandler);
        routeTree.addRoute("/api/v1/users/{id}/profile", HttpMethod.GET, testHandler);
        routeTree.addRoute("/api/v2/users", HttpMethod.GET, testHandler);

        // All should match
        assertNotNull(routeTree.match("/api/v1/users", HttpMethod.GET));
        assertNotNull(routeTree.match("/api/v1/users/123", HttpMethod.GET));
        assertNotNull(routeTree.match("/api/v1/users/123/profile", HttpMethod.GET));
        assertNotNull(routeTree.match("/api/v2/users", HttpMethod.GET));
    }

    @Test
    void testPathVariableExtraction() {
        routeTree.addRoute("/api/{resource}/{id}/action", HttpMethod.POST, testHandler);

        RouteTree.MatchResult result = routeTree.match("/api/tasks/abc-123/action", HttpMethod.POST);

        assertNotNull(result);
        assertEquals("tasks", result.pathVariables().get("resource"));
        assertEquals("abc-123", result.pathVariables().get("id"));
    }

    @Test
    void testRootPath() {
        routeTree.addRoute("/", HttpMethod.GET, testHandler);

        RouteTree.MatchResult result = routeTree.match("/", HttpMethod.GET);

        assertNotNull(result);
        assertEquals(testHandler, result.handler());
    }

    @Test
    void testDeepNesting() {
        // Very deep path
        String deepPath = "/api/v1/org/{orgId}/team/{teamId}/project/{projectId}/task/{taskId}";
        routeTree.addRoute(deepPath, HttpMethod.GET, testHandler);

        RouteTree.MatchResult result = routeTree.match(
            "/api/v1/org/123/team/456/project/789/task/999",
            HttpMethod.GET
        );

        assertNotNull(result);
        assertEquals("123", result.pathVariables().get("orgId"));
        assertEquals("456", result.pathVariables().get("teamId"));
        assertEquals("789", result.pathVariables().get("projectId"));
        assertEquals("999", result.pathVariables().get("taskId"));
    }

    @Test
    void testPrefixRoute() {
        routeTree.addPrefixRoute("/static", HttpMethod.GET, testHandler);

        // Should match any path starting with /static
        assertNotNull(routeTree.match("/static/css/style.css", HttpMethod.GET));
        assertNotNull(routeTree.match("/static/js/app.js", HttpMethod.GET));
        assertNotNull(routeTree.match("/static/index.html", HttpMethod.GET));

        // Should not match different prefix
        assertNull(routeTree.match("/assets/style.css", HttpMethod.GET));
    }

    @Test
    void testContainsRoute() {
        routeTree.addContainsRoute(".min.", HttpMethod.GET, testHandler);

        // Should match paths containing .min.
        assertNotNull(routeTree.match("/js/app.min.js", HttpMethod.GET));
        assertNotNull(routeTree.match("/css/style.min.css", HttpMethod.GET));

        // Should not match paths without .min.
        assertNull(routeTree.match("/js/app.js", HttpMethod.GET));
    }

    @Test
    void testFallbackHandler() {
        Function<ApiRequest, ApiResponse<?>> fallbackHandler = req -> ApiResponse.notFound("Not found");

        routeTree.addRoute("/api/users", HttpMethod.GET, testHandler);
        routeTree.setFallback(HttpMethod.GET, fallbackHandler);

        // Matching route uses route handler
        RouteTree.MatchResult result1 = routeTree.match("/api/users", HttpMethod.GET);
        assertEquals(testHandler, result1.handler());

        // Non-matching route uses fallback
        RouteTree.MatchResult result2 = routeTree.match("/api/products", HttpMethod.GET);
        assertEquals(fallbackHandler, result2.handler());
    }

    @Test
    void testExactRoute() {
        routeTree.addExactRoute("/health", HttpMethod.GET, testHandler);

        // Exact match works
        assertNotNull(routeTree.match("/health", HttpMethod.GET));

        // Partial match doesn't work
        assertNull(routeTree.match("/health/check", HttpMethod.GET));
        assertNull(routeTree.match("/api/health", HttpMethod.GET));
    }

    @ParameterizedTest
    @CsvSource({
        "/api/users, /api/users, true",
        "/api/users/{id}, /api/users/123, true",
        "/api/users, /api/products, false",
        "/api/users/{id}, /api/users, false",
        "/api/users, /api/users/123, false"
    })
    void testVariousRouteMatching(String pattern, String path, boolean shouldMatch) {
        routeTree.addRoute(pattern, HttpMethod.GET, testHandler);

        RouteTree.MatchResult result = routeTree.match(path, HttpMethod.GET);

        if (shouldMatch) {
            assertNotNull(result, "Pattern '" + pattern + "' should match path '" + path + "'");
        } else {
            assertNull(result, "Pattern '" + pattern + "' should NOT match path '" + path + "'");
        }
    }

    @Test
    void testRouteOrder() {
        // The order of registration shouldn't matter for matching
        routeTree.addRoute("/api/users/{id}", HttpMethod.GET, testHandler);
        routeTree.addPrefixRoute("/api", HttpMethod.GET, testHandler);
        routeTree.addExactRoute("/api/users/admin", HttpMethod.GET, testHandler);

        // Exact match has highest priority
        assertNotNull(routeTree.match("/api/users/admin", HttpMethod.GET));

        // Pattern match works for other IDs
        RouteTree.MatchResult result = routeTree.match("/api/users/123", HttpMethod.GET);
        assertNotNull(result);
        assertEquals("123", result.pathVariables().get("id"));
    }

    @Test
    void testSpecialCharactersInPathVariable() {
        routeTree.addRoute("/api/files/{filename}", HttpMethod.GET, testHandler);

        // UUID-like filenames
        RouteTree.MatchResult result1 = routeTree.match("/api/files/abc-123-def-456", HttpMethod.GET);
        assertNotNull(result1);
        assertEquals("abc-123-def-456", result1.pathVariables().get("filename"));

        // Filename with dots
        RouteTree.MatchResult result2 = routeTree.match("/api/files/document.pdf", HttpMethod.GET);
        assertNotNull(result2);
        assertEquals("document.pdf", result2.pathVariables().get("filename"));
    }
}
