package com.example.api.validation;

import com.example.api.exception.ValidationException.ValidationError;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ParameterValidator class.
 * Target coverage: 100%
 */
class ParameterValidatorTest {

    // ========== validateString tests ==========

    @Test
    void testValidateString_Valid() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "name", "John", false, null, null, null, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateString_Required_Missing() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "name", null, true, null, null, null, null
        );

        assertEquals(1, errors.size());
        assertEquals("required", errors.get(0).getConstraint());
        assertEquals("name is required", errors.get(0).getMessage());
        assertNull(errors.get(0).getValue());
    }

    @Test
    void testValidateString_Required_Empty() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "name", "", true, null, null, null, null
        );

        assertEquals(1, errors.size());
        assertEquals("required", errors.get(0).getConstraint());
        assertEquals("name is required", errors.get(0).getMessage());
    }

    @Test
    void testValidateString_NotRequired_Null() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "name", null, false, 5, 10, ".*", Arrays.asList("valid")
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateString_MinLength_Valid() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "name", "John", false, 3, null, null, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateString_MinLength_Exact() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "name", "abc", false, 3, null, null, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateString_MinLength_TooShort() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "name", "ab", false, 3, null, null, null
        );

        assertEquals(1, errors.size());
        assertEquals("minLength", errors.get(0).getConstraint());
        assertEquals("name must be at least 3 characters (got 2)", errors.get(0).getMessage());
        assertEquals("ab", errors.get(0).getValue());
    }

    @Test
    void testValidateString_MaxLength_Valid() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "name", "John", false, null, 10, null, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateString_MaxLength_Exact() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "name", "1234567890", false, null, 10, null, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateString_MaxLength_TooLong() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "name", "12345678901", false, null, 10, null, null
        );

        assertEquals(1, errors.size());
        assertEquals("maxLength", errors.get(0).getConstraint());
        assertEquals("name must be at most 10 characters (got 11)", errors.get(0).getMessage());
        assertEquals("12345678901", errors.get(0).getValue());
    }

    @Test
    void testValidateString_MinAndMaxLength_BothViolated() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "name", "a", false, 3, 10, null, null
        );

        assertEquals(1, errors.size());
        assertEquals("minLength", errors.get(0).getConstraint());
    }

    @Test
    void testValidateString_MinAndMaxLength_OnlyMinViolated() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "name", "ab", false, 3, 10, null, null
        );

        assertEquals(1, errors.size());
        assertEquals("minLength", errors.get(0).getConstraint());
    }

    @Test
    void testValidateString_MinAndMaxLength_OnlyMaxViolated() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "name", "12345678901", false, 3, 10, null, null
        );

        assertEquals(1, errors.size());
        assertEquals("maxLength", errors.get(0).getConstraint());
    }

    @Test
    void testValidateString_Pattern_Valid() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "email", "test@example.com", false, null, null, "^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$", null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateString_Pattern_Invalid() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "email", "invalid-email", false, null, null, "^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$", null
        );

        assertEquals(1, errors.size());
        assertEquals("pattern", errors.get(0).getConstraint());
        assertTrue(errors.get(0).getMessage().contains("must match pattern"));
    }

    @Test
    void testValidateString_Pattern_NullPattern() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "field", "any-value", false, null, null, null, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateString_Enum_Valid() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "status", "active", false, null, null, null, Arrays.asList("active", "inactive", "pending")
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateString_Enum_Invalid() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "status", "invalid", false, null, null, null, Arrays.asList("active", "inactive", "pending")
        );

        assertEquals(1, errors.size());
        assertEquals("enum", errors.get(0).getConstraint());
        assertEquals("status must be one of: active, inactive, pending", errors.get(0).getMessage());
        assertEquals("invalid", errors.get(0).getValue());
    }

    @Test
    void testValidateString_Enum_Null() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "status", "any", false, null, null, null, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateString_Enum_EmptyList() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "status", "any", false, null, null, null, Collections.emptyList()
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateString_MultipleConstraintsViolated() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "field", "a", false, 3, 10, "^[a-z]{3,}$", Arrays.asList("valid", "allowed")
        );

        // Should collect multiple errors (minLength, pattern, enum)
        assertTrue(errors.size() >= 2);
        boolean hasMinLength = errors.stream().anyMatch(e -> "minLength".equals(e.getConstraint()));
        assertTrue(hasMinLength);
    }

    @Test
    void testValidateString_PatternCaching() {
        String pattern = "^[a-z]+$";

        // First call - pattern should be compiled and cached
        List<ValidationError> errors1 = ParameterValidator.validateString(
            "field1", "abc", false, null, null, pattern, null
        );

        // Second call with same pattern - should use cached pattern
        List<ValidationError> errors2 = ParameterValidator.validateString(
            "field2", "xyz", false, null, null, pattern, null
        );

        assertTrue(errors1.isEmpty());
        assertTrue(errors2.isEmpty());

        // Invalid value with cached pattern
        List<ValidationError> errors3 = ParameterValidator.validateString(
            "field3", "ABC123", false, null, null, pattern, null
        );

        assertEquals(1, errors3.size());
        assertEquals("pattern", errors3.get(0).getConstraint());
    }

    // ========== validateInteger tests ==========

    @Test
    void testValidateInteger_Valid() {
        List<ValidationError> errors = ParameterValidator.validateInteger(
            "age", 25, false, null, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateInteger_Required_Missing() {
        List<ValidationError> errors = ParameterValidator.validateInteger(
            "age", null, true, null, null
        );

        assertEquals(1, errors.size());
        assertEquals("required", errors.get(0).getConstraint());
        assertEquals("age is required", errors.get(0).getMessage());
        assertNull(errors.get(0).getValue());
    }

    @Test
    void testValidateInteger_NotRequired_Null() {
        List<ValidationError> errors = ParameterValidator.validateInteger(
            "age", null, false, 1, 100
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateInteger_Minimum_Valid() {
        List<ValidationError> errors = ParameterValidator.validateInteger(
            "age", 18, false, 18, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateInteger_Minimum_BelowMinimum() {
        List<ValidationError> errors = ParameterValidator.validateInteger(
            "age", 17, false, 18, null
        );

        assertEquals(1, errors.size());
        assertEquals("minimum", errors.get(0).getConstraint());
        assertEquals("age must be at least 18 (got 17)", errors.get(0).getMessage());
        assertEquals(17, errors.get(0).getValue());
    }

    @Test
    void testValidateInteger_Maximum_Valid() {
        List<ValidationError> errors = ParameterValidator.validateInteger(
            "age", 65, false, null, 65
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateInteger_Maximum_AboveMaximum() {
        List<ValidationError> errors = ParameterValidator.validateInteger(
            "age", 66, false, null, 65
        );

        assertEquals(1, errors.size());
        assertEquals("maximum", errors.get(0).getConstraint());
        assertEquals("age must be at most 65 (got 66)", errors.get(0).getMessage());
        assertEquals(66, errors.get(0).getValue());
    }

    @Test
    void testValidateInteger_MinAndMax_BothViolated() {
        List<ValidationError> errors = ParameterValidator.validateInteger(
            "age", 200, false, 18, 65
        );

        assertEquals(1, errors.size());
        assertEquals("maximum", errors.get(0).getConstraint());
    }

    @Test
    void testValidateInteger_MinAndMax_Valid() {
        List<ValidationError> errors = ParameterValidator.validateInteger(
            "age", 30, false, 18, 65
        );

        assertTrue(errors.isEmpty());
    }

    // ========== validateNumber tests ==========

    @Test
    void testValidateNumber_Valid() {
        List<ValidationError> errors = ParameterValidator.validateNumber(
            "price", 19.99, false, null, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateNumber_Required_Missing() {
        List<ValidationError> errors = ParameterValidator.validateNumber(
            "price", null, true, null, null
        );

        assertEquals(1, errors.size());
        assertEquals("required", errors.get(0).getConstraint());
        assertEquals("price is required", errors.get(0).getMessage());
        assertNull(errors.get(0).getValue());
    }

    @Test
    void testValidateNumber_NotRequired_Null() {
        List<ValidationError> errors = ParameterValidator.validateNumber(
            "price", null, false, 0.01, 1000.0
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateNumber_Minimum_Valid() {
        List<ValidationError> errors = ParameterValidator.validateNumber(
            "price", 0.01, false, 0.01, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateNumber_Minimum_BelowMinimum() {
        List<ValidationError> errors = ParameterValidator.validateNumber(
            "price", 0.005, false, 0.01, null
        );

        assertEquals(1, errors.size());
        assertEquals("minimum", errors.get(0).getConstraint());
        assertTrue(errors.get(0).getMessage().contains("must be at least 0.01"));
    }

    @Test
    void testValidateNumber_Maximum_Valid() {
        List<ValidationError> errors = ParameterValidator.validateNumber(
            "price", 1000.0, false, null, 1000.0
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateNumber_Maximum_AboveMaximum() {
        List<ValidationError> errors = ParameterValidator.validateNumber(
            "price", 1000.01, false, null, 1000.0
        );

        assertEquals(1, errors.size());
        assertEquals("maximum", errors.get(0).getConstraint());
        assertTrue(errors.get(0).getMessage().contains("must be at most 1000"));
    }

    @Test
    void testValidateNumber_MinAndMax_Valid() {
        List<ValidationError> errors = ParameterValidator.validateNumber(
            "price", 50.0, false, 0.01, 1000.0
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateNumber_Integer() {
        List<ValidationError> errors = ParameterValidator.validateNumber(
            "count", 42, false, 0, 100
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateNumber_Float() {
        List<ValidationError> errors = ParameterValidator.validateNumber(
            "ratio", 0.5f, false, 0.0, 1.0
        );

        assertTrue(errors.isEmpty());
    }

    // ========== validateAndParseInteger tests ==========

    @Test
    void testValidateAndParseInteger_ValidString() {
        List<ValidationError> errors = ParameterValidator.validateAndParseInteger(
            "page", "42", false, null, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateAndParseInteger_Required_Missing() {
        List<ValidationError> errors = ParameterValidator.validateAndParseInteger(
            "page", null, true, null, null
        );

        assertEquals(1, errors.size());
        assertEquals("required", errors.get(0).getConstraint());
        assertEquals("page is required", errors.get(0).getMessage());
    }

    @Test
    void testValidateAndParseInteger_Required_Empty() {
        List<ValidationError> errors = ParameterValidator.validateAndParseInteger(
            "page", "", true, null, null
        );

        assertEquals(1, errors.size());
        assertEquals("required", errors.get(0).getConstraint());
    }

    @Test
    void testValidateAndParseInteger_NotRequired_Null() {
        List<ValidationError> errors = ParameterValidator.validateAndParseInteger(
            "page", null, false, 1, 100
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateAndParseInteger_NotRequired_Empty() {
        List<ValidationError> errors = ParameterValidator.validateAndParseInteger(
            "page", "", false, 1, 100
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateAndParseInteger_InvalidFormat() {
        List<ValidationError> errors = ParameterValidator.validateAndParseInteger(
            "page", "not-a-number", false, null, null
        );

        assertEquals(1, errors.size());
        assertEquals("type", errors.get(0).getConstraint());
        assertEquals("page must be a valid integer", errors.get(0).getMessage());
        assertEquals("not-a-number", errors.get(0).getValue());
    }

    @Test
    void testValidateAndParseInteger_InvalidFormat_Decimal() {
        List<ValidationError> errors = ParameterValidator.validateAndParseInteger(
            "page", "42.5", false, null, null
        );

        assertEquals(1, errors.size());
        assertEquals("type", errors.get(0).getConstraint());
    }

    @Test
    void testValidateAndParseInteger_WithMinimum_Valid() {
        List<ValidationError> errors = ParameterValidator.validateAndParseInteger(
            "page", "1", false, 1, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateAndParseInteger_WithMinimum_Invalid() {
        List<ValidationError> errors = ParameterValidator.validateAndParseInteger(
            "page", "0", false, 1, null
        );

        assertEquals(1, errors.size());
        assertEquals("minimum", errors.get(0).getConstraint());
    }

    @Test
    void testValidateAndParseInteger_WithMaximum_Valid() {
        List<ValidationError> errors = ParameterValidator.validateAndParseInteger(
            "page", "100", false, null, 100
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateAndParseInteger_WithMaximum_Invalid() {
        List<ValidationError> errors = ParameterValidator.validateAndParseInteger(
            "page", "101", false, null, 100
        );

        assertEquals(1, errors.size());
        assertEquals("maximum", errors.get(0).getConstraint());
    }

    @Test
    void testValidateAndParseInteger_WithMinAndMax_Valid() {
        List<ValidationError> errors = ParameterValidator.validateAndParseInteger(
            "page", "50", false, 1, 100
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateAndParseInteger_NegativeNumber() {
        List<ValidationError> errors = ParameterValidator.validateAndParseInteger(
            "offset", "-10", false, null, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateAndParseInteger_Zero() {
        List<ValidationError> errors = ParameterValidator.validateAndParseInteger(
            "count", "0", false, null, null
        );

        assertTrue(errors.isEmpty());
    }

    // ========== validateAndParseNumber tests ==========

    @Test
    void testValidateAndParseNumber_ValidDecimal() {
        List<ValidationError> errors = ParameterValidator.validateAndParseNumber(
            "price", "19.99", false, null, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateAndParseNumber_ValidInteger() {
        List<ValidationError> errors = ParameterValidator.validateAndParseNumber(
            "price", "42", false, null, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateAndParseNumber_Required_Missing() {
        List<ValidationError> errors = ParameterValidator.validateAndParseNumber(
            "price", null, true, null, null
        );

        assertEquals(1, errors.size());
        assertEquals("required", errors.get(0).getConstraint());
        assertEquals("price is required", errors.get(0).getMessage());
    }

    @Test
    void testValidateAndParseNumber_Required_Empty() {
        List<ValidationError> errors = ParameterValidator.validateAndParseNumber(
            "price", "", true, null, null
        );

        assertEquals(1, errors.size());
        assertEquals("required", errors.get(0).getConstraint());
    }

    @Test
    void testValidateAndParseNumber_NotRequired_Null() {
        List<ValidationError> errors = ParameterValidator.validateAndParseNumber(
            "price", null, false, 0.01, 1000.0
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateAndParseNumber_NotRequired_Empty() {
        List<ValidationError> errors = ParameterValidator.validateAndParseNumber(
            "price", "", false, 0.01, 1000.0
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateAndParseNumber_InvalidFormat() {
        List<ValidationError> errors = ParameterValidator.validateAndParseNumber(
            "price", "not-a-number", false, null, null
        );

        assertEquals(1, errors.size());
        assertEquals("type", errors.get(0).getConstraint());
        assertEquals("price must be a valid number", errors.get(0).getMessage());
        assertEquals("not-a-number", errors.get(0).getValue());
    }

    @Test
    void testValidateAndParseNumber_InvalidFormat_MultipleDecimals() {
        List<ValidationError> errors = ParameterValidator.validateAndParseNumber(
            "price", "19.99.00", false, null, null
        );

        assertEquals(1, errors.size());
        assertEquals("type", errors.get(0).getConstraint());
    }

    @Test
    void testValidateAndParseNumber_WithMinimum_Valid() {
        List<ValidationError> errors = ParameterValidator.validateAndParseNumber(
            "price", "0.01", false, 0.01, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateAndParseNumber_WithMinimum_Invalid() {
        List<ValidationError> errors = ParameterValidator.validateAndParseNumber(
            "price", "0.001", false, 0.01, null
        );

        assertEquals(1, errors.size());
        assertEquals("minimum", errors.get(0).getConstraint());
    }

    @Test
    void testValidateAndParseNumber_WithMaximum_Valid() {
        List<ValidationError> errors = ParameterValidator.validateAndParseNumber(
            "price", "1000.0", false, null, 1000.0
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateAndParseNumber_WithMaximum_Invalid() {
        List<ValidationError> errors = ParameterValidator.validateAndParseNumber(
            "price", "1000.01", false, null, 1000.0
        );

        assertEquals(1, errors.size());
        assertEquals("maximum", errors.get(0).getConstraint());
    }

    @Test
    void testValidateAndParseNumber_WithMinAndMax_Valid() {
        List<ValidationError> errors = ParameterValidator.validateAndParseNumber(
            "price", "50.5", false, 0.01, 1000.0
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateAndParseNumber_NegativeNumber() {
        List<ValidationError> errors = ParameterValidator.validateAndParseNumber(
            "temperature", "-10.5", false, null, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateAndParseNumber_Zero() {
        List<ValidationError> errors = ParameterValidator.validateAndParseNumber(
            "value", "0.0", false, null, null
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateAndParseNumber_ScientificNotation() {
        List<ValidationError> errors = ParameterValidator.validateAndParseNumber(
            "value", "1.5e2", false, null, null
        );

        assertTrue(errors.isEmpty());
    }

    // ========== Edge cases and special scenarios ==========

    @Test
    void testValidateString_AllConstraints_Valid() {
        List<ValidationError> errors = ParameterValidator.validateString(
            "username",
            "john",
            true,
            3,
            10,
            "^[a-z]+$",
            Arrays.asList("john", "jane", "admin")
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateInteger_ZeroValues() {
        List<ValidationError> errors = ParameterValidator.validateInteger(
            "count", 0, false, 0, 0
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateNumber_VerySmallNumber() {
        List<ValidationError> errors = ParameterValidator.validateNumber(
            "epsilon", 0.0000001, false, 0.0, 1.0
        );

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateNumber_VeryLargeNumber() {
        List<ValidationError> errors = ParameterValidator.validateNumber(
            "huge", 1e10, false, null, null
        );

        assertTrue(errors.isEmpty());
    }
}
