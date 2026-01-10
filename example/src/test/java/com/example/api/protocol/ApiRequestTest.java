package com.example.api.protocol;

import com.example.api.mock.MockCefFactory;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.network.CefRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ApiRequest wrapper.
 * Target coverage: 90%+
 */
@ExtendWith(MockitoExtension.class)
class ApiRequestTest {

    @Test
    void testPathExtraction() {
        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "http://localhost:8080/api/users/123",
            "GET"
        );
        CefBrowser browser = MockCefFactory.createMockBrowser();
        CefFrame frame = MockCefFactory.createMockFrame();

        ApiRequest request = new ApiRequest(cefRequest, browser, frame);

        assertEquals("/api/users/123", request.getPath());
    }

    @Test
    void testQueryParamsParsing() {
        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "http://localhost/api/search?q=test&page=1&size=20",
            "GET"
        );

        ApiRequest request = new ApiRequest(
            cefRequest,
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        assertEquals("test", request.getQueryParam("q"));
        assertEquals("1", request.getQueryParam("page"));
        assertEquals("20", request.getQueryParam("size"));
    }

    @Test
    void testQueryParamsUrlDecoding() {
        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "http://localhost/api/search?q=hello+world&special=%3D",
            "GET"
        );

        ApiRequest request = new ApiRequest(
            cefRequest,
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        assertEquals("hello world", request.getQueryParam("q"));
        assertEquals("=", request.getQueryParam("special"));
    }

    @Test
    void testEmptyQueryParams() {
        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "http://localhost/api/resource",
            "GET"
        );

        ApiRequest request = new ApiRequest(
            cefRequest,
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        assertNull(request.getQueryParam("nonexistent"));
        assertTrue(request.getQueryParams().isEmpty());
    }

    @Test
    void testBodyDeserialization() {
        String jsonBody = "{\"title\":\"Test Task\",\"description\":\"Description\"}";
        CefRequest cefRequest = MockCefFactory.createMockRequestWithBody(
            "http://localhost/api/tasks",
            "POST",
            jsonBody
        );

        ApiRequest request = new ApiRequest(
            cefRequest,
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        @SuppressWarnings("unchecked")
        Map<String, String> body = request.getBody(Map.class);

        assertNotNull(body);
        assertEquals("Test Task", body.get("title"));
        assertEquals("Description", body.get("description"));
    }

    @Test
    void testBodyDeserializationError() {
        String invalidJson = "{invalid json}";
        CefRequest cefRequest = MockCefFactory.createMockRequestWithBody(
            "http://localhost/api/tasks",
            "POST",
            invalidJson
        );

        ApiRequest request = new ApiRequest(
            cefRequest,
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        // Should throw BadRequestException with message about invalid body
        Exception exception = assertThrows(Exception.class, () -> {
            request.getBody(Map.class);
        });

        // Check that error message indicates a request body issue
        assertTrue(exception.getMessage().contains("Invalid request body") ||
                   exception.getMessage().contains("Unrecognized token"));
    }

    @Test
    void testEmptyBodyHandling() {
        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "http://localhost/api/resource",
            "GET"
        );

        ApiRequest request = new ApiRequest(
            cefRequest,
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        String body = request.getBody(String.class);
        // Empty body should return null or empty string
        assertTrue(body == null || body.isEmpty());
    }

    @Test
    void testPathVariableAccess() {
        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "http://localhost/api/users/123",
            "GET"
        );

        ApiRequest request = new ApiRequest(
            cefRequest,
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        // Set path variables (typically done by RouteTree)
        Map<String, String> pathVars = new HashMap<>();
        pathVars.put("id", "123");
        pathVars.put("action", "profile");
        request.setPathVariables(pathVars);

        assertEquals("123", request.getPathVariable("id"));
        assertEquals("profile", request.getPathVariable("action"));
        assertNull(request.getPathVariable("nonexistent"));
    }

    @Test
    void testSetPathVariables() {
        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "http://localhost/api/data",
            "GET"
        );

        ApiRequest request = new ApiRequest(
            cefRequest,
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        Map<String, String> vars = new HashMap<>();
        vars.put("key1", "value1");
        vars.put("key2", "value2");

        request.setPathVariables(vars);

        Map<String, String> retrieved = request.getPathVariables();
        assertEquals("value1", retrieved.get("key1"));
        assertEquals("value2", retrieved.get("key2"));
    }

    @Test
    void testGetCefObjects() {
        CefBrowser browser = MockCefFactory.createMockBrowser();
        CefFrame frame = MockCefFactory.createMockFrame();
        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "http://localhost/api/test",
            "GET"
        );

        ApiRequest request = new ApiRequest(cefRequest, browser, frame);

        assertSame(browser, request.getCefBrowser());
        assertSame(frame, request.getCefFrame());
        assertSame(cefRequest, request.getCefRequest());
    }

    @Test
    void testMethodAccess() {
        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "http://localhost/api/resource",
            "POST"
        );

        ApiRequest request = new ApiRequest(
            cefRequest,
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        assertEquals(HttpMethod.POST, request.getMethod());
    }

    @Test
    void testHeaderAccess() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer token123");

        CefRequest cefRequest = MockCefFactory.createMockRequestWithHeaders(
            "http://localhost/api/resource",
            "GET",
            headers
        );

        ApiRequest request = new ApiRequest(
            cefRequest,
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        assertEquals("application/json", request.getHeader("Content-Type"));
        assertEquals("Bearer token123", request.getHeader("Authorization"));
        assertNull(request.getHeader("NonExistent"));
    }

    @Test
    void testLazyQueryParamsParsing() {
        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "http://localhost/api/resource?foo=bar",
            "GET"
        );

        ApiRequest request = new ApiRequest(
            cefRequest,
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        // First access triggers parsing
        assertEquals("bar", request.getQueryParam("foo"));

        // Subsequent access uses cached result
        assertEquals("bar", request.getQueryParam("foo"));
    }

    @Test
    void testLazyBodyParsing() {
        String jsonBody = "{\"test\":\"value\"}";
        CefRequest cefRequest = MockCefFactory.createMockRequestWithBody(
            "http://localhost/api/resource",
            "POST",
            jsonBody
        );

        ApiRequest request = new ApiRequest(
            cefRequest,
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        // First access triggers parsing
        @SuppressWarnings("unchecked")
        Map<String, String> body1 = request.getBody(Map.class);
        assertNotNull(body1);

        // Second access parses again (body string is cached, but deserialized object is not)
        @SuppressWarnings("unchecked")
        Map<String, String> body2 = request.getBody(Map.class);
        assertEquals(body1, body2);  // Equal content, but not same instance
    }

    @Test
    void testMalformedUrl() {
        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "not a valid url",
            "GET"
        );

        ApiRequest request = new ApiRequest(
            cefRequest,
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        // Should handle gracefully, likely return empty path
        String path = request.getPath();
        assertNotNull(path);
    }

    @Test
    void testQueryParamsWithMultipleValues() {
        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "http://localhost/api/resource?tags=java&tags=kotlin&tags=rust",
            "GET"
        );

        ApiRequest request = new ApiRequest(
            cefRequest,
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        // Implementation might return last value or comma-separated
        String tags = request.getQueryParam("tags");
        assertNotNull(tags);
        // Exact behavior depends on implementation
    }

    @Test
    void testBodyAsString() {
        String textBody = "Plain text content";
        CefRequest cefRequest = MockCefFactory.createMockRequestWithBody(
            "http://localhost/api/resource",
            "POST",
            textBody
        );

        ApiRequest request = new ApiRequest(
            cefRequest,
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        // Use getBodyString() for plain text, not getBody(String.class) which expects JSON
        String body = request.getBodyString();
        assertEquals(textBody, body);
    }

    @Test
    void testPathWithFragmentAndQuery() {
        CefRequest cefRequest = MockCefFactory.createMockRequest(
            "http://localhost/api/resource?query=value#fragment",
            "GET"
        );

        ApiRequest request = new ApiRequest(
            cefRequest,
            MockCefFactory.createMockBrowser(),
            MockCefFactory.createMockFrame()
        );

        // Path should not include query or fragment
        String path = request.getPath();
        assertEquals("/api/resource", path);

        // Query params should still be accessible
        assertEquals("value", request.getQueryParam("query"));
    }
}
