package com.example.api.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for NotFoundException.
 * Target coverage: 100%
 */
@DisplayName("NotFoundException Tests")
class NotFoundExceptionTest {

    @Test
    @DisplayName("Constructor with message creates exception with HTTP 404")
    void testConstructorWithMessage() {
        String message = "Resource not found";
        NotFoundException exception = new NotFoundException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getStatusCode()).isEqualTo(404);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Constructor with message and cause preserves both")
    void testConstructorWithMessageAndCause() {
        String message = "User with ID 123 not found";
        IllegalArgumentException cause = new IllegalArgumentException("Invalid ID format");
        NotFoundException exception = new NotFoundException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getStatusCode()).isEqualTo(404);
        assertThat(exception.getCause()).isSameAs(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Invalid ID format");
    }

    @Test
    @DisplayName("StatusCode is always 404 for NotFoundException")
    void testStatusCodeIsAlways404() {
        NotFoundException exception1 = new NotFoundException("Error 1");
        NotFoundException exception2 = new NotFoundException("Error 2", new RuntimeException());

        assertThat(exception1.getStatusCode()).isEqualTo(404);
        assertThat(exception2.getStatusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("NotFoundException inherits from ApiException")
    void testInheritanceFromApiException() {
        NotFoundException exception = new NotFoundException("Not found");

        assertThat(exception).isInstanceOf(ApiException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }

    @Test
    @DisplayName("Exception can be caught as ApiException")
    void testCatchAsApiException() {
        try {
            throw new NotFoundException("Entity not found");
        } catch (ApiException e) {
            assertThat(e.getStatusCode()).isEqualTo(404);
            assertThat(e.getMessage()).isEqualTo("Entity not found");
        }
    }

    @Test
    @DisplayName("Exception can be caught as RuntimeException")
    void testCatchAsRuntimeException() {
        try {
            throw new NotFoundException("Page not found");
        } catch (RuntimeException e) {
            assertThat(e).isInstanceOf(NotFoundException.class);
            assertThat(e.getMessage()).isEqualTo("Page not found");
        }
    }

    @Test
    @DisplayName("toString() contains message")
    void testToStringContainsMessage() {
        String message = "Article with slug 'not-exists' not found";
        NotFoundException exception = new NotFoundException(message);

        assertThat(exception.toString()).contains(message);
        assertThat(exception.toString()).contains("NotFoundException");
    }

    @Test
    @DisplayName("Null message is handled correctly")
    void testNullMessage() {
        NotFoundException exception = new NotFoundException(null);

        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getStatusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("Empty message is handled correctly")
    void testEmptyMessage() {
        NotFoundException exception = new NotFoundException("");

        assertThat(exception.getMessage()).isEmpty();
        assertThat(exception.getStatusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("Cause chain is preserved correctly")
    void testCauseChain() {
        RuntimeException level1 = new RuntimeException("Database query failed");
        IllegalStateException level2 = new IllegalStateException("Connection lost", level1);
        NotFoundException exception = new NotFoundException("Record not found", level2);

        assertThat(exception.getCause()).isSameAs(level2);
        assertThat(exception.getCause().getCause()).isSameAs(level1);
        assertThat(exception.getCause().getCause().getCause()).isNull();
    }

    @Test
    @DisplayName("Multiple instances are independent")
    void testMultipleInstancesAreIndependent() {
        NotFoundException exception1 = new NotFoundException("User not found");
        NotFoundException exception2 = new NotFoundException("Product not found");

        assertThat(exception1.getMessage()).isEqualTo("User not found");
        assertThat(exception2.getMessage()).isEqualTo("Product not found");
        assertThat(exception1).isNotSameAs(exception2);
    }

    @Test
    @DisplayName("getMessage() returns expected value")
    void testGetMessage() {
        String message = "Post with ID 999 not found";
        NotFoundException exception = new NotFoundException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("getStatusCode() returns expected value")
    void testGetStatusCode() {
        NotFoundException exception = new NotFoundException("Some resource");

        assertThat(exception.getStatusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("getCause() returns expected cause")
    void testGetCause() {
        RuntimeException cause = new RuntimeException("Original error");
        NotFoundException exception = new NotFoundException("Wrapped error", cause);

        assertThat(exception.getCause()).isSameAs(cause);
    }

    @Test
    @DisplayName("getCause() returns null when no cause provided")
    void testGetCauseReturnsNullWhenNoCause() {
        NotFoundException exception = new NotFoundException("Simple error");

        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Different message types for resource identifiers")
    void testDifferentResourceIdentifierMessages() {
        String userMessage = "User ID: 123 not found";
        String uuidMessage = "Resource with UUID: 550e8400-e29b-41d4-a716-446655440000 not found";
        String slugMessage = "Post with slug: 'missing-article' not found";

        NotFoundException ex1 = new NotFoundException(userMessage);
        NotFoundException ex2 = new NotFoundException(uuidMessage);
        NotFoundException ex3 = new NotFoundException(slugMessage);

        assertThat(ex1.getMessage()).isEqualTo(userMessage);
        assertThat(ex2.getMessage()).isEqualTo(uuidMessage);
        assertThat(ex3.getMessage()).isEqualTo(slugMessage);
    }

    @Test
    @DisplayName("Exception stack trace is properly generated")
    void testStackTrace() {
        NotFoundException exception = new NotFoundException("Not found");
        StackTraceElement[] stackTrace = exception.getStackTrace();

        assertThat(stackTrace).isNotEmpty();
        assertThat(stackTrace[0].getClassName()).contains("NotFoundExceptionTest");
    }

    @Test
    @DisplayName("Exception with long message")
    void testLongMessage() {
        String longMessage = "Resource 'B'.repeat(400) + ' not found'";
        NotFoundException exception = new NotFoundException(longMessage);

        assertThat(exception.getMessage()).isEqualTo(longMessage);
    }

    @Test
    @DisplayName("Exception with special characters in cause message")
    void testSpecialCharactersInCauseMessage() {
        RuntimeException cause = new RuntimeException("Query: SELECT * FROM users WHERE id = '123'");
        NotFoundException exception = new NotFoundException("Not found in database", cause);

        assertThat(exception.getCause().getMessage()).contains("SELECT");
    }

    @Test
    @DisplayName("Is serializable (implements RuntimeException)")
    void testIsSerializable() {
        NotFoundException exception = new NotFoundException("Not found");

        assertThat(exception).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Two exceptions with same message are not equal (different objects)")
    void testEqualityNotBasedOnMessage() {
        NotFoundException ex1 = new NotFoundException("Not found");
        NotFoundException ex2 = new NotFoundException("Not found");

        assertThat(ex1).isNotEqualTo(ex2);
        assertThat(ex1).isNotSameAs(ex2);
    }

    @Test
    @DisplayName("Status code consistency across operations")
    void testStatusCodeConsistency() {
        NotFoundException exception = new NotFoundException("Error", new RuntimeException("Cause"));

        int code1 = exception.getStatusCode();
        int code2 = exception.getStatusCode();
        int code3 = exception.getStatusCode();

        assertThat(code1).isEqualTo(code2).isEqualTo(code3).isEqualTo(404);
    }

    @Test
    @DisplayName("Common 404 scenarios")
    void testCommon404Scenarios() {
        NotFoundException userNotFound = new NotFoundException("User not found");
        NotFoundException postNotFound = new NotFoundException("Post not found");
        NotFoundException commentNotFound = new NotFoundException("Comment not found");
        NotFoundException categoryNotFound = new NotFoundException("Category not found");

        assertThat(userNotFound.getStatusCode()).isEqualTo(404);
        assertThat(postNotFound.getStatusCode()).isEqualTo(404);
        assertThat(commentNotFound.getStatusCode()).isEqualTo(404);
        assertThat(categoryNotFound.getStatusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("Cause from different exception types")
    void testCauseFromDifferentExceptionTypes() {
        IllegalArgumentException cause1 = new IllegalArgumentException("Invalid ID");
        NullPointerException cause2 = new NullPointerException("Null reference");
        IndexOutOfBoundsException cause3 = new IndexOutOfBoundsException("Index out of range");

        NotFoundException ex1 = new NotFoundException("Not found", cause1);
        NotFoundException ex2 = new NotFoundException("Not found", cause2);
        NotFoundException ex3 = new NotFoundException("Not found", cause3);

        assertThat(ex1.getCause()).isInstanceOf(IllegalArgumentException.class);
        assertThat(ex2.getCause()).isInstanceOf(NullPointerException.class);
        assertThat(ex3.getCause()).isInstanceOf(IndexOutOfBoundsException.class);
    }
}
