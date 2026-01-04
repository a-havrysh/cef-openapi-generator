package io.github.cef.codegen;

import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.languages.AbstractJavaCodegen;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CefCodegen extends AbstractJavaCodegen implements CodegenConfig {

    public CefCodegen() {
        super();
        this.hideGenerationTimestamp = true;

        // Set embedded template directory for JAR loading
        embeddedTemplateDir = templateDir = "cef";

        // Disable swagger annotations
        additionalProperties.put("useSwaggerAnnotations", false);
        additionalProperties.put("swaggerAnnotations", false);
    }

    @Override
    public String getName() {
        return "cef";
    }

    @Override
    public String getHelp() {
        return "CEF generator for BPMN Editor";
    }

    @Override
    public void processOpts() {
        super.processOpts();

        // Remove swagger from default imports
        if (defaultIncludes != null) {
            defaultIncludes.remove("io.swagger.annotations.ApiModel");
            defaultIncludes.remove("io.swagger.annotations.ApiModelProperty");
        }

        // Add stub swagger annotations for compilation
        String swaggerPackage = (sourceFolder + File.separator + "io" + File.separator + "swagger" + File.separator + "annotations").replace(".", File.separator);
        supportingFiles.add(new SupportingFile("apiModel.mustache", swaggerPackage, "ApiModel.java"));
        supportingFiles.add(new SupportingFile("apiModelProperty.mustache", swaggerPackage, "ApiModelProperty.java"));

        String swaggerV3Package = (sourceFolder + File.separator + "io" + File.separator + "swagger" + File.separator + "v3" + File.separator + "oas" + File.separator + "annotations" + File.separator + "media").replace(".", File.separator);
        supportingFiles.add(new SupportingFile("schemaAnnotation.mustache", swaggerV3Package, "Schema.java"));

        String jacksonPackage = (sourceFolder + File.separator + "org" + File.separator + "openapitools" + File.separator + "jackson" + File.separator + "nullable").replace(".", File.separator);
        supportingFiles.add(new SupportingFile("jsonNullable.mustache", jacksonPackage, "JsonNullable.java"));

        // Clear default API templates and add only services
        apiTemplateFiles.clear();
        apiTemplateFiles.put("apiService.mustache", "Service.java");

        // Add protocol layer
        var protocolPackage = apiPackage + ".protocol";
        var protocolFolder = (sourceFolder + File.separator + protocolPackage).replace(".", File.separator);
        supportingFiles.add(new SupportingFile("httpMethod.mustache", protocolFolder, "HttpMethod.java"));
        supportingFiles.add(new SupportingFile("apiRequest.mustache", protocolFolder, "ApiRequest.java"));
        supportingFiles.add(new SupportingFile("apiResponse.mustache", protocolFolder, "ApiResponse.java"));

        // Add routing layer (Trie-based)
        var routingPackage = apiPackage + ".routing";
        var routingFolder = (sourceFolder + File.separator + routingPackage).replace(".", File.separator);
        supportingFiles.add(new SupportingFile("routeTree.mustache", routingFolder, "RouteTree.java"));
        supportingFiles.add(new SupportingFile("routeNode.mustache", routingFolder, "RouteNode.java"));

        // Add CEF integration layer
        var cefPackage = apiPackage + ".cef";
        var cefFolder = (sourceFolder + File.separator + cefPackage).replace(".", File.separator);
        supportingFiles.add(new SupportingFile("apiCefRequestHandler.mustache", cefFolder, "ApiCefRequestHandler.java"));
        supportingFiles.add(new SupportingFile("apiCefRequestHandlerBuilder.mustache", cefFolder, "ApiCefRequestHandlerBuilder.java"));
        supportingFiles.add(new SupportingFile("apiResourceRequestHandler.mustache", cefFolder, "ApiResourceRequestHandler.java"));
        supportingFiles.add(new SupportingFile("apiResponseHandler.mustache", cefFolder, "ApiResponseHandler.java"));

        // Add utility layer
        var utilPackage = apiPackage + ".util";
        var utilFolder = (sourceFolder + File.separator + utilPackage).replace(".", File.separator);
        supportingFiles.add(new SupportingFile("contentTypeResolver.mustache", utilFolder, "ContentTypeResolver.java"));

        // Add exception layer
        var exceptionPackage = apiPackage + ".exception";
        var exceptionFolder = (sourceFolder + File.separator + exceptionPackage).replace(".", File.separator);
        supportingFiles.add(new SupportingFile("apiException.mustache", exceptionFolder, "ApiException.java"));
        supportingFiles.add(new SupportingFile("badRequestException.mustache", exceptionFolder, "BadRequestException.java"));
        supportingFiles.add(new SupportingFile("notFoundException.mustache", exceptionFolder, "NotFoundException.java"));
        supportingFiles.add(new SupportingFile("internalServerErrorException.mustache", exceptionFolder, "InternalServerErrorException.java"));
        supportingFiles.add(new SupportingFile("notImplementedException.mustache", exceptionFolder, "NotImplementedException.java"));
    }

    @Override
    public Map<String, org.openapitools.codegen.model.ModelsMap> postProcessAllModels(Map<String, org.openapitools.codegen.model.ModelsMap> objs) {
        objs = super.postProcessAllModels(objs);

        // Remove swagger and jackson-nullable imports from all models
        for (org.openapitools.codegen.model.ModelsMap modelsMap : objs.values()) {
            for (Object modelMapObj : modelsMap.getModels()) {
                org.openapitools.codegen.model.ModelMap modelMap = (org.openapitools.codegen.model.ModelMap) modelMapObj;
                CodegenModel model = modelMap.getModel();
                if (model != null && model.imports != null) {
                    model.imports.removeIf(imp ->
                        imp.contains("swagger") ||
                        imp.contains("JsonNullable") ||
                        imp.contains("oas.annotations")
                    );
                }
            }
        }

        return objs;
    }

    @Override
    public CodegenModel fromModel(String name, io.swagger.v3.oas.models.media.Schema schema) {
        CodegenModel model = super.fromModel(name, schema);

        // Remove unwanted imports before template rendering
        if (model.imports != null) {
            model.imports.removeIf(imp ->
                imp.contains("swagger") ||
                imp.contains("JsonNullable") ||
                imp.contains("oas.annotations")
            );
        }

        return model;
    }

    @Override
    public org.openapitools.codegen.model.ModelsMap postProcessModels(org.openapitools.codegen.model.ModelsMap objs) {
        objs = super.postProcessModels(objs);

        for (Object modelMapObj : objs.getModels()) {
            org.openapitools.codegen.model.ModelMap modelMap = (org.openapitools.codegen.model.ModelMap) modelMapObj;
            CodegenModel model = modelMap.getModel();

            if (model != null && model.isEnum && model.allowableValues != null) {
                processEnumVendorExtensions(model);
            }
        }

        return objs;
    }

    private void processEnumVendorExtensions(CodegenModel model) {
        Map<String, Object> allowableValues = model.allowableValues;
        List<Map<String, Object>> enumVars = (List<Map<String, Object>>) allowableValues.get("enumVars");

        if (enumVars == null || enumVars.isEmpty()) {
            return;
        }

        // Universal enum fields - collect all x-enum-field-* extensions
        Map<String, List<?>> fields = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : model.vendorExtensions.entrySet()) {
            if (entry.getKey().startsWith("x-enum-field-")) {
                String fieldName = entry.getKey().substring(13);
                if (entry.getValue() instanceof List) {
                    fields.put(fieldName, (List<?>) entry.getValue());
                }
            }
        }

        if (!fields.isEmpty()) {
            List<Map<String, String>> fieldMeta = new java.util.ArrayList<>();
            for (Map.Entry<String, List<?>> field : fields.entrySet()) {
                String name = field.getKey();
                List<?> values = field.getValue();

                // Auto-detect type from first value
                String fieldType = detectFieldType(values);

                Map<String, String> meta = new java.util.HashMap<>();
                meta.put("name", name);
                meta.put("capitalizedName", name.substring(0, 1).toUpperCase() + name.substring(1));
                meta.put("type", fieldType);
                fieldMeta.add(meta);
            }

            model.vendorExtensions.put("enumFields", fieldMeta);

            // Populate enum values with field data
            for (int i = 0; i < enumVars.size(); i++) {
                Map<String, Object> enumVar = enumVars.get(i);
                List<String> fieldValues = new java.util.ArrayList<>();

                // Collect values in the same order as fieldMeta
                for (Map<String, String> meta : fieldMeta) {
                    String fieldName = meta.get("name");
                    List<?> fieldValuesList = fields.get(fieldName);
                    if (fieldValuesList != null && i < fieldValuesList.size()) {
                        Object value = fieldValuesList.get(i);
                        String formattedValue = formatValueForJava(value, meta.get("type"));
                        fieldValues.add(formattedValue);
                        enumVar.put(fieldName, value);
                    }
                }

                // Create constructor args string for template
                if (!fieldValues.isEmpty()) {
                    String constructorArgs = String.join(", ", fieldValues);
                    enumVar.put("constructorArgs", constructorArgs);
                }
            }
        }
    }

    /**
     * Formats a value for Java code generation based on its type.
     */
    private String formatValueForJava(Object value, String type) {
        if (value == null) {
            return "null";
        }

        if ("String".equals(type)) {
            return "\"" + value.toString().replace("\"", "\\\"") + "\"";
        } else if ("Integer".equals(type) || "Long".equals(type) ||
                   "Double".equals(type) || "Float".equals(type)) {
            return value.toString();
        } else if ("Boolean".equals(type)) {
            return value.toString();
        } else if (type.startsWith("java.math.")) {
            return "new " + type + "(\"" + value.toString() + "\")";
        } else {
            // Default: treat as string
            return "\"" + value.toString().replace("\"", "\\\"") + "\"";
        }
    }

    /**
     * Detects Java type from the first element of the list.
     * Supports all primitive types and their wrappers.
     */
    private String detectFieldType(List<?> values) {
        if (values == null || values.isEmpty()) {
            return "String";
        }

        Object firstValue = values.get(0);
        if (firstValue == null) {
            return "String";
        }

        if (firstValue instanceof Integer) {
            return "Integer";
        } else if (firstValue instanceof Long) {
            return "Long";
        } else if (firstValue instanceof Double) {
            return "Double";
        } else if (firstValue instanceof Float) {
            return "Float";
        } else if (firstValue instanceof Boolean) {
            return "Boolean";
        } else if (firstValue instanceof java.math.BigDecimal) {
            return "java.math.BigDecimal";
        } else if (firstValue instanceof java.math.BigInteger) {
            return "java.math.BigInteger";
        } else {
            return "String";
        }
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        var folder = apiFileFolder();
        var filename = toApiFilename(tag);

        if ("apiService.mustache".equals(templateName)) {
            return folder + File.separator + "service" + File.separator + filename + "Service.java";
        }

        return super.apiFilename(templateName, tag);
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> bundle) {
        // The bundle already contains apiInfo with all APIs from the OpenAPI spec
        // We just need to ensure it's available for the registrator.mustache template
        // The DefaultGenerator automatically populates this data structure
        return super.postProcessSupportingFileData(bundle);
    }
}
