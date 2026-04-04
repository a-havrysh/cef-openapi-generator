package io.github.cef.codegen.processing;

import lombok.experimental.UtilityClass;
import org.openapitools.codegen.CodegenModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Processes x-enum-field-* vendor extensions on enum models.
 *
 * Converts vendor extensions into Mustache template data:
 * enumFields list + constructorArgs per enum value.
 */
@UtilityClass
public class EnumFieldProcessor {

    private final String VENDOR_PREFIX = "x-enum-field-";
    private final String ENUM_VARS_KEY = "enumVars";
    public final String ENUM_FIELDS_KEY = "enumFields";
    public final String HAS_ENUM_FIELDS_KEY = "hasEnumFields";
    private final String CONSTRUCTOR_ARGS_KEY = "constructorArgs";
    private final String FIELD_NAME = "name";
    private final String FIELD_TYPE = "type";
    private final String FIELD_CAPITALIZED = "capitalizedName";
    private final String FIELD_VALUE = "value";

    @SuppressWarnings("unchecked")
    public void process(CodegenModel model) {
        if (model == null || !model.isEnum
            || model.allowableValues == null) {
            return;
        }

        var enumVars = (List<Map<String, Object>>)
            model.allowableValues.get(ENUM_VARS_KEY);
        if (enumVars == null || enumVars.isEmpty()) return;

        var rawFields = extractFields(model);
        if (rawFields.isEmpty()) return;

        var fields = withValueField(rawFields, enumVars);
        var fieldMeta = buildFieldMetadata(fields);

        model.vendorExtensions.put(ENUM_FIELDS_KEY, fieldMeta);
        model.vendorExtensions.put(HAS_ENUM_FIELDS_KEY, true);
        populateConstructorArgs(enumVars, fields, fieldMeta);
    }

    private Map<String, List<?>> extractFields(CodegenModel model) {
        var fields = new LinkedHashMap<String, List<?>>();
        for (var entry : model.vendorExtensions.entrySet()) {
            if (entry.getKey().startsWith(VENDOR_PREFIX)
                && entry.getValue() instanceof List<?> list
            ) {
                var name = entry.getKey()
                    .substring(VENDOR_PREFIX.length());
                fields.put(name, list);
            }
        }
        return fields;
    }

    /**
     * Returns fields with "value" prepended if not already present.
     * Does not mutate the input map.
     */
    private Map<String, List<?>> withValueField(
        Map<String, List<?>> fields,
        List<Map<String, Object>> enumVars
    ) {
        if (fields.containsKey(FIELD_VALUE)) return fields;

        var values = enumVars.stream()
            .map(ev -> {
                var raw = ev.get(FIELD_VALUE);
                return (Object) (raw != null
                    ? raw.toString().replaceAll("^\"|\"$", "")
                    : "");
            })
            .toList();

        var result = new LinkedHashMap<String, List<?>>();
        result.put(FIELD_VALUE, values);
        result.putAll(fields);
        return result;
    }

    private List<Map<String, String>> buildFieldMetadata(
        Map<String, List<?>> fields
    ) {
        return fields.entrySet().stream()
            .map(entry -> Map.of(
                FIELD_NAME, entry.getKey(),
                FIELD_CAPITALIZED, capitalize(entry.getKey()),
                FIELD_TYPE, TypeConverter.detectType(entry.getValue())
            ))
            .toList();
    }

    private void populateConstructorArgs(
        List<Map<String, Object>> enumVars,
        Map<String, List<?>> fields,
        List<Map<String, String>> fieldMeta
    ) {
        for (int i = 0; i < enumVars.size(); i++) {
            var enumVar = enumVars.get(i);
            var args = new ArrayList<String>();

            for (var meta : fieldMeta) {
                var fieldValues = fields.get(meta.get(FIELD_NAME));
                if (fieldValues != null && i < fieldValues.size()) {
                    var value = fieldValues.get(i);
                    args.add(TypeConverter.formatLiteral(
                        value, meta.get(FIELD_TYPE)));
                    enumVar.put(meta.get(FIELD_NAME), value);
                }
            }

            if (!args.isEmpty()) {
                enumVar.put(CONSTRUCTOR_ARGS_KEY,
                    String.join(", ", args));
            }
        }
    }

    private String capitalize(String s) {
        if (s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
