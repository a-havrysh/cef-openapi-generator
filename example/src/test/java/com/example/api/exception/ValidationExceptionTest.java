package com.example.api.exception;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ValidationException class.
 * Target coverage: 100%
 */
class ValidationExceptionTest {

    @Test
    void testConstructorWithMessageAndErrors() {
        List<ValidationException.ValidationError> errors = Arrays.asList(
            new ValidationException.ValidationError("name", "ab", "minLength", "name must be at least 3 characters"),
            new ValidationException.ValidationError("age", 15, "minimum", "age must be at least 18")
        );

        ValidationException exception = new ValidationException("Custom validation failed", errors);

        assertEquals("Custom validation failed", exception.getMessage());
        assertEquals(2, exception.getErrors().size());
        assertEquals(400, exception.getStatusCode());
        assertTrue(exception instanceof BadRequestException);
    }

    @Test
    void testConstructorWithErrorsOnly_EmptyList() {
        List<ValidationException.ValidationError> errors = Collections.emptyList();

        ValidationException exception = new ValidationException(errors);

        assertEquals("Validation failed", exception.getMessage());
        assertEquals(0, exception.getErrors().size());
        assertEquals(400, exception.getStatusCode());
    }

    @Test
    void testConstructorWithErrorsOnly_SingleError() {
        List<ValidationException.ValidationError> errors = Collections.singletonList(
            new ValidationException.ValidationError("email", "invalid", "pattern", "email must match pattern: ^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
        );

        ValidationException exception = new ValidationException(errors);

        assertEquals("email must match pattern: ^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", exception.getMessage());
        assertEquals(1, exception.getErrors().size());
    }

    @Test
    void testConstructorWithErrorsOnly_MultipleErrors() {
        List<ValidationException.ValidationError> errors = Arrays.asList(
            new ValidationException.ValidationError("name", null, "required", "name is required"),
            new ValidationException.ValidationError("email", "invalid", "pattern", "email must match pattern"),
            new ValidationException.ValidationError("age", 15, "minimum", "age must be at least 18")
        );

        ValidationException exception = new ValidationException(errors);

        assertEquals("Validation failed with 3 error(s)", exception.getMessage());
        assertEquals(3, exception.getErrors().size());
    }

    @Test
    void testGetErrors_ReturnsUnmodifiableList() {
        List<ValidationException.ValidationError> errors = new ArrayList<>();
        errors.add(new ValidationException.ValidationError("field", "value", "required", "field is required"));

        ValidationException exception = new ValidationException(errors);
        List<ValidationException.ValidationError> returnedErrors = exception.getErrors();

        assertThrows(UnsupportedOperationException.class, () -> {
            returnedErrors.add(new ValidationException.ValidationError("another", "value", "required", "another is required"));
        });
    }

    @Test
    void testGetErrors_IsCopy() {
        List<ValidationException.ValidationError> errors = new ArrayList<>();
        errors.add(new ValidationException.ValidationError("field", "value", "required", "field is required"));

        ValidationException exception = new ValidationException(errors);

        // Modify original list
        errors.add(new ValidationException.ValidationError("another", "value", "required", "another is required"));

        // Exception's list should not be affected
        assertEquals(1, exception.getErrors().size());
    }

    @Test
    void testValidationError_AllFields() {
        ValidationException.ValidationError error = new ValidationException.ValidationError(
            "username",
            "ab",
            "minLength",
            "username must be at least 3 characters (got 2)"
        );

        assertEquals("username", error.getParameter());
        assertEquals("ab", error.getValue());
        assertEquals("minLength", error.getConstraint());
        assertEquals("username must be at least 3 characters (got 2)", error.getMessage());
    }

    @Test
    void testValidationError_NullValue() {
        ValidationException.ValidationError error = new ValidationException.ValidationError(
            "email",
            null,
            "required",
            "email is required"
        );

        assertEquals("email", error.getParameter());
        assertNull(error.getValue());
        assertEquals("required", error.getConstraint());
        assertEquals("email is required", error.getMessage());
    }

    @Test
    void testValidationError_NumericValue() {
        ValidationException.ValidationError error = new ValidationException.ValidationError(
            "age",
            17,
            "minimum",
            "age must be at least 18 (got 17)"
        );

        assertEquals("age", error.getParameter());
        assertEquals(17, error.getValue());
        assertEquals("minimum", error.getConstraint());
        assertEquals("age must be at least 18 (got 17)", error.getMessage());
    }

    @Test
    void testValidationError_ToString() {
        ValidationException.ValidationError error = new ValidationException.ValidationError(
            "status",
            "invalid",
            "enum",
            "status must be one of: pending, active, completed"
        );

        String toString = error.toString();

        assertTrue(toString.contains("status must be one of: pending, active, completed"));
        assertTrue(toString.contains("parameter: status"));
        assertTrue(toString.contains("constraint: enum"));
    }

    @Test
    void testValidationError_ToStringWithNullValue() {
        ValidationException.ValidationError error = new ValidationException.ValidationError(
            "field",
            null,
            "required",
            "field is required"
        );

        String toString = error.toString();

        assertTrue(toString.contains("field is required"));
        assertTrue(toString.contains("parameter: field"));
        assertTrue(toString.contains("constraint: required"));
    }

    @Test
    void testExceptionInheritance() {
        List<ValidationException.ValidationError> errors = Collections.emptyList();
        ValidationException exception = new ValidationException(errors);

        assertTrue(exception instanceof BadRequestException);
        assertTrue(exception instanceof ApiException);
        assertTrue(exception instanceof Exception);
    }

    @Test
    void testMultipleErrorTypes() {
        List<ValidationException.ValidationError> errors = Arrays.asList(
            new ValidationException.ValidationError("email", null, "required", "email is required"),
            new ValidationException.ValidationError("username", "ab", "minLength", "username too short"),
            new ValidationException.ValidationError("age", 200, "maximum", "age too large"),
            new ValidationException.ValidationError("status", "invalid", "enum", "status must be valid"),
            new ValidationException.ValidationError("phone", "+123", "pattern", "phone must match pattern")
        );

        ValidationException exception = new ValidationException(errors);

        assertEquals(5, exception.getErrors().size());
        assertEquals("Validation failed with 5 error(s)", exception.getMessage());

        // Verify error types
        assertEquals("required", exception.getErrors().get(0).getConstraint());
        assertEquals("minLength", exception.getErrors().get(1).getConstraint());
        assertEquals("maximum", exception.getErrors().get(2).getConstraint());
        assertEquals("enum", exception.getErrors().get(3).getConstraint());
        assertEquals("pattern", exception.getErrors().get(4).getConstraint());
    }

    @Test
    void testBuildMessage_WithZeroErrors() {
        ValidationException exception = new ValidationException(Collections.emptyList());
        assertEquals("Validation failed", exception.getMessage());
    }

    @Test
    void testBuildMessage_WithOneError() {
        ValidationException.ValidationError error = new ValidationException.ValidationError(
            "field",
            "value",
            "constraint",
            "Single error message"
        );
        ValidationException exception = new ValidationException(Collections.singletonList(error));
        assertEquals("Single error message", exception.getMessage());
    }

    @Test
    void testBuildMessage_WithTwoErrors() {
        List<ValidationException.ValidationError> errors = Arrays.asList(
            new ValidationException.ValidationError("field1", "value1", "constraint1", "Error 1"),
            new ValidationException.ValidationError("field2", "value2", "constraint2", "Error 2")
        );
        ValidationException exception = new ValidationException(errors);
        assertEquals("Validation failed with 2 error(s)", exception.getMessage());
    }

    @Test
    void testThrowAndCatch() {
        List<ValidationException.ValidationError> errors = Collections.singletonList(
            new ValidationException.ValidationError("field", "value", "required", "field is required")
        );

        assertThrows(ValidationException.class, () -> {
            throw new ValidationException(errors);
        });

        assertThrows(BadRequestException.class, () -> {
            throw new ValidationException(errors);
        });

        assertThrows(ApiException.class, () -> {
            throw new ValidationException(errors);
        });
    }

    @Test
    void testGetStatusCode() {
        ValidationException exception = new ValidationException(Collections.emptyList());
        assertEquals(400, exception.getStatusCode());
    }
}
