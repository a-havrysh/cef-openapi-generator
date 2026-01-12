package com.example.api.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for BadRequestException.
 * Target coverage: 100%
 */
@DisplayName("BadRequestException Tests")
class BadRequestExceptionTest {

    @Test
    @DisplayName("Constructor with message creates exception with HTTP 400")
    void testConstructorWithMessage() {
        String message = "Invalid request body";
        BadRequestException exception = new BadRequestException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getStatusCode()).isEqualTo(400);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Constructor with message and cause preserves both")
    void testConstructorWithMessageAndCause() {
        String message = "Failed to parse JSON";
        IllegalArgumentException cause = new IllegalArgumentException("Invalid JSON syntax");
        BadRequestException exception = new BadRequestException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getStatusCode()).isEqualTo(400);
        assertThat(exception.getCause()).isSameAs(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Invalid JSON syntax");
    }

    @Test
    @DisplayName("StatusCode is always 400 for BadRequestException")
    void testStatusCodeIsAlways400() {
        BadRequestException exception1 = new BadRequestException("Error 1");
        BadRequestException exception2 = new BadRequestException("Error 2", new RuntimeException());

        assertThat(exception1.getStatusCode()).isEqualTo(400);
        assertThat(exception2.getStatusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("BadRequestException inherits from ApiException")
    void testInheritanceFromApiException() {
        BadRequestException exception = new BadRequestException("Bad request");

        assertThat(exception).isInstanceOf(ApiException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }

    @Test
    @DisplayName("Exception can be caught as ApiException")
    void testCatchAsApiException() {
        try {
            throw new BadRequestException("Invalid parameter");
        } catch (ApiException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
            assertThat(e.getMessage()).isEqualTo("Invalid parameter");
        }
    }

    @Test
    @DisplayName("Exception can be caught as RuntimeException")
    void testCatchAsRuntimeException() {
        try {
            throw new BadRequestException("Bad input");
        } catch (RuntimeException e) {
            assertThat(e).isInstanceOf(BadRequestException.class);
            assertThat(e.getMessage()).isEqualTo("Bad input");
        }
    }

    @Test
    @DisplayName("toString() contains message")
    void testToStringContainsMessage() {
        String message = "Malformed request";
        BadRequestException exception = new BadRequestException(message);

        assertThat(exception.toString()).contains(message);
        assertThat(exception.toString()).contains("BadRequestException");
    }

    @Test
    @DisplayName("Null message is handled correctly")
    void testNullMessage() {
        BadRequestException exception = new BadRequestException(null);

        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getStatusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Empty message is handled correctly")
    void testEmptyMessage() {
        BadRequestException exception = new BadRequestException("");

        assertThat(exception.getMessage()).isEmpty();
        assertThat(exception.getStatusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Cause chain is preserved correctly")
    void testCauseChain() {
        RuntimeException level1 = new RuntimeException("Level 1");
        IllegalStateException level2 = new IllegalStateException("Level 2", level1);
        BadRequestException exception = new BadRequestException("Top level", level2);

        assertThat(exception.getCause()).isSameAs(level2);
        assertThat(exception.getCause().getCause()).isSameAs(level1);
        assertThat(exception.getCause().getCause().getCause()).isNull();
    }

    @Test
    @DisplayName("Multiple instances are independent")
    void testMultipleInstancesAreIndependent() {
        BadRequestException exception1 = new BadRequestException("Error 1");
        BadRequestException exception2 = new BadRequestException("Error 2");

        assertThat(exception1.getMessage()).isEqualTo("Error 1");
        assertThat(exception2.getMessage()).isEqualTo("Error 2");
        assertThat(exception1).isNotSameAs(exception2);
    }

    @Test
    @DisplayName("getMessage() returns expected value")
    void testGetMessage() {
        String message = "Invalid request parameters";
        BadRequestException exception = new BadRequestException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("getStatusCode() returns expected value")
    void testGetStatusCode() {
        BadRequestException exception = new BadRequestException("Some error");

        assertThat(exception.getStatusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("getCause() returns expected cause")
    void testGetCause() {
        RuntimeException cause = new RuntimeException("Original error");
        BadRequestException exception = new BadRequestException("Wrapped error", cause);

        assertThat(exception.getCause()).isSameAs(cause);
    }

    @Test
    @DisplayName("getCause() returns null when no cause provided")
    void testGetCauseReturnsNullWhenNoCause() {
        BadRequestException exception = new BadRequestException("Simple error");

        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Different message types")
    void testDifferentMessageTypes() {
        String simpleMessage = "Simple";
        String multilineMessage = "Line 1\nLine 2\nLine 3";
        String specialCharsMessage = "Error with special chars: @#$%^&*()";

        BadRequestException ex1 = new BadRequestException(simpleMessage);
        BadRequestException ex2 = new BadRequestException(multilineMessage);
        BadRequestException ex3 = new BadRequestException(specialCharsMessage);

        assertThat(ex1.getMessage()).isEqualTo(simpleMessage);
        assertThat(ex2.getMessage()).isEqualTo(multilineMessage);
        assertThat(ex3.getMessage()).isEqualTo(specialCharsMessage);
    }

    @Test
    @DisplayName("Exception stack trace is properly generated")
    void testStackTrace() {
        BadRequestException exception = new BadRequestException("Test error");
        StackTraceElement[] stackTrace = exception.getStackTrace();

        assertThat(stackTrace).isNotEmpty();
        assertThat(stackTrace[0].getClassName()).contains("BadRequestExceptionTest");
    }

    @Test
    @DisplayName("Exception with long message")
    void testLongMessage() {
        String longMessage = "A".repeat(500);
        BadRequestException exception = new BadRequestException(longMessage);

        assertThat(exception.getMessage()).isEqualTo(longMessage);
        assertThat(exception.getMessage()).hasSize(500);
    }

    @Test
    @DisplayName("Exception with special characters in cause message")
    void testSpecialCharactersInCauseMessage() {
        RuntimeException cause = new RuntimeException("Cause with special: \n\t\r");
        BadRequestException exception = new BadRequestException("Main error", cause);

        assertThat(exception.getCause().getMessage()).contains("\n");
    }

    @Test
    @DisplayName("Is serializable (implements RuntimeException)")
    void testIsSerializable() {
        BadRequestException exception = new BadRequestException("Error");

        assertThat(exception).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Two exceptions with same message are not equal (different objects)")
    void testEqualityNotBasedOnMessage() {
        BadRequestException ex1 = new BadRequestException("Same message");
        BadRequestException ex2 = new BadRequestException("Same message");

        assertThat(ex1).isNotEqualTo(ex2);
        assertThat(ex1).isNotSameAs(ex2);
    }

    @Test
    @DisplayName("Status code consistency across operations")
    void testStatusCodeConsistency() {
        BadRequestException exception = new BadRequestException("Error", new RuntimeException("Cause"));

        int code1 = exception.getStatusCode();
        int code2 = exception.getStatusCode();
        int code3 = exception.getStatusCode();

        assertThat(code1).isEqualTo(code2).isEqualTo(code3).isEqualTo(400);
    }
}
