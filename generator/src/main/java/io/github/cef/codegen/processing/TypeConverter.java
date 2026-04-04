package io.github.cef.codegen.processing;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Detects Java types from values and formats them for code generation.
 * Also provides Java→Kotlin type conversion.
 */
public final class TypeConverter {

    private static final Map<Class<?>, String> TYPE_MAP = Map.of(
        Integer.class, "Integer",
        Long.class, "Long",
        Double.class, "Double",
        Float.class, "Float",
        Boolean.class, "Boolean",
        BigDecimal.class, "java.math.BigDecimal",
        BigInteger.class, "java.math.BigInteger"
    );

    private TypeConverter() {}

    /** Detects Java type name from the first non-null element in a list. Defaults to "String". */
    public static String detectType(List<?> values) {
        if (values == null || values.isEmpty()) return "String";
        var first = values.get(0);
        if (first == null) return "String";
        return TYPE_MAP.getOrDefault(first.getClass(), "String");
    }

    /** Formats a value as a Java/Kotlin literal string based on its type. */
    public static String formatLiteral(Object value, String type) {
        if (value == null) return "null";

        return switch (type) {
            case "String" -> quote(value.toString());
            case "Integer", "Long", "Double", "Float", "Boolean" -> value.toString();
            default -> {
                if (type.startsWith("java.math.")) {
                    yield "new " + type + "(\"" + value + "\")";
                }
                yield quote(value.toString());
            }
        };
    }

    /** Replaces Java types with Kotlin equivalents in a type string. */
    public static String kotlinify(String type) {
        if (type == null) return null;
        return type
            .replace("java.util.List", "List")
            .replace("java.util.Map", "Map")
            .replace("java.util.Set", "Set")
            .replace("java.lang.Object", "Any")
            .replace("Object", "Any")
            .replace("Integer", "Int")
            .replace("Void", "Unit");
    }

    /** Replaces Java collection constructors with Kotlin idioms in default values. */
    public static String kotlinifyDefaultValue(String defaultValue) {
        if (defaultValue == null) return null;
        return defaultValue
            .replace("new ArrayList<>()", "emptyList()")
            .replace("new ArrayList<", "mutableListOf<")
            .replace("new HashMap<>()", "emptyMap()")
            .replace("new HashMap<", "mutableMapOf<")
            .replace("new HashSet<>()", "emptySet()")
            .replace("new HashSet<", "mutableSetOf<");
    }

    private static String quote(String value) {
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }
}
