package com.example.api.protocol;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ApiResponse wrapper.
 * Target coverage: 95%+
 */
class ApiResponseTest {

    @Test
    void testOkResponse() {
        String body = "Success";
        ApiResponse<String> response = ApiResponse.ok(body);

        assertEquals(200, response.getStatusCode());
        assertEquals(body, response.getBody());
        assertNotNull(response.getContentType());
    }

    @Test
    void testOkWithContentType() {
        String body = "Test data";
        ApiResponse<String> response = ApiResponse.ok(body, "text/plain");

        assertEquals(200, response.getStatusCode());
        assertEquals(body, response.getBody());
        assertEquals("text/plain", response.getContentType());
    }

    @Test
    void testNoContent() {
        ApiResponse<Void> response = ApiResponse.noContent();

        assertEquals(204, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testStatusResponse() {
        String body = "Created";
        ApiResponse<String> response = ApiResponse.status(201, body);

        assertEquals(201, response.getStatusCode());
        assertEquals(body, response.getBody());
    }

    @Test
    void testNotFound() {
        ApiResponse<?> response = ApiResponse.notFound("Resource not found");

        assertEquals(404, response.getStatusCode());
    }

    @Test
    void testCreated() {
        Map<String, String> body = new HashMap<>();
        body.put("id", "123");

        ApiResponse<Map<String, String>> response = ApiResponse.created(body);

        assertEquals(201, response.getStatusCode());
        assertEquals(body, response.getBody());
    }

    @Test
    void testContentTypeBuilder() {
        ApiResponse<String> response = ApiResponse.ok("data")
            .contentType("application/xml");

        assertEquals("application/xml", response.getContentType());
    }

    @Test
    void testHeaderBuilder() {
        ApiResponse<String> response = ApiResponse.ok("data")
            .header("X-Custom-Header", "custom-value");

        assertEquals("custom-value", response.getHeaders().get("X-Custom-Header"));
    }

    @Test
    void testChainedBuilders() {
        ApiResponse<String> response = ApiResponse.ok("data")
            .contentType("application/json")
            .header("X-Request-Id", "123")
            .header("X-Correlation-Id", "456");

        assertEquals("application/json", response.getContentType());
        assertEquals("123", response.getHeaders().get("X-Request-Id"));
        assertEquals("456", response.getHeaders().get("X-Correlation-Id"));
    }

    @Test
    void testImmutability() {
        ApiResponse<String> original = ApiResponse.ok("data");
        ApiResponse<String> modified = original.contentType("text/plain");

        // Builder should return new instance
        assertNotSame(original, modified);

        // Original should remain unchanged
        assertNotEquals("text/plain", original.getContentType());
    }

    @Test
    void testGenericTypeHandling() {
        // String
        ApiResponse<String> stringResponse = ApiResponse.ok("text");
        assertEquals("text", stringResponse.getBody());

        // Map
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        ApiResponse<Map<String, Object>> mapResponse = ApiResponse.ok(map);
        assertEquals(map, mapResponse.getBody());

        // Custom object (simulated)
        TestObject obj = new TestObject("test");
        ApiResponse<TestObject> objectResponse = ApiResponse.ok(obj);
        assertEquals(obj, objectResponse.getBody());
    }

    @Test
    void testNullBody() {
        ApiResponse<String> response = ApiResponse.ok(null);

        assertEquals(200, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testMultipleHeaders() {
        ApiResponse<String> response = ApiResponse.ok("data")
            .header("Header1", "value1")
            .header("Header2", "value2")
            .header("Header3", "value3");

        Map<String, String> headers = response.getHeaders();
        assertEquals("value1", headers.get("Header1"));
        assertEquals("value2", headers.get("Header2"));
        assertEquals("value3", headers.get("Header3"));
    }

    @Test
    void testOverwriteContentType() {
        ApiResponse<String> response = ApiResponse.ok("data", "application/json")
            .contentType("text/plain");

        assertEquals("text/plain", response.getContentType());
    }

    @Test
    void testBadRequest() {
        ApiResponse<?> response = ApiResponse.badRequest("Invalid input");

        assertEquals(400, response.getStatusCode());
    }

    @Test
    void testInternalServerError() {
        ApiResponse<?> response = ApiResponse.internalServerError("Server error");

        assertEquals(500, response.getStatusCode());
    }

    @Test
    void testCustomStatusCodes() {
        // Accepted (202)
        ApiResponse<String> accepted = ApiResponse.status(202, "Accepted");
        assertEquals(202, accepted.getStatusCode());

        // Conflict (409)
        ApiResponse<String> conflict = ApiResponse.status(409, "Conflict");
        assertEquals(409, conflict.getStatusCode());

        // Service Unavailable (503)
        ApiResponse<String> unavailable = ApiResponse.status(503, "Unavailable");
        assertEquals(503, unavailable.getStatusCode());
    }

    @Test
    void testBuilderChainPreservesBody() {
        String body = "test data";
        ApiResponse<String> response = ApiResponse.ok(body)
            .contentType("text/plain")
            .header("X-Test", "value");

        assertEquals(body, response.getBody());
        assertEquals("text/plain", response.getContentType());
        assertEquals("value", response.getHeaders().get("X-Test"));
    }

    @Test
    void testEmptyHeaders() {
        ApiResponse<String> response = ApiResponse.ok("data");

        Map<String, String> headers = response.getHeaders();
        assertNotNull(headers);
        // May be empty or contain default headers
    }

    @Test
    void testLocationHeader() {
        // Common pattern for 201 Created responses
        ApiResponse<String> response = ApiResponse.created("resource")
            .header("Location", "/api/resources/123");

        assertEquals(201, response.getStatusCode());
        assertEquals("/api/resources/123", response.getHeaders().get("Location"));
    }

    // Test helper class
    private static class TestObject {
        private final String value;

        TestObject(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TestObject)) return false;
            TestObject other = (TestObject) obj;
            return value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }
}
