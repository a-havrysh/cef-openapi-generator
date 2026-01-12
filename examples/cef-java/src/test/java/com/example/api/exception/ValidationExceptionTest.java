package com.example.api.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for ValidationException class.
 * Target coverage: 100%
 */
@DisplayName("ValidationException Tests")
class ValidationExceptionTest {

    @Test
    @DisplayName("Constructor with message and errors creates exception")
    void testConstructorWithMessageAndErrors() {
        List<ValidationException.ValidationError> errors = Arrays.asList(
            new ValidationException.ValidationError("name", "ab", "minLength", "name must be at least 3 characters"),
            new ValidationException.ValidationError("age", 15, "minimum", "age must be at least 18")
        );

        ValidationException exception = new ValidationException("Custom validation failed", errors);

        assertThat(exception.getMessage()).isEqualTo("Custom validation failed");
        assertThat(exception.getErrors()).hasSize(2);
        assertThat(exception.getStatusCode()).isEqualTo(400);
        assertThat(exception).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Constructor with empty error list generates default message")
    void testConstructorWithErrorsOnly_EmptyList() {
        List<ValidationException.ValidationError> errors = Collections.emptyList();

        ValidationException exception = new ValidationException(errors);

        assertThat(exception.getMessage()).isEqualTo("Validation failed");
        assertThat(exception.getErrors()).isEmpty();
        assertThat(exception.getStatusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Constructor with single error uses error message as exception message")
    void testConstructorWithErrorsOnly_SingleError() {
        List<ValidationException.ValidationError> errors = Collections.singletonList(
            new ValidationException.ValidationError("email", "invalid", "pattern", "email must match pattern: ^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
        );

        ValidationException exception = new ValidationException(errors);

        assertThat(exception.getMessage()).isEqualTo("email must match pattern: ^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
        assertThat(exception.getErrors()).hasSize(1);
    }

    @Test
    @DisplayName("Constructor with multiple errors generates count message")
    void testConstructorWithErrorsOnly_MultipleErrors() {
        List<ValidationException.ValidationError> errors = Arrays.asList(
            new ValidationException.ValidationError("name", null, "required", "name is required"),
            new ValidationException.ValidationError("email", "invalid", "pattern", "email must match pattern"),
            new ValidationException.ValidationError("age", 15, "minimum", "age must be at least 18")
        );

        ValidationException exception = new ValidationException(errors);

        assertThat(exception.getMessage()).isEqualTo("Validation failed with 3 error(s)");
        assertThat(exception.getErrors()).hasSize(3);
    }

    @Test
    @DisplayName("getErrors() returns unmodifiable list")
    void testGetErrors_ReturnsUnmodifiableList() {
        List<ValidationException.ValidationError> errors = new ArrayList<>();
        errors.add(new ValidationException.ValidationError("field", "value", "required", "field is required"));

        ValidationException exception = new ValidationException(errors);
        List<ValidationException.ValidationError> returnedErrors = exception.getErrors();

        assertThatThrownBy(() -> {
            returnedErrors.add(new ValidationException.ValidationError("another", "value", "required", "another is required"));
        }).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("getErrors() returns defensive copy of errors")
    void testGetErrors_IsCopy() {
        List<ValidationException.ValidationError> errors = new ArrayList<>();
        errors.add(new ValidationException.ValidationError("field", "value", "required", "field is required"));

        ValidationException exception = new ValidationException(errors);

        // Modify original list
        errors.add(new ValidationException.ValidationError("another", "value", "required", "another is required"));

        // Exception's list should not be affected
        assertThat(exception.getErrors()).hasSize(1);
    }

    @Test
    @DisplayName("ValidationError contains all fields")
    void testValidationError_AllFields() {
        ValidationException.ValidationError error = new ValidationException.ValidationError(
            "username",
            "ab",
            "minLength",
            "username must be at least 3 characters (got 2)"
        );

        assertThat(error.getParameter()).isEqualTo("username");
        assertThat(error.getValue()).isEqualTo("ab");
        assertThat(error.getConstraint()).isEqualTo("minLength");
        assertThat(error.getMessage()).isEqualTo("username must be at least 3 characters (got 2)");
    }

    @Test
    @DisplayName("ValidationError handles null value")
    void testValidationError_NullValue() {
        ValidationException.ValidationError error = new ValidationException.ValidationError(
            "email",
            null,
            "required",
            "email is required"
        );

        assertThat(error.getParameter()).isEqualTo("email");
        assertThat(error.getValue()).isNull();
        assertThat(error.getConstraint()).isEqualTo("required");
        assertThat(error.getMessage()).isEqualTo("email is required");
    }

    @Test
    @DisplayName("ValidationError handles numeric values")
    void testValidationError_NumericValue() {
        ValidationException.ValidationError error = new ValidationException.ValidationError(
            "age",
            17,
            "minimum",
            "age must be at least 18 (got 17)"
        );

        assertThat(error.getParameter()).isEqualTo("age");
        assertThat(error.getValue()).isEqualTo(17);
        assertThat(error.getConstraint()).isEqualTo("minimum");
        assertThat(error.getMessage()).isEqualTo("age must be at least 18 (got 17)");
    }

    @Test
    @DisplayName("ValidationError toString() includes all information")
    void testValidationError_ToString() {
        ValidationException.ValidationError error = new ValidationException.ValidationError(
            "status",
            "invalid",
            "enum",
            "status must be one of: pending, active, completed"
        );

        String toString = error.toString();

        assertThat(toString).contains("status must be one of: pending, active, completed");
        assertThat(toString).contains("parameter: status");
        assertThat(toString).contains("constraint: enum");
    }

    @Test
    @DisplayName("ValidationError toString() handles null values")
    void testValidationError_ToStringWithNullValue() {
        ValidationException.ValidationError error = new ValidationException.ValidationError(
            "field",
            null,
            "required",
            "field is required"
        );

        String toString = error.toString();

        assertThat(toString).contains("field is required");
        assertThat(toString).contains("parameter: field");
        assertThat(toString).contains("constraint: required");
    }

    @Test
    @DisplayName("ValidationException inherits from BadRequestException")
    void testExceptionInheritance() {
        List<ValidationException.ValidationError> errors = Collections.emptyList();
        ValidationException exception = new ValidationException(errors);

        assertThat(exception).isInstanceOf(BadRequestException.class);
        assertThat(exception).isInstanceOf(ApiException.class);
        assertThat(exception).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("ValidationException handles multiple error types")
    void testMultipleErrorTypes() {
        List<ValidationException.ValidationError> errors = Arrays.asList(
            new ValidationException.ValidationError("email", null, "required", "email is required"),
            new ValidationException.ValidationError("username", "ab", "minLength", "username too short"),
            new ValidationException.ValidationError("age", 200, "maximum", "age too large"),
            new ValidationException.ValidationError("status", "invalid", "enum", "status must be valid"),
            new ValidationException.ValidationError("phone", "+123", "pattern", "phone must match pattern")
        );

        ValidationException exception = new ValidationException(errors);

        assertThat(exception.getErrors()).hasSize(5);
        assertThat(exception.getMessage()).isEqualTo("Validation failed with 5 error(s)");

        // Verify error types
        assertThat(exception.getErrors().get(0).getConstraint()).isEqualTo("required");
        assertThat(exception.getErrors().get(1).getConstraint()).isEqualTo("minLength");
        assertThat(exception.getErrors().get(2).getConstraint()).isEqualTo("maximum");
        assertThat(exception.getErrors().get(3).getConstraint()).isEqualTo("enum");
        assertThat(exception.getErrors().get(4).getConstraint()).isEqualTo("pattern");
    }

    @Test
    @DisplayName("Message building with zero errors")
    void testBuildMessage_WithZeroErrors() {
        ValidationException exception = new ValidationException(Collections.emptyList());
        assertThat(exception.getMessage()).isEqualTo("Validation failed");
    }

    @Test
    @DisplayName("Message building with one error uses error message")
    void testBuildMessage_WithOneError() {
        ValidationException.ValidationError error = new ValidationException.ValidationError(
            "field",
            "value",
            "constraint",
            "Single error message"
        );
        ValidationException exception = new ValidationException(Collections.singletonList(error));
        assertThat(exception.getMessage()).isEqualTo("Single error message");
    }

    @Test
    @DisplayName("Message building with two errors shows count")
    void testBuildMessage_WithTwoErrors() {
        List<ValidationException.ValidationError> errors = Arrays.asList(
            new ValidationException.ValidationError("field1", "value1", "constraint1", "Error 1"),
            new ValidationException.ValidationError("field2", "value2", "constraint2", "Error 2")
        );
        ValidationException exception = new ValidationException(errors);
        assertThat(exception.getMessage()).isEqualTo("Validation failed with 2 error(s)");
    }

    @Test
    @DisplayName("ValidationException can be caught as different types")
    void testThrowAndCatch() {
        List<ValidationException.ValidationError> errors = Collections.singletonList(
            new ValidationException.ValidationError("field", "value", "required", "field is required")
        );

        assertThatThrownBy(() -> {
            throw new ValidationException(errors);
        }).isInstanceOf(ValidationException.class);

        assertThatThrownBy(() -> {
            throw new ValidationException(errors);
        }).isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> {
            throw new ValidationException(errors);
        }).isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("ValidationException status code is 400")
    void testGetStatusCode() {
        ValidationException exception = new ValidationException(Collections.emptyList());
        assertThat(exception.getStatusCode()).isEqualTo(400);
    }
}
