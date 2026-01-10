package com.example.api.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ApiException base class.
 * Target coverage: 100%
 */
class ApiExceptionTest {

    @Test
    void testConstructorWithStatusAndMessage() {
        ApiException exception = new ApiException(400, "Bad request");

        assertEquals(400, exception.getStatusCode());
        assertEquals("Bad request", exception.getMessage());
    }

    @Test
    void testConstructorWithCause() {
        RuntimeException cause = new RuntimeException("Root cause");
        ApiException exception = new ApiException(500, "Server error", cause);

        assertEquals(500, exception.getStatusCode());
        assertEquals("Server error", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void testGetStatusCode() {
        ApiException exception = new ApiException(404, "Not found");

        assertEquals(404, exception.getStatusCode());
    }

    @Test
    void testBadRequestFactory() {
        ApiException exception = ApiException.badRequest("Invalid input");

        assertEquals(400, exception.getStatusCode());
        assertEquals("Invalid input", exception.getMessage());
        assertTrue(exception instanceof BadRequestException);
    }

    @Test
    void testNotFoundFactory() {
        ApiException exception = ApiException.notFound("Resource not found");

        assertEquals(404, exception.getStatusCode());
        assertEquals("Resource not found", exception.getMessage());
        assertTrue(exception instanceof NotFoundException);
    }

    @Test
    void testInternalErrorFactory() {
        ApiException exception = ApiException.internalError("Server error");

        assertEquals(500, exception.getStatusCode());
        assertEquals("Server error", exception.getMessage());
        assertTrue(exception instanceof InternalServerErrorException);
    }

    @Test
    void testExceptionMessage() {
        String message = "Custom error message";
        ApiException exception = new ApiException(418, message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception.toString().contains(message));
    }

    @Test
    void testCustomStatusCodes() {
        ApiException e402 = new ApiException(402, "Payment Required");
        ApiException e409 = new ApiException(409, "Conflict");
        ApiException e503 = new ApiException(503, "Service Unavailable");

        assertEquals(402, e402.getStatusCode());
        assertEquals(409, e409.getStatusCode());
        assertEquals(503, e503.getStatusCode());
    }

    @Test
    void testCausePreservation() {
        Exception cause1 = new RuntimeException("Level 1");
        Exception cause2 = new IllegalArgumentException("Level 2", cause1);
        ApiException exception = new ApiException(500, "Server error", cause2);

        assertEquals(cause2, exception.getCause());
        assertEquals(cause1, exception.getCause().getCause());
    }

    @Test
    void testNullMessage() {
        ApiException exception = new ApiException(400, null);

        assertEquals(400, exception.getStatusCode());
        assertNull(exception.getMessage());
    }
}
