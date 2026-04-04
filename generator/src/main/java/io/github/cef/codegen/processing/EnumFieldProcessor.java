package io.github.cef.codegen.processing;

import org.openapitools.codegen.CodegenModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Processes x-enum-field-* vendor extensions on enum models.
 *
 * Converts OpenAPI vendor extensions like:
 * ```yaml
 * x-enum-field-displayName: ["Light", "Dark"]
 * x-enum-field-description: ["Light theme", "Dark theme"]
 * ```
 *
 * Into Mustache template data: enumFields list + constructorArgs per enum value.
 */
public final class EnumFieldProcessor {

    private static final String VENDOR_PREFIX = "x-enum-field-";
    private static final String ENUM_VARS_KEY = "enumVars";
    public static final String ENUM_FIELDS_KEY = "enumFields";
    public static final String HAS_ENUM_FIELDS_KEY = "hasEnumFields";
    private static final String CONSTRUCTOR_ARGS_KEY = "constructorArgs";

    private EnumFieldProcessor() {}

    /**
     * Processes vendor extensions on an enum model.
     * Extracts fields, auto-injects "value" if missing, builds metadata, populates constructor args.
     */
    @SuppressWarnings("unchecked")
    public static void process(CodegenModel model) {
        if (model == null || !model.isEnum || model.allowableValues == null) return;

        var enumVars = (List<Map<String, Object>>) model.allowableValues.get(ENUM_VARS_KEY);
        if (enumVars == null || enumVars.isEmpty()) return;

        var rawFields = extractFields(model);
        if (rawFields.isEmpty()) return;

        var fields = withValueField(rawFields, enumVars);

        var fieldMeta = buildFieldMetadata(fields);
        model.vendorExtensions.put(ENUM_FIELDS_KEY, fieldMeta);
        model.vendorExtensions.put(HAS_ENUM_FIELDS_KEY, true);
        populateConstructorArgs(enumVars, fields, fieldMeta);
    }

    /**
     * Scans vendor extensions for x-enum-field-* entries.
     * Returns ordered map: field name → list of values per enum constant.
     */
    private static Map<String, List<?>> extractFields(CodegenModel model) {
        var fields = new LinkedHashMap<String, List<?>>();
        for (var entry : model.vendorExtensions.entrySet()) {
            if (entry.getKey().startsWith(VENDOR_PREFIX) && entry.getValue() instanceof List<?> list) {
                var fieldName = entry.getKey().substring(VENDOR_PREFIX.length());
                fields.put(fieldName, list);
            }
        }
        return fields;
    }

    /**
     * Returns fields with a "value" field prepended if not already present.
     * Does not mutate the input map.
     */
    private static Map<String, List<?>> withValueField(Map<String, List<?>> fields, List<Map<String, Object>> enumVars) {
        if (fields.containsKey("value")) return fields;

        var values = enumVars.stream()
            .map(ev -> {
                var raw = ev.get("value");
                return (Object) (raw != null ? raw.toString().replaceAll("^\"|\"$", "") : "");
            })
            .toList();

        var result = new LinkedHashMap<String, List<?>>();
        result.put("value", values);
        result.putAll(fields);
        return result;
    }

    /**
     * Builds metadata (name, capitalizedName, type) for each enum field.
     */
    private static List<Map<String, String>> buildFieldMetadata(Map<String, List<?>> fields) {
        return fields.entrySet().stream()
            .map(entry -> Map.of(
                "name", entry.getKey(),
                "capitalizedName", capitalize(entry.getKey()),
                "type", TypeConverter.detectType(entry.getValue())
            ))
            .toList();
    }

    /**
     * For each enum constant, builds the constructorArgs string
     * (e.g., "CLASSIC_BPMN", "Classic BPMN", "Description").
     */
    private static void populateConstructorArgs(
        List<Map<String, Object>> enumVars,
        Map<String, List<?>> fields,
        List<Map<String, String>> fieldMeta
    ) {
        for (int i = 0; i < enumVars.size(); i++) {
            var enumVar = enumVars.get(i);
            var args = new ArrayList<String>();

            for (var meta : fieldMeta) {
                var fieldValues = fields.get(meta.get("name"));
                if (fieldValues != null && i < fieldValues.size()) {
                    var value = fieldValues.get(i);
                    args.add(TypeConverter.formatLiteral(value, meta.get("type")));
                    enumVar.put(meta.get("name"), value);
                }
            }

            if (!args.isEmpty()) {
                enumVar.put(CONSTRUCTOR_ARGS_KEY, String.join(", ", args));
            }
        }
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
