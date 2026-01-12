package com.example.api.benchmark;

import com.example.api.protocol.ApiRequest;
import com.example.api.protocol.ApiResponse;
import com.example.api.protocol.HttpMethod;
import com.example.api.routing.RouteTree;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Benchmark comparing RouteTree (trie-based) vs regex pattern matching.
 * Goal: Validate "2.6x faster than regex" claim in README.
 *
 * Expected result: RouteTree throughput >= 2.5x regex matcher throughput
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class RouteTreeVsRegexBenchmark {

    private RouteTree routeTree;
    private List<RegexRoute> regexRoutes;
    private List<String> testPaths;

    @Setup
    public void setup() {
        // Initialize RouteTree
        routeTree = new RouteTree();
        regexRoutes = new ArrayList<>();

        Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("test");

        // Add 100 realistic API routes
        String[] patterns = {
            "/api/users/{id}",
            "/api/users/{id}/posts",
            "/api/users/{id}/posts/{postId}",
            "/api/users/{id}/comments",
            "/api/posts/{id}",
            "/api/posts/{id}/comments",
            "/api/products/{id}",
            "/api/products/{id}/reviews",
            "/api/orders/{orderId}",
            "/api/orders/{orderId}/items",
            "/api/categories/{categoryId}",
            "/api/categories/{categoryId}/products",
            "/api/auth/login",
            "/api/auth/logout",
            "/api/auth/refresh",
            "/api/search/users",
            "/api/search/products",
            "/api/admin/users/{userId}",
            "/api/admin/roles/{roleId}",
            "/api/admin/permissions"
        };

        // Expand to 100 routes by adding variants
        for (int i = 0; i < 5; i++) {
            for (String pattern : patterns) {
                String versioned = "/v" + (i + 1) + pattern;

                // Add to RouteTree
                routeTree.addRoute(versioned, HttpMethod.GET, handler);

                // Convert to regex and add to regex routes
                String regex = patternToRegex(versioned);
                regexRoutes.add(new RegexRoute(Pattern.compile(regex), handler));
            }
        }

        // Generate test paths (mix of matching and non-matching)
        testPaths = new ArrayList<>();
        testPaths.add("/v1/api/users/123");
        testPaths.add("/v2/api/users/456/posts");
        testPaths.add("/v3/api/products/789");
        testPaths.add("/v1/api/orders/abc-123");
        testPaths.add("/v2/api/search/users");
        testPaths.add("/v4/api/admin/users/admin");
        testPaths.add("/v5/api/categories/electronics");
        testPaths.add("/v1/api/posts/999/comments");
        testPaths.add("/v3/api/users/user-xyz/posts/post-123");
        testPaths.add("/v2/api/products/prod-456/reviews");

        // Add some non-matching paths
        testPaths.add("/api/nonexistent");
        testPaths.add("/v6/api/unknown");
        testPaths.add("/other/path");

        System.out.println("Setup complete: " + regexRoutes.size() + " routes, " + testPaths.size() + " test paths");
    }

    @Benchmark
    public void benchmarkRouteTree(Blackhole bh) {
        for (String path : testPaths) {
            RouteTree.MatchResult result = routeTree.match(path, HttpMethod.GET);
            bh.consume(result);
        }
    }

    @Benchmark
    public void benchmarkRegexMatcher(Blackhole bh) {
        for (String path : testPaths) {
            MatchResult result = matchWithRegex(path);
            bh.consume(result);
        }
    }

    private MatchResult matchWithRegex(String path) {
        for (RegexRoute route : regexRoutes) {
            Matcher matcher = route.pattern.matcher(path);
            if (matcher.matches()) {
                // Extract path variables
                Map<String, String> vars = new HashMap<>();
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    vars.put("var" + i, matcher.group(i));
                }
                return new MatchResult(route.handler, vars);
            }
        }
        return null;
    }

    private String patternToRegex(String pattern) {
        // Convert OpenAPI pattern to regex: /api/users/{id} -> /api/users/([^/]+)
        return pattern.replaceAll("\\{[^}]+\\}", "([^/]+)");
    }

    // Regex route holder
    private static class RegexRoute {
        final Pattern pattern;
        final Function<ApiRequest, ApiResponse<?>> handler;

        RegexRoute(Pattern pattern, Function<ApiRequest, ApiResponse<?>> handler) {
            this.pattern = pattern;
            this.handler = handler;
        }
    }

    // Match result for regex
    private static class MatchResult {
        final Function<ApiRequest, ApiResponse<?>> handler;
        final Map<String, String> pathVariables;

        MatchResult(Function<ApiRequest, ApiResponse<?>> handler, Map<String, String> pathVariables) {
            this.handler = handler;
            this.pathVariables = pathVariables;
        }
    }
}
