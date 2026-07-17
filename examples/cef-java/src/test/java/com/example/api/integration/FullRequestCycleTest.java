package com.example.api.integration;

import com.example.api.cef.ApiCefRequestHandler;
import com.example.api.protocol.ApiRequest;
import com.example.api.protocol.ApiResponse;
import com.example.api.protocol.HttpMethod;
import com.example.api.routing.RouteTree;
import com.example.api.mock.MockCefFactory;
import com.intellij.openapi.project.Project;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefResourceHandler;
import org.cef.handler.CefResourceRequestHandler;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

/**
 * Integration test for full request cycle:
 * CEF request → ApiCefRequestHandler → RouteTree → handler → ApiResponse
 */
class FullRequestCycleTest {

    private Project mockProject;

    @BeforeEach
    void setUp() {
        mockProject = MockCefFactory.createMockProject();
    }

    @Test
    void testFullGetRequestCycle() {
        // Given: Handler with GET route
        Function<ApiRequest, ApiResponse<?>> handler = request -> {
            String id = request.getPathVariable("id");
            return ApiResponse.ok("User " + id);
        };

        ApiCefRequestHandler apiHandler = ApiCefRequestHandler.builder(mockProject)
            .withRoute("/api/users/{id}", HttpMethod.GET, handler)
            .build();

        // When: Process GET request
        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "http://localhost:5173/api/users/123",
            "GET"
        );
        CefBrowser browser = MockCefFactory.createMockBrowser();
        CefFrame frame = MockCefFactory.createMockFrame();

        CefResourceRequestHandler resourceHandler = apiHandler.getResourceRequestHandler(
            browser, frame, cefRequest, false, false, null, null
        );

        // Then: Request is handled
        assertNotNull(resourceHandler, "Handler should process matching request");
    }

    @Test
    void testFullPostRequestCycle() {
        // Given: Handler with POST route
        Function<ApiRequest, ApiResponse<?>> handler = request -> {
            Object body = request.getBody(Object.class);
            return ApiResponse.created(body);
        };

        ApiCefRequestHandler apiHandler = ApiCefRequestHandler.builder(mockProject)
            .withExact("/api/tasks", HttpMethod.POST, handler)
            .build();

        // When: Process POST request with body
        String jsonBody = "{\"title\":\"New Task\"}";
        CefRequest cefRequest = MockCefFactory.createMockRequestWithBody(
            "http://localhost:5173/api/tasks",
            "POST",
            jsonBody
        );
        CefBrowser browser = MockCefFactory.createMockBrowser();
        CefFrame frame = MockCefFactory.createMockFrame();

        CefResourceRequestHandler resourceHandler = apiHandler.getResourceRequestHandler(
            browser, frame, cefRequest, false, false, null, null
        );

        // Then: Request is handled
        assertNotNull(resourceHandler);
    }

    @Test
    void testPathVariablesInFullCycle() {
        // Given: Route with multiple path variables
        Function<ApiRequest, ApiResponse<?>> handler = request -> {
            String userId = request.getPathVariable("userId");
            String postId = request.getPathVariable("postId");
            return ApiResponse.ok("User " + userId + ", Post " + postId);
        };

        ApiCefRequestHandler apiHandler = ApiCefRequestHandler.builder(mockProject)
            .withRoute("/api/users/{userId}/posts/{postId}", HttpMethod.GET, handler)
            .build();

        // When: Process request
        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "http://localhost:5173/api/users/42/posts/999",
            "GET"
        );

        CefResourceRequestHandler resourceHandler = apiHandler.getResourceRequestHandler(
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame(),
            cefRequest,
            false, false, null, null
        );

        // Then: Handler processes with path variables
        assertNotNull(resourceHandler);
    }

    @Test
    void testQueryParamsInFullCycle() {
        // Given: Handler using query parameters
        Function<ApiRequest, ApiResponse<?>> handler = request -> {
            String page = request.getQueryParam("page");
            String size = request.getQueryParam("size");
            return ApiResponse.ok("Page " + page + ", Size " + size);
        };

        ApiCefRequestHandler apiHandler = ApiCefRequestHandler.builder(mockProject)
            .withExact("/api/tasks", HttpMethod.GET, handler)
            .build();

        // When: Process request with query params
        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "http://localhost:5173/api/tasks?page=1&size=20",
            "GET"
        );

        CefResourceRequestHandler resourceHandler = apiHandler.getResourceRequestHandler(
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame(),
            cefRequest,
            false, false, null, null
        );

        // Then: Handler processes with query params
        assertNotNull(resourceHandler);
    }

    @Test
    void testNotFoundInFullCycle() {
        // Given: Handler for specific route
        Function<ApiRequest, ApiResponse<?>> handler = request -> ApiResponse.ok("Found");

        ApiCefRequestHandler apiHandler = ApiCefRequestHandler.builder(mockProject)
            .withExact("/api/existing", HttpMethod.GET, handler)
            .build();

        // When: Request non-existent route
        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "http://localhost:5173/api/nonexistent",
            "GET"
        );

        CefResourceRequestHandler resourceHandler = apiHandler.getResourceRequestHandler(
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame(),
            cefRequest,
            false, false, null, null
        );

        // Then: Returns null (no handler)
        assertNull(resourceHandler, "Non-matching route should return null");
    }

    @Test
    void testMultipleRoutesInFullCycle() {
        // Given: Multiple routes
        Function<ApiRequest, ApiResponse<?>> getUsersHandler = req -> ApiResponse.ok("Users list");
        Function<ApiRequest, ApiResponse<?>> getUserHandler = req -> ApiResponse.ok("User " + req.getPathVariable("id"));
        Function<ApiRequest, ApiResponse<?>> createUserHandler = req -> ApiResponse.created("Created");

        ApiCefRequestHandler apiHandler = ApiCefRequestHandler.builder(mockProject)
            .withExact("/api/users", HttpMethod.GET, getUsersHandler)
            .withRoute("/api/users/{id}", HttpMethod.GET, getUserHandler)
            .withExact("/api/users", HttpMethod.POST, createUserHandler)
            .build();

        // When/Then: Each route works
        CefRequest getListRequest = MockCefFactory.createMockRequest("http://localhost:5173/api/users", "GET");
        assertNotNull(apiHandler.getResourceRequestHandler(
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame(),
            getListRequest, false, false, null, null
        ));

        CefRequest getUserRequest = MockCefFactory.createMockRequest("http://localhost:5173/api/users/123", "GET");
        assertNotNull(apiHandler.getResourceRequestHandler(
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame(),
            getUserRequest, false, false, null, null
        ));

        CefRequest createRequest = MockCefFactory.createMockRequest("http://localhost:5173/api/users", "POST");
        assertNotNull(apiHandler.getResourceRequestHandler(
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame(),
            createRequest, false, false, null, null
        ));
    }

    @Test
    void testUrlFilteringInFullCycle() {
        // Given: Handler with URL filtering
        Function<ApiRequest, ApiResponse<?>> handler = req -> ApiResponse.ok("test");

        ApiCefRequestHandler apiHandler = ApiCefRequestHandler.builder(mockProject)
            .withUrlFilter("http://localhost:5173")
            .withExact("/api/resource", HttpMethod.GET, handler)
            .build();

        // When: Request with matching URL prefix
        CefRequest matchingRequest = MockCefFactory.createMockRequest(
            "http://localhost:5173/api/resource",
            "GET"
        );

        CefResourceRequestHandler matchingHandler = apiHandler.getResourceRequestHandler(
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame(),
            matchingRequest, false, false, null, null
        );

        // Then: Matching URL is processed
        assertNotNull(matchingHandler);

        // When: Request with non-matching URL prefix
        CefRequest nonMatchingRequest = MockCefFactory.createMockRequest(
            "http://example.com/api/resource",
            "GET"
        );

        CefResourceRequestHandler nonMatchingHandler = apiHandler.getResourceRequestHandler(
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame(),
            nonMatchingRequest, false, false, null, null
        );

        // Then: Non-matching URL returns null
        assertNull(nonMatchingHandler);
    }

    @Test
    void testValidationActuallyRunsForRealRoutedRequest() {
        // Before the fix, ValidationInterceptor keyed its metadata by the registered route
        // pattern (e.g. "/api/tasks") but looked requests up by their resolved path — which is
        // identical here since this route has no path variables, but the interceptor's lookup
        // used request.getPath() directly with no route match in between. This test drives a
        // real request through the full CEF -> RouteTree -> ValidationInterceptor pipeline
        // (not a hand-mocked ApiRequest) to prove the enum constraint on `status` is enforced.
        ApiCefRequestHandler apiHandler = ApiCefRequestHandler.builder(mockProject)
            .withApiRoutes()
            .withValidation()
            .build();

        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "http://localhost:5173/api/tasks?status=not_a_real_status",
            "GET"
        );
        CefBrowser browser = MockCefFactory.createMockBrowser();
        CefFrame frame = MockCefFactory.createMockFrame();

        CefResourceRequestHandler resourceRequestHandler = apiHandler.getResourceRequestHandler(
            browser, frame, cefRequest, false, false, null, null
        );
        assertNotNull(resourceRequestHandler);

        CefResourceHandler resourceHandler = resourceRequestHandler.getResourceHandler(browser, frame, cefRequest);
        assertEquals(400, readStatus(resourceHandler, cefRequest));
    }

    @Test
    void testMethodMismatchReturnsFourOhFiveNotSilentFallback() {
        // /api/tasks only has GET and POST registered — DELETE should come back as a clean 405,
        // not silently fall through to a null resource handler (which previously degraded into
        // CEF's default static-resource handling).
        ApiCefRequestHandler apiHandler = ApiCefRequestHandler.builder(mockProject)
            .withApiRoutes()
            .build();

        CefRequest cefRequest = MockCefFactory.createMockRequest("http://localhost:5173/api/tasks", "DELETE");
        CefBrowser browser = MockCefFactory.createMockBrowser();
        CefFrame frame = MockCefFactory.createMockFrame();

        CefResourceRequestHandler resourceRequestHandler = apiHandler.getResourceRequestHandler(
            browser, frame, cefRequest, false, false, null, null
        );
        assertNotNull(resourceRequestHandler, "A structurally-known path should still route to the API handler");

        CefResourceHandler resourceHandler = resourceRequestHandler.getResourceHandler(browser, frame, cefRequest);
        assertEquals(405, readStatus(resourceHandler, cefRequest));
    }

    @Test
    void testMethodMismatchStillFourOhFiveWhenGetFallbackIsRegistered() {
        // Regression guard: a GET fallback (e.g. serving static webview assets for any path no
        // API route claims) must NOT swallow a 405 for a path that DOES have a registered route,
        // just under a different method. RouteTree.match() folds the fallback into its result,
        // so the 405 check has to run against matchStrict() (exact/pattern only) before ever
        // consulting match() — this is exactly the case a fallback-free test wouldn't catch.
        Function<ApiRequest, ApiResponse<?>> fallbackHandler = req -> ApiResponse.ok("static asset");

        ApiCefRequestHandler apiHandler = ApiCefRequestHandler.builder(mockProject)
            .withApiRoutes()
            .withFallback(HttpMethod.GET, fallbackHandler)
            .build();

        // /api/browser/notify is POST-only — GET should 405, not fall through to the GET fallback
        CefRequest cefRequest = MockCefFactory.createMockRequest("http://localhost:5173/api/browser/notify", "GET");
        CefBrowser browser = MockCefFactory.createMockBrowser();
        CefFrame frame = MockCefFactory.createMockFrame();

        CefResourceRequestHandler resourceRequestHandler = apiHandler.getResourceRequestHandler(
            browser, frame, cefRequest, false, false, null, null
        );
        assertNotNull(resourceRequestHandler);

        CefResourceHandler resourceHandler = resourceRequestHandler.getResourceHandler(browser, frame, cefRequest);
        assertEquals(405, readStatus(resourceHandler, cefRequest));
    }

    private int readStatus(CefResourceHandler resourceHandler, CefRequest cefRequest) {
        assertNotNull(resourceHandler);
        CefResponse response = mock(CefResponse.class, withSettings().lenient());
        resourceHandler.getResponseHeaders(response, new IntRef(), new StringRef());
        org.mockito.ArgumentCaptor<Integer> statusCaptor = org.mockito.ArgumentCaptor.forClass(Integer.class);
        verify(response).setStatus(statusCaptor.capture());
        return statusCaptor.getValue();
    }
}
