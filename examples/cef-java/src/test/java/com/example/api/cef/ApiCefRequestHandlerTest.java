package com.example.api.cef;

import com.example.api.protocol.ApiResponse;
import com.example.api.protocol.HttpMethod;
import com.example.api.routing.RouteTree;
import com.example.api.mock.MockCefFactory;
import com.intellij.openapi.project.Project;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefResourceRequestHandler;
import org.cef.network.CefRequest;
import org.cef.misc.BoolRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ApiCefRequestHandler Tests")
class ApiCefRequestHandlerTest {

    private Project mockProject;
    private CefBrowser mockBrowser;
    private CefFrame mockFrame;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockProject = MockCefFactory.createMockProject();
        mockBrowser = MockCefFactory.createMockBrowser();
        mockFrame = MockCefFactory.createMockFrame();
    }

    @Nested
    @DisplayName("ResourceRequestHandler Tests")
    class ResourceRequestHandlerTests {

        @Test
        @DisplayName("Should return ApiResourceRequestHandler for valid route matching")
        void testValidRouteMatchingReturnsHandler() {
            // Given: Handler with a registered route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/users/{id}", HttpMethod.GET, req -> ApiResponse.ok("user"))
                    .build();

            // When: Request matches the route
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/users/123",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should return resource handler
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should return null for non-matching route")
        void testNonMatchingRouteReturnsNull() {
            // Given: Handler with a specific route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/users/{id}", HttpMethod.GET, req -> ApiResponse.ok("user"))
                    .build();

            // When: Request does not match any route
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/products/456",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should return null
            assertThat(resourceHandler).isNull();
        }

        @Test
        @DisplayName("Should filter requests based on URL prefixes")
        void testUrlFilteringWithValidPrefix() {
            // Given: Handler with URL prefix filter
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter("http://localhost:5173")
                    .withRoute("/api/users/{id}", HttpMethod.GET, req -> ApiResponse.ok("user"))
                    .build();

            // When: Request matches allowed prefix and route
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/users/123",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should return handler
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should reject requests from non-matching URL prefixes")
        void testUrlFilteringWithInvalidPrefix() {
            // Given: Handler with URL prefix filter
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter("http://localhost:5173")
                    .withRoute("/api/users/{id}", HttpMethod.GET, req -> ApiResponse.ok("user"))
                    .build();

            // When: Request does not match allowed prefix
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://malicious.com/api/users/123",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should return null
            assertThat(resourceHandler).isNull();
        }

        @Test
        @DisplayName("Should handle null URL gracefully")
        void testNullUrlHandling() {
            // Given: Handler with URL prefix filter
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter("http://localhost:5173")
                    .build();

            // When: Request has null URL
            CefRequest cefRequest = MockCefFactory.createMockRequest(null, "GET");

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should return null
            assertThat(resourceHandler).isNull();
        }

        @Test
        @DisplayName("Should match GET requests correctly")
        void testGetMethodMatching() {
            // Given: Handler with GET route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // When: GET request is made
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/items",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should return handler
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should match POST requests correctly")
        void testPostMethodMatching() {
            // Given: Handler with POST route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/items", HttpMethod.POST, req -> ApiResponse.created("item"))
                    .build();

            // When: POST request is made
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/items",
                    "POST"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should return handler
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should match PUT requests correctly")
        void testPutMethodMatching() {
            // Given: Handler with PUT route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/items/{id}", HttpMethod.PUT, req -> ApiResponse.ok("updated"))
                    .build();

            // When: PUT request is made
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/items/123",
                    "PUT"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should return handler
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should match PATCH requests correctly")
        void testPatchMethodMatching() {
            // Given: Handler with PATCH route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/items/{id}", HttpMethod.PATCH, req -> ApiResponse.ok("patched"))
                    .build();

            // When: PATCH request is made
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/items/456",
                    "PATCH"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should return handler
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should match DELETE requests correctly")
        void testDeleteMethodMatching() {
            // Given: Handler with DELETE route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/items/{id}", HttpMethod.DELETE, req -> ApiResponse.ok("deleted"))
                    .build();

            // When: DELETE request is made
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/items/789",
                    "DELETE"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should return handler
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should handle navigation requests")
        void testNavigationRequestHandling() {
            // Given: Handler with a route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/users/{id}", HttpMethod.GET, req -> ApiResponse.ok("user"))
                    .build();

            // When: Navigation request is made
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/users/123",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, true, false, null, null
            );

            // Then: Should still return handler
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should handle download requests")
        void testDownloadRequestHandling() {
            // Given: Handler with a route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/files", HttpMethod.GET, req -> ApiResponse.ok("file"))
                    .build();

            // When: Download request is made
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/files",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, true, null, null
            );

            // Then: Should return handler if route matches
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should handle multiple URL prefixes in filter")
        void testMultiplePrefixesInFilter() {
            // Given: Handler with multiple URL prefixes
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter("http://localhost:5173", "http://localhost:8080")
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // When: Request from first allowed prefix
            CefRequest cefRequest1 = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/items",
                    "GET"
            );
            CefResourceRequestHandler handler1 = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest1, false, false, null, null
            );

            // Then: Should return handler
            assertThat(handler1).isNotNull();

            // When: Request from second allowed prefix
            CefRequest cefRequest2 = MockCefFactory.createMockRequest(
                    "http://localhost:8080/api/items",
                    "GET"
            );
            CefResourceRequestHandler handler2 = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest2, false, false, null, null
            );

            // Then: Should return handler
            assertThat(handler2).isNotNull();
        }
    }

    @Nested
    @DisplayName("URL Extraction Tests")
    class UrlExtractionTests {

        @Test
        @DisplayName("Should extract path from valid URL")
        void testPathExtractionFromValidUrl() {
            // Given: Handler with exact route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withExact("/api/users", HttpMethod.GET, req -> ApiResponse.ok("users"))
                    .build();

            // When: Request with valid URL is made
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/users",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Path should be extracted correctly and handler matched
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should handle URLs with query parameters")
        void testUrlWithQueryParameters() {
            // Given: Handler with exact route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withExact("/api/users", HttpMethod.GET, req -> ApiResponse.ok("users"))
                    .build();

            // When: Request with query parameters
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/users?page=1&size=10",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Query parameters should be ignored, path matched
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should handle URLs with fragments")
        void testUrlWithFragments() {
            // Given: Handler with exact route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withExact("/api/docs", HttpMethod.GET, req -> ApiResponse.ok("docs"))
                    .build();

            // When: Request with fragment
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/docs#section-2",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Fragment should be ignored, path matched
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should return empty string for invalid URLs")
        void testInvalidUrlHandling() {
            // Given: Handler with a route that won't match empty path
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/users/{id}", HttpMethod.GET, req -> ApiResponse.ok("user"))
                    .build();

            // When: Request with malformed URL
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "not a valid url",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should return null as path won't match
            assertThat(resourceHandler).isNull();
        }

        @Test
        @DisplayName("Should handle null URLs")
        void testNullUrlExtraction() {
            // Given: Handler with a route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // When: Request with null URL
            CefRequest cefRequest = MockCefFactory.createMockRequest(null, "GET");

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should handle gracefully and return null
            assertThat(resourceHandler).isNull();
        }

        @Test
        @DisplayName("Should handle different protocols (http)")
        void testHttpProtocol() {
            // Given: Handler with a route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withExact("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // When: Request with http protocol
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/items",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should work correctly
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should handle different protocols (https)")
        void testHttpsProtocol() {
            // Given: Handler with a route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withExact("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // When: Request with https protocol
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "https://localhost:5173/api/items",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should work correctly
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should handle URLs with ports")
        void testUrlsWithPorts() {
            // Given: Handler with a route
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withExact("/api/users", HttpMethod.GET, req -> ApiResponse.ok("users"))
                    .build();

            // When: Request with port number
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:8080/api/users",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should extract path correctly
            assertThat(resourceHandler).isNotNull();
        }
    }

    @Nested
    @DisplayName("URL Filtering Tests")
    class UrlFilteringTests {

        @Test
        @DisplayName("Should match exact URL prefixes")
        void testExactPrefixMatching() {
            // Given: Handler with prefix filter
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter("http://localhost:5173")
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // When: Request with exact prefix
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/items",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should return handler
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should be case-sensitive when matching prefixes")
        void testCaseSensitivePrefixMatching() {
            // Given: Handler with prefix filter
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter("http://localhost:5173")
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // When: Request with different case
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "HTTP://LOCALHOST:5173/api/items",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should not match (case sensitive)
            assertThat(resourceHandler).isNull();
        }

        @Test
        @DisplayName("Should support multiple URL prefix filters")
        void testMultiplePrefixFiltering() {
            // Given: Handler with multiple prefixes
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter("http://localhost:5173", "http://app.example.com")
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // When: Request from one of the prefixes
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://app.example.com/api/items",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should return handler
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should return null for URL with null prefix list")
        void testNullUrlWithNullPrefixList() {
            // Given: Handler without URL filter (no prefixes)
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // When: Any URL is provided
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://any-domain.com/api/items",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should return handler (no filtering)
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should return null for empty prefix list")
        void testEmptyPrefixListReturnsNull() {
            // Given: Handler with empty URL filter (empty varargs array)
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter(new String[0])  // Empty array means empty list
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // When: Request is made
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/items",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should return null (empty filter means nothing matches)
            assertThat(resourceHandler).isNull();
        }

        @Test
        @DisplayName("Should reject URLs that do not match any prefix")
        void testNonMatchingUrlReturnsNull() {
            // Given: Handler with specific prefix
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter("http://localhost:5173")
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // When: Request from different domain
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://evil.com/api/items",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should return null
            assertThat(resourceHandler).isNull();
        }

        @Test
        @DisplayName("Should match prefix even with additional path")
        void testPrefixMatchingWithAdditionalPath() {
            // Given: Handler with prefix filter
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter("http://localhost:5173")
                    .withRoute("/app/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // When: Request includes the domain prefix with more path
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/app/api/items",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should match the prefix and route
            assertThat(resourceHandler).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder Integration Tests")
    class BuilderIntegrationTests {

        @Test
        @DisplayName("Should create working handler via builder")
        void testHandlerCreatedViaBuilderWorks() {
            // Given: Handler created via builder
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/test", HttpMethod.GET, req -> ApiResponse.ok("test"))
                    .build();

            // When: Request is made
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/test",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should work correctly
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should match routes added via builder")
        void testBuilderAddedRoutesAreMatched() {
            // Given: Handler with multiple routes via builder
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/users/{id}", HttpMethod.GET, req -> ApiResponse.ok("user"))
                    .withRoute("/api/posts/{id}", HttpMethod.GET, req -> ApiResponse.ok("post"))
                    .withRoute("/api/comments", HttpMethod.POST, req -> ApiResponse.created("comment"))
                    .build();

            // When: Request matches first route
            CefRequest cefRequest1 = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/users/123",
                    "GET"
            );
            CefResourceRequestHandler handler1 = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest1, false, false, null, null
            );

            // Then: Should return handler
            assertThat(handler1).isNotNull();

            // When: Request matches second route
            CefRequest cefRequest2 = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/posts/456",
                    "GET"
            );
            CefResourceRequestHandler handler2 = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest2, false, false, null, null
            );

            // Then: Should return handler
            assertThat(handler2).isNotNull();

            // When: Request matches third route
            CefRequest cefRequest3 = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/comments",
                    "POST"
            );
            CefResourceRequestHandler handler3 = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest3, false, false, null, null
            );

            // Then: Should return handler
            assertThat(handler3).isNotNull();
        }

        @Test
        @DisplayName("Should apply URL filters configured by builder")
        void testBuilderConfiguredUrlFiltersWork() {
            // Given: Handler with URL filter via builder
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter("http://localhost:5173")
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // When: Valid request is made
            CefRequest validRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/items",
                    "GET"
            );
            CefResourceRequestHandler handlerValid = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, validRequest, false, false, null, null
            );

            // Then: Should return handler
            assertThat(handlerValid).isNotNull();

            // When: Invalid request is made
            CefRequest invalidRequest = MockCefFactory.createMockRequest(
                    "http://wrong-domain.com/api/items",
                    "GET"
            );
            CefResourceRequestHandler handlerInvalid = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, invalidRequest, false, false, null, null
            );

            // Then: Should return null
            assertThat(handlerInvalid).isNull();
        }

        @Test
        @DisplayName("Should register interceptors configured by builder")
        void testBuilderConfiguredInterceptorsWork() {
            // Given: Handler with CORS interceptor
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withCors()
                    .withValidation()
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // When: Request is made
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/items",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should still return handler with interceptors configured
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should use custom exception handler if configured")
        void testCustomExceptionHandlerConfiguration() {
            // Given: Handler with custom exception handler
            com.example.api.interceptor.ExceptionHandler customHandler =
                    (ex, req) -> ApiResponse.internalServerError("Custom error");

            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withExceptionHandler(customHandler)
                    .withRoute("/api/items", HttpMethod.GET, req -> ApiResponse.ok("items"))
                    .build();

            // When: Request is made
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/items",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Handler should be returned with exception handler configured
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should combine multiple builder features")
        void testComplexBuilderConfiguration() {
            // Given: Handler with multiple features
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withUrlFilter("http://localhost:5173")
                    .withCors()
                    .withValidation()
                    .withRoute("/api/users/{id}", HttpMethod.GET, req -> ApiResponse.ok("user"))
                    .withRoute("/api/users", HttpMethod.POST, req -> ApiResponse.created("user"))
                    .withPrefix("/api/v2", HttpMethod.GET, req -> ApiResponse.ok("v2"))
                    .build();

            // When: Request matches route with filters
            CefRequest cefRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/users/123",
                    "GET"
            );

            CefResourceRequestHandler resourceHandler = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, cefRequest, false, false, null, null
            );

            // Then: Should work correctly with all features
            assertThat(resourceHandler).isNotNull();
        }

        @Test
        @DisplayName("Should support all route types from builder")
        void testAllRouteTypesFromBuilder() {
            // Given: Handler with all route types
            ApiCefRequestHandler handler = ApiCefRequestHandler.builder(mockProject)
                    .withRoute("/api/pattern/{id}", HttpMethod.GET, req -> ApiResponse.ok("pattern"))
                    .withPrefix("/static", HttpMethod.GET, req -> ApiResponse.ok("static"))
                    .withExact("/health", HttpMethod.GET, req -> ApiResponse.ok("ok"))
                    .withContains(".json", HttpMethod.GET, req -> ApiResponse.ok("json"))
                    .build();

            // When: Pattern route request
            CefRequest patternRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/pattern/123",
                    "GET"
            );
            CefResourceRequestHandler handler1 = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, patternRequest, false, false, null, null
            );
            assertThat(handler1).isNotNull();

            // When: Prefix route request
            CefRequest prefixRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/static/file.css",
                    "GET"
            );
            CefResourceRequestHandler handler2 = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, prefixRequest, false, false, null, null
            );
            assertThat(handler2).isNotNull();

            // When: Exact route request
            CefRequest exactRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/health",
                    "GET"
            );
            CefResourceRequestHandler handler3 = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, exactRequest, false, false, null, null
            );
            assertThat(handler3).isNotNull();

            // When: Contains route request
            CefRequest containsRequest = MockCefFactory.createMockRequest(
                    "http://localhost:5173/api/data.json",
                    "GET"
            );
            CefResourceRequestHandler handler4 = handler.getResourceRequestHandler(
                    mockBrowser, mockFrame, containsRequest, false, false, null, null
            );
            assertThat(handler4).isNotNull();
        }
    }
}
