package io.github.cef.codegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CefKotlinCodegenTest {

    private CefKotlinCodegen codegen;

    @BeforeEach
    void setUp() {
        codegen = new CefKotlinCodegen();
        codegen.processOpts();
    }

    @Test void name() { assertEquals("cef-kotlin", codegen.getName()); }
    @Test void help() { assertTrue(codegen.getHelp().contains("Kotlin")); }

    @Nested
    class TypeMappings {
        @Test void integer() { assertEquals("Int", codegen.typeMapping().get("integer")); }
        @Test void string()  { assertEquals("String", codegen.typeMapping().get("string")); }
        @Test void object()  { assertEquals("Any", codegen.typeMapping().get("object")); }
        @Test void voidType(){ assertEquals("Unit", codegen.typeMapping().get("Void")); }
        @Test void array()   { assertEquals("List", codegen.typeMapping().get("array")); }
        @Test void map()     { assertEquals("Map", codegen.typeMapping().get("map")); }
        @Test void binary()  { assertEquals("ByteArray", codegen.typeMapping().get("binary")); }
    }

    @Nested
    class TypeDeclaration {

        @Test void stringSchema() {
            var schema = new io.swagger.v3.oas.models.media.StringSchema();
            var result = codegen.getTypeDeclaration(schema);
            assertEquals("String", result);
        }

        @Test void arrayOfStrings() {
            var schema = new io.swagger.v3.oas.models.media.ArraySchema();
            schema.setItems(new io.swagger.v3.oas.models.media.StringSchema());
            var result = codegen.getTypeDeclaration(schema);
            assertTrue(result.contains("List"), "Expected List: " + result);
            assertFalse(result.contains("java.util"), "Should not contain java.util: " + result);
        }
    }

    @Nested
    class DefaultValues {

        @Test void arrayDefault() {
            var schema = new io.swagger.v3.oas.models.media.ArraySchema();
            schema.setItems(new io.swagger.v3.oas.models.media.StringSchema());
            var result = codegen.toDefaultValue(schema);
            if (result != null) {
                assertFalse(result.contains("new ArrayList"), "Should not contain Java: " + result);
            }
        }
    }

    @Nested
    class TemplateConfiguration {

        @Test void modelTemplatesUseKt() {
            assertTrue(codegen.modelTemplateFiles().containsValue(".kt"));
            assertFalse(codegen.modelTemplateFiles().containsValue(".java"));
        }

        @Test void apiTemplatesUseKt() {
            var values = codegen.apiTemplateFiles().values();
            assertTrue(values.stream().allMatch(v -> v.endsWith(".kt")));
        }

        @Test void noDocTemplates() {
            assertTrue(codegen.modelDocTemplateFiles().isEmpty());
            assertTrue(codegen.apiDocTemplateFiles().isEmpty());
        }

        @Test void noTestTemplates() {
            assertTrue(codegen.modelTestTemplateFiles().isEmpty());
            assertTrue(codegen.apiTestTemplateFiles().isEmpty());
        }

        @Test void supportingFilesAreKt() {
            codegen.supportingFiles().forEach(sf -> {
                var name = sf.getDestinationFilename();
                assertFalse(name.endsWith(".java"), "Java file found: " + name);
            });
        }

        @Test void noMockService() {
            codegen.supportingFiles().forEach(sf ->
                assertFalse(sf.getTemplateFile().contains("mockService"),
                    "MockService should be excluded"));
        }
    }

    @Nested
    class FilenameConversion {

        @Test void modelFilenameNoJava() {
            var name = codegen.toModelFilename("UserDto");
            assertFalse(name.contains(".java"), "Should not contain .java: " + name);
        }

        @Test void apiFilenameNoJava() {
            var name = codegen.toApiFilename("Config");
            assertFalse(name.contains(".java"), "Should not contain .java: " + name);
        }
    }

    @Nested
    class PropertyKotlinification {

        @Test void kotlinifiesDataType() {
            var model = new CodegenModel();
            model.imports = new HashSet<>();
            var prop = new CodegenProperty();
            prop.dataType = "java.util.List<java.lang.Object>";
            prop.datatypeWithEnum = "java.util.Map<String, Integer>";
            prop.baseType = "Object";

            codegen.postProcessModelProperty(model, prop);

            assertEquals("List<Any>", prop.dataType);
            assertEquals("Map<String, Int>", prop.datatypeWithEnum);
            assertEquals("Any", prop.baseType);
        }

        @Test void kotlinifiesDefaultValue() {
            var model = new CodegenModel();
            model.imports = new HashSet<>();
            var prop = new CodegenProperty();
            prop.dataType = "String";
            prop.defaultValue = "new ArrayList<>()";

            codegen.postProcessModelProperty(model, prop);

            assertEquals("emptyList()", prop.defaultValue);
        }

        @Test void escapesDollarInBaseName() {
            var model = new CodegenModel();
            model.imports = new HashSet<>();
            var prop = new CodegenProperty();
            prop.baseName = "$schema";
            prop.name = "$schema";
            prop.dataType = "String";

            codegen.postProcessModelProperty(model, prop);

            assertEquals("\\$schema", prop.baseName);
            assertEquals("`$schema`", prop.name);
        }

        @Test void doesNotEscapeRegularName() {
            var model = new CodegenModel();
            model.imports = new HashSet<>();
            var prop = new CodegenProperty();
            prop.baseName = "name";
            prop.name = "name";
            prop.dataType = "String";

            codegen.postProcessModelProperty(model, prop);

            assertEquals("name", prop.baseName);
            assertEquals("name", prop.name);
        }
    }

    @Nested
    class ImportFiltering {

        @Test void removesJavaUtilImports() {
            var model = new CodegenModel();
            model.imports = new HashSet<>(Set.of("java.util.List", "java.util.Map", "com.example.dto.UserDto"));
            model.vars = List.of();

            var modelsMap = wrapModel(model);
            codegen.postProcessModels(modelsMap);

            assertFalse(model.imports.contains("java.util.List"));
            assertFalse(model.imports.contains("java.util.Map"));
            assertTrue(model.imports.contains("com.example.dto.UserDto"));
        }

        @Test void removesKotlinBuiltins() {
            var model = new CodegenModel();
            model.imports = new HashSet<>(Set.of("Int", "String", "Any", "com.example.Real"));
            model.vars = List.of();

            var modelsMap = wrapModel(model);
            codegen.postProcessModels(modelsMap);

            assertFalse(model.imports.contains("Int"));
            assertFalse(model.imports.contains("String"));
            assertTrue(model.imports.contains("com.example.Real"));
        }
    }

    private org.openapitools.codegen.model.ModelsMap wrapModel(CodegenModel model) {
        var modelsMap = new org.openapitools.codegen.model.ModelsMap();
        var modelMap = new org.openapitools.codegen.model.ModelMap();
        modelMap.setModel(model);
        modelsMap.setModels(List.of(modelMap));
        return modelsMap;
    }
}
