package com.example.api.benchmark;

import com.example.api.protocol.ApiRequest;
import com.example.api.protocol.ApiResponse;
import com.example.api.protocol.HttpMethod;
import com.example.api.routing.RouteTree;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Benchmark for RouteTree performance characteristics.
 * Measures different route type matching performance.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class RouteTreeBenchmark {

    private RouteTree routeTree;
    private Function<ApiRequest, ApiResponse<?>> handler;

    @Setup
    public void setup() {
        routeTree = new RouteTree();
        handler = req -> ApiResponse.ok("test");

        // Add exact routes
        for (int i = 0; i < 10; i++) {
            routeTree.addExactRoute("/api/exact/" + i, HttpMethod.GET, handler);
        }

        // Add pattern routes
        for (int i = 0; i < 50; i++) {
            routeTree.addRoute("/api/pattern/" + i + "/{id}", HttpMethod.GET, handler);
        }

        // Add prefix routes
        routeTree.addPrefixRoute("/static", HttpMethod.GET, handler);
        routeTree.addPrefixRoute("/assets", HttpMethod.GET, handler);

        // Add contains routes
        routeTree.addContainsRoute(".min.", HttpMethod.GET, handler);
        routeTree.addContainsRoute(".bundle.", HttpMethod.GET, handler);
    }

    @Benchmark
    public void benchmarkExactRouteMatch(Blackhole bh) {
        bh.consume(routeTree.match("/api/exact/5", HttpMethod.GET));
    }

    @Benchmark
    public void benchmarkPatternRouteMatch(Blackhole bh) {
        bh.consume(routeTree.match("/api/pattern/25/user-123", HttpMethod.GET));
    }

    @Benchmark
    public void benchmarkPrefixRouteMatch(Blackhole bh) {
        bh.consume(routeTree.match("/static/css/style.css", HttpMethod.GET));
    }

    @Benchmark
    public void benchmarkContainsRouteMatch(Blackhole bh) {
        bh.consume(routeTree.match("/js/app.min.js", HttpMethod.GET));
    }

    @Benchmark
    public void benchmarkCacheHit(Blackhole bh) {
        // First access to warm cache
        routeTree.match("/api/pattern/10/cached-item", HttpMethod.GET);

        // Benchmark cached access
        bh.consume(routeTree.match("/api/pattern/10/cached-item", HttpMethod.GET));
    }

    @Benchmark
    public void benchmarkCacheMiss(Blackhole bh) {
        // Always different paths - cache miss
        bh.consume(routeTree.match("/api/pattern/15/" + System.nanoTime(), HttpMethod.GET));
    }
}
