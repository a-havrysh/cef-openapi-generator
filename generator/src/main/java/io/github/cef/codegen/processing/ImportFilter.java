package io.github.cef.codegen.processing;

import org.openapitools.codegen.CodegenModel;

import java.util.Set;

/**
 * Filters unwanted imports from generated models.
 * Base implementation removes Swagger/OpenAPI annotations.
 * Kotlin subclass logic adds Java collection and built-in type filtering.
 */
public final class ImportFilter {

    /** Substrings — any import containing these is filtered. */
    private static final Set<String> JAVA_FILTER_CONTAINS = Set.of(
        "swagger", "JsonNullable", "oas.annotations"
    );

    /** Exact simple names — only filter if the import's simple name matches exactly. */
    private static final Set<String> JAVA_FILTER_EXACT = Set.of(
        "ApiModel", "ApiModelProperty", "Schema"
    );

    /** Kotlin built-in types that never need importing. */
    private static final Set<String> KOTLIN_BUILTIN_TYPES = Set.of(
        "Int", "Long", "Float", "Double", "Boolean", "String", "Any",
        "List", "Map", "Set", "Array", "Byte", "Short", "Char",
        "Unit", "Nothing", "Pair", "Triple"
    );

    /** Java types that map to Kotlin built-ins and should be filtered. */
    private static final Set<String> KOTLIN_FILTER_KEYWORDS = Set.of(
        "java.util", "java.lang", "ArrayList", "HashMap",
        "LinkedHashMap", "HashSet", "Arrays", "Object"
    );

    private ImportFilter() {}

    /** Removes annotation-related imports from a model (Java generator). */
    public static void cleanupJavaImports(CodegenModel model) {
        if (model == null || model.imports == null) return;
        model.imports.removeIf(ImportFilter::isJavaFilteredImport);
    }

    /** Removes annotation + Java type imports from a model (Kotlin generator). */
    public static void cleanupKotlinImports(CodegenModel model) {
        if (model == null || model.imports == null) return;
        model.imports.removeIf(imp -> isJavaFilteredImport(imp) || isKotlinFilteredImport(imp));
    }

    /** Checks if import should be filtered for Kotlin, given the model package for self-import detection. */
    public static boolean shouldFilterForKotlin(String importStr, String modelPackage) {
        if (importStr == null || importStr.isEmpty()) return true;
        if (isJavaFilteredImport(importStr)) return true;
        if (isKotlinFilteredImport(importStr)) return true;

        // Self-package imports of built-in type names (e.g., com.example.dto.Int)
        if (modelPackage != null && importStr.startsWith(modelPackage + ".")) {
            var simpleName = simpleName(importStr);
            if (KOTLIN_BUILTIN_TYPES.contains(simpleName)) return true;
            return Set.of("ArrayList", "HashMap", "LinkedHashMap", "HashSet").contains(simpleName);
        }

        return false;
    }

    private static boolean isJavaFilteredImport(String importStr) {
        if (importStr == null || importStr.isEmpty()) return true;
        if (JAVA_FILTER_CONTAINS.stream().anyMatch(importStr::contains)) return true;
        var simpleName = simpleName(importStr);
        return JAVA_FILTER_EXACT.contains(simpleName);
    }

    private static boolean isKotlinFilteredImport(String importStr) {
        var simpleName = simpleName(importStr);
        if (KOTLIN_BUILTIN_TYPES.contains(simpleName) || KOTLIN_BUILTIN_TYPES.contains(importStr)) return true;
        return KOTLIN_FILTER_KEYWORDS.stream().anyMatch(importStr::contains);
    }

    private static String simpleName(String fqn) {
        int dot = fqn.lastIndexOf('.');
        return dot >= 0 ? fqn.substring(dot + 1) : fqn;
    }
}
