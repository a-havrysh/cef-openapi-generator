package io.github.cef.codegen.processing;

import io.swagger.v3.oas.models.media.Schema;
import org.openapitools.codegen.CodegenParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Extracts OpenAPI validation constraints from parameters and stores them
 * as vendor extensions (x-min-length, x-maximum, x-pattern, etc.)
 * for use in generated validation code.
 */
public final class ParameterConstraintExtractor {

    /** All vendor extension keys that indicate validation is needed. */
    private static final Set<String> VALIDATION_KEYS = Set.of(
        "x-min-length", "x-max-length", "x-pattern",
        "x-minimum", "x-maximum", "x-exclusive-minimum", "x-exclusive-maximum",
        "x-multiple-of", "x-has-enum-values",
        "x-min-items", "x-max-items", "x-unique-items", "x-item-enum-values",
        "x-format"
    );

    private ParameterConstraintExtractor() {}

    /**
     * Extracts all validation constraints from the parameter schema
     * and stores them as vendor extensions on the CodegenParameter.
     */
    public static void extract(CodegenParameter param, Schema<?> schema) {
        if (schema == null) return;

        if (param.isString) extractStringConstraints(param, schema);
        if (param.isInteger || param.isLong || param.isNumber) extractNumericConstraints(param, schema);
        if (param.isArray) extractArrayConstraints(param, schema);

        extractNullable(param, schema);
        extractEnumValues(param, schema);
        markHasValidation(param);
    }

    private static void extractStringConstraints(CodegenParameter param, Schema<?> schema) {
        var format = schema.getFormat();
        if ("date".equals(format)) {
            param.vendorExtensions.put("x-is-date", true);
            param.vendorExtensions.put("x-java-type", "java.time.LocalDate");
        } else if ("date-time".equals(format)) {
            param.vendorExtensions.put("x-is-date-time", true);
            param.vendorExtensions.put("x-java-type", "java.time.OffsetDateTime");
        } else if (format != null && !format.isEmpty()) {
            param.vendorExtensions.put("x-format", format);
        }

        putIfNotNull(param, "x-min-length", schema.getMinLength());
        putIfNotNull(param, "x-max-length", schema.getMaxLength());

        if (schema.getPattern() != null) {
            param.vendorExtensions.put("x-pattern", schema.getPattern().replace("\\", "\\\\"));
        }
    }

    private static void extractNumericConstraints(CodegenParameter param, Schema<?> schema) {
        putIfNotNull(param, "x-minimum", schema.getMinimum());
        putIfNotNull(param, "x-maximum", schema.getMaximum());
        putIfNotNull(param, "x-exclusive-minimum", schema.getExclusiveMinimum());
        putIfNotNull(param, "x-exclusive-maximum", schema.getExclusiveMaximum());
        putIfNotNull(param, "x-multiple-of", schema.getMultipleOf());
    }

    private static void extractArrayConstraints(CodegenParameter param, Schema<?> schema) {
        putIfNotNull(param, "x-min-items", schema.getMinItems());
        putIfNotNull(param, "x-max-items", schema.getMaxItems());
        putIfNotNull(param, "x-unique-items", schema.getUniqueItems());

        if (schema.getItems() != null) {
            extractItemEnumValues(param, schema.getItems());
        }
    }

    private static void extractNullable(CodegenParameter param, Schema<?> schema) {
        if (Boolean.TRUE.equals(schema.getNullable())) {
            param.vendorExtensions.put("x-nullable", true);
        }
    }

    private static void extractEnumValues(CodegenParameter param, Schema<?> schema) {
        if (schema.getEnum() == null || schema.getEnum().isEmpty()) return;

        var quoted = schema.getEnum().stream()
            .map(v -> "\"" + v + "\"")
            .reduce((a, b) -> a + ", " + b)
            .orElse("");

        param.vendorExtensions.put("x-enum-values-string", quoted);
        param.vendorExtensions.put("x-has-enum-values", true);
    }

    private static void extractItemEnumValues(CodegenParameter param, Schema<?> itemsSchema) {
        if (itemsSchema.getEnum() == null || itemsSchema.getEnum().isEmpty()) return;

        var quoted = itemsSchema.getEnum().stream()
            .map(v -> "\"" + v + "\"")
            .reduce((a, b) -> a + ", " + b)
            .orElse("");

        param.vendorExtensions.put("x-item-enum-values-string", quoted);
        param.vendorExtensions.put("x-item-enum-values", true);
    }

    private static void markHasValidation(CodegenParameter param) {
        boolean has = param.required || VALIDATION_KEYS.stream()
            .anyMatch(param.vendorExtensions::containsKey);
        param.vendorExtensions.put("x-has-validation", has);
    }

    private static void putIfNotNull(CodegenParameter param, String key, Object value) {
        if (value != null) param.vendorExtensions.put(key, value);
    }
}
