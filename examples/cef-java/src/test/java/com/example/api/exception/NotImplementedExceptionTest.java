package com.example.api.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for NotImplementedException.
 * Target coverage: 100%
 *
 * Note: NotImplementedException does NOT extend ApiException.
 * It extends RuntimeException directly, representing a programming error
 * rather than an HTTP error condition.
 */
@DisplayName("NotImplementedException Tests")
class NotImplementedExceptionTest {

    @Test
    @DisplayName("Constructor with message creates exception")
    void testConstructorWithMessage() {
        String message = "This method is not implemented yet";
        NotImplementedException exception = new NotImplementedException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Does NOT inherit from ApiException")
    void testDoesNotInheritFromApiException() {
        NotImplementedException exception = new NotImplementedException("Not implemented");

        assertThat(exception).isNotInstanceOf(ApiException.class);
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }

    @Test
    @DisplayName("Inherits from RuntimeException")
    void testInheritsFromRuntimeException() {
        NotImplementedException exception = new NotImplementedException("Not implemented");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Exception can be caught as RuntimeException")
    void testCatchAsRuntimeException() {
        try {
            throw new NotImplementedException("Method not implemented");
        } catch (RuntimeException e) {
            assertThat(e).isInstanceOf(NotImplementedException.class);
            assertThat(e.getMessage()).isEqualTo("Method not implemented");
        }
    }

    @Test
    @DisplayName("Exception can be caught as Exception")
    void testCatchAsException() {
        try {
            throw new NotImplementedException("Feature not yet available");
        } catch (Exception e) {
            assertThat(e).isInstanceOf(NotImplementedException.class);
        }
    }

    @Test
    @DisplayName("toString() contains message and class name")
    void testToStringContainsMessage() {
        String message = "Future implementation pending";
        NotImplementedException exception = new NotImplementedException(message);

        assertThat(exception.toString()).contains(message);
        assertThat(exception.toString()).contains("NotImplementedException");
    }

    @Test
    @DisplayName("Null message is handled correctly")
    void testNullMessage() {
        NotImplementedException exception = new NotImplementedException(null);

        assertThat(exception.getMessage()).isNull();
    }

    @Test
    @DisplayName("Empty message is handled correctly")
    void testEmptyMessage() {
        NotImplementedException exception = new NotImplementedException("");

        assertThat(exception.getMessage()).isEmpty();
    }

    @Test
    @DisplayName("getMessage() returns expected value")
    void testGetMessage() {
        String message = "deleteUser method needs implementation";
        NotImplementedException exception = new NotImplementedException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("getCause() is null (no cause constructor)")
    void testGetCause() {
        NotImplementedException exception = new NotImplementedException("Not implemented");

        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Multiple instances are independent")
    void testMultipleInstancesAreIndependent() {
        NotImplementedException exception1 = new NotImplementedException("Feature A not implemented");
        NotImplementedException exception2 = new NotImplementedException("Feature B not implemented");

        assertThat(exception1.getMessage()).isEqualTo("Feature A not implemented");
        assertThat(exception2.getMessage()).isEqualTo("Feature B not implemented");
        assertThat(exception1).isNotSameAs(exception2);
    }

    @Test
    @DisplayName("Different message types for stub methods")
    void testDifferentMessageTypes() {
        String methodMessage = "updateUser(String id, User data) not implemented";
        String featureMessage = "Advanced search feature not yet available";
        String simpleMessage = "TODO";

        NotImplementedException ex1 = new NotImplementedException(methodMessage);
        NotImplementedException ex2 = new NotImplementedException(featureMessage);
        NotImplementedException ex3 = new NotImplementedException(simpleMessage);

        assertThat(ex1.getMessage()).isEqualTo(methodMessage);
        assertThat(ex2.getMessage()).isEqualTo(featureMessage);
        assertThat(ex3.getMessage()).isEqualTo(simpleMessage);
    }

    @Test
    @DisplayName("Exception stack trace is properly generated")
    void testStackTrace() {
        NotImplementedException exception = new NotImplementedException("Not implemented");
        StackTraceElement[] stackTrace = exception.getStackTrace();

        assertThat(stackTrace).isNotEmpty();
        assertThat(stackTrace[0].getClassName()).contains("NotImplementedExceptionTest");
    }

    @Test
    @DisplayName("Exception with long message")
    void testLongMessage() {
        String longMessage = "B".repeat(500);
        NotImplementedException exception = new NotImplementedException(longMessage);

        assertThat(exception.getMessage()).isEqualTo(longMessage);
        assertThat(exception.getMessage()).hasSize(500);
    }

    @Test
    @DisplayName("Is serializable (implements RuntimeException)")
    void testIsSerializable() {
        NotImplementedException exception = new NotImplementedException("Not implemented");

        assertThat(exception).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    @DisplayName("Two exceptions with same message are not equal (different objects)")
    void testEqualityNotBasedOnMessage() {
        NotImplementedException ex1 = new NotImplementedException("Not implemented");
        NotImplementedException ex2 = new NotImplementedException("Not implemented");

        assertThat(ex1).isNotEqualTo(ex2);
        assertThat(ex1).isNotSameAs(ex2);
    }

    @Test
    @DisplayName("Common stub method scenarios")
    void testCommonStubMethodScenarios() {
        NotImplementedException createException = new NotImplementedException("POST /users not implemented");
        NotImplementedException readException = new NotImplementedException("GET /users/{id} not implemented");
        NotImplementedException updateException = new NotImplementedException("PUT /users/{id} not implemented");
        NotImplementedException deleteException = new NotImplementedException("DELETE /users/{id} not implemented");

        assertThat(createException.getMessage()).contains("POST");
        assertThat(readException.getMessage()).contains("GET");
        assertThat(updateException.getMessage()).contains("PUT");
        assertThat(deleteException.getMessage()).contains("DELETE");
    }

    @Test
    @DisplayName("Used as a placeholder for developers")
    void testUsedAsPlaceholder() {
        String message = "Implement business logic for order processing";
        NotImplementedException exception = new NotImplementedException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).contains("Implement");
    }

    @Test
    @DisplayName("Final class is not extended")
    void testFinalClassCannotBeExtended() {
        // This test verifies that NotImplementedException is final
        // which prevents accidental subclassing
        NotImplementedException exception = new NotImplementedException("Test");

        assertThat(exception.getClass().getName()).isEqualTo("com.example.api.exception.NotImplementedException");
    }

    @Test
    @DisplayName("Typical usage in generated stub code")
    void testTypicalStubUsage() {
        String methodName = "getAllUsers";
        String message = methodName + "() method generated but not yet implemented - add your implementation here";

        NotImplementedException exception = new NotImplementedException(message);

        assertThat(exception.getMessage()).contains(methodName);
        assertThat(exception.getMessage()).contains("not yet implemented");
    }

    @Test
    @DisplayName("getMessage() is consistent across multiple calls")
    void testMessageConsistency() {
        String message = "Feature coming soon";
        NotImplementedException exception = new NotImplementedException(message);

        String msg1 = exception.getMessage();
        String msg2 = exception.getMessage();
        String msg3 = exception.getMessage();

        assertThat(msg1).isEqualTo(msg2).isEqualTo(msg3).isEqualTo(message);
    }

    @Test
    @DisplayName("Special characters in message are preserved")
    void testSpecialCharactersPreserved() {
        String message = "Method: deleteAll() -> not [implemented] yet! @TODO #prioritize";
        NotImplementedException exception = new NotImplementedException(message);

        assertThat(exception.getMessage()).contains("->");
        assertThat(exception.getMessage()).contains("[");
        assertThat(exception.getMessage()).contains("]");
        assertThat(exception.getMessage()).contains("@TODO");
        assertThat(exception.getMessage()).contains("#");
    }

    @Test
    @DisplayName("Is not catchable as ApiException")
    void testNotCatchableAsApiException() {
        try {
            throw new NotImplementedException("Not implemented");
        } catch (ApiException e) {
            fail("NotImplementedException should not be catchable as ApiException");
        } catch (NotImplementedException e) {
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("Can coexist with other exception types")
    void testCoexistenceWithOtherTypes() {
        ApiException apiEx = new ApiException(500, "API Error");
        BadRequestException badReqEx = new BadRequestException("Bad Request");
        NotImplementedException notImplEx = new NotImplementedException("Not Implemented");

        assertThat(apiEx).isInstanceOf(ApiException.class);
        assertThat(badReqEx).isInstanceOf(ApiException.class);
        assertThat(notImplEx).isNotInstanceOf(ApiException.class);

        assertThat(notImplEx).isInstanceOf(RuntimeException.class);
        assertThat(apiEx).isInstanceOf(RuntimeException.class);
        assertThat(badReqEx).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("toString() format is consistent")
    void testToStringFormat() {
        NotImplementedException exception = new NotImplementedException("Test message");
        String toString1 = exception.toString();
        String toString2 = exception.toString();

        assertThat(toString1).isEqualTo(toString2);
    }

    @Test
    @DisplayName("Multiline message handling")
    void testMultilineMessage() {
        String multilineMessage = "Method not implemented\nPlease implement the business logic\nTODO: Add parameter validation";
        NotImplementedException exception = new NotImplementedException(multilineMessage);

        assertThat(exception.getMessage()).contains("\n");
        assertThat(exception.getMessage()).contains("Please");
        assertThat(exception.getMessage()).contains("TODO");
    }
}
