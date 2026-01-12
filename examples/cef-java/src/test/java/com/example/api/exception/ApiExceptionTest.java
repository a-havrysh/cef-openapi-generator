package com.example.api.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for ApiException base class.
 * Target coverage: 100%
 */
@DisplayName("ApiException Tests")
class ApiExceptionTest {

    @Test
    @DisplayName("Constructor with status code and message creates exception")
    void testConstructorWithStatusAndMessage() {
        ApiException exception = new ApiException(400, "Bad request");

        assertThat(exception.getStatusCode()).isEqualTo(400);
        assertThat(exception.getMessage()).isEqualTo("Bad request");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Constructor with cause preserves all data")
    void testConstructorWithCause() {
        RuntimeException cause = new RuntimeException("Root cause");
        ApiException exception = new ApiException(500, "Server error", cause);

        assertThat(exception.getStatusCode()).isEqualTo(500);
        assertThat(exception.getMessage()).isEqualTo("Server error");
        assertThat(exception.getCause()).isSameAs(cause);
    }

    @Test
    @DisplayName("getStatusCode() returns correct value")
    void testGetStatusCode() {
        ApiException exception = new ApiException(404, "Not found");

        assertThat(exception.getStatusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("badRequest factory creates BadRequestException with HTTP 400")
    void testBadRequestFactory() {
        ApiException exception = ApiException.badRequest("Invalid input");

        assertThat(exception.getStatusCode()).isEqualTo(400);
        assertThat(exception.getMessage()).isEqualTo("Invalid input");
        assertThat(exception).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("notFound factory creates NotFoundException with HTTP 404")
    void testNotFoundFactory() {
        ApiException exception = ApiException.notFound("Resource not found");

        assertThat(exception.getStatusCode()).isEqualTo(404);
        assertThat(exception.getMessage()).isEqualTo("Resource not found");
        assertThat(exception).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("internalError factory creates InternalServerErrorException with HTTP 500")
    void testInternalErrorFactory() {
        ApiException exception = ApiException.internalError("Server error");

        assertThat(exception.getStatusCode()).isEqualTo(500);
        assertThat(exception.getMessage()).isEqualTo("Server error");
        assertThat(exception).isInstanceOf(InternalServerErrorException.class);
    }

    @Test
    @DisplayName("toString() contains message")
    void testExceptionMessage() {
        String message = "Custom error message";
        ApiException exception = new ApiException(418, message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.toString()).contains(message);
    }

    @Test
    @DisplayName("Custom status codes are preserved")
    void testCustomStatusCodes() {
        ApiException e402 = new ApiException(402, "Payment Required");
        ApiException e409 = new ApiException(409, "Conflict");
        ApiException e503 = new ApiException(503, "Service Unavailable");

        assertThat(e402.getStatusCode()).isEqualTo(402);
        assertThat(e409.getStatusCode()).isEqualTo(409);
        assertThat(e503.getStatusCode()).isEqualTo(503);
    }

    @Test
    @DisplayName("Cause chain is preserved through exception hierarchy")
    void testCausePreservation() {
        Exception cause1 = new RuntimeException("Level 1");
        Exception cause2 = new IllegalArgumentException("Level 2", cause1);
        ApiException exception = new ApiException(500, "Server error", cause2);

        assertThat(exception.getCause()).isSameAs(cause2);
        assertThat(exception.getCause().getCause()).isSameAs(cause1);
    }

    @Test
    @DisplayName("Null message is handled correctly")
    void testNullMessage() {
        ApiException exception = new ApiException(400, null);

        assertThat(exception.getStatusCode()).isEqualTo(400);
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    @DisplayName("ApiException is a RuntimeException")
    void testIsRuntimeException() {
        ApiException exception = new ApiException(500, "Error");

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }

    @Test
    @DisplayName("Multiple instances with different status codes")
    void testMultipleInstancesWithDifferentStatusCodes() {
        ApiException ex1 = new ApiException(200, "OK");
        ApiException ex2 = new ApiException(201, "Created");
        ApiException ex3 = new ApiException(400, "Bad Request");
        ApiException ex4 = new ApiException(500, "Server Error");

        assertThat(ex1.getStatusCode()).isEqualTo(200);
        assertThat(ex2.getStatusCode()).isEqualTo(201);
        assertThat(ex3.getStatusCode()).isEqualTo(400);
        assertThat(ex4.getStatusCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("Exception can be caught and rethrown")
    void testRethrowException() {
        try {
            try {
                throw new ApiException(503, "Service Unavailable");
            } catch (ApiException e) {
                assertThat(e.getStatusCode()).isEqualTo(503);
                throw e;
            }
        } catch (ApiException e) {
            assertThat(e.getMessage()).isEqualTo("Service Unavailable");
        }
    }

    @Test
    @DisplayName("Exception stack trace is generated")
    void testStackTrace() {
        ApiException exception = new ApiException(500, "Error");
        StackTraceElement[] stackTrace = exception.getStackTrace();

        assertThat(stackTrace).isNotEmpty();
    }

    @Test
    @DisplayName("Is serializable")
    void testIsSerializable() {
        ApiException exception = new ApiException(500, "Error");

        assertThat(exception).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Long message is preserved")
    void testLongMessage() {
        String longMessage = "X".repeat(1000);
        ApiException exception = new ApiException(400, longMessage);

        assertThat(exception.getMessage()).isEqualTo(longMessage);
    }

    @Test
    @DisplayName("Multiline message is preserved")
    void testMultilineMessage() {
        String multilineMessage = "Line 1\nLine 2\nLine 3";
        ApiException exception = new ApiException(400, multilineMessage);

        assertThat(exception.getMessage()).isEqualTo(multilineMessage);
        assertThat(exception.getMessage()).contains("\n");
    }

    @Test
    @DisplayName("getMessage is consistent across calls")
    void testMessageConsistency() {
        String message = "Consistent message";
        ApiException exception = new ApiException(400, message);

        assertThat(exception.getMessage()).isEqualTo(exception.getMessage());
    }

    @Test
    @DisplayName("getStatusCode is consistent across calls")
    void testStatusCodeConsistency() {
        ApiException exception = new ApiException(404, "Not found");

        assertThat(exception.getStatusCode()).isEqualTo(exception.getStatusCode());
    }
}
