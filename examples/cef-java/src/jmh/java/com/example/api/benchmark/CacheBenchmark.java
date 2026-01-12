package com.example.api.benchmark;

import com.example.api.protocol.ApiRequest;
import com.example.api.protocol.ApiResponse;
import com.example.api.protocol.HttpMethod;
import com.example.api.routing.RouteTree;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Benchmark for RouteTree LRU cache effectiveness.
 * Tests cache hit rate, miss rate, and eviction performance.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class CacheBenchmark {

    private RouteTree routeTree;
    private List<String> hitPaths;  // Paths that will hit cache
    private List<String> missPaths; // Paths that will miss cache
    private List<String> mixedPaths; // 80% hit, 20% miss

    @Setup
    public void setup() {
        routeTree = new RouteTree();
        Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("test");

        // Add pattern routes (these will be cached)
        for (int i = 0; i < 20; i++) {
            routeTree.addRoute("/api/items/{id}", HttpMethod.GET, handler);
        }

        // Prepare test paths
        hitPaths = new ArrayList<>();
        missPaths = new ArrayList<>();
        mixedPaths = new ArrayList<>();

        // Warm cache with 50 paths
        for (int i = 0; i < 50; i++) {
            String path = "/api/items/" + i;
            routeTree.match(path, HttpMethod.GET);
            hitPaths.add(path);
        }

        // Prepare miss paths (unique on each benchmark invocation)
        for (int i = 1000; i < 1200; i++) {
            missPaths.add("/api/items/" + i);
        }

        // Prepare mixed workload: 80% from cached, 20% new
        Random random = new Random(42);
        for (int i = 0; i < 100; i++) {
            if (random.nextDouble() < 0.8) {
                // 80% - pick from cached paths
                mixedPaths.add(hitPaths.get(random.nextInt(hitPaths.size())));
            } else {
                // 20% - new path
                mixedPaths.add("/api/items/new-" + i);
            }
        }
    }

    @Benchmark
    public void benchmarkCacheHitRate100Percent(Blackhole bh) {
        // All paths are in cache
        for (String path : hitPaths) {
            bh.consume(routeTree.match(path, HttpMethod.GET));
        }
    }

    @Benchmark
    public void benchmarkCacheMissRate100Percent(Blackhole bh) {
        // All paths are cache misses
        for (String path : missPaths) {
            bh.consume(routeTree.match(path, HttpMethod.GET));
        }
    }

    @Benchmark
    public void benchmarkMixedWorkload80_20(Blackhole bh) {
        // 80% hit, 20% miss - realistic scenario
        for (String path : mixedPaths) {
            bh.consume(routeTree.match(path, HttpMethod.GET));
        }
    }

    @Benchmark
    public void benchmarkLruEviction(Blackhole bh) {
        // Force cache eviction by adding 110 entries (exceeds 100 limit)
        for (int i = 0; i < 110; i++) {
            bh.consume(routeTree.match("/api/items/eviction-" + i, HttpMethod.GET));
        }
    }
}
