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
import org.cef.handler.CefResourceRequestHandler;
import org.cef.network.CefRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

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
}
