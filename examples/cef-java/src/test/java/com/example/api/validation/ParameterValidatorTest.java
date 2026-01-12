package com.example.api.validation;

import com.example.api.exception.ValidationException.ValidationError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ParameterValidator Tests")
class ParameterValidatorTest {

    @Nested
    @DisplayName("validateString Tests")
    class ValidateStringTests {

        @Nested
        @DisplayName("Required constraint")
        class RequiredTests {
            @Test
            void required_true_with_null_value_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateString("name", null, true, null, null, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getParameter()).isEqualTo("name");
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
                assertThat(errors.get(0).getValue()).isNull();
                assertThat(errors.get(0).getMessage()).contains("required");
            }

            @Test
            void required_true_with_empty_string_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "", true, null, null, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void required_true_with_valid_value_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "John", true, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_false_with_null_value_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateString("name", null, false, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_false_with_empty_string_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "", false, null, null, null, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("MinLength constraint")
        class MinLengthTests {
            @Test
            void minLength_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "John", false, 3, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minLength_not_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "Jo", false, 3, null, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("minLength");
                assertThat(errors.get(0).getMessage()).contains("3");
                assertThat(errors.get(0).getMessage()).contains("2");
            }

            @Test
            void minLength_exact_boundary() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "Joh", false, 3, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minLength_with_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateString("name", null, false, 3, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minLength_empty_string_fails() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "", false, 1, null, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("minLength");
            }

            @Test
            void minLength_zero() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "", false, 0, null, null, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("MaxLength constraint")
        class MaxLengthTests {
            @Test
            void maxLength_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "John", false, null, 5, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void maxLength_not_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "JohnDoe", false, null, 5, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("maxLength");
                assertThat(errors.get(0).getMessage()).contains("5");
                assertThat(errors.get(0).getMessage()).contains("7");
            }

            @Test
            void maxLength_exact_boundary() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "Johns", false, null, 5, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void maxLength_with_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateString("name", null, false, null, 5, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void maxLength_zero() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "", false, null, 0, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void maxLength_fails_for_single_char_with_zero_max() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "a", false, null, 0, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("maxLength");
            }
        }

        @Nested
        @DisplayName("Pattern constraint")
        class PatternTests {
            @Test
            void pattern_match_succeeds() {
                List<ValidationError> errors = ParameterValidator.validateString("email", "john@example.com", false, null, null, "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", null);

                assertThat(errors).isEmpty();
            }

            @Test
            void pattern_match_fails() {
                List<ValidationError> errors = ParameterValidator.validateString("email", "invalid-email", false, null, null, "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("pattern");
                assertThat(errors.get(0).getMessage()).contains("pattern");
            }

            @Test
            void pattern_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateString("field", "any value", false, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void pattern_numeric_only() {
                List<ValidationError> errors = ParameterValidator.validateString("code", "12345", false, null, null, "^[0-9]+$", null);

                assertThat(errors).isEmpty();
            }

            @Test
            void pattern_numeric_only_fails() {
                List<ValidationError> errors = ParameterValidator.validateString("code", "1234a", false, null, null, "^[0-9]+$", null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("pattern");
            }

            @Test
            void pattern_with_null_value_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateString("field", null, false, null, null, "^[0-9]+$", null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Enum constraint")
        class EnumTests {
            @Test
            void enum_value_in_list() {
                List<String> allowed = Arrays.asList("active", "inactive", "pending");
                List<ValidationError> errors = ParameterValidator.validateString("status", "active", false, null, null, null, allowed);

                assertThat(errors).isEmpty();
            }

            @Test
            void enum_value_not_in_list() {
                List<String> allowed = Arrays.asList("active", "inactive", "pending");
                List<ValidationError> errors = ParameterValidator.validateString("status", "invalid", false, null, null, null, allowed);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("enum");
                assertThat(errors.get(0).getMessage()).contains("active");
                assertThat(errors.get(0).getMessage()).contains("inactive");
                assertThat(errors.get(0).getMessage()).contains("pending");
            }

            @Test
            void enum_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateString("status", "value", false, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void enum_empty_list_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateString("status", "value", false, null, null, null, Collections.emptyList());

                assertThat(errors).isEmpty();
            }

            @Test
            void enum_single_value() {
                List<ValidationError> errors = ParameterValidator.validateString("status", "active", false, null, null, null, Collections.singletonList("active"));

                assertThat(errors).isEmpty();
            }

            @Test
            void enum_single_value_mismatch() {
                List<ValidationError> errors = ParameterValidator.validateString("status", "inactive", false, null, null, null, Collections.singletonList("active"));

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("enum");
            }

            @Test
            void enum_with_null_value_returns_no_error() {
                List<String> allowed = Arrays.asList("active", "inactive");
                List<ValidationError> errors = ParameterValidator.validateString("status", null, false, null, null, null, allowed);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Combined constraints")
        class CombinedConstraintsTests {
            @Test
            void minLength_and_maxLength_both_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "John", false, 3, 10, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minLength_fails_maxLength_succeeds() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "Jo", false, 3, 10, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("minLength");
            }

            @Test
            void minLength_succeeds_maxLength_fails() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "VeryLongNameHere", false, 3, 10, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("maxLength");
            }

            @Test
            void minLength_and_maxLength_both_fail() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "Jo", false, 5, 10, null, null);

                assertThat(errors).hasSize(1); // Only minLength fails as it's checked first
                assertThat(errors.get(0).getConstraint()).isEqualTo("minLength");
            }

            @Test
            void all_constraints_satisfied() {
                List<String> allowed = Arrays.asList("john", "jane");
                List<ValidationError> errors = ParameterValidator.validateString(
                    "username", "john", true, 3, 10, "^[a-z]+$", allowed);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_fails_early() {
                List<String> allowed = Arrays.asList("john");
                List<ValidationError> errors = ParameterValidator.validateString(
                    "username", null, true, 3, 10, "^[a-z]+$", allowed);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void multiple_constraints_fail() {
                List<String> allowed = Arrays.asList("active", "inactive");
                List<ValidationError> errors = ParameterValidator.validateString(
                    "status", "invalid123", false, 3, 10, "^[a-z]+$", allowed);

                // Should have pattern and enum errors
                assertThat(errors).hasSizeGreaterThanOrEqualTo(1);
                List<String> constraints = errors.stream()
                    .map(ValidationError::getConstraint)
                    .toList();
                assertThat(constraints).contains("pattern");
            }
        }

        @Nested
        @DisplayName("Edge cases")
        class EdgeCaseTests {
            @Test
            void unicode_characters() {
                List<ValidationError> errors = ParameterValidator.validateString("name", "Jöhn", false, 2, 10, null, null);

                assertThat(errors).isEmpty();
                assertThat(ParameterValidator.validateString("name", "😀", false, 1, 10, null, null)).isEmpty();
            }

            @Test
            void very_long_string() {
                String longString = "a".repeat(10000);
                List<ValidationError> errors = ParameterValidator.validateString("description", longString, false, null, 20000, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void very_long_string_exceeds_max() {
                String longString = "a".repeat(10000);
                List<ValidationError> errors = ParameterValidator.validateString("description", longString, false, null, 5000, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("maxLength");
            }

            @Test
            void special_characters() {
                List<ValidationError> errors = ParameterValidator.validateString("special", "!@#$%^&*()", false, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void whitespace_only_string() {
                List<ValidationError> errors = ParameterValidator.validateString("field", "   ", false, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void whitespace_with_minLength() {
                List<ValidationError> errors = ParameterValidator.validateString("field", "   ", false, 5, null, null, null);

                assertThat(errors).hasSize(1);
            }
        }
    }

    @Nested
    @DisplayName("validateInteger Tests")
    class ValidateIntegerTests {

        @Nested
        @DisplayName("Required constraint")
        class RequiredTests {
            @Test
            void required_true_with_null_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", null, true, null, null, null, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void required_true_with_value_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 25, true, null, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_false_with_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", null, false, null, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_false_with_zero_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 0, false, null, null, null, null, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Minimum constraint (inclusive)")
        class MinimumInclusiveTests {
            @Test
            void minimum_inclusive_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 25, false, 18, null, false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minimum_inclusive_at_boundary() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 18, false, 18, null, false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minimum_inclusive_not_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 17, false, 18, null, false, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("minimum");
                assertThat(errors.get(0).getMessage()).contains("18");
                assertThat(errors.get(0).getMessage()).contains("17");
            }

            @Test
            void minimum_inclusive_with_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", null, false, 18, null, false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minimum_with_negative_value() {
                List<ValidationError> errors = ParameterValidator.validateInteger("temperature", -5, false, -10, null, false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minimum_null_constraint_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 5, false, null, null, false, null, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Minimum constraint (exclusive)")
        class MinimumExclusiveTests {
            @Test
            void minimum_exclusive_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 25, false, 18, null, true, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minimum_exclusive_at_boundary_fails() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 18, false, 18, null, true, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("exclusiveMinimum");
                assertThat(errors.get(0).getMessage()).contains("greater than");
            }

            @Test
            void minimum_exclusive_false_uses_inclusive() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 18, false, 18, null, false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minimum_exclusive_null_uses_inclusive() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 18, false, 18, null, null, null, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Maximum constraint (inclusive)")
        class MaximumInclusiveTests {
            @Test
            void maximum_inclusive_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 25, false, null, 65, null, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void maximum_inclusive_at_boundary() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 65, false, null, 65, null, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void maximum_inclusive_not_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 70, false, null, 65, null, false, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("maximum");
                assertThat(errors.get(0).getMessage()).contains("65");
                assertThat(errors.get(0).getMessage()).contains("70");
            }

            @Test
            void maximum_inclusive_with_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", null, false, null, 65, null, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void maximum_with_negative_value() {
                List<ValidationError> errors = ParameterValidator.validateInteger("temperature", -5, false, null, -10, null, false, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("maximum");
            }

            @Test
            void maximum_null_constraint_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 100, false, null, null, null, false, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Maximum constraint (exclusive)")
        class MaximumExclusiveTests {
            @Test
            void maximum_exclusive_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 25, false, null, 65, null, true, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void maximum_exclusive_at_boundary_fails() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 65, false, null, 65, null, true, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("exclusiveMaximum");
                assertThat(errors.get(0).getMessage()).contains("less than");
            }

            @Test
            void maximum_exclusive_false_uses_inclusive() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 65, false, null, 65, null, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void maximum_exclusive_null_uses_inclusive() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 65, false, null, 65, null, null, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("MultipleOf constraint")
        class MultipleOfTests {
            @Test
            void multipleOf_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateInteger("quantity", 10, false, null, null, null, null, 5);

                assertThat(errors).isEmpty();
            }

            @Test
            void multipleOf_zero_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateInteger("quantity", 0, false, null, null, null, null, 5);

                assertThat(errors).isEmpty();
            }

            @Test
            void multipleOf_not_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateInteger("quantity", 12, false, null, null, null, null, 5);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("multipleOf");
                assertThat(errors.get(0).getMessage()).contains("5");
                assertThat(errors.get(0).getMessage()).contains("12");
            }

            @Test
            void multipleOf_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateInteger("quantity", 12, false, null, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void multipleOf_zero_or_negative_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateInteger("quantity", 12, false, null, null, null, null, 0);
                assertThat(errors).isEmpty();

                errors = ParameterValidator.validateInteger("quantity", 12, false, null, null, null, null, -5);
                assertThat(errors).isEmpty();
            }

            @Test
            void multipleOf_one_always_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateInteger("quantity", 123, false, null, null, null, null, 1);

                assertThat(errors).isEmpty();
            }

            @Test
            void multipleOf_with_negative_values() {
                List<ValidationError> errors = ParameterValidator.validateInteger("value", -10, false, null, null, null, null, 5);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Combined constraints")
        class CombinedConstraintsTests {
            @Test
            void minimum_and_maximum_both_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 25, false, 18, 65, false, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minimum_fails_maximum_succeeds() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 15, false, 18, 65, false, false, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("minimum");
            }

            @Test
            void minimum_succeeds_maximum_fails() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 70, false, 18, 65, false, false, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("maximum");
            }

            @Test
            void minimum_and_maximum_both_fail() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", 5, false, 18, 65, false, false, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("minimum");
            }

            @Test
            void all_constraints_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateInteger("quantity", 10, true, 5, 20, false, false, 5);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_fails_early() {
                List<ValidationError> errors = ParameterValidator.validateInteger("age", null, true, 18, 65, false, false, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }
        }

        @Nested
        @DisplayName("Edge cases")
        class EdgeCaseTests {
            @Test
            void zero_value() {
                List<ValidationError> errors = ParameterValidator.validateInteger("value", 0, false, -10, 10, false, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void negative_values() {
                List<ValidationError> errors = ParameterValidator.validateInteger("temperature", -25, false, -30, 50, false, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void integer_max_value() {
                List<ValidationError> errors = ParameterValidator.validateInteger("value", Integer.MAX_VALUE, false, Integer.MIN_VALUE, Integer.MAX_VALUE, false, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void integer_min_value() {
                List<ValidationError> errors = ParameterValidator.validateInteger("value", Integer.MIN_VALUE, false, Integer.MIN_VALUE, Integer.MAX_VALUE, false, false, null);

                assertThat(errors).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("validateNumber Tests")
    class ValidateNumberTests {

        @Nested
        @DisplayName("Required constraint")
        class RequiredTests {
            @Test
            void required_true_with_null_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", null, true, null, null, null, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void required_true_with_double_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 19.99, true, null, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_true_with_float_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 19.99f, true, null, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_false_with_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", null, false, null, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_false_with_zero_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 0.0, false, null, null, null, null, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Minimum constraint (inclusive)")
        class MinimumInclusiveTests {
            @Test
            void minimum_inclusive_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 19.99, false, 10.0, null, false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minimum_inclusive_at_boundary() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 10.0, false, 10.0, null, false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minimum_inclusive_not_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 9.99, false, 10.0, null, false, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("minimum");
            }

            @Test
            void minimum_with_float_and_double() {
                List<ValidationError> errors = ParameterValidator.validateNumber("value", 15.5f, false, 10.0, null, false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minimum_null_constraint_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 5.0, false, null, null, false, null, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Minimum constraint (exclusive)")
        class MinimumExclusiveTests {
            @Test
            void minimum_exclusive_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 19.99, false, 10.0, null, true, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minimum_exclusive_at_boundary_fails() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 10.0, false, 10.0, null, true, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("exclusiveMinimum");
            }

            @Test
            void minimum_exclusive_false_uses_inclusive() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 10.0, false, 10.0, null, false, null, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Maximum constraint (inclusive)")
        class MaximumInclusiveTests {
            @Test
            void maximum_inclusive_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 19.99, false, null, 50.0, null, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void maximum_inclusive_at_boundary() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 50.0, false, null, 50.0, null, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void maximum_inclusive_not_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 50.01, false, null, 50.0, null, false, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("maximum");
            }

            @Test
            void maximum_null_constraint_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 100.0, false, null, null, null, false, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Maximum constraint (exclusive)")
        class MaximumExclusiveTests {
            @Test
            void maximum_exclusive_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 19.99, false, null, 50.0, null, true, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void maximum_exclusive_at_boundary_fails() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 50.0, false, null, 50.0, null, true, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("exclusiveMaximum");
            }

            @Test
            void maximum_exclusive_false_uses_inclusive() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 50.0, false, null, 50.0, null, false, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("MultipleOf constraint")
        class MultipleOfTests {
            @Test
            void multipleOf_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateNumber("quantity", 10.0, false, null, null, null, null, 5.0);

                assertThat(errors).isEmpty();
            }

            @Test
            void multipleOf_with_floating_point() {
                List<ValidationError> errors = ParameterValidator.validateNumber("value", 1.5, false, null, null, null, null, 0.5);

                assertThat(errors).isEmpty();
            }

            @Test
            void multipleOf_with_precision_tolerance() {
                List<ValidationError> errors = ParameterValidator.validateNumber("value", 1.2, false, null, null, null, null, 0.1);

                assertThat(errors).isEmpty();
            }

            @Test
            void multipleOf_not_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateNumber("quantity", 12.5, false, null, null, null, null, 5.0);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("multipleOf");
            }

            @Test
            void multipleOf_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateNumber("quantity", 12.5, false, null, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void multipleOf_zero_or_negative_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateNumber("quantity", 12.5, false, null, null, null, null, 0.0);
                assertThat(errors).isEmpty();

                errors = ParameterValidator.validateNumber("quantity", 12.5, false, null, null, null, null, -5.0);
                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Combined constraints")
        class CombinedConstraintsTests {
            @Test
            void minimum_and_maximum_both_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 25.5, false, 10.0, 50.0, false, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minimum_fails_maximum_succeeds() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", 9.99, false, 10.0, 50.0, false, false, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("minimum");
            }

            @Test
            void all_constraints_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateNumber("quantity", 10.0, true, 5.0, 20.0, false, false, 5.0);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_fails_early() {
                List<ValidationError> errors = ParameterValidator.validateNumber("price", null, true, 10.0, 50.0, false, false, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }
        }

        @Nested
        @DisplayName("Edge cases")
        class EdgeCaseTests {
            @Test
            void zero_value() {
                List<ValidationError> errors = ParameterValidator.validateNumber("value", 0.0, false, -10.0, 10.0, false, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void negative_values() {
                List<ValidationError> errors = ParameterValidator.validateNumber("temperature", -25.5, false, -30.0, 50.0, false, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void very_small_positive_number() {
                List<ValidationError> errors = ParameterValidator.validateNumber("value", 0.00001, false, 0.0, null, false, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void very_large_number() {
                List<ValidationError> errors = ParameterValidator.validateNumber("value", 1e10, false, null, 1e11, false, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void double_max_value() {
                List<ValidationError> errors = ParameterValidator.validateNumber("value", Double.MAX_VALUE, false, null, null, false, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void double_min_value() {
                List<ValidationError> errors = ParameterValidator.validateNumber("value", Double.MIN_VALUE, false, null, null, false, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void infinity_value() {
                List<ValidationError> errors = ParameterValidator.validateNumber("value", Double.POSITIVE_INFINITY, false, null, null, false, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void nan_value() {
                List<ValidationError> errors = ParameterValidator.validateNumber("value", Double.NaN, false, null, null, false, false, null);

                assertThat(errors).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("validateAndParseInteger Tests")
    class ValidateAndParseIntegerTests {

        @Nested
        @DisplayName("Required constraint")
        class RequiredTests {
            @Test
            void required_true_with_null_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", null, true, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void required_true_with_empty_string_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", "", true, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void required_true_with_valid_number_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", "25", true, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_false_with_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", null, false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_false_with_empty_string_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", "", false, null, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Type parsing")
        class TypeParsingTests {
            @Test
            void valid_integer_parses_successfully() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", "25", false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void invalid_integer_format_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", "not-a-number", false, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("type");
                assertThat(errors.get(0).getMessage()).contains("valid integer");
            }

            @Test
            void integer_with_leading_zeros() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("code", "00025", false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void negative_integer() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("temperature", "-15", false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void floating_point_string_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", "25.5", false, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("type");
            }

            @Test
            void whitespace_string_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", "  ", false, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("type");
            }

            @Test
            void integer_max_value() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("value", String.valueOf(Integer.MAX_VALUE), false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void integer_min_value() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("value", String.valueOf(Integer.MIN_VALUE), false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void exceeds_integer_max_value() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("value", "9999999999", false, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("type");
            }
        }

        @Nested
        @DisplayName("Minimum and Maximum validation")
        class MinMaxValidationTests {
            @Test
            void minimum_validation_passes_through() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", "25", false, 18, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minimum_validation_fails() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", "15", false, 18, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("minimum");
            }

            @Test
            void maximum_validation_passes_through() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", "25", false, null, 65);

                assertThat(errors).isEmpty();
            }

            @Test
            void maximum_validation_fails() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", "70", false, null, 65);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("maximum");
            }

            @Test
            void both_minimum_and_maximum_succeed() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", "25", false, 18, 65);

                assertThat(errors).isEmpty();
            }

            @Test
            void both_minimum_and_maximum_fail() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", "10", false, 18, 65);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("minimum");
            }
        }

        @Nested
        @DisplayName("Combined constraints")
        class CombinedConstraintsTests {
            @Test
            void required_fails_early_type_not_checked() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", null, true, 18, 65);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void type_fails_before_range_validation() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", "invalid", false, 18, 65);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("type");
            }

            @Test
            void all_constraints_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateAndParseInteger("age", "25", true, 18, 65);

                assertThat(errors).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("validateAndParseNumber Tests")
    class ValidateAndParseNumberTests {

        @Nested
        @DisplayName("Required constraint")
        class RequiredTests {
            @Test
            void required_true_with_null_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseNumber("price", null, true, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void required_true_with_empty_string_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseNumber("price", "", true, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void required_true_with_valid_number_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseNumber("price", "19.99", true, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_false_with_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseNumber("price", null, false, null, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Type parsing")
        class TypeParsingTests {
            @Test
            void valid_double_parses_successfully() {
                List<ValidationError> errors = ParameterValidator.validateAndParseNumber("price", "19.99", false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_integer_string_parses_successfully() {
                List<ValidationError> errors = ParameterValidator.validateAndParseNumber("price", "20", false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void invalid_number_format_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseNumber("price", "not-a-number", false, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("type");
                assertThat(errors.get(0).getMessage()).contains("valid number");
            }

            @Test
            void negative_number() {
                List<ValidationError> errors = ParameterValidator.validateAndParseNumber("temperature", "-15.5", false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void scientific_notation() {
                List<ValidationError> errors = ParameterValidator.validateAndParseNumber("value", "1.23e-4", false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void whitespace_string_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseNumber("price", "  ", false, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("type");
            }

            @Test
            void double_max_value() {
                List<ValidationError> errors = ParameterValidator.validateAndParseNumber("value", String.valueOf(Double.MAX_VALUE), false, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void infinity() {
                List<ValidationError> errors = ParameterValidator.validateAndParseNumber("value", "Infinity", false, null, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Minimum and Maximum validation")
        class MinMaxValidationTests {
            @Test
            void minimum_validation_passes_through() {
                List<ValidationError> errors = ParameterValidator.validateAndParseNumber("price", "25.5", false, 10.0, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minimum_validation_fails() {
                List<ValidationError> errors = ParameterValidator.validateAndParseNumber("price", "9.99", false, 10.0, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("minimum");
            }

            @Test
            void maximum_validation_passes_through() {
                List<ValidationError> errors = ParameterValidator.validateAndParseNumber("price", "25.5", false, null, 50.0);

                assertThat(errors).isEmpty();
            }

            @Test
            void maximum_validation_fails() {
                List<ValidationError> errors = ParameterValidator.validateAndParseNumber("price", "50.01", false, null, 50.0);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("maximum");
            }

            @Test
            void both_constraints_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateAndParseNumber("price", "25.5", false, 10.0, 50.0);

                assertThat(errors).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("validateAndParseDate Tests")
    class ValidateAndParseDateTests {

        @Nested
        @DisplayName("Required constraint")
        class RequiredTests {
            @Test
            void required_true_with_null_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDate("date", null, true);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void required_true_with_empty_string_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDate("date", "", true);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void required_true_with_valid_date_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDate("date", "2026-01-11", true);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_false_with_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDate("date", null, false);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Date format validation")
        class DateFormatTests {
            @Test
            void valid_iso8601_date() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDate("date", "2026-01-11", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void invalid_date_format() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDate("date", "01-11-2026", false);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }

            @Test
            void invalid_month() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDate("date", "2026-13-11", false);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }

            @Test
            void invalid_day() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDate("date", "2026-02-30", false);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }

            @Test
            void leap_year_date() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDate("date", "2024-02-29", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void non_leap_year_feb_29() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDate("date", "2023-02-29", false);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }

            @Test
            void date_with_time_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDate("date", "2026-01-11T10:00:00Z", false);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }
        }

        @Nested
        @DisplayName("Edge cases")
        class EdgeCaseTests {
            @Test
            void year_2000() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDate("date", "2000-01-01", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void far_future_date() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDate("date", "9999-12-31", false);

                assertThat(errors).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("validateAndParseDateTime Tests")
    class ValidateAndParseDateTimeTests {

        @Nested
        @DisplayName("Required constraint")
        class RequiredTests {
            @Test
            void required_true_with_null_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDateTime("timestamp", null, true);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void required_true_with_empty_string_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDateTime("timestamp", "", true);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void required_true_with_valid_datetime_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDateTime("timestamp", "2026-01-11T10:00:00Z", true);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_false_with_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDateTime("timestamp", null, false);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("DateTime format validation")
        class DateTimeFormatTests {
            @Test
            void valid_iso8601_with_z_timezone() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDateTime("timestamp", "2026-01-11T10:00:00Z", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_iso8601_with_positive_offset() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDateTime("timestamp", "2026-01-11T10:00:00+01:00", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_iso8601_with_negative_offset() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDateTime("timestamp", "2026-01-11T10:00:00-05:00", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_iso8601_with_nanoseconds() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDateTime("timestamp", "2026-01-11T10:00:00.123456789Z", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_iso8601_with_milliseconds() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDateTime("timestamp", "2026-01-11T10:00:00.123Z", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void invalid_datetime_format() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDateTime("timestamp", "2026-01-11 10:00:00", false);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }

            @Test
            void date_only_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDateTime("timestamp", "2026-01-11", false);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }

            @Test
            void missing_timezone() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDateTime("timestamp", "2026-01-11T10:00:00", false);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }
        }

        @Nested
        @DisplayName("Edge cases")
        class EdgeCaseTests {
            @Test
            void midnight() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDateTime("timestamp", "2026-01-11T00:00:00Z", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void end_of_day() {
                List<ValidationError> errors = ParameterValidator.validateAndParseDateTime("timestamp", "2026-01-11T23:59:59Z", false);

                assertThat(errors).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("validateAndParseBoolean Tests")
    class ValidateAndParseBooleanTests {

        @Nested
        @DisplayName("Required constraint")
        class RequiredTests {
            @Test
            void required_true_with_null_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", null, true);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void required_true_with_empty_string_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "", true);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void required_true_with_valid_boolean_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "true", true);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_false_with_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", null, false);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Boolean format validation")
        class BooleanFormatTests {
            @Test
            void true_lowercase() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "true", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void false_lowercase() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "false", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void true_uppercase() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "TRUE", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void false_uppercase() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "FALSE", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void true_mixedcase() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "True", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void one_for_true() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "1", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void zero_for_false() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "0", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void yes() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "yes", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void no() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "no", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void on() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "on", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void off() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "off", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void invalid_boolean_value() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "maybe", false);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("type");
                assertThat(errors.get(0).getMessage()).contains("boolean");
            }

            @Test
            void numeric_but_not_01() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "2", false);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("type");
            }

            @Test
            void partial_match_fails() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "tr", false);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("type");
            }
        }

        @Nested
        @DisplayName("Case insensitivity")
        class CaseInsensitivityTests {
            @Test
            void yes_uppercase() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "YES", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void no_uppercase() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "NO", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void on_uppercase() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "ON", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void off_uppercase() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "OFF", false);

                assertThat(errors).isEmpty();
            }

            @Test
            void mixed_case_yes() {
                List<ValidationError> errors = ParameterValidator.validateAndParseBoolean("flag", "Yes", false);

                assertThat(errors).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("validateArray Tests")
    class ValidateArrayTests {

        @Nested
        @DisplayName("Required constraint")
        class RequiredTests {
            @Test
            void required_true_with_null_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", null, true, null, null, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void required_true_with_empty_string_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "", true, null, null, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void required_true_with_valid_array_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1,tag2", true, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_false_with_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", null, false, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_false_with_empty_string_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "", false, null, null, null, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Array parsing")
        class ArrayParsingTests {
            @Test
            void single_item() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1", false, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void multiple_items() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1,tag2,tag3", false, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void items_with_whitespace() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1, tag2, tag3", false, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void items_with_leading_trailing_whitespace() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "  tag1  ,  tag2  ", false, null, null, null, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("MinItems constraint")
        class MinItemsTests {
            @Test
            void minItems_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1,tag2,tag3", false, 2, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minItems_at_boundary() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1,tag2", false, 2, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minItems_not_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1", false, 2, null, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("minItems");
                assertThat(errors.get(0).getMessage()).contains("2");
                assertThat(errors.get(0).getMessage()).contains("1");
            }

            @Test
            void minItems_zero() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "", false, 0, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minItems_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1", false, null, null, null, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("MaxItems constraint")
        class MaxItemsTests {
            @Test
            void maxItems_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1,tag2", false, null, 5, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void maxItems_at_boundary() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1,tag2,tag3", false, null, 3, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void maxItems_not_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1,tag2,tag3,tag4", false, null, 3, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("maxItems");
                assertThat(errors.get(0).getMessage()).contains("3");
                assertThat(errors.get(0).getMessage()).contains("4");
            }

            @Test
            void maxItems_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1,tag2,tag3,tag4", false, null, null, null, null);

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("UniqueItems constraint")
        class UniqueItemsTests {
            @Test
            void uniqueItems_all_unique() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1,tag2,tag3", false, null, null, true, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void uniqueItems_has_duplicates() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1,tag2,tag1", false, null, null, true, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("uniqueItems");
                assertThat(errors.get(0).getMessage()).contains("duplicate");
            }

            @Test
            void uniqueItems_multiple_duplicates() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1,tag2,tag1,tag3,tag2", false, null, null, true, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("uniqueItems");
            }

            @Test
            void uniqueItems_false_allows_duplicates() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1,tag2,tag1", false, null, null, false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void uniqueItems_null_allows_duplicates() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1,tag2,tag1", false, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void uniqueItems_single_item() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1", false, null, null, true, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void uniqueItems_with_whitespace_are_unique() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1, tag2", false, null, null, true, null);

                assertThat(errors).isEmpty(); // whitespace is trimmed
            }

            @Test
            void uniqueItems_with_whitespace_duplicates() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1, tag1", false, null, null, true, null);

                assertThat(errors).hasSize(1); // whitespace is trimmed, so they match and are duplicates
                assertThat(errors.get(0).getConstraint()).isEqualTo("uniqueItems");
            }
        }

        @Nested
        @DisplayName("ItemEnumValues constraint")
        class ItemEnumValuesTests {
            @Test
            void all_items_in_enum() {
                List<String> allowed = Arrays.asList("active", "inactive", "pending");
                List<ValidationError> errors = ParameterValidator.validateArray("status", "active,pending", false, null, null, null, allowed);

                assertThat(errors).isEmpty();
            }

            @Test
            void item_not_in_enum() {
                List<String> allowed = Arrays.asList("active", "inactive", "pending");
                List<ValidationError> errors = ParameterValidator.validateArray("status", "active,invalid,pending", false, null, null, null, allowed);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("enum");
                assertThat(errors.get(0).getMessage()).contains("active");
                assertThat(errors.get(0).getMessage()).contains("inactive");
            }

            @Test
            void multiple_items_not_in_enum() {
                List<String> allowed = Arrays.asList("active", "inactive");
                List<ValidationError> errors = ParameterValidator.validateArray("status", "active,invalid1,invalid2", false, null, null, null, allowed);

                assertThat(errors).hasSize(2); // One error for each invalid item
                assertThat(errors.get(0).getConstraint()).isEqualTo("enum");
                assertThat(errors.get(1).getConstraint()).isEqualTo("enum");
            }

            @Test
            void itemEnumValues_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateArray("status", "active,invalid", false, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void itemEnumValues_empty_list_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateArray("status", "active,invalid", false, null, null, null, Collections.emptyList());

                assertThat(errors).isEmpty();
            }

            @Test
            void single_item_enum() {
                List<String> allowed = Collections.singletonList("active");
                List<ValidationError> errors = ParameterValidator.validateArray("status", "active", false, null, null, null, allowed);

                assertThat(errors).isEmpty();
            }

            @Test
            void single_item_enum_mismatch() {
                List<String> allowed = Collections.singletonList("active");
                List<ValidationError> errors = ParameterValidator.validateArray("status", "inactive", false, null, null, null, allowed);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("enum");
            }
        }

        @Nested
        @DisplayName("Combined constraints")
        class CombinedConstraintsTests {
            @Test
            void minItems_and_maxItems_both_satisfied() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1,tag2,tag3", false, 2, 5, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void minItems_fails_maxItems_succeeds() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1", false, 2, 5, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("minItems");
            }

            @Test
            void minItems_succeeds_maxItems_fails() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1,tag2,tag3,tag4,tag5,tag6", false, 2, 5, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("maxItems");
            }

            @Test
            void all_constraints_satisfied() {
                List<String> allowed = Arrays.asList("active", "inactive", "pending");
                List<ValidationError> errors = ParameterValidator.validateArray("status", "active,pending", true, 1, 3, true, allowed);

                assertThat(errors).isEmpty();
            }

            @Test
            void required_fails_early() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", null, true, 2, 5, null, null);

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }
        }

        @Nested
        @DisplayName("Edge cases")
        class EdgeCaseTests {
            @Test
            void empty_items_in_array() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1,,tag3", false, null, null, null, null);

                // Empty strings are filtered out during parsing
                assertThat(errors).isEmpty();
            }

            @Test
            void only_commas() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", ",,,", false, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void single_comma() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", ",", false, null, null, null, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void items_with_special_characters() {
                List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag!@#,tag$%^", false, null, null, null, null);

                assertThat(errors).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("validateFormat Tests")
    class ValidateFormatTests {

        @Nested
        @DisplayName("Required constraint")
        class RequiredTests {
            @Test
            void required_true_with_null_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateFormat("email", null, true, "email");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void required_true_with_empty_string_returns_error() {
                List<ValidationError> errors = ParameterValidator.validateFormat("email", "", true, "email");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("required");
            }

            @Test
            void required_false_with_null_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateFormat("email", null, false, "email");

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Email format")
        class EmailFormatTests {
            @Test
            void valid_email() {
                List<ValidationError> errors = ParameterValidator.validateFormat("email", "user@example.com", false, "email");

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_email_with_dots() {
                List<ValidationError> errors = ParameterValidator.validateFormat("email", "user.name@example.co.uk", false, "email");

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_email_with_plus() {
                List<ValidationError> errors = ParameterValidator.validateFormat("email", "user+tag@example.com", false, "email");

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_email_with_hyphen() {
                List<ValidationError> errors = ParameterValidator.validateFormat("email", "user-name@example.com", false, "email");

                assertThat(errors).isEmpty();
            }

            @Test
            void invalid_email_no_at() {
                List<ValidationError> errors = ParameterValidator.validateFormat("email", "invalid.email.com", false, "email");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }

            @Test
            void invalid_email_no_domain() {
                List<ValidationError> errors = ParameterValidator.validateFormat("email", "user@", false, "email");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }

            @Test
            void invalid_email_no_tld() {
                List<ValidationError> errors = ParameterValidator.validateFormat("email", "user@domain", false, "email");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }

            @Test
            void invalid_email_multiple_at() {
                List<ValidationError> errors = ParameterValidator.validateFormat("email", "user@domain@com", false, "email");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }
        }

        @Nested
        @DisplayName("UUID format")
        class UuidFormatTests {
            @Test
            void valid_uuid_with_hyphens() {
                List<ValidationError> errors = ParameterValidator.validateFormat("id", "550e8400-e29b-41d4-a716-446655440000", false, "uuid");

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_uuid_uppercase() {
                List<ValidationError> errors = ParameterValidator.validateFormat("id", "550E8400-E29B-41D4-A716-446655440000", false, "uuid");

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_uuid_without_hyphens() {
                List<ValidationError> errors = ParameterValidator.validateFormat("id", "550e8400e29b41d4a716446655440000", false, "uuid");

                assertThat(errors).isEmpty();
            }

            @Test
            void invalid_uuid_wrong_format() {
                List<ValidationError> errors = ParameterValidator.validateFormat("id", "not-a-uuid", false, "uuid");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }

            @Test
            void invalid_uuid_wrong_length() {
                List<ValidationError> errors = ParameterValidator.validateFormat("id", "550e8400-e29b-41d4-a716", false, "uuid");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }

            @Test
            void invalid_uuid_invalid_chars() {
                List<ValidationError> errors = ParameterValidator.validateFormat("id", "550e8400-e29b-41d4-g716-446655440000", false, "uuid");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }
        }

        @Nested
        @DisplayName("URI format")
        class UriFormatTests {
            @Test
            void valid_http_uri() {
                List<ValidationError> errors = ParameterValidator.validateFormat("url", "http://example.com", false, "uri");

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_https_uri() {
                List<ValidationError> errors = ParameterValidator.validateFormat("url", "https://example.com/path", false, "uri");

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_ftp_uri() {
                List<ValidationError> errors = ParameterValidator.validateFormat("url", "ftp://example.com", false, "uri");

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_uri_with_query() {
                List<ValidationError> errors = ParameterValidator.validateFormat("url", "http://example.com?param=value", false, "uri");

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_uri_with_fragment() {
                List<ValidationError> errors = ParameterValidator.validateFormat("url", "http://example.com#section", false, "uri");

                assertThat(errors).isEmpty();
            }

            @Test
            void invalid_uri() {
                List<ValidationError> errors = ParameterValidator.validateFormat("url", "not a valid uri!", false, "uri");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }
        }

        @Nested
        @DisplayName("URI reference format")
        class UriReferenceFormatTests {
            @Test
            void valid_absolute_uri_reference() {
                List<ValidationError> errors = ParameterValidator.validateFormat("url", "http://example.com", false, "uri-reference");

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_relative_uri_reference() {
                List<ValidationError> errors = ParameterValidator.validateFormat("url", "/path/to/resource", false, "uri-reference");

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Hostname format")
        class HostnameFormatTests {
            @Test
            void valid_simple_hostname() {
                List<ValidationError> errors = ParameterValidator.validateFormat("hostname", "example", false, "hostname");

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_fqdn() {
                List<ValidationError> errors = ParameterValidator.validateFormat("hostname", "example.com", false, "hostname");

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_hostname_with_hyphen() {
                List<ValidationError> errors = ParameterValidator.validateFormat("hostname", "my-server.example.com", false, "hostname");

                assertThat(errors).isEmpty();
            }

            @Test
            void invalid_hostname_leading_hyphen() {
                List<ValidationError> errors = ParameterValidator.validateFormat("hostname", "-example.com", false, "hostname");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }

            @Test
            void invalid_hostname_trailing_hyphen() {
                List<ValidationError> errors = ParameterValidator.validateFormat("hostname", "example-.com", false, "hostname");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }

            @Test
            void invalid_hostname_special_chars() {
                List<ValidationError> errors = ParameterValidator.validateFormat("hostname", "exam@le.com", false, "hostname");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }

            @Test
            void invalid_hostname_too_long() {
                String tooLong = "a".repeat(254);
                List<ValidationError> errors = ParameterValidator.validateFormat("hostname", tooLong, false, "hostname");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }
        }

        @Nested
        @DisplayName("IPv4 format")
        class IPv4FormatTests {
            @Test
            void valid_ipv4() {
                List<ValidationError> errors = ParameterValidator.validateFormat("ip", "192.168.1.1", false, "ipv4");

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_ipv4_zeros() {
                List<ValidationError> errors = ParameterValidator.validateFormat("ip", "0.0.0.0", false, "ipv4");

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_ipv4_max() {
                List<ValidationError> errors = ParameterValidator.validateFormat("ip", "255.255.255.255", false, "ipv4");

                assertThat(errors).isEmpty();
            }

            @Test
            void invalid_ipv4_octet_too_high() {
                List<ValidationError> errors = ParameterValidator.validateFormat("ip", "256.168.1.1", false, "ipv4");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }

            @Test
            void invalid_ipv4_too_few_octets() {
                List<ValidationError> errors = ParameterValidator.validateFormat("ip", "192.168.1", false, "ipv4");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }

            @Test
            void invalid_ipv4_non_numeric() {
                List<ValidationError> errors = ParameterValidator.validateFormat("ip", "192.168.a.1", false, "ipv4");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }
        }

        @Nested
        @DisplayName("IPv6 format")
        class IPv6FormatTests {
            @Test
            void valid_ipv6_full() {
                List<ValidationError> errors = ParameterValidator.validateFormat("ip", "2001:0db8:85a3:0000:0000:8a2e:0370:7334", false, "ipv6");

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_ipv6_compressed() {
                List<ValidationError> errors = ParameterValidator.validateFormat("ip", "2001:db8::1", false, "ipv6");

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_ipv6_loopback() {
                List<ValidationError> errors = ParameterValidator.validateFormat("ip", "::1", false, "ipv6");

                assertThat(errors).isEmpty();
            }

            @Test
            void valid_ipv6_ipv4_mapped() {
                List<ValidationError> errors = ParameterValidator.validateFormat("ip", "::ffff:192.0.2.1", false, "ipv6");

                assertThat(errors).isEmpty();
            }

            @Test
            void invalid_ipv6() {
                List<ValidationError> errors = ParameterValidator.validateFormat("ip", "gggg::1", false, "ipv6");

                assertThat(errors).hasSize(1);
                assertThat(errors.get(0).getConstraint()).isEqualTo("format");
            }
        }

        @Nested
        @DisplayName("Unknown format")
        class UnknownFormatTests {
            @Test
            void unknown_format_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateFormat("field", "any-value", false, "unknown-format");

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Format case insensitivity")
        class FormatCaseInsensitivityTests {
            @Test
            void format_uppercase() {
                List<ValidationError> errors = ParameterValidator.validateFormat("field", "user@example.com", false, "EMAIL");

                assertThat(errors).isEmpty();
            }

            @Test
            void format_mixedcase() {
                List<ValidationError> errors = ParameterValidator.validateFormat("field", "550e8400-e29b-41d4-a716-446655440000", false, "UUID");

                assertThat(errors).isEmpty();
            }
        }

        @Nested
        @DisplayName("Format with null or empty format")
        class NullFormatTests {
            @Test
            void null_format_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateFormat("field", "any-value", false, null);

                assertThat(errors).isEmpty();
            }

            @Test
            void empty_format_returns_no_error() {
                List<ValidationError> errors = ParameterValidator.validateFormat("field", "any-value", false, "");

                assertThat(errors).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Pattern caching and thread safety")
    class PatternCachingTests {

        @Test
        void pattern_cache_reuses_compiled_patterns() {
            String pattern = "^[a-z]+$";

            // First validation compiles the pattern
            List<ValidationError> errors1 = ParameterValidator.validateString("field1", "abc", false, null, null, pattern, null);
            assertThat(errors1).isEmpty();

            // Second validation should reuse cached pattern
            List<ValidationError> errors2 = ParameterValidator.validateString("field2", "xyz", false, null, null, pattern, null);
            assertThat(errors2).isEmpty();
        }

        @Test
        void format_cache_reuses_patterns() {
            // Email validation uses pattern cache internally
            List<ValidationError> errors1 = ParameterValidator.validateFormat("email1", "user@example.com", false, "email");
            assertThat(errors1).isEmpty();

            List<ValidationError> errors2 = ParameterValidator.validateFormat("email2", "admin@example.com", false, "email");
            assertThat(errors2).isEmpty();
        }

        @Test
        void concurrent_pattern_compilation_thread_safe() throws InterruptedException {
            String pattern = "^[A-Z][a-z]+$";
            AtomicInteger successCount = new AtomicInteger(0);
            int threadCount = 10;
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        List<ValidationError> errors = ParameterValidator.validateString(
                            "field", "Valid", false, null, null, pattern, null);
                        if (errors.isEmpty()) {
                            successCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }

            latch.await();
            assertThat(successCount.get()).isEqualTo(threadCount);
        }

        @Test
        void concurrent_format_validation_thread_safe() throws InterruptedException {
            AtomicInteger successCount = new AtomicInteger(0);
            int threadCount = 10;
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                new Thread(() -> {
                    try {
                        String email = "user" + index + "@example.com";
                        List<ValidationError> errors = ParameterValidator.validateFormat(
                            "email", email, false, "email");
                        if (errors.isEmpty()) {
                            successCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }

            latch.await();
            assertThat(successCount.get()).isEqualTo(threadCount);
        }
    }

    @Nested
    @DisplayName("Error message formatting")
    class ErrorMessageFormattingTests {

        @Test
        void string_error_includes_length_info() {
            List<ValidationError> errors = ParameterValidator.validateString("name", "ab", false, 5, null, null, null);

            assertThat(errors).hasSize(1);
            String message = errors.get(0).getMessage();
            assertThat(message).contains("5");
            assertThat(message).contains("2");
        }

        @Test
        void integer_error_includes_bounds() {
            List<ValidationError> errors = ParameterValidator.validateInteger("age", 15, false, 18, null, false, null, null);

            assertThat(errors).hasSize(1);
            String message = errors.get(0).getMessage();
            assertThat(message).contains("18");
            assertThat(message).contains("15");
        }

        @Test
        void enum_error_lists_allowed_values() {
            List<String> allowed = Arrays.asList("active", "inactive", "pending");
            List<ValidationError> errors = ParameterValidator.validateString("status", "invalid", false, null, null, null, allowed);

            assertThat(errors).hasSize(1);
            String message = errors.get(0).getMessage();
            assertThat(message).contains("active");
            assertThat(message).contains("inactive");
            assertThat(message).contains("pending");
        }

        @Test
        void pattern_error_includes_pattern() {
            List<ValidationError> errors = ParameterValidator.validateString("email", "invalid", false, null, null, "[a-z]+@[a-z]+", null);

            assertThat(errors).hasSize(1);
            String message = errors.get(0).getMessage();
            assertThat(message).contains("[a-z]+@[a-z]+");
        }

        @Test
        void array_error_includes_count_info() {
            List<ValidationError> errors = ParameterValidator.validateArray("tags", "tag1", false, 2, null, null, null);

            assertThat(errors).hasSize(1);
            String message = errors.get(0).getMessage();
            assertThat(message).contains("2");
            assertThat(message).contains("1");
        }
    }

    @Nested
    @DisplayName("Validation result consistency")
    class ValidationResultConsistencyTests {

        @Test
        void empty_errors_list_for_valid_input() {
            List<ValidationError> errors = ParameterValidator.validateString("name", "Valid Name", false, null, null, null, null);

            assertThat(errors).isNotNull();
            assertThat(errors).isEmpty();
        }

        @Test
        void non_null_errors_list_for_invalid_input() {
            List<ValidationError> errors = ParameterValidator.validateString("name", null, true, null, null, null, null);

            assertThat(errors).isNotNull();
            assertThat(errors).isNotEmpty();
        }

        @Test
        void error_contains_parameter_name() {
            List<ValidationError> errors = ParameterValidator.validateString("myParam", null, true, null, null, null, null);

            assertThat(errors.get(0).getParameter()).isEqualTo("myParam");
        }

        @Test
        void error_contains_constraint_type() {
            List<ValidationError> errors = ParameterValidator.validateString("name", "ab", false, 5, null, null, null);

            assertThat(errors.get(0).getConstraint()).isNotNull();
            assertThat(errors.get(0).getConstraint()).isNotEmpty();
        }

        @Test
        void error_contains_message() {
            List<ValidationError> errors = ParameterValidator.validateString("name", "ab", false, 5, null, null, null);

            assertThat(errors.get(0).getMessage()).isNotNull();
            assertThat(errors.get(0).getMessage()).isNotEmpty();
        }

        @Test
        void error_contains_value() {
            List<ValidationError> errors = ParameterValidator.validateString("name", "ab", false, 5, null, null, null);

            assertThat(errors.get(0).getValue()).isEqualTo("ab");
        }
    }
}
