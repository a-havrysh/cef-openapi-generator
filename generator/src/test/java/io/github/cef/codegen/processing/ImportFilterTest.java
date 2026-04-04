package io.github.cef.codegen.processing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.openapitools.codegen.CodegenModel;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class ImportFilterTest {

    @Nested
    class JavaImports {

        @Test void removesSwagger() {
            var model = modelWith("io.swagger.annotations.ApiModel");
            ImportFilter.cleanupJavaImports(model);
            assertTrue(model.imports.isEmpty());
        }

        @Test void removesJsonNullable() {
            var model = modelWith("org.openapitools.jackson.nullable.JsonNullable");
            ImportFilter.cleanupJavaImports(model);
            assertTrue(model.imports.isEmpty());
        }

        @Test void removesOasAnnotations() {
            var model = modelWith("io.swagger.v3.oas.annotations.Schema");
            ImportFilter.cleanupJavaImports(model);
            assertTrue(model.imports.isEmpty());
        }

        @Test void keepsJavaUtil() {
            var model = modelWith("java.util.List");
            ImportFilter.cleanupJavaImports(model);
            assertTrue(model.imports.contains("java.util.List"));
        }

        @Test void keepsRegularImport() {
            var model = modelWith("com.example.dto.UserDto");
            ImportFilter.cleanupJavaImports(model);
            assertTrue(model.imports.contains("com.example.dto.UserDto"));
        }

        @Test void handlesNull() {
            assertDoesNotThrow(() -> ImportFilter.cleanupJavaImports(null));
        }

        @Test void handlesNullImports() {
            var model = new CodegenModel();
            model.imports = null;
            assertDoesNotThrow(() -> ImportFilter.cleanupJavaImports(model));
        }
    }

    @Nested
    class KotlinImports {

        @Test void removesJavaUtil() {
            var model = modelWith("java.util.List");
            ImportFilter.cleanupKotlinImports(model);
            assertTrue(model.imports.isEmpty());
        }

        @Test void removesArrayList() {
            var model = modelWith("java.util.ArrayList");
            ImportFilter.cleanupKotlinImports(model);
            assertTrue(model.imports.isEmpty());
        }

        @Test void removesHashMap() {
            var model = modelWith("java.util.HashMap");
            ImportFilter.cleanupKotlinImports(model);
            assertTrue(model.imports.isEmpty());
        }

        @Test void removesKotlinBuiltin() {
            var model = modelWith("Int");
            ImportFilter.cleanupKotlinImports(model);
            assertTrue(model.imports.isEmpty());
        }

        @Test void keepsCrossPackageModelImport() {
            var model = modelWith("com.example.dto.ElementTemplate");
            ImportFilter.cleanupKotlinImports(model);
            assertTrue(model.imports.contains("com.example.dto.ElementTemplate"));
        }
    }

    @Nested
    class ShouldFilterForKotlin {

        @Test void filtersNull()       { assertTrue(ImportFilter.shouldFilterForKotlin(null, null)); }
        @Test void filtersEmpty()      { assertTrue(ImportFilter.shouldFilterForKotlin("", null)); }
        @Test void filtersSwagger()    { assertTrue(ImportFilter.shouldFilterForKotlin("io.swagger.annotations.Api", null)); }
        @Test void filtersJavaUtil()   { assertTrue(ImportFilter.shouldFilterForKotlin("java.util.Map", null)); }
        @Test void filtersJavaLang()   { assertTrue(ImportFilter.shouldFilterForKotlin("java.lang.String", null)); }
        @Test void filtersBareInt()    { assertTrue(ImportFilter.shouldFilterForKotlin("Int", null)); }
        @Test void filtersBareList()   { assertTrue(ImportFilter.shouldFilterForKotlin("List", null)); }
        @Test void filtersBareAny()    { assertTrue(ImportFilter.shouldFilterForKotlin("Any", null)); }
        @Test void filtersBareUnit()   { assertTrue(ImportFilter.shouldFilterForKotlin("Unit", null)); }
        @Test void keepsRegular()      { assertFalse(ImportFilter.shouldFilterForKotlin("com.example.MyType", null)); }
        @Test void keepsJackson()      { assertFalse(ImportFilter.shouldFilterForKotlin("com.fasterxml.jackson.annotation.JsonProperty", null)); }

        @Test void filtersSelfPackageBuiltin() {
            assertTrue(ImportFilter.shouldFilterForKotlin("com.example.dto.Int", "com.example.dto"));
        }

        @Test void filtersSelfPackageArrayList() {
            assertTrue(ImportFilter.shouldFilterForKotlin("com.example.dto.ArrayList", "com.example.dto"));
        }

        @Test void keepsSelfPackageModel() {
            assertFalse(ImportFilter.shouldFilterForKotlin("com.example.dto.UserDto", "com.example.dto"));
        }
    }

    private CodegenModel modelWith(String... imports) {
        var model = new CodegenModel();
        model.imports = new HashSet<>();
        for (var imp : imports) model.imports.add(imp);
        return model;
    }
}
