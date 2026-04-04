package io.github.cef.codegen.processing;

import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class EnumFieldProcessorTest {

    @Test
    @SuppressWarnings("unchecked")
    void processesVendorExtensionFields() {
        var model = enumModel("ACTIVE", "INACTIVE");
        model.vendorExtensions.put("x-enum-field-displayName", Arrays.asList("Active", "Inactive"));
        model.vendorExtensions.put("x-enum-field-priority", Arrays.asList(1, 2));

        EnumFieldProcessor.process(model);

        assertTrue(model.vendorExtensions.containsKey("enumFields"));
        assertTrue((Boolean) model.vendorExtensions.get("hasEnumFields"));

        var enumVars = (List<Map<String, Object>>) model.allowableValues.get("enumVars");
        // Should have constructorArgs with auto-injected value + displayName + priority
        assertTrue(enumVars.get(0).containsKey("constructorArgs"));
        var args = (String) enumVars.get(0).get("constructorArgs");
        assertTrue(args.contains("Active"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void injectsValueFieldWhenMissing() {
        var model = enumModel("LIGHT", "DARK");
        model.vendorExtensions.put("x-enum-field-displayName", Arrays.asList("Light", "Dark"));

        EnumFieldProcessor.process(model);

        var fields = (List<Map<String, String>>) model.vendorExtensions.get("enumFields");
        // First field should be "value" (auto-injected)
        assertEquals("value", fields.get(0).get("name"));
        assertEquals("displayName", fields.get(1).get("name"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void doesNotInjectValueWhenExplicit() {
        var model = enumModel("NONE", "GROOVY");
        model.vendorExtensions.put("x-enum-field-value", Arrays.asList("", "groovy"));
        model.vendorExtensions.put("x-enum-field-displayName", Arrays.asList("None", "Groovy"));

        EnumFieldProcessor.process(model);

        var fields = (List<Map<String, String>>) model.vendorExtensions.get("enumFields");
        // "value" comes from vendor extension, not auto-injected — should appear once
        long valueCount = fields.stream().filter(f -> "value".equals(f.get("name"))).count();
        assertEquals(1, valueCount);
    }

    @Test
    void detectsIntegerType() {
        var model = enumModel("A", "B");
        model.vendorExtensions.put("x-enum-field-code", Arrays.asList(1, 2));

        EnumFieldProcessor.process(model);

        @SuppressWarnings("unchecked")
        var fields = (List<Map<String, String>>) model.vendorExtensions.get("enumFields");
        var codeField = fields.stream().filter(f -> "code".equals(f.get("name"))).findFirst().orElseThrow();
        assertEquals("Integer", codeField.get("type"));
    }

    @Test
    void detectsBooleanType() {
        var model = enumModel("ON", "OFF");
        model.vendorExtensions.put("x-enum-field-active", Arrays.asList(true, false));

        EnumFieldProcessor.process(model);

        @SuppressWarnings("unchecked")
        var fields = (List<Map<String, String>>) model.vendorExtensions.get("enumFields");
        var activeField = fields.stream().filter(f -> "active".equals(f.get("name"))).findFirst().orElseThrow();
        assertEquals("Boolean", activeField.get("type"));
    }

    @Test
    void skipsNonEnumModel() {
        var model = new CodegenModel();
        model.isEnum = false;
        EnumFieldProcessor.process(model);
        assertFalse(model.vendorExtensions.containsKey("enumFields"));
    }

    @Test
    void skipsNullModel() {
        assertDoesNotThrow(() -> EnumFieldProcessor.process(null));
    }

    @Test
    void skipsEnumWithoutVendorExtensions() {
        var model = enumModel("A", "B");
        // No x-enum-field-* extensions
        EnumFieldProcessor.process(model);
        assertFalse(model.vendorExtensions.containsKey("enumFields"));
    }

    @Test
    void skipsEnumWithoutEnumVars() {
        var model = new CodegenModel();
        model.isEnum = true;
        model.allowableValues = new HashMap<>();
        // No "enumVars" key
        model.vendorExtensions = new HashMap<>();
        model.vendorExtensions.put("x-enum-field-name", Arrays.asList("a", "b"));

        EnumFieldProcessor.process(model);
        assertFalse(model.vendorExtensions.containsKey("enumFields"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void capitalizesFieldNames() {
        var model = enumModel("X");
        model.vendorExtensions.put("x-enum-field-myField", List.of("val"));

        EnumFieldProcessor.process(model);

        var fields = (List<Map<String, String>>) model.vendorExtensions.get("enumFields");
        var myField = fields.stream().filter(f -> "myField".equals(f.get("name"))).findFirst().orElseThrow();
        assertEquals("MyField", myField.get("capitalizedName"));
    }

    private CodegenModel enumModel(String... values) {
        var model = new CodegenModel();
        model.isEnum = true;
        model.allowableValues = new HashMap<>();
        model.allowableValues.put("values", Arrays.asList(values));

        // Build enumVars like OpenAPI Generator does
        var enumVars = new ArrayList<Map<String, Object>>();
        for (var v : values) {
            var ev = new HashMap<String, Object>();
            ev.put("name", v);
            ev.put("value", "\"" + v + "\"");
            enumVars.add(ev);
        }
        model.allowableValues.put("enumVars", enumVars);
        model.vendorExtensions = new HashMap<>();
        return model;
    }
}
