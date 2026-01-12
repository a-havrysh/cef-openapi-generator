package com.example.api.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for InternalServerErrorException.
 * Target coverage: 100%
 */
@DisplayName("InternalServerErrorException Tests")
class InternalServerErrorExceptionTest {

    @Test
    @DisplayName("Constructor with message creates exception with HTTP 500")
    void testConstructorWithMessage() {
        String message = "Database connection failed";
        InternalServerErrorException exception = new InternalServerErrorException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getStatusCode()).isEqualTo(500);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Constructor with message and cause preserves both")
    void testConstructorWithMessageAndCause() {
        String message = "Failed to save user data";
        Exception cause = new RuntimeException("Connection timeout");
        InternalServerErrorException exception = new InternalServerErrorException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getStatusCode()).isEqualTo(500);
        assertThat(exception.getCause()).isSameAs(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Connection timeout");
    }

    @Test
    @DisplayName("StatusCode is always 500 for InternalServerErrorException")
    void testStatusCodeIsAlways500() {
        InternalServerErrorException exception1 = new InternalServerErrorException("Error 1");
        InternalServerErrorException exception2 = new InternalServerErrorException("Error 2", new RuntimeException());

        assertThat(exception1.getStatusCode()).isEqualTo(500);
        assertThat(exception2.getStatusCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("InternalServerErrorException inherits from ApiException")
    void testInheritanceFromApiException() {
        InternalServerErrorException exception = new InternalServerErrorException("Server error");

        assertThat(exception).isInstanceOf(ApiException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }

    @Test
    @DisplayName("Exception can be caught as ApiException")
    void testCatchAsApiException() {
        try {
            throw new InternalServerErrorException("Unexpected error");
        } catch (ApiException e) {
            assertThat(e.getStatusCode()).isEqualTo(500);
            assertThat(e.getMessage()).isEqualTo("Unexpected error");
        }
    }

    @Test
    @DisplayName("Exception can be caught as RuntimeException")
    void testCatchAsRuntimeException() {
        try {
            throw new InternalServerErrorException("Processing failed");
        } catch (RuntimeException e) {
            assertThat(e).isInstanceOf(InternalServerErrorException.class);
            assertThat(e.getMessage()).isEqualTo("Processing failed");
        }
    }

    @Test
    @DisplayName("toString() contains message")
    void testToStringContainsMessage() {
        String message = "Internal configuration error";
        InternalServerErrorException exception = new InternalServerErrorException(message);

        assertThat(exception.toString()).contains(message);
        assertThat(exception.toString()).contains("InternalServerErrorException");
    }

    @Test
    @DisplayName("Null message is handled correctly")
    void testNullMessage() {
        InternalServerErrorException exception = new InternalServerErrorException(null);

        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getStatusCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("Empty message is handled correctly")
    void testEmptyMessage() {
        InternalServerErrorException exception = new InternalServerErrorException("");

        assertThat(exception.getMessage()).isEmpty();
        assertThat(exception.getStatusCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("Cause chain is preserved correctly")
    void testCauseChain() {
        RuntimeException level1 = new RuntimeException("Low-level IO error");
        IllegalStateException level2 = new IllegalStateException("Connection lost", level1);
        InternalServerErrorException exception = new InternalServerErrorException("Server failed", level2);

        assertThat(exception.getCause()).isSameAs(level2);
        assertThat(exception.getCause().getCause()).isSameAs(level1);
        assertThat(exception.getCause().getCause().getCause()).isNull();
    }

    @Test
    @DisplayName("Multiple instances are independent")
    void testMultipleInstancesAreIndependent() {
        InternalServerErrorException exception1 = new InternalServerErrorException("Error 1");
        InternalServerErrorException exception2 = new InternalServerErrorException("Error 2");

        assertThat(exception1.getMessage()).isEqualTo("Error 1");
        assertThat(exception2.getMessage()).isEqualTo("Error 2");
        assertThat(exception1).isNotSameAs(exception2);
    }

    @Test
    @DisplayName("getMessage() returns expected value")
    void testGetMessage() {
        String message = "Failed to process payment";
        InternalServerErrorException exception = new InternalServerErrorException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("getStatusCode() returns expected value")
    void testGetStatusCode() {
        InternalServerErrorException exception = new InternalServerErrorException("Some error");

        assertThat(exception.getStatusCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("getCause() returns expected cause")
    void testGetCause() {
        RuntimeException cause = new RuntimeException("Actual error");
        InternalServerErrorException exception = new InternalServerErrorException("Wrapped error", cause);

        assertThat(exception.getCause()).isSameAs(cause);
    }

    @Test
    @DisplayName("getCause() returns null when no cause provided")
    void testGetCauseReturnsNullWhenNoCause() {
        InternalServerErrorException exception = new InternalServerErrorException("Simple error");

        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Different server error scenarios")
    void testDifferentServerErrorScenarios() {
        String dbError = "Database query failed";
        String fileError = "Failed to read file from disk";
        String serviceError = "External service unavailable";
        String configError = "Invalid server configuration";

        InternalServerErrorException ex1 = new InternalServerErrorException(dbError);
        InternalServerErrorException ex2 = new InternalServerErrorException(fileError);
        InternalServerErrorException ex3 = new InternalServerErrorException(serviceError);
        InternalServerErrorException ex4 = new InternalServerErrorException(configError);

        assertThat(ex1.getMessage()).isEqualTo(dbError);
        assertThat(ex2.getMessage()).isEqualTo(fileError);
        assertThat(ex3.getMessage()).isEqualTo(serviceError);
        assertThat(ex4.getMessage()).isEqualTo(configError);
    }

    @Test
    @DisplayName("Exception stack trace is properly generated")
    void testStackTrace() {
        InternalServerErrorException exception = new InternalServerErrorException("Server error");
        StackTraceElement[] stackTrace = exception.getStackTrace();

        assertThat(stackTrace).isNotEmpty();
        assertThat(stackTrace[0].getClassName()).contains("InternalServerErrorExceptionTest");
    }

    @Test
    @DisplayName("Exception with long message")
    void testLongMessage() {
        String longMessage = "A".repeat(500);
        InternalServerErrorException exception = new InternalServerErrorException(longMessage);

        assertThat(exception.getMessage()).isEqualTo(longMessage);
        assertThat(exception.getMessage()).hasSize(500);
    }

    @Test
    @DisplayName("Exception with special characters in cause message")
    void testSpecialCharactersInCauseMessage() {
        RuntimeException cause = new RuntimeException("Error at line 42: Invalid < > & characters");
        InternalServerErrorException exception = new InternalServerErrorException("Parsing error", cause);

        assertThat(exception.getCause().getMessage()).contains("<");
        assertThat(exception.getCause().getMessage()).contains(">");
        assertThat(exception.getCause().getMessage()).contains("&");
    }

    @Test
    @DisplayName("Is serializable (implements RuntimeException)")
    void testIsSerializable() {
        InternalServerErrorException exception = new InternalServerErrorException("Error");

        assertThat(exception).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Two exceptions with same message are not equal (different objects)")
    void testEqualityNotBasedOnMessage() {
        InternalServerErrorException ex1 = new InternalServerErrorException("Same error");
        InternalServerErrorException ex2 = new InternalServerErrorException("Same error");

        assertThat(ex1).isNotEqualTo(ex2);
        assertThat(ex1).isNotSameAs(ex2);
    }

    @Test
    @DisplayName("Status code consistency across operations")
    void testStatusCodeConsistency() {
        InternalServerErrorException exception = new InternalServerErrorException("Error", new RuntimeException("Cause"));

        int code1 = exception.getStatusCode();
        int code2 = exception.getStatusCode();
        int code3 = exception.getStatusCode();

        assertThat(code1).isEqualTo(code2).isEqualTo(code3).isEqualTo(500);
    }

    @Test
    @DisplayName("Common 500 error scenarios")
    void testCommon500Scenarios() {
        InternalServerErrorException dbError = new InternalServerErrorException("Database error");
        InternalServerErrorException cacheError = new InternalServerErrorException("Cache error");
        InternalServerErrorException memoryError = new InternalServerErrorException("Out of memory");
        InternalServerErrorException timeoutError = new InternalServerErrorException("Operation timeout");

        assertThat(dbError.getStatusCode()).isEqualTo(500);
        assertThat(cacheError.getStatusCode()).isEqualTo(500);
        assertThat(memoryError.getStatusCode()).isEqualTo(500);
        assertThat(timeoutError.getStatusCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("Deep cause chain")
    void testDeepCauseChain() {
        Exception level1 = new RuntimeException("L1");
        Exception level2 = new IllegalArgumentException("L2", level1);
        Exception level3 = new IllegalStateException("L3", level2);
        Exception level4 = new UnsupportedOperationException("L4", level3);
        InternalServerErrorException exception = new InternalServerErrorException("Top", level4);

        assertThat(exception.getCause()).isEqualTo(level4);
        assertThat(exception.getCause().getCause()).isEqualTo(level3);
        assertThat(exception.getCause().getCause().getCause()).isEqualTo(level2);
        assertThat(exception.getCause().getCause().getCause().getCause()).isEqualTo(level1);
    }

    @Test
    @DisplayName("Wrapping different exception types")
    void testWrappingDifferentExceptionTypes() {
        RuntimeException runtimeCause = new RuntimeException("Runtime error");
        IllegalArgumentException illegalCause = new IllegalArgumentException("Illegal argument");
        NullPointerException nullCause = new NullPointerException("Null pointer");

        InternalServerErrorException ex1 = new InternalServerErrorException("Error 1", runtimeCause);
        InternalServerErrorException ex2 = new InternalServerErrorException("Error 2", illegalCause);
        InternalServerErrorException ex3 = new InternalServerErrorException("Error 3", nullCause);

        assertThat(ex1.getCause()).isInstanceOf(RuntimeException.class);
        assertThat(ex2.getCause()).isInstanceOf(IllegalArgumentException.class);
        assertThat(ex3.getCause()).isInstanceOf(NullPointerException.class);
    }
}
