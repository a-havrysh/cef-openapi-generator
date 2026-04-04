package io.github.cef.codegen.processing;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Detects Java types from values and formats them for code generation.
 * Also provides Java→Kotlin type conversion.
 */
@UtilityClass
public class TypeConverter {

    private final String DEFAULT_TYPE = "String";
    private final String JAVA_MATH_PREFIX = "java.math.";

    private final Map<Class<?>, String> TYPE_MAP = Map.of(
        Integer.class, "Integer",
        Long.class, "Long",
        Double.class, "Double",
        Float.class, "Float",
        Boolean.class, "Boolean",
        BigDecimal.class, "java.math.BigDecimal",
        BigInteger.class, "java.math.BigInteger"
    );

    /**
     * Detects Java type name from the first non-null element.
     * Defaults to "String".
     */
    public String detectType(List<?> values) {
        if (values == null || values.isEmpty()) return DEFAULT_TYPE;
        var first = values.get(0);
        if (first == null) return DEFAULT_TYPE;
        return TYPE_MAP.getOrDefault(first.getClass(), DEFAULT_TYPE);
    }

    /** Formats a value as a Java/Kotlin literal based on its type. */
    public String formatLiteral(Object value, String type) {
        if (value == null) return "null";

        return switch (type) {
            case "String" -> quote(value.toString());
            case "Integer", "Long", "Double", "Float", "Boolean" ->
                value.toString();
            default -> {
                if (type.startsWith(JAVA_MATH_PREFIX)) {
                    yield "new " + type + "(\"" + value + "\")";
                }
                yield quote(value.toString());
            }
        };
    }

    /** Replaces Java types with Kotlin equivalents. */
    public String kotlinify(String type) {
        if (type == null) return null;
        return type
            .replace("java.util.List", "List")
            .replace("java.util.Map", "Map")
            .replace("java.util.Set", "Set")
            .replace("java.lang.Object", "Any")
            .replaceAll("\\bObject\\b", "Any")
            .replaceAll("\\bInteger\\b", "Int")
            .replaceAll("\\bVoid\\b", "Unit");
    }

    /** Replaces Java collection constructors with Kotlin idioms. */
    public String kotlinifyDefaultValue(String defaultValue) {
        if (defaultValue == null) return null;
        return defaultValue
            .replace("new ArrayList<>()", "emptyList()")
            .replace("new ArrayList<", "mutableListOf<")
            .replace("new HashMap<>()", "emptyMap()")
            .replace("new HashMap<", "mutableMapOf<")
            .replace("new HashSet<>()", "emptySet()")
            .replace("new HashSet<", "mutableSetOf<");
    }

    private String quote(String value) {
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }
}
