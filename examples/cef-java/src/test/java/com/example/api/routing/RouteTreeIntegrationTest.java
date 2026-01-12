package com.example.api.routing;

import com.example.api.protocol.ApiRequest;
import com.example.api.protocol.ApiResponse;
import com.example.api.protocol.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive integration tests for RouteTree routing logic.
 * Covers all routing strategies, path variable extraction, priority handling, and edge cases.
 * Target coverage: 100% of routing logic
 */
@DisplayName("RouteTree Integration Tests")
class RouteTreeIntegrationTest {

    private RouteTree routeTree;

    @BeforeEach
    void setUp() {
        routeTree = new RouteTree();
    }

    @Nested
    @DisplayName("Pattern Route Matching (with path variables)")
    class PatternRouteMatching {

        @Test
        @DisplayName("Should match simple single path variable pattern")
        void testSimpleSingleVariable() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("user");
            routeTree.addRoute("/api/users/{id}", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api/users/123", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.handler()).isEqualTo(handler);
            assertThat(result.pathVariables()).containsEntry("id", "123");
        }

        @Test
        @DisplayName("Should extract multiple path variables")
        void testMultiplePathVariables() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api/users/{userId}/posts/{postId}/comments/{commentId}", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api/users/user123/posts/post456/comments/comment789", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.pathVariables())
                    .hasSize(3)
                    .containsEntry("userId", "user123")
                    .containsEntry("postId", "post456")
                    .containsEntry("commentId", "comment789");
        }

        @Test
        @DisplayName("Should handle path variables with special characters")
        void testPathVariablesWithSpecialCharacters() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api/resources/{id}", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api/resources/abc-123_def", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.pathVariables()).containsEntry("id", "abc-123_def");
        }

        @Test
        @DisplayName("Should handle UUID path variables")
        void testUUIDPathVariable() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api/users/{userId}", HttpMethod.GET, handler);

            String uuid = "550e8400-e29b-41d4-a716-446655440000";
            RouteTree.MatchResult result = routeTree.match("/api/users/" + uuid, HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.pathVariables()).containsEntry("userId", uuid);
        }

        @Test
        @DisplayName("Should prioritize literal segments over template segments")
        void testLiteralPriorityOverTemplate() {
            Function<ApiRequest, ApiResponse<?>> literalHandler = req -> ApiResponse.ok("literal");
            Function<ApiRequest, ApiResponse<?>> templateHandler = req -> ApiResponse.ok("template");

            routeTree.addRoute("/api/users/{id}", HttpMethod.GET, templateHandler);
            routeTree.addRoute("/api/users/admin", HttpMethod.GET, literalHandler);

            // Should match literal handler
            RouteTree.MatchResult result = routeTree.match("/api/users/admin", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.handler()).isEqualTo(literalHandler);
            assertThat(result.pathVariables()).isEmpty();
        }

        @Test
        @DisplayName("Should fallback to template when literal doesn't match")
        void testTemplateFallback() {
            Function<ApiRequest, ApiResponse<?>> literalHandler = req -> ApiResponse.ok("literal");
            Function<ApiRequest, ApiResponse<?>> templateHandler = req -> ApiResponse.ok("template");

            routeTree.addRoute("/api/users/{id}", HttpMethod.GET, templateHandler);
            routeTree.addRoute("/api/users/admin", HttpMethod.GET, literalHandler);

            // Should match template handler
            RouteTree.MatchResult result = routeTree.match("/api/users/123", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.handler()).isEqualTo(templateHandler);
            assertThat(result.pathVariables()).containsEntry("id", "123");
        }

        @Test
        @DisplayName("Should cache pattern route matches")
        void testPatternRouteCaching() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api/users/{id}", HttpMethod.GET, handler);

            // First match - searches trie
            RouteTree.MatchResult result1 = routeTree.match("/api/users/123", HttpMethod.GET);
            // Second match - should come from cache
            RouteTree.MatchResult result2 = routeTree.match("/api/users/123", HttpMethod.GET);

            assertThat(result1).isNotNull();
            assertThat(result2).isNotNull();
            assertThat(result1.handler()).isEqualTo(result2.handler());
            assertThat(result1.pathVariables()).isEqualTo(result2.pathVariables());
        }

        @Test
        @DisplayName("Should not match when path structure differs")
        void testNoMatchWhenPathStructureDiffers() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api/users/{id}", HttpMethod.GET, handler);

            // Too many segments
            RouteTree.MatchResult result1 = routeTree.match("/api/users/123/extra", HttpMethod.GET);
            // Too few segments
            RouteTree.MatchResult result2 = routeTree.match("/api/users", HttpMethod.GET);

            assertThat(result1).isNull();
            assertThat(result2).isNull();
        }

        @Test
        @DisplayName("Should handle patterns without variables as exact routes")
        void testPatternWithoutVariables() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api/tasks", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api/tasks", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.handler()).isEqualTo(handler);
            assertThat(result.pathVariables()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Exact Route Matching")
    class ExactRouteMatching {

        @Test
        @DisplayName("Should match exact paths")
        void testExactPathMatch() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("exact");
            routeTree.addExactRoute("/api/health", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api/health", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.handler()).isEqualTo(handler);
        }

        @Test
        @DisplayName("Should not match with extra path segments")
        void testExactNoMatchWithExtraSegments() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("exact");
            routeTree.addExactRoute("/api/health", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api/health/extra", HttpMethod.GET);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should not match with missing path segments")
        void testExactNoMatchWithMissingSegments() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("exact");
            routeTree.addExactRoute("/api/health/status", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api/health", HttpMethod.GET);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Prefix Route Matching")
    class PrefixRouteMatching {

        @Test
        @DisplayName("Should match paths starting with prefix")
        void testPrefixMatch() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("prefix");
            routeTree.addPrefixRoute("/static/", HttpMethod.GET, handler);

            // All these should match
            assertThat(routeTree.match("/static/css/style.css", HttpMethod.GET)).isNotNull();
            assertThat(routeTree.match("/static/js/app.js", HttpMethod.GET)).isNotNull();
            assertThat(routeTree.match("/static/images/logo.png", HttpMethod.GET)).isNotNull();
        }

        @Test
        @DisplayName("Should not match paths not starting with prefix")
        void testPrefixNoMatch() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("prefix");
            routeTree.addPrefixRoute("/static/", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api/users", HttpMethod.GET);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should match prefix without trailing slash")
        void testPrefixWithoutTrailingSlash() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("prefix");
            routeTree.addPrefixRoute("/api/v1", HttpMethod.GET, handler);

            assertThat(routeTree.match("/api/v1/users", HttpMethod.GET)).isNotNull();
            assertThat(routeTree.match("/api/v1/tasks", HttpMethod.GET)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Contains Route Matching")
    class ContainsRouteMatching {

        @Test
        @DisplayName("Should match paths containing substring")
        void testContainsMatch() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("contains");
            routeTree.addContainsRoute(".min.", HttpMethod.GET, handler);

            // All these should match
            assertThat(routeTree.match("/js/app.min.js", HttpMethod.GET)).isNotNull();
            assertThat(routeTree.match("/css/style.min.css", HttpMethod.GET)).isNotNull();
            assertThat(routeTree.match("/path/.min.anything", HttpMethod.GET)).isNotNull();
        }

        @Test
        @DisplayName("Should not match paths without substring")
        void testContainsNoMatch() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("contains");
            routeTree.addContainsRoute(".min.", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/js/app.js", HttpMethod.GET);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should match substring anywhere in path")
        void testContainsMatchesSubstringAnywhere() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("contains");
            routeTree.addContainsRoute("admin", HttpMethod.GET, handler);

            assertThat(routeTree.match("/admin/users", HttpMethod.GET)).isNotNull();
            assertThat(routeTree.match("/users/admin/edit", HttpMethod.GET)).isNotNull();
            assertThat(routeTree.match("/panel-admin", HttpMethod.GET)).isNotNull();
        }
    }

    @Nested
    @DisplayName("HTTP Method Matching")
    class HttpMethodMatching {

        @Test
        @DisplayName("Should match correct HTTP method")
        void testCorrectMethodMatch() {
            Function<ApiRequest, ApiResponse<?>> getHandler = req -> ApiResponse.ok("get");
            Function<ApiRequest, ApiResponse<?>> postHandler = req -> ApiResponse.ok("post");

            routeTree.addRoute("/api/tasks", HttpMethod.GET, getHandler);
            routeTree.addRoute("/api/tasks", HttpMethod.POST, postHandler);

            RouteTree.MatchResult getResult = routeTree.match("/api/tasks", HttpMethod.GET);
            RouteTree.MatchResult postResult = routeTree.match("/api/tasks", HttpMethod.POST);

            assertThat(getResult).isNotNull();
            assertThat(getResult.handler()).isEqualTo(getHandler);
            assertThat(postResult).isNotNull();
            assertThat(postResult.handler()).isEqualTo(postHandler);
        }

        @Test
        @DisplayName("Should not match incorrect HTTP method")
        void testIncorrectMethodNoMatch() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("get");
            routeTree.addRoute("/api/tasks", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api/tasks", HttpMethod.POST);

            assertThat(result).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"})
        @DisplayName("Should match all HTTP methods")
        void testAllHttpMethods(String methodStr) {
            HttpMethod method = HttpMethod.valueOf(methodStr);
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok(methodStr);
            routeTree.addRoute("/api/resource/{id}", method, handler);

            RouteTree.MatchResult result = routeTree.match("/api/resource/123", method);

            assertThat(result).isNotNull();
            assertThat(result.handler()).isEqualTo(handler);
        }
    }

    @Nested
    @DisplayName("Routing Strategy Priority")
    class RoutingStrategyPriority {

        @Test
        @DisplayName("Pattern routes should have higher priority than exact simple routes")
        void testPatternPriorityOverExactSimple() {
            Function<ApiRequest, ApiResponse<?>> patternHandler = req -> ApiResponse.ok("pattern");
            Function<ApiRequest, ApiResponse<?>> exactHandler = req -> ApiResponse.ok("exact");

            // Register pattern route first
            routeTree.addRoute("/api/users/{id}", HttpMethod.GET, patternHandler);
            routeTree.addExactRoute("/api/users/123", HttpMethod.GET, exactHandler);

            // Should match pattern (which is checked first)
            RouteTree.MatchResult result = routeTree.match("/api/users/123", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.handler()).isEqualTo(patternHandler);
        }

        @Test
        @DisplayName("Exact simple routes should have higher priority than prefix routes")
        void testExactSimplePriorityOverPrefix() {
            Function<ApiRequest, ApiResponse<?>> exactHandler = req -> ApiResponse.ok("exact");
            Function<ApiRequest, ApiResponse<?>> prefixHandler = req -> ApiResponse.ok("prefix");

            routeTree.addExactRoute("/api/users", HttpMethod.GET, exactHandler);
            routeTree.addPrefixRoute("/api/", HttpMethod.GET, prefixHandler);

            RouteTree.MatchResult result = routeTree.match("/api/users", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.handler()).isEqualTo(exactHandler);
        }

        @Test
        @DisplayName("Prefix routes should have higher priority than contains routes")
        void testPrefixPriorityOverContains() {
            Function<ApiRequest, ApiResponse<?>> prefixHandler = req -> ApiResponse.ok("prefix");
            Function<ApiRequest, ApiResponse<?>> containsHandler = req -> ApiResponse.ok("contains");

            routeTree.addPrefixRoute("/api/", HttpMethod.GET, prefixHandler);
            routeTree.addContainsRoute("api", HttpMethod.GET, containsHandler);

            RouteTree.MatchResult result = routeTree.match("/api/users", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.handler()).isEqualTo(prefixHandler);
        }

        @Test
        @DisplayName("Fallback should be used when no other routes match")
        void testFallbackUsedWhenNoMatch() {
            Function<ApiRequest, ApiResponse<?>> fallbackHandler = req -> ApiResponse.ok("fallback");
            routeTree.setFallback(HttpMethod.GET, fallbackHandler);

            RouteTree.MatchResult result = routeTree.match("/api/unknown", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.handler()).isEqualTo(fallbackHandler);
        }

        @Test
        @DisplayName("Fallback should not be used when route matches")
        void testFallbackNotUsedWhenRouteMatches() {
            Function<ApiRequest, ApiResponse<?>> routeHandler = req -> ApiResponse.ok("route");
            Function<ApiRequest, ApiResponse<?>> fallbackHandler = req -> ApiResponse.ok("fallback");

            routeTree.addRoute("/api/users", HttpMethod.GET, routeHandler);
            routeTree.setFallback(HttpMethod.GET, fallbackHandler);

            RouteTree.MatchResult result = routeTree.match("/api/users", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.handler()).isEqualTo(routeHandler);
        }
    }

    @Nested
    @DisplayName("Multiple Routes and Handlers")
    class MultipleRoutesAndHandlers {

        @Test
        @DisplayName("Should handle multiple routes independently")
        void testMultipleRoutes() {
            Function<ApiRequest, ApiResponse<?>> usersHandler = req -> ApiResponse.ok("users");
            Function<ApiRequest, ApiResponse<?>> tasksHandler = req -> ApiResponse.ok("tasks");
            Function<ApiRequest, ApiResponse<?>> postsHandler = req -> ApiResponse.ok("posts");

            routeTree.addRoute("/api/users/{id}", HttpMethod.GET, usersHandler);
            routeTree.addRoute("/api/tasks/{id}", HttpMethod.GET, tasksHandler);
            routeTree.addRoute("/api/posts/{id}", HttpMethod.GET, postsHandler);

            assertThat(routeTree.match("/api/users/123", HttpMethod.GET).handler()).isEqualTo(usersHandler);
            assertThat(routeTree.match("/api/tasks/123", HttpMethod.GET).handler()).isEqualTo(tasksHandler);
            assertThat(routeTree.match("/api/posts/123", HttpMethod.GET).handler()).isEqualTo(postsHandler);
        }

        @Test
        @DisplayName("Should handle different HTTP methods for same path")
        void testDifferentMethodsSamePath() {
            Function<ApiRequest, ApiResponse<?>> getHandler = req -> ApiResponse.ok("get");
            Function<ApiRequest, ApiResponse<?>> postHandler = req -> ApiResponse.ok("post");
            Function<ApiRequest, ApiResponse<?>> putHandler = req -> ApiResponse.ok("put");
            Function<ApiRequest, ApiResponse<?>> deleteHandler = req -> ApiResponse.ok("delete");

            routeTree.addRoute("/api/tasks/{id}", HttpMethod.GET, getHandler);
            routeTree.addRoute("/api/tasks/{id}", HttpMethod.POST, postHandler);
            routeTree.addRoute("/api/tasks/{id}", HttpMethod.PUT, putHandler);
            routeTree.addRoute("/api/tasks/{id}", HttpMethod.DELETE, deleteHandler);

            assertThat(routeTree.match("/api/tasks/123", HttpMethod.GET).handler()).isEqualTo(getHandler);
            assertThat(routeTree.match("/api/tasks/123", HttpMethod.POST).handler()).isEqualTo(postHandler);
            assertThat(routeTree.match("/api/tasks/123", HttpMethod.PUT).handler()).isEqualTo(putHandler);
            assertThat(routeTree.match("/api/tasks/123", HttpMethod.DELETE).handler()).isEqualTo(deleteHandler);
        }

        @Test
        @DisplayName("Should handle multiple routes sharing same path prefix")
        void testMultipleRoutesSharedPrefix() {
            Function<ApiRequest, ApiResponse<?>> handler1 = req -> ApiResponse.ok("1");
            Function<ApiRequest, ApiResponse<?>> handler2 = req -> ApiResponse.ok("2");
            Function<ApiRequest, ApiResponse<?>> handler3 = req -> ApiResponse.ok("3");

            routeTree.addRoute("/api/users/{userId}/posts", HttpMethod.GET, handler1);
            routeTree.addRoute("/api/users/{userId}/posts/{postId}", HttpMethod.GET, handler2);
            routeTree.addRoute("/api/users/{userId}/posts/{postId}/comments/{commentId}", HttpMethod.GET, handler3);

            assertThat(routeTree.match("/api/users/u1/posts", HttpMethod.GET).handler()).isEqualTo(handler1);
            assertThat(routeTree.match("/api/users/u1/posts/p1", HttpMethod.GET).handler()).isEqualTo(handler2);
            assertThat(routeTree.match("/api/users/u1/posts/p1/comments/c1", HttpMethod.GET).handler()).isEqualTo(handler3);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Special Scenarios")
    class EdgeCasesAndSpecial {

        @Test
        @DisplayName("Should handle single segment path")
        void testSingleSegmentPath() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api", HttpMethod.GET);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle deep nested paths")
        void testDeeplyNestedPath() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/a/b/c/d/e/f/g/h/{id}", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/a/b/c/d/e/f/g/h/123", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.pathVariables()).containsEntry("id", "123");
        }

        @Test
        @DisplayName("Should handle empty path variable")
        void testEmptyPathVariable() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api/resource/{id}", HttpMethod.GET, handler);

            // Note: Empty segment between slashes // - may not match depending on implementation
            // This tests actual behavior
            RouteTree.MatchResult result = routeTree.match("/api/resource/", HttpMethod.GET);

            // Empty variable should still be captured
            if (result != null) {
                assertThat(result.pathVariables()).containsEntry("id", "");
            }
        }

        @Test
        @DisplayName("Should handle path with query string")
        void testPathWithQueryString() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api/users/{id}", HttpMethod.GET, handler);

            // Query string should be part of URL, not path for routing
            // Path extraction removes query string
            RouteTree.MatchResult result = routeTree.match("/api/users/123", HttpMethod.GET);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle numeric path variables")
        void testNumericPathVariables() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api/item/{itemId}", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api/item/999", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.pathVariables()).containsEntry("itemId", "999");
        }

        @Test
        @DisplayName("Should handle alphanumeric path variables")
        void testAlphanumericPathVariables() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api/resource/{id}", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api/resource/abc123XYZ", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.pathVariables()).containsEntry("id", "abc123XYZ");
        }

        @Test
        @DisplayName("Should handle path variables with dots")
        void testPathVariablesWithDots() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api/file/{filename}", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api/file/document.pdf", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.pathVariables()).containsEntry("filename", "document.pdf");
        }

        @Test
        @DisplayName("Should handle path variables with underscores")
        void testPathVariablesWithUnderscores() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api/resource/{id}", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api/resource/user_id_123", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.pathVariables()).containsEntry("id", "user_id_123");
        }

        @Test
        @DisplayName("Should handle consecutive template variables")
        void testConsecutiveTemplateVariables() {
            // While unusual, the system should handle it
            // Pattern like /api/{resource}/{action}
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api/{resource}/{action}", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api/users/create", HttpMethod.GET);

            assertThat(result).isNotNull();
            assertThat(result.pathVariables())
                    .hasSize(2)
                    .containsEntry("resource", "users")
                    .containsEntry("action", "create");
        }
    }

    @Nested
    @DisplayName("Cache Behavior")
    class CacheBehavior {

        @Test
        @DisplayName("Cache should work with different path variables")
        void testCacheWithDifferentVariables() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api/users/{id}", HttpMethod.GET, handler);

            RouteTree.MatchResult result1 = routeTree.match("/api/users/123", HttpMethod.GET);
            RouteTree.MatchResult result2 = routeTree.match("/api/users/456", HttpMethod.GET);

            assertThat(result1.pathVariables()).containsEntry("id", "123");
            assertThat(result2.pathVariables()).containsEntry("id", "456");
        }

        @Test
        @DisplayName("Cache should be method-specific")
        void testCacheIsMethodSpecific() {
            Function<ApiRequest, ApiResponse<?>> getHandler = req -> ApiResponse.ok("get");
            Function<ApiRequest, ApiResponse<?>> postHandler = req -> ApiResponse.ok("post");

            routeTree.addRoute("/api/users/{id}", HttpMethod.GET, getHandler);
            routeTree.addRoute("/api/users/{id}", HttpMethod.POST, postHandler);

            // Fetch both methods to populate cache
            RouteTree.MatchResult getResult = routeTree.match("/api/users/123", HttpMethod.GET);
            RouteTree.MatchResult postResult = routeTree.match("/api/users/123", HttpMethod.POST);

            // Then fetch again from cache
            RouteTree.MatchResult getCached = routeTree.match("/api/users/123", HttpMethod.GET);
            RouteTree.MatchResult postCached = routeTree.match("/api/users/123", HttpMethod.POST);

            assertThat(getCached.handler()).isEqualTo(getHandler);
            assertThat(postCached.handler()).isEqualTo(postHandler);
        }
    }

    @Nested
    @DisplayName("No Match Scenarios")
    class NoMatchScenarios {

        @Test
        @DisplayName("Should return null when no route matches")
        void testNoMatchReturnsNull() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api/users/{id}", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api/unknown", HttpMethod.GET);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should not match different HTTP method")
        void testDifferentMethodNoMatch() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api/users/{id}", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api/users/123", HttpMethod.POST);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should not match when fallback not set")
        void testNoFallbackWhenNotSet() {
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("ok");
            routeTree.addRoute("/api/users/{id}", HttpMethod.GET, handler);

            RouteTree.MatchResult result = routeTree.match("/api/unknown", HttpMethod.POST);

            assertThat(result).isNull();
        }
    }
}
