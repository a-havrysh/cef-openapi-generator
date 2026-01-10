package com.example.api.benchmark;

import com.example.api.protocol.ApiRequest;
import com.example.api.protocol.ApiResponse;
import com.example.api.protocol.HttpMethod;
import com.example.api.routing.RouteTree;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Benchmark for RouteTree scalability.
 * Tests performance with different tree sizes: 100, 1000, 10000 routes.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class LargeTreeBenchmark {

    @State(Scope.Benchmark)
    public static class Tree100 {
        RouteTree routeTree;
        List<String> testPaths;

        @Setup
        public void setup() {
            routeTree = new RouteTree();
            testPaths = new ArrayList<>();
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("test");

            for (int i = 0; i < 100; i++) {
                String pattern = "/api/resource" + i + "/{id}";
                routeTree.addRoute(pattern, HttpMethod.GET, handler);
                testPaths.add("/api/resource" + i + "/item-123");
            }
        }
    }

    @State(Scope.Benchmark)
    public static class Tree1000 {
        RouteTree routeTree;
        List<String> testPaths;

        @Setup
        public void setup() {
            routeTree = new RouteTree();
            testPaths = new ArrayList<>();
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("test");

            for (int i = 0; i < 1000; i++) {
                String pattern = "/api/resource" + i + "/{id}";
                routeTree.addRoute(pattern, HttpMethod.GET, handler);
                testPaths.add("/api/resource" + i + "/item-123");
            }
        }
    }

    @State(Scope.Benchmark)
    public static class Tree10000 {
        RouteTree routeTree;
        List<String> testPaths;

        @Setup
        public void setup() {
            routeTree = new RouteTree();
            testPaths = new ArrayList<>();
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("test");

            for (int i = 0; i < 10000; i++) {
                String pattern = "/api/resource" + i + "/{id}";
                routeTree.addRoute(pattern, HttpMethod.GET, handler);
                testPaths.add("/api/resource" + i + "/item-123");
            }
        }
    }

    @State(Scope.Benchmark)
    public static class DeepNesting {
        RouteTree routeTree;
        String deepPath;

        @Setup
        public void setup() {
            routeTree = new RouteTree();
            Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("test");

            // Create deeply nested route (10 levels)
            StringBuilder pattern = new StringBuilder();
            StringBuilder testPath = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                pattern.append("/level").append(i).append("/{id").append(i).append("}");
                testPath.append("/level").append(i).append("/value").append(i);
            }

            routeTree.addRoute(pattern.toString(), HttpMethod.GET, handler);
            deepPath = testPath.toString();
        }
    }

    @Benchmark
    public void benchmark100Routes(Tree100 state, Blackhole bh) {
        for (String path : state.testPaths) {
            bh.consume(state.routeTree.match(path, HttpMethod.GET));
        }
    }

    @Benchmark
    public void benchmark1000Routes(Tree1000 state, Blackhole bh) {
        for (String path : state.testPaths) {
            bh.consume(state.routeTree.match(path, HttpMethod.GET));
        }
    }

    @Benchmark
    public void benchmark10000Routes(Tree10000 state, Blackhole bh) {
        // Sample 100 paths to keep benchmark duration reasonable
        for (int i = 0; i < 100; i++) {
            String path = state.testPaths.get(i * 100);
            bh.consume(state.routeTree.match(path, HttpMethod.GET));
        }
    }

    @Benchmark
    public void benchmarkDeepNesting(DeepNesting state, Blackhole bh) {
        bh.consume(state.routeTree.match(state.deepPath, HttpMethod.GET));
    }
}
