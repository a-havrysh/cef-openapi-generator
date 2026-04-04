package io.github.cef.codegen.processing;

import lombok.experimental.UtilityClass;
import org.openapitools.codegen.CodegenModel;

import java.util.Set;

/**
 * Filters unwanted imports from generated models.
 * Handles both Java (annotations) and Kotlin (built-in types).
 */
@UtilityClass
public class ImportFilter {

    private final Set<String> JAVA_FILTER_CONTAINS = Set.of(
        "swagger", "JsonNullable", "oas.annotations"
    );

    private final Set<String> JAVA_FILTER_EXACT = Set.of(
        "ApiModel", "ApiModelProperty", "Schema"
    );

    private final Set<String> KOTLIN_BUILTIN_TYPES = Set.of(
        "Int", "Long", "Float", "Double", "Boolean", "String", "Any",
        "List", "Map", "Set", "Array", "Byte", "Short", "Char",
        "Unit", "Nothing", "Pair", "Triple"
    );

    private final Set<String> KOTLIN_FILTER_KEYWORDS = Set.of(
        "java.util", "java.lang", "ArrayList", "HashMap",
        "LinkedHashMap", "HashSet", "Arrays", "Object"
    );

    private final Set<String> JAVA_COLLECTION_SIMPLE_NAMES = Set.of(
        "ArrayList", "HashMap", "LinkedHashMap", "HashSet"
    );

    /** Removes annotation-related imports (Java generator). */
    public void cleanupJavaImports(CodegenModel model) {
        if (model == null || model.imports == null) return;
        model.imports.removeIf(ImportFilter::isJavaFiltered);
    }

    /** Removes annotation + Java type imports (Kotlin generator). */
    public void cleanupKotlinImports(CodegenModel model) {
        if (model == null || model.imports == null) return;
        model.imports.removeIf(
            imp -> isJavaFiltered(imp) || isKotlinFiltered(imp)
        );
    }

    /** Full Kotlin filter including self-package detection. */
    public boolean shouldFilterForKotlin(
        String importStr,
        String modelPackage
    ) {
        if (importStr == null || importStr.isEmpty()) return true;
        if (isJavaFiltered(importStr)) return true;
        if (isKotlinFiltered(importStr)) return true;

        if (modelPackage != null
            && importStr.startsWith(modelPackage + ".")
        ) {
            var simpleName = simpleName(importStr);
            return KOTLIN_BUILTIN_TYPES.contains(simpleName)
                || JAVA_COLLECTION_SIMPLE_NAMES.contains(simpleName);
        }

        return false;
    }

    private boolean isJavaFiltered(String importStr) {
        if (importStr == null || importStr.isEmpty()) return true;
        if (JAVA_FILTER_CONTAINS.stream().anyMatch(importStr::contains)) {
            return true;
        }
        return JAVA_FILTER_EXACT.contains(simpleName(importStr));
    }

    private boolean isKotlinFiltered(String importStr) {
        var simpleName = simpleName(importStr);
        if (KOTLIN_BUILTIN_TYPES.contains(simpleName)) return true;
        if (KOTLIN_BUILTIN_TYPES.contains(importStr)) return true;
        return KOTLIN_FILTER_KEYWORDS.stream().anyMatch(importStr::contains);
    }

    private String simpleName(String fqn) {
        int dot = fqn.lastIndexOf('.');
        return dot >= 0 ? fqn.substring(dot + 1) : fqn;
    }
}
