package com.example.api.routing;

import com.example.api.protocol.ApiRequest;
import com.example.api.protocol.ApiResponse;
import com.example.api.protocol.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RouteTree LRU cache behavior.
 * Cache size limit: 100 entries
 * Cache key format: "METHOD:path"
 * Only pattern routes are cached (not exact/simple routes)
 */
class RouteTreeCacheTest {

    private RouteTree routeTree;
    private Function<ApiRequest, ApiResponse<?>> testHandler;

    @BeforeEach
    void setUp() {
        routeTree = new RouteTree();
        testHandler = request -> ApiResponse.ok("test");
    }

    @Test
    void testCacheHit() {
        // Given: Pattern route that should be cached
        routeTree.addRoute("/api/users/{id}", HttpMethod.GET, testHandler);

        // First match - cache miss, stores in cache
        RouteTree.MatchResult result1 = routeTree.match("/api/users/123", HttpMethod.GET);
        assertNotNull(result1);

        // Second match - cache hit
        RouteTree.MatchResult result2 = routeTree.match("/api/users/123", HttpMethod.GET);
        assertNotNull(result2);
        assertEquals(result1.handler(), result2.handler());
        assertEquals(result1.pathVariables(), result2.pathVariables());
    }

    @Test
    void testCacheMiss() {
        routeTree.addRoute("/api/users/{id}", HttpMethod.GET, testHandler);

        // Different paths should be cache misses
        RouteTree.MatchResult result1 = routeTree.match("/api/users/123", HttpMethod.GET);
        RouteTree.MatchResult result2 = routeTree.match("/api/users/456", HttpMethod.GET);

        assertNotNull(result1);
        assertNotNull(result2);

        // Different path variables
        assertEquals("123", result1.pathVariables().get("id"));
        assertEquals("456", result2.pathVariables().get("id"));
    }

    @Test
    void testLruEviction() {
        // Given: Pattern route
        routeTree.addRoute("/api/items/{id}", HttpMethod.GET, testHandler);

        // Fill cache with 100 entries (0-99)
        for (int i = 0; i < 100; i++) {
            routeTree.match("/api/items/" + i, HttpMethod.GET);
        }

        // Access entry 0 again to verify it's still cached
        RouteTree.MatchResult before = routeTree.match("/api/items/0", HttpMethod.GET);
        assertNotNull(before);

        // Add 101st entry - should evict least recently used
        routeTree.match("/api/items/100", HttpMethod.GET);

        // Entry 1 (not accessed recently) might be evicted
        // Entry 0 (accessed recently) should still work
        RouteTree.MatchResult after = routeTree.match("/api/items/0", HttpMethod.GET);
        assertNotNull(after);
    }

    @Test
    void testCacheKeyFormat() {
        routeTree.addRoute("/api/data/{id}", HttpMethod.GET, testHandler);
        routeTree.addRoute("/api/data/{id}", HttpMethod.POST, testHandler);

        // Same path, different methods should be separate cache entries
        RouteTree.MatchResult getResult = routeTree.match("/api/data/123", HttpMethod.GET);
        RouteTree.MatchResult postResult = routeTree.match("/api/data/123", HttpMethod.POST);

        assertNotNull(getResult);
        assertNotNull(postResult);

        // Both should work independently
        assertEquals("123", getResult.pathVariables().get("id"));
        assertEquals("123", postResult.pathVariables().get("id"));
    }

    @Test
    void testOnlyPatternRoutesCached() {
        // Pattern route (should be cached)
        routeTree.addRoute("/api/pattern/{id}", HttpMethod.GET, testHandler);

        // Exact route (should NOT be cached - O(1) lookup already)
        routeTree.addExactRoute("/api/exact", HttpMethod.GET, testHandler);

        // First access of pattern route
        RouteTree.MatchResult pattern1 = routeTree.match("/api/pattern/123", HttpMethod.GET);
        assertNotNull(pattern1);

        // Second access should use cache
        RouteTree.MatchResult pattern2 = routeTree.match("/api/pattern/123", HttpMethod.GET);
        assertNotNull(pattern2);

        // Exact route should work (but not via cache)
        RouteTree.MatchResult exact = routeTree.match("/api/exact", HttpMethod.GET);
        assertNotNull(exact);
    }

    @Test
    void testCacheStoresPathVariables() {
        routeTree.addRoute("/api/users/{userId}/posts/{postId}", HttpMethod.GET, testHandler);

        // First match - populates cache
        RouteTree.MatchResult result1 = routeTree.match("/api/users/42/posts/999", HttpMethod.GET);
        assertEquals("42", result1.pathVariables().get("userId"));
        assertEquals("999", result1.pathVariables().get("postId"));

        // Second match - from cache, path variables should be identical
        RouteTree.MatchResult result2 = routeTree.match("/api/users/42/posts/999", HttpMethod.GET);
        assertEquals("42", result2.pathVariables().get("userId"));
        assertEquals("999", result2.pathVariables().get("postId"));
    }

    @Test
    void testCacheWithMultipleRoutes() {
        // Multiple pattern routes
        routeTree.addRoute("/api/users/{id}", HttpMethod.GET, testHandler);
        routeTree.addRoute("/api/posts/{id}", HttpMethod.GET, testHandler);
        routeTree.addRoute("/api/comments/{id}", HttpMethod.GET, testHandler);

        // Cache different paths
        routeTree.match("/api/users/1", HttpMethod.GET);
        routeTree.match("/api/posts/2", HttpMethod.GET);
        routeTree.match("/api/comments/3", HttpMethod.GET);

        // All should still work (cached)
        assertNotNull(routeTree.match("/api/users/1", HttpMethod.GET));
        assertNotNull(routeTree.match("/api/posts/2", HttpMethod.GET));
        assertNotNull(routeTree.match("/api/comments/3", HttpMethod.GET));
    }

    @Test
    void testCacheSizeLimit() {
        routeTree.addRoute("/api/items/{id}", HttpMethod.GET, testHandler);

        // Add 150 entries (exceeds 100 limit)
        for (int i = 0; i < 150; i++) {
            RouteTree.MatchResult result = routeTree.match("/api/items/" + i, HttpMethod.GET);
            assertNotNull(result, "Entry " + i + " should match");
        }

        // Most recent 100 should still work efficiently
        for (int i = 50; i < 150; i++) {
            RouteTree.MatchResult result = routeTree.match("/api/items/" + i, HttpMethod.GET);
            assertNotNull(result, "Recent entry " + i + " should still match");
        }
    }

    @Test
    void testCacheWithDifferentPathVariableValues() {
        routeTree.addRoute("/api/users/{id}/profile", HttpMethod.GET, testHandler);

        // Cache multiple values for same pattern
        String[] ids = {"user1", "user2", "user3", "admin", "123", "abc-def"};

        for (String id : ids) {
            RouteTree.MatchResult result = routeTree.match("/api/users/" + id + "/profile", HttpMethod.GET);
            assertNotNull(result);
            assertEquals(id, result.pathVariables().get("id"));
        }

        // All should be cached and retrievable
        for (String id : ids) {
            RouteTree.MatchResult result = routeTree.match("/api/users/" + id + "/profile", HttpMethod.GET);
            assertNotNull(result);
            assertEquals(id, result.pathVariables().get("id"));
        }
    }

    @Test
    void testCacheDoesNotAffectMatching() {
        routeTree.addRoute("/api/cached/{id}", HttpMethod.GET, testHandler);

        // First access (no cache)
        RouteTree.MatchResult noCacheResult = routeTree.match("/api/cached/test", HttpMethod.GET);

        // Second access (from cache)
        RouteTree.MatchResult cachedResult = routeTree.match("/api/cached/test", HttpMethod.GET);

        // Results should be functionally identical
        assertEquals(noCacheResult.handler(), cachedResult.handler());
        assertEquals(noCacheResult.pathVariables(), cachedResult.pathVariables());
    }
}
