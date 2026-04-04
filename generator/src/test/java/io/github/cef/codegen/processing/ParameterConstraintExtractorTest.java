package io.github.cef.codegen.processing;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenParameter;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParameterConstraintExtractorTest {

    @Nested
    class StringConstraints {

        @Test void minLength() {
            var param = stringParam(s -> s.setMinLength(3));
            assertEquals(3, param.vendorExtensions.get("x-min-length"));
        }

        @Test void maxLength() {
            var param = stringParam(s -> s.setMaxLength(100));
            assertEquals(100, param.vendorExtensions.get("x-max-length"));
        }

        @Test void pattern() {
            var param = stringParam(s -> s.setPattern("^[a-z]+$"));
            assertEquals("^[a-z]+$", param.vendorExtensions.get("x-pattern"));
        }

        @Test void patternEscapesBackslash() {
            var param = stringParam(s -> s.setPattern("\\d+"));
            assertEquals("\\\\d+", param.vendorExtensions.get("x-pattern"));
        }

        @Test void dateFormat() {
            var param = stringParam(s -> s.setFormat("date"));
            assertTrue((Boolean) param.vendorExtensions.get("x-is-date"));
            assertEquals("java.time.LocalDate", param.vendorExtensions.get("x-java-type"));
        }

        @Test void dateTimeFormat() {
            var param = stringParam(s -> s.setFormat("date-time"));
            assertTrue((Boolean) param.vendorExtensions.get("x-is-date-time"));
            assertEquals("java.time.OffsetDateTime", param.vendorExtensions.get("x-java-type"));
        }

        @Test void customFormat() {
            var param = stringParam(s -> s.setFormat("uuid"));
            assertEquals("uuid", param.vendorExtensions.get("x-format"));
            assertFalse(param.vendorExtensions.containsKey("x-is-date"));
        }

        @Test void noFormat() {
            var param = stringParam(s -> {});
            assertFalse(param.vendorExtensions.containsKey("x-format"));
            assertFalse(param.vendorExtensions.containsKey("x-is-date"));
        }
    }

    @Nested
    class NumericConstraints {

        @Test void minimum() {
            var param = intParam(s -> s.setMinimum(new BigDecimal("0")));
            assertEquals(new BigDecimal("0"), param.vendorExtensions.get("x-minimum"));
        }

        @Test void maximum() {
            var param = intParam(s -> s.setMaximum(new BigDecimal("999")));
            assertEquals(new BigDecimal("999"), param.vendorExtensions.get("x-maximum"));
        }

        @Test void exclusiveMinimum() {
            var param = intParam(s -> s.setExclusiveMinimum(true));
            assertTrue((Boolean) param.vendorExtensions.get("x-exclusive-minimum"));
        }

        @Test void multipleOf() {
            var param = intParam(s -> s.setMultipleOf(new BigDecimal("10")));
            assertEquals(new BigDecimal("10"), param.vendorExtensions.get("x-multiple-of"));
        }

        @Test void nothingSetMeansNoExtensions() {
            var param = intParam(s -> {});
            assertFalse(param.vendorExtensions.containsKey("x-minimum"));
            assertFalse(param.vendorExtensions.containsKey("x-maximum"));
        }
    }

    @Nested
    class EnumValues {

        @Test void extractsValues() {
            var param = stringParam(s -> s.setEnum(List.of("a", "b", "c")));
            assertTrue((Boolean) param.vendorExtensions.get("x-has-enum-values"));
            var str = (String) param.vendorExtensions.get("x-enum-values-string");
            assertEquals("\"a\", \"b\", \"c\"", str);
        }

        @Test void emptyEnumIgnored() {
            var param = stringParam(s -> s.setEnum(List.of()));
            assertFalse(param.vendorExtensions.containsKey("x-has-enum-values"));
        }

        @Test void nullEnumIgnored() {
            var param = stringParam(s -> s.setEnum(null));
            assertFalse(param.vendorExtensions.containsKey("x-has-enum-values"));
        }
    }

    @Nested
    class ArrayConstraints {

        @Test void minMaxItems() {
            var param = arrayParam(s -> { s.setMinItems(1); s.setMaxItems(50); });
            assertEquals(1, param.vendorExtensions.get("x-min-items"));
            assertEquals(50, param.vendorExtensions.get("x-max-items"));
        }

        @Test void uniqueItems() {
            var param = arrayParam(s -> s.setUniqueItems(true));
            assertTrue((Boolean) param.vendorExtensions.get("x-unique-items"));
        }

        @Test void itemEnumValues() {
            var itemSchema = new StringSchema();
            itemSchema.setEnum(List.of("x", "y"));
            var param = arrayParam(s -> s.setItems(itemSchema));
            assertTrue((Boolean) param.vendorExtensions.get("x-item-enum-values"));
            assertTrue(((String) param.vendorExtensions.get("x-item-enum-values-string")).contains("\"x\""));
        }
    }

    @Nested
    class Nullable {

        @Test void nullable() {
            var param = stringParam(s -> s.setNullable(true));
            assertTrue((Boolean) param.vendorExtensions.get("x-nullable"));
        }

        @Test void notNullable() {
            var param = stringParam(s -> s.setNullable(false));
            assertFalse(param.vendorExtensions.containsKey("x-nullable"));
        }

        @Test void nullableNull() {
            var param = stringParam(s -> s.setNullable(null));
            assertFalse(param.vendorExtensions.containsKey("x-nullable"));
        }
    }

    @Nested
    class HasValidation {

        @Test void requiredMarksValidation() {
            var param = new CodegenParameter();
            param.required = true;
            param.vendorExtensions = new java.util.HashMap<>();
            ParameterConstraintExtractor.extract(param, new StringSchema());
            assertTrue((Boolean) param.vendorExtensions.get("x-has-validation"));
        }

        @Test void constraintMarksValidation() {
            var param = stringParam(s -> s.setMinLength(1));
            assertTrue((Boolean) param.vendorExtensions.get("x-has-validation"));
        }

        @Test void noConstraintNoValidation() {
            var param = stringParam(s -> {});
            assertFalse((Boolean) param.vendorExtensions.getOrDefault("x-has-validation", false));
        }
    }

    @Test void nullSchemaIsNoop() {
        var param = new CodegenParameter();
        param.vendorExtensions = new java.util.HashMap<>();
        ParameterConstraintExtractor.extract(param, null);
        assertFalse(param.vendorExtensions.containsKey("x-has-validation"));
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private CodegenParameter stringParam(java.util.function.Consumer<StringSchema> config) {
        var param = new CodegenParameter();
        param.isString = true;
        param.vendorExtensions = new java.util.HashMap<>();
        var schema = new StringSchema();
        config.accept(schema);
        ParameterConstraintExtractor.extract(param, schema);
        return param;
    }

    private CodegenParameter intParam(java.util.function.Consumer<IntegerSchema> config) {
        var param = new CodegenParameter();
        param.isInteger = true;
        param.vendorExtensions = new java.util.HashMap<>();
        var schema = new IntegerSchema();
        config.accept(schema);
        ParameterConstraintExtractor.extract(param, schema);
        return param;
    }

    private CodegenParameter arrayParam(java.util.function.Consumer<ArraySchema> config) {
        var param = new CodegenParameter();
        param.isArray = true;
        param.vendorExtensions = new java.util.HashMap<>();
        var schema = new ArraySchema();
        config.accept(schema);
        ParameterConstraintExtractor.extract(param, schema);
        return param;
    }
}
