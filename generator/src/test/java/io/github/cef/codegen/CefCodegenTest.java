package io.github.cef.codegen;

import io.github.cef.codegen.config.FileSpec;
import io.github.cef.codegen.config.PackageSuffix;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CefCodegenTest {

    private CefCodegen codegen;

    @BeforeEach
    void setUp() {
        codegen = new CefCodegen();
        codegen.processOpts();
    }

    // ── Identity ────────────────────────────────────────────────────────

    @Test void name() { assertEquals("cef", codegen.getName()); }
    @Test void help() { assertTrue(codegen.getHelp().contains("CEF")); }

    // ── Config enums ────────────────────────────────────────────────────

    @Nested
    class ConfigEnums {

        @Test void fileSpecValues()   { assertTrue(FileSpec.values().length >= 19); }
        @Test void packageSuffixes()  { assertEquals(7, PackageSuffix.values().length); }
        @Test void kotlinFileName()   { assertEquals("Service.kt", FileSpec.API_SERVICE.kotlinFileName()); }
        @Test void kotlinFileNameReq(){ assertEquals("ApiRequest.kt", FileSpec.API_REQUEST.kotlinFileName()); }

        @Test void allFileSpecsValid() {
            for (var spec : FileSpec.values()) {
                assertNotNull(spec.getTemplateName());
                assertTrue(spec.getTemplateName().endsWith(".mustache"));
                assertTrue(spec.getFileName().endsWith(".java"));
            }
        }

        @Test void allSuffixesStartWithDot() {
            for (var suffix : PackageSuffix.values()) {
                assertTrue(suffix.getValue().startsWith("."));
            }
        }

        @Test void fileSpec() {
            assertEquals("apiService.mustache", FileSpec.API_SERVICE.getTemplateName());
            assertEquals("Service.java", FileSpec.API_SERVICE.getFileName());
        }

        @Test void packageSuffix() {
            assertEquals(".protocol", PackageSuffix.PROTOCOL.getValue());
        }
    }

    // ── Template configuration ──────────────────────────────────────────

    @Nested
    class TemplateConfig {

        @Test void apiTemplates() {
            assertTrue(codegen.apiTemplateFiles().containsKey("api/apiService.mustache"));
            assertTrue(codegen.apiTemplateFiles().containsKey("api/mockService.mustache"));
        }

        @Test void supportingFilesContainAllLayers() {
            var templates = codegen.supportingFiles().stream()
                .map(sf -> sf.getTemplateFile()).toList();

            // Protocol
            assertTrue(templates.contains("protocol/httpMethod.mustache"));
            assertTrue(templates.contains("protocol/apiRequest.mustache"));
            assertTrue(templates.contains("protocol/apiResponse.mustache"));
            assertTrue(templates.contains("protocol/multipartFile.mustache"));
            // Routing
            assertTrue(templates.contains("routing/routeTree.mustache"));
            assertTrue(templates.contains("routing/routeNode.mustache"));
            // Exception
            assertTrue(templates.contains("exception/apiException.mustache"));
            assertTrue(templates.contains("exception/validationException.mustache"));
            // Validation
            assertTrue(templates.contains("validation/parameterValidator.mustache"));
            // Interceptor
            assertTrue(templates.contains("interceptor/requestInterceptor.mustache"));
            assertTrue(templates.contains("interceptor/corsInterceptor.mustache"));
            assertTrue(templates.contains("interceptor/validationInterceptor.mustache"));
            // CEF
            assertTrue(templates.contains("cef/apiCefRequestHandler.mustache"));
            assertTrue(templates.contains("cef/apiCefRequestHandlerBuilder.mustache"));
            // Utility
            assertTrue(templates.contains("util/contentTypeResolver.mustache"));
            assertTrue(templates.contains("util/multipartParser.mustache"));
        }

        @Test void layerOutputFilenames() {
            var filenames = codegen.supportingFiles().stream()
                .map(sf -> sf.getDestinationFilename()).toList();

            assertTrue(filenames.stream().anyMatch(f -> f.contains("HttpMethod")));
            assertTrue(filenames.stream().anyMatch(f -> f.contains("RouteTree")));
            assertTrue(filenames.stream().anyMatch(f -> f.contains("ApiCefRequestHandler")));
            assertTrue(filenames.stream().anyMatch(f -> f.contains("ApiException")));
            assertTrue(filenames.stream().anyMatch(f -> f.contains("ParameterValidator")));
            assertTrue(filenames.stream().anyMatch(f -> f.contains("RequestInterceptor")));
            assertTrue(filenames.stream().anyMatch(f -> f.contains("ContentTypeResolver")));
        }

        @Test void modelNamingSuffix() {
            codegen.additionalProperties().put("modelSuffix", "Dto");
            codegen.processOpts();
            assertEquals("Dto", codegen.getModelNameSuffix());
        }

        @Test void modelNamingPrefix() {
            codegen.additionalProperties().put("modelPrefix", "Api");
            codegen.processOpts();
            assertEquals("Api", codegen.getModelNamePrefix());
        }
    }

    // ── API filename routing ────────────────────────────────────────────

    @Nested
    class ApiFilename {

        @Test void serviceTemplate() {
            var filename = codegen.apiFilename("apiService.mustache", "Tasks");
            assertTrue(filename.contains("service"), "Should route to service subdir");
            assertTrue(filename.contains("Service.java"));
        }

        @Test void otherTemplate() {
            var filename = codegen.apiFilename("other.mustache", "Tasks");
            assertNotNull(filename);
        }
    }

    // ── Parameter constraint extraction ─────────────────────────────────

    @Nested
    class ParameterExtraction {

        @Nested
        class StringConstraints {

            @Test void minMaxLength() {
                var result = extractParam(stringSchema(s -> { s.setMinLength(1); s.setMaxLength(200); }));
                assertEquals(1, result.vendorExtensions.get("x-min-length"));
                assertEquals(200, result.vendorExtensions.get("x-max-length"));
                assertTrue((Boolean) result.vendorExtensions.get("x-has-validation"));
            }

            @Test void pattern() {
                var result = extractParam(stringSchema(s -> s.setPattern("^[A-Z]{3}$")));
                assertEquals("^[A-Z]{3}$", result.vendorExtensions.get("x-pattern"));
            }

            @Test void enumValues() {
                var result = extractParam(stringSchema(s -> s.setEnum(List.of("pending", "completed"))));
                assertTrue((Boolean) result.vendorExtensions.get("x-has-enum-values"));
                var enumStr = (String) result.vendorExtensions.get("x-enum-values-string");
                assertTrue(enumStr.contains("\"pending\""));
            }

            @Test void singleEnum() {
                var result = extractParam(stringSchema(s -> s.setEnum(List.of("ONLY"))));
                assertEquals("\"ONLY\"", result.vendorExtensions.get("x-enum-values-string"));
            }

            @Test void emptyEnum() {
                var result = extractParam(stringSchema(s -> s.setEnum(Collections.emptyList())));
                assertFalse(result.vendorExtensions.containsKey("x-has-enum-values"));
            }

            @Test void dateFormat() {
                var result = extractParam(stringSchema(s -> s.setFormat("date")));
                if (result.isString) {
                    assertTrue((Boolean) result.vendorExtensions.get("x-is-date"));
                    assertEquals("java.time.LocalDate", result.vendorExtensions.get("x-java-type"));
                }
            }

            @Test void dateTimeFormat() {
                var result = extractParam(stringSchema(s -> s.setFormat("date-time")));
                if (result.isString) {
                    assertTrue((Boolean) result.vendorExtensions.get("x-is-date-time"));
                }
            }

            @Test void emailFormat() {
                var result = extractParam(stringSchema(s -> s.setFormat("email")));
                assertEquals("email", result.vendorExtensions.get("x-format"));
            }

            @Test void uuidFormat() {
                var result = extractParam(stringSchema(s -> s.setFormat("uuid")));
                assertEquals("uuid", result.vendorExtensions.get("x-format"));
            }

            @Test void allCombined() {
                var result = extractParam(stringSchema(s -> {
                    s.setMinLength(5); s.setMaxLength(50);
                    s.setPattern("^[a-z]+$"); s.setFormat("email");
                    s.setEnum(List.of("a", "b"));
                }));
                assertEquals(5, result.vendorExtensions.get("x-min-length"));
                assertEquals(50, result.vendorExtensions.get("x-max-length"));
                assertNotNull(result.vendorExtensions.get("x-pattern"));
                assertEquals("email", result.vendorExtensions.get("x-format"));
                assertTrue((Boolean) result.vendorExtensions.get("x-has-enum-values"));
            }
        }

        @Nested
        class NumericConstraints {

            @Test void minMax() {
                var result = extractParam(intSchema(s -> {
                    s.setMinimum(new BigDecimal("1")); s.setMaximum(new BigDecimal("1000"));
                }));
                assertEquals(new BigDecimal("1"), result.vendorExtensions.get("x-minimum"));
                assertEquals(new BigDecimal("1000"), result.vendorExtensions.get("x-maximum"));
            }

            @Test void exclusiveBounds() {
                var result = extractParam(intSchema(s -> {
                    s.setMinimum(new BigDecimal("0")); s.setExclusiveMinimum(true);
                    s.setMaximum(new BigDecimal("100")); s.setExclusiveMaximum(true);
                }));
                assertTrue((Boolean) result.vendorExtensions.get("x-exclusive-minimum"));
                assertTrue((Boolean) result.vendorExtensions.get("x-exclusive-maximum"));
            }

            @Test void multipleOf() {
                var result = extractParam(intSchema(s -> s.setMultipleOf(new BigDecimal("5"))));
                assertEquals(new BigDecimal("5"), result.vendorExtensions.get("x-multiple-of"));
            }
        }

        @Nested
        class ArrayConstraints {

            @Test void itemsConstraints() {
                var schema = new ArraySchema();
                schema.setMinItems(1); schema.setMaxItems(10); schema.setUniqueItems(true);
                var itemSchema = new StringSchema();
                itemSchema.setEnum(List.of("java", "kotlin"));
                schema.setItems(itemSchema);

                var result = extractParam(schema);
                assertEquals(1, result.vendorExtensions.get("x-min-items"));
                assertEquals(10, result.vendorExtensions.get("x-max-items"));
                assertTrue((Boolean) result.vendorExtensions.get("x-unique-items"));
                assertTrue(result.vendorExtensions.containsKey("x-item-enum-values"));
            }
        }

        @Nested
        class Misc {

            @Test void nullable() {
                var result = extractParam(stringSchema(s -> s.setNullable(true)));
                assertTrue((Boolean) result.vendorExtensions.get("x-nullable"));
            }

            @Test void required() {
                var param = new Parameter();
                param.setName("id"); param.setRequired(true); param.setSchema(new StringSchema());
                var result = codegen.fromParameter(param, new HashSet<>());
                assertTrue((Boolean) result.vendorExtensions.get("x-has-validation"));
            }

            @Test void noSchema() {
                var param = new Parameter();
                param.setName("test"); param.setSchema(null);
                var result = codegen.fromParameter(param, new HashSet<>());
                assertFalse(result.vendorExtensions.containsKey("x-has-validation"));
            }

            @Test void noConstraints() {
                var param = new Parameter();
                param.setName("simple"); param.setRequired(false); param.setSchema(new StringSchema());
                var result = codegen.fromParameter(param, new HashSet<>());
                assertFalse((Boolean) result.vendorExtensions.getOrDefault("x-has-validation", false));
            }
        }

        // helpers
        private CodegenParameter extractParam(Schema<?> schema) {
            var param = new Parameter();
            param.setName("test"); param.setSchema(schema);
            return codegen.fromParameter(param, new HashSet<>());
        }

        private StringSchema stringSchema(java.util.function.Consumer<StringSchema> config) {
            var s = new StringSchema(); config.accept(s); return s;
        }

        private IntegerSchema intSchema(java.util.function.Consumer<IntegerSchema> config) {
            var s = new IntegerSchema(); config.accept(s); return s;
        }
    }

    // ── Model post-processing ───────────────────────────────────────────

    @Nested
    class ModelPostProcessing {

        @Test void removesSwaggerImports() {
            var model = new CodegenModel();
            model.imports = new HashSet<>(Set.of(
                "io.swagger.annotations.ApiModel",
                "org.openapitools.jackson.nullable.JsonNullable",
                "java.util.List"
            ));
            codegen.postProcessModelProperty(model, new CodegenProperty());

            assertFalse(model.imports.contains("io.swagger.annotations.ApiModel"));
            assertFalse(model.imports.contains("org.openapitools.jackson.nullable.JsonNullable"));
            assertTrue(model.imports.contains("java.util.List"));
        }

        @Test void handlesNullImports() {
            var model = new CodegenModel();
            model.imports = null;
            assertDoesNotThrow(() -> codegen.postProcessModelProperty(model, new CodegenProperty()));
        }

        @Test void handlesEmptyImports() {
            var model = new CodegenModel();
            model.imports = new HashSet<>();
            codegen.postProcessModelProperty(model, new CodegenProperty());
            assertTrue(model.imports.isEmpty());
        }

        @Test void enumFieldProcessing() {
            var model = enumModel("ACTIVE", "INACTIVE");
            model.vendorExtensions.put("x-enum-field-priority", Arrays.asList(1, 2));
            model.vendorExtensions.put("x-enum-field-displayName", Arrays.asList("Active", "Inactive"));

            var result = codegen.postProcessModels(wrapModel(model));

            assertNotNull(result);
            assertTrue(model.vendorExtensions.containsKey("enumFields"));
            assertTrue((Boolean) model.vendorExtensions.get("hasEnumFields"));
        }

        @Test void enumWithMultipleFieldTypes() {
            var model = enumModel("ACTIVE", "INACTIVE");
            model.vendorExtensions.put("x-enum-field-priority", Arrays.asList(1, 2));
            model.vendorExtensions.put("x-enum-field-displayName", Arrays.asList("Active", "Inactive"));
            model.vendorExtensions.put("x-enum-field-enabled", Arrays.asList(true, false));
            model.vendorExtensions.put("x-enum-field-weight", Arrays.asList(1.5, 0.5));
            model.vendorExtensions.put("x-enum-field-code", Arrays.asList(100L, 200L));

            codegen.postProcessModels(wrapModel(model));

            assertTrue(model.vendorExtensions.containsKey("enumFields"));
            assertTrue((Boolean) model.vendorExtensions.get("hasEnumFields"));
        }
    }

    // ── Server URL processing ───────────────────────────────────────────

    @Nested
    class ServerUrls {

        @Test void extractsServerUrls() {
            var openAPI = new OpenAPI();
            var s1 = new Server(); s1.setUrl("http://localhost:8080");
            var s2 = new Server(); s2.setUrl("https://api.example.com");
            openAPI.setServers(List.of(s1, s2));
            codegen.setOpenAPI(openAPI);

            var result = codegen.postProcessSupportingFileData(new HashMap<>());

            assertTrue((Boolean) result.get("hasServers"));
            @SuppressWarnings("unchecked")
            var urls = (List<String>) result.get("serverUrls");
            assertEquals(2, urls.size());
            assertTrue(urls.contains("http://localhost:8080"));
        }

        @Test void emptyServers() {
            var openAPI = new OpenAPI();
            openAPI.setServers(Collections.emptyList());
            codegen.setOpenAPI(openAPI);

            var result = codegen.postProcessSupportingFileData(new HashMap<>());
            assertFalse(result.containsKey("hasServers"));
        }

        @Test void nullServerUrl() {
            var openAPI = new OpenAPI();
            var s = new Server(); s.setUrl(null);
            openAPI.setServers(List.of(s));
            codegen.setOpenAPI(openAPI);

            var result = codegen.postProcessSupportingFileData(new HashMap<>());
            assertFalse(result.containsKey("hasServers"));
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private CodegenModel enumModel(String... values) {
        var model = new CodegenModel();
        model.isEnum = true;
        model.imports = new HashSet<>();
        model.allowableValues = new HashMap<>();
        model.allowableValues.put("values", Arrays.asList(values));

        var enumVars = new ArrayList<Map<String, Object>>();
        for (var v : values) {
            enumVars.add(new HashMap<>(Map.of("name", v, "value", "\"" + v + "\"")));
        }
        model.allowableValues.put("enumVars", enumVars);
        model.vendorExtensions = new HashMap<>();
        return model;
    }

    private ModelsMap wrapModel(CodegenModel model) {
        var modelsMap = new ModelsMap();
        var modelMap = new ModelMap();
        modelMap.setModel(model);
        modelsMap.setModels(List.of(modelMap));
        return modelsMap;
    }
}
