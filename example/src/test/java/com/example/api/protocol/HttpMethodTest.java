package com.example.api.protocol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for HttpMethod enum.
 * Target coverage: 100%
 */
class HttpMethodTest {

    @Test
    void testAllMethodsPresent() {
        // Ensure all 7 HTTP methods exist
        assertEquals(7, HttpMethod.values().length);

        assertNotNull(HttpMethod.GET);
        assertNotNull(HttpMethod.POST);
        assertNotNull(HttpMethod.PUT);
        assertNotNull(HttpMethod.DELETE);
        assertNotNull(HttpMethod.PATCH);
        assertNotNull(HttpMethod.OPTIONS);
        assertNotNull(HttpMethod.HEAD);
    }

    @ParameterizedTest
    @EnumSource(HttpMethod.class)
    void testFromStringConversion(HttpMethod method) {
        HttpMethod converted = HttpMethod.valueOf(method.name());
        assertEquals(method, converted);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"})
    void testFromStringCaseMatching(String methodName) {
        HttpMethod method = HttpMethod.valueOf(methodName);
        assertNotNull(method);
        assertEquals(methodName, method.name());
    }

    @Test
    void testInvalidMethodThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            HttpMethod.valueOf("INVALID");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            HttpMethod.valueOf("get"); // lowercase
        });
    }

    @Test
    void testMethodEquality() {
        assertEquals(HttpMethod.GET, HttpMethod.GET);
        assertEquals(HttpMethod.POST, HttpMethod.POST);

        assertNotEquals(HttpMethod.GET, HttpMethod.POST);
        assertNotEquals(HttpMethod.PUT, HttpMethod.DELETE);
    }

    @Test
    void testMethodName() {
        assertEquals("GET", HttpMethod.GET.name());
        assertEquals("POST", HttpMethod.POST.name());
        assertEquals("PUT", HttpMethod.PUT.name());
        assertEquals("DELETE", HttpMethod.DELETE.name());
        assertEquals("PATCH", HttpMethod.PATCH.name());
        assertEquals("OPTIONS", HttpMethod.OPTIONS.name());
        assertEquals("HEAD", HttpMethod.HEAD.name());
    }

    @Test
    void testMethodOrdinal() {
        // Ordinals are assigned in declaration order
        assertTrue(HttpMethod.GET.ordinal() >= 0);
        assertTrue(HttpMethod.POST.ordinal() >= 0);

        // Each method has unique ordinal
        assertNotEquals(HttpMethod.GET.ordinal(), HttpMethod.POST.ordinal());
    }

    @Test
    void testEnumSwitch() {
        // Verify enum can be used in switch statements
        for (HttpMethod method : HttpMethod.values()) {
            String result = switch (method) {
                case GET -> "read";
                case POST -> "create";
                case PUT -> "update";
                case DELETE -> "delete";
                case PATCH -> "partial update";
                case OPTIONS -> "preflight";
                case HEAD -> "metadata";
            };

            assertNotNull(result);
        }
    }

    @Test
    void testGetMethod() {
        // GET is typically used for reading
        assertEquals("GET", HttpMethod.GET.name());
    }

    @Test
    void testPostMethod() {
        // POST is typically used for creating
        assertEquals("POST", HttpMethod.POST.name());
    }

    @Test
    void testPutMethod() {
        // PUT is typically used for updating
        assertEquals("PUT", HttpMethod.PUT.name());
    }

    @Test
    void testDeleteMethod() {
        // DELETE is typically used for deleting
        assertEquals("DELETE", HttpMethod.DELETE.name());
    }

    @Test
    void testPatchMethod() {
        // PATCH is typically used for partial updates
        assertEquals("PATCH", HttpMethod.PATCH.name());
    }

    @Test
    void testOptionsMethod() {
        // OPTIONS is typically used for CORS preflight
        assertEquals("OPTIONS", HttpMethod.OPTIONS.name());
    }

    @Test
    void testHeadMethod() {
        // HEAD is typically used for metadata
        assertEquals("HEAD", HttpMethod.HEAD.name());
    }

    @Test
    void testEnumCollection() {
        HttpMethod[] methods = HttpMethod.values();

        // Should contain all methods
        assertTrue(methods.length == 7);

        // Should be iterable
        int count = 0;
        for (HttpMethod ignored : methods) {
            count++;
        }
        assertEquals(7, count);
    }
}
