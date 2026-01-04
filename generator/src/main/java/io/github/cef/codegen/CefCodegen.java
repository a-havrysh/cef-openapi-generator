package io.github.cef.codegen;

import io.swagger.v3.oas.models.servers.Server;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.languages.AbstractJavaCodegen;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static io.github.cef.codegen.CefCodegen.FileSpec.*;
import static io.github.cef.codegen.CefCodegen.PackageSuffix.*;
import static io.github.cef.codegen.CefCodegen.ImportFilter.*;
import static io.github.cef.codegen.CefCodegen.TypeName.*;

/**
 * CEF (Chromium Embedded Framework) code generator for OpenAPI specifications.
 * <p>
 * This generator creates a complete CEF-based API infrastructure including:
 * <ul>
 *   <li>Service interfaces with type-safe method signatures</li>
 *   <li>Protocol layer (HTTP methods, requests, responses)</li>
 *   <li>Routing layer (route tree and nodes for URL matching)</li>
 *   <li>CEF integration layer (request handlers, response handlers)</li>
 *   <li>Utility layer (content type resolution)</li>
 *   <li>Exception layer (API-specific exceptions)</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * OpenAPI openAPI = new OpenAPIV3Parser().read("openapi.yaml");
 * CefCodegen codegen = new CefCodegen();
 * codegen.setOutputDir("/path/to/output");
 * codegen.processOpts();
 * }</pre>
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Clean model generation without unnecessary annotations</li>
 *   <li>Advanced enum support with custom fields via vendor extensions</li>
 *   <li>Server URL tracking for request filtering</li>
 *   <li>Modular architecture with separate layers</li>
 * </ul>
 *
 * @see AbstractJavaCodegen
 */
public class CefCodegen extends AbstractJavaCodegen {

    /**
     * File specification combining template name and generated file name.
     */
    @Getter
    @RequiredArgsConstructor
    public enum FileSpec {
        API_SERVICE("apiService.mustache", "Service.java"),
        HTTP_METHOD("httpMethod.mustache", "HttpMethod.java"),
        API_REQUEST("apiRequest.mustache", "ApiRequest.java"),
        API_RESPONSE("apiResponse.mustache", "ApiResponse.java"),
        ROUTE_TREE("routeTree.mustache", "RouteTree.java"),
        ROUTE_NODE("routeNode.mustache", "RouteNode.java"),
        API_CEF_REQUEST_HANDLER("apiCefRequestHandler.mustache", "ApiCefRequestHandler.java"),
        API_CEF_REQUEST_HANDLER_BUILDER("apiCefRequestHandlerBuilder.mustache", "ApiCefRequestHandlerBuilder.java"),
        API_RESOURCE_REQUEST_HANDLER("apiResourceRequestHandler.mustache", "ApiResourceRequestHandler.java"),
        API_RESPONSE_HANDLER("apiResponseHandler.mustache", "ApiResponseHandler.java"),
        CONTENT_TYPE_RESOLVER("contentTypeResolver.mustache", "ContentTypeResolver.java"),
        API_EXCEPTION("apiException.mustache", "ApiException.java"),
        BAD_REQUEST_EXCEPTION("badRequestException.mustache", "BadRequestException.java"),
        NOT_FOUND_EXCEPTION("notFoundException.mustache", "NotFoundException.java"),
        INTERNAL_SERVER_ERROR_EXCEPTION("internalServerErrorException.mustache", "InternalServerErrorException.java"),
        NOT_IMPLEMENTED_EXCEPTION("notImplementedException.mustache", "NotImplementedException.java");

        private final String templateName;
        private final String fileName;
    }

    /**
     * Package name suffixes for different architectural layers.
     */
    @Getter
    @RequiredArgsConstructor
    public enum PackageSuffix {
        PROTOCOL(".protocol"),
        ROUTING(".routing"),
        CEF(".cef"),
        UTIL(".util"),
        EXCEPTION(".exception");

        private final String suffix;
    }

    /**
     * Keywords used to filter unwanted imports from generated models.
     */
    @Getter
    @RequiredArgsConstructor
    public enum ImportFilter {
        SWAGGER("swagger"),
        JSON_NULLABLE("JsonNullable"),
        OAS_ANNOTATIONS("oas.annotations"),
        API_MODEL("ApiModel"),
        API_MODEL_PROPERTY("ApiModelProperty"),
        SCHEMA("Schema");

        private final String keyword;
    }

    /**
     * Java type names for field type detection and formatting.
     */
    @Getter
    @RequiredArgsConstructor
    public enum TypeName {
        STRING("String"),
        INTEGER("Integer"),
        LONG("Long"),
        DOUBLE("Double"),
        FLOAT("Float"),
        BOOLEAN("Boolean"),
        BIG_DECIMAL("java.math.BigDecimal"),
        BIG_INTEGER("java.math.BigInteger");

        private final String typeName;
    }

    // Generator constants
    private static final String GENERATOR_NAME = "cef";
    private static final String GENERATOR_HELP = "CEF generator for BPMN Editor";
    private static final String TEMPLATE_DIR = "cef";
    private static final String SERVICE_SUBDIR = "service";

    // Enum processing constants
    private static final String ENUM_FIELD_PREFIX = "x-enum-field-";
    private static final String ENUM_VARS_KEY = "enumVars";
    private static final String ENUM_FIELDS_KEY = "enumFields";
    private static final String CONSTRUCTOR_ARGS_KEY = "constructorArgs";
    private static final String FIELD_NAME_KEY = "name";
    private static final String FIELD_CAPITALIZED_NAME_KEY = "capitalizedName";
    private static final String FIELD_TYPE_KEY = "type";
    private static final int ENUM_FIELD_PREFIX_LENGTH = 13;

    // Type detection constants
    private static final String TYPE_JAVA_MATH_PREFIX = "java.math.";

    // Bundle keys
    private static final String BUNDLE_SERVER_URLS = "serverUrls";
    private static final String BUNDLE_HAS_SERVERS = "hasServers";

    /**
     * Constructs a new CefCodegen instance with CEF-specific configuration.
     * <p>
     * Initializes the generator with:
     * <ul>
     *   <li>Hidden generation timestamp</li>
     *   <li>Swagger2 annotation library</li>
     *   <li>OpenAPI nullable disabled</li>
     *   <li>Bean validation disabled</li>
     * </ul>
     */
    public CefCodegen() {
        super();
        this.hideGenerationTimestamp = true;
        embeddedTemplateDir = templateDir = TEMPLATE_DIR;

        this.setAnnotationLibrary(AnnotationLibrary.SWAGGER2);
        this.setOpenApiNullable(false);
        this.setUseBeanValidation(false);
    }

    /**
     * Returns the unique name identifier for this generator.
     *
     * @return generator name "cef"
     */
    @Override
    public String getName() {
        return GENERATOR_NAME;
    }

    /**
     * Returns the help description for this generator.
     *
     * @return generator help text
     */
    @Override
    public String getHelp() {
        return GENERATOR_HELP;
    }

    /**
     * Processes generator options and configures all architectural layers.
     * <p>
     * This method:
     * <ol>
     *   <li>Calls parent class option processing</li>
     *   <li>Clears default API templates</li>
     *   <li>Configures API service template</li>
     *   <li>Adds supporting files for all layers (protocol, routing, CEF, utility, exception)</li>
     * </ol>
     */
    @Override
    public void processOpts() {
        super.processOpts();

        apiTemplateFiles.clear();
        apiTemplateFiles.put(API_SERVICE.getTemplateName(), API_SERVICE.getFileName());

        addProtocolLayer();
        addRoutingLayer();
        addCefIntegrationLayer();
        addUtilityLayer();
        addExceptionLayer();
    }

    /**
     * Adds protocol layer supporting files.
     * <p>
     * Generates:
     * <ul>
     *   <li>HttpMethod - HTTP method enumeration</li>
     *   <li>ApiRequest - request wrapper with method, URL, headers, body</li>
     *   <li>ApiResponse - response wrapper with status, headers, body</li>
     * </ul>
     */
    private void addProtocolLayer() {
        var folder = buildFolderPath(apiPackage + PROTOCOL.getSuffix());
        supportingFiles.add(new SupportingFile(HTTP_METHOD.getTemplateName(), folder, HTTP_METHOD.getFileName()));
        supportingFiles.add(new SupportingFile(API_REQUEST.getTemplateName(), folder, API_REQUEST.getFileName()));
        supportingFiles.add(new SupportingFile(API_RESPONSE.getTemplateName(), folder, API_RESPONSE.getFileName()));
    }

    /**
     * Adds routing layer supporting files.
     * <p>
     * Generates:
     * <ul>
     *   <li>RouteTree - tree structure for URL pattern matching</li>
     *   <li>RouteNode - individual node in the route tree</li>
     * </ul>
     */
    private void addRoutingLayer() {
        var folder = buildFolderPath(apiPackage + ROUTING.getSuffix());
        supportingFiles.add(new SupportingFile(ROUTE_TREE.getTemplateName(), folder, ROUTE_TREE.getFileName()));
        supportingFiles.add(new SupportingFile(ROUTE_NODE.getTemplateName(), folder, ROUTE_NODE.getFileName()));
    }

    /**
     * Adds CEF integration layer supporting files.
     * <p>
     * Generates:
     * <ul>
     *   <li>ApiCefRequestHandler - main CEF request handler</li>
     *   <li>ApiCefRequestHandlerBuilder - builder for request handler configuration</li>
     *   <li>ApiResourceRequestHandler - resource-level request handler</li>
     *   <li>ApiResponseHandler - response processing handler</li>
     * </ul>
     */
    private void addCefIntegrationLayer() {
        var folder = buildFolderPath(apiPackage + CEF.getSuffix());
        supportingFiles.add(new SupportingFile(API_CEF_REQUEST_HANDLER.getTemplateName(), folder, API_CEF_REQUEST_HANDLER.getFileName()));
        supportingFiles.add(new SupportingFile(API_CEF_REQUEST_HANDLER_BUILDER.getTemplateName(), folder, API_CEF_REQUEST_HANDLER_BUILDER.getFileName()));
        supportingFiles.add(new SupportingFile(API_RESOURCE_REQUEST_HANDLER.getTemplateName(), folder, API_RESOURCE_REQUEST_HANDLER.getFileName()));
        supportingFiles.add(new SupportingFile(API_RESPONSE_HANDLER.getTemplateName(), folder, API_RESPONSE_HANDLER.getFileName()));
    }

    /**
     * Adds utility layer supporting files.
     * <p>
     * Generates:
     * <ul>
     *   <li>ContentTypeResolver - content type detection and resolution</li>
     * </ul>
     */
    private void addUtilityLayer() {
        supportingFiles.add(new SupportingFile(
                CONTENT_TYPE_RESOLVER.getTemplateName(),
                buildFolderPath(apiPackage + UTIL.getSuffix()),
                CONTENT_TYPE_RESOLVER.getFileName()
        ));
    }

    /**
     * Adds exception layer supporting files.
     * <p>
     * Generates:
     * <ul>
     *   <li>ApiException - base API exception</li>
     *   <li>BadRequestException - 400 error</li>
     *   <li>NotFoundException - 404 error</li>
     *   <li>InternalServerErrorException - 500 error</li>
     *   <li>NotImplementedException - 501 error</li>
     * </ul>
     */
    private void addExceptionLayer() {
        var folder = buildFolderPath(apiPackage + EXCEPTION.getSuffix());
        supportingFiles.add(new SupportingFile(API_EXCEPTION.getTemplateName(), folder, API_EXCEPTION.getFileName()));
        supportingFiles.add(new SupportingFile(BAD_REQUEST_EXCEPTION.getTemplateName(), folder, BAD_REQUEST_EXCEPTION.getFileName()));
        supportingFiles.add(new SupportingFile(NOT_FOUND_EXCEPTION.getTemplateName(), folder, NOT_FOUND_EXCEPTION.getFileName()));
        supportingFiles.add(new SupportingFile(INTERNAL_SERVER_ERROR_EXCEPTION.getTemplateName(), folder, INTERNAL_SERVER_ERROR_EXCEPTION.getFileName()));
        supportingFiles.add(new SupportingFile(NOT_IMPLEMENTED_EXCEPTION.getTemplateName(), folder, NOT_IMPLEMENTED_EXCEPTION.getFileName()));
    }

    /**
     * Builds folder path from package name using platform-specific separators.
     *
     * @param packageName fully qualified package name (e.g., "com.example.api.protocol")
     * @return folder path with platform-specific separators (e.g., "src/main/java/com/example/api/protocol")
     */
    private String buildFolderPath(String packageName) {
        return (sourceFolder + File.separator + packageName).replace(".", File.separator);
    }

    /**
     * Generates the API file path for a given template and tag.
     * <p>
     * For API service templates, places generated files in a "service" subdirectory.
     * For other templates, delegates to parent implementation.
     *
     * @param templateName name of the template file
     * @param tag API tag used for file naming
     * @return complete file path for the generated API file
     */
    @Override
    public String apiFilename(String templateName, String tag) {
        if (API_SERVICE.getTemplateName().equals(templateName)) {
            return apiFileFolder() + File.separator + SERVICE_SUBDIR + File.separator + toApiFilename(tag) + API_SERVICE.getFileName();
        }
        return super.apiFilename(templateName, tag);
    }

    /**
     * Post-processes model property after generation.
     * <p>
     * Calls parent implementation and then removes unwanted imports from the model.
     *
     * @param model the CodegenModel being processed
     * @param property the CodegenProperty being processed
     */
    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);
        cleanupModelImports(model);
    }

    /**
     * Updates codegen property enum configuration.
     * <p>
     * Delegates to parent implementation without additional processing.
     *
     * @param var the CodegenProperty enum to update
     */
    @Override
    public void updateCodegenPropertyEnum(CodegenProperty var) {
        super.updateCodegenPropertyEnum(var);
    }

    /**
     * Post-processes all models after generation.
     * <p>
     * Performs:
     * <ol>
     *   <li>Parent class post-processing</li>
     *   <li>Import cleanup for all models</li>
     *   <li>Vendor extension processing for enum models</li>
     * </ol>
     *
     * @param objs models map containing all generated models
     * @return processed models map
     */
    @Override
    public ModelsMap postProcessModels(ModelsMap objs) {
        var processedObjs = super.postProcessModels(objs);

        for (var modelMapObj : processedObjs.getModels()) {
            var modelMap = (ModelMap) modelMapObj;
            var model = modelMap.getModel();

            cleanupModelImports(model);

            if (model != null && model.isEnum && model.allowableValues != null) {
                processEnumVendorExtensions(model);
            }
        }

        return processedObjs;
    }

    /**
     * Post-processes supporting file data to include server URLs.
     * <p>
     * Extracts server URLs from OpenAPI specification and adds them to the bundle
     * for use in URL filtering and routing logic. This enables the generated code
     * to match incoming requests against defined server URLs.
     *
     * @param bundle the data bundle to augment with server information
     * @return augmented bundle with server URLs and hasServers flag
     */
    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> bundle) {
        var processedBundle = super.postProcessSupportingFileData(bundle);

        if (openAPI != null && openAPI.getServers() != null && !openAPI.getServers().isEmpty()) {
            var serverUrls = openAPI.getServers().stream()
                    .map(Server::getUrl)
                    .filter(url -> url != null && !url.isEmpty())
                    .toList();

            if (!serverUrls.isEmpty()) {
                processedBundle.put(BUNDLE_SERVER_URLS, serverUrls);
                processedBundle.put(BUNDLE_HAS_SERVERS, true);
            }
        }

        return processedBundle;
    }

    /**
     * Removes unwanted imports from generated models.
     * <p>
     * Filters out imports related to:
     * <ul>
     *   <li>Swagger annotations (@ApiModel, @ApiModelProperty, @Schema)</li>
     *   <li>Jackson annotations (JsonNullable)</li>
     *   <li>OpenAPI annotations (oas.annotations)</li>
     * </ul>
     * <p>
     * This keeps models clean and framework-agnostic.
     * <p>
     * <strong>DO NOT MODIFY:</strong> This method's logic must remain unchanged.
     *
     * @param model the model to clean up (may be null)
     */
    private void cleanupModelImports(CodegenModel model) {
        if (model == null || model.imports == null) {
            return;
        }

        model.imports.removeIf(imp ->
                imp == null
                        || imp.isEmpty()
                        || imp.contains(SWAGGER.getKeyword())
                        || imp.contains(JSON_NULLABLE.getKeyword())
                        || imp.contains(OAS_ANNOTATIONS.getKeyword())
                        || imp.equals(API_MODEL.getKeyword())
                        || imp.equals(API_MODEL_PROPERTY.getKeyword())
                        || imp.equals(SCHEMA.getKeyword())
        );
    }

    /**
     * Processes vendor extensions for enum models to support custom fields.
     * <p>
     * Extracts custom field definitions from vendor extensions (x-enum-field-*),
     * builds field metadata, and populates enum values with field data.
     * <p>
     * Example OpenAPI specification:
     * <pre>{@code
     * components:
     *   schemas:
     *     Status:
     *       type: string
     *       enum: [ACTIVE, INACTIVE]
     *       x-enum-field-code: [1, 0]
     *       x-enum-field-description: ["Active status", "Inactive status"]
     * }</pre>
     * <p>
     * Generates enum with constructor:
     * <pre>{@code
     * public enum Status {
     *     ACTIVE(1, "Active status"),
     *     INACTIVE(0, "Inactive status");
     *
     *     private final Integer code;
     *     private final String description;
     * }
     * }</pre>
     * <p>
     * <strong>DO NOT MODIFY:</strong> This method's logic must remain unchanged.
     *
     * @param model the enum model to process
     */
    @SuppressWarnings("unchecked")
    private void processEnumVendorExtensions(CodegenModel model) {
        var allowableValues = model.allowableValues;
        var enumVars = (List<Map<String, Object>>) allowableValues.get(ENUM_VARS_KEY);

        if (enumVars == null || enumVars.isEmpty()) {
            return;
        }

        var fields = extractEnumFields(model);
        if (!fields.isEmpty()) {
            var fieldMeta = buildFieldMetadata(fields);
            model.vendorExtensions.put(ENUM_FIELDS_KEY, fieldMeta);
            populateEnumValues(enumVars, fields, fieldMeta);
        }

        cleanupModelImports(model);
    }

    /**
     * Extracts enum field definitions from vendor extensions.
     * <p>
     * Scans vendor extensions for keys starting with "x-enum-field-" and
     * extracts the field name and values list.
     *
     * @param model the model containing vendor extensions
     * @return map of field names to their values (preserves insertion order)
     */
    private Map<String, List<?>> extractEnumFields(CodegenModel model) {
        var fields = new LinkedHashMap<String, List<?>>();
        for (var entry : model.vendorExtensions.entrySet()) {
            if (entry.getKey().startsWith(ENUM_FIELD_PREFIX)) {
                var fieldName = entry.getKey().substring(ENUM_FIELD_PREFIX_LENGTH);
                if (entry.getValue() instanceof List) {
                    fields.put(fieldName, (List<?>) entry.getValue());
                }
            }
        }
        return fields;
    }

    /**
     * Builds metadata for enum fields including name, capitalized name, and type.
     * <p>
     * For each field, creates metadata map with:
     * <ul>
     *   <li>name - original field name (e.g., "code")</li>
     *   <li>capitalizedName - capitalized for getter methods (e.g., "Code")</li>
     *   <li>type - detected Java type (e.g., "Integer", "String")</li>
     * </ul>
     *
     * @param fields map of field names to their values
     * @return list of field metadata maps
     */
    private List<Map<String, String>> buildFieldMetadata(Map<String, List<?>> fields) {
        var fieldMeta = new ArrayList<Map<String, String>>();
        for (var field : fields.entrySet()) {
            var name = field.getKey();
            var meta = new HashMap<String, String>();
            meta.put(FIELD_NAME_KEY, name);
            meta.put(FIELD_CAPITALIZED_NAME_KEY, name.substring(0, 1).toUpperCase() + name.substring(1));
            meta.put(FIELD_TYPE_KEY, detectFieldType(field.getValue()));
            fieldMeta.add(meta);
        }
        return fieldMeta;
    }

    /**
     * Populates enum variables with field data and constructor arguments.
     * <p>
     * For each enum value, builds:
     * <ul>
     *   <li>Individual field values from corresponding positions in field lists</li>
     *   <li>Constructor arguments string (comma-separated formatted values)</li>
     *   <li>Field value mappings for template access</li>
     * </ul>
     * <p>
     * Example: For enum value "ACTIVE" at index 0 with fields [1, "Active"],
     * generates: constructorArgs = "1, \"Active\""
     *
     * @param enumVars list of enum variable maps to populate
     * @param fields map of field names to their values
     * @param fieldMeta list of field metadata with types
     */
    private void populateEnumValues(List<Map<String, Object>> enumVars, Map<String, List<?>> fields, List<Map<String, String>> fieldMeta) {
        for (var i = 0; i < enumVars.size(); i++) {
            var enumVar = enumVars.get(i);
            var fieldValues = new ArrayList<String>();

            for (var meta : fieldMeta) {
                var fieldValuesList = fields.get(meta.get(FIELD_NAME_KEY));
                if (fieldValuesList != null && i < fieldValuesList.size()) {
                    var value = fieldValuesList.get(i);
                    fieldValues.add(formatValueForJava(value, meta.get(FIELD_TYPE_KEY)));
                    enumVar.put(meta.get(FIELD_NAME_KEY), value);
                }
            }

            if (!fieldValues.isEmpty()) {
                enumVar.put(CONSTRUCTOR_ARGS_KEY, String.join(", ", fieldValues));
            }
        }
    }

    /**
     * Formats a value for Java code generation based on its type.
     * <p>
     * Type-specific formatting:
     * <ul>
     *   <li>String: wraps in quotes, escapes internal quotes</li>
     *   <li>Integer, Long, Double, Float, Boolean: uses toString()</li>
     *   <li>BigDecimal, BigInteger: generates "new Type(\"value\")" constructor call</li>
     *   <li>null: returns "null" literal</li>
     *   <li>unknown types: defaults to quoted string</li>
     * </ul>
     *
     * @param value the value to format (may be null)
     * @param type the Java type name (e.g., "String", "Integer", "java.math.BigDecimal")
     * @return formatted string representation suitable for Java code
     */
    private String formatValueForJava(Object value, String type) {
        if (value == null) {
            return "null";
        }

        if (STRING.getTypeName().equals(type)) {
            return "\"" + value.toString().replace("\"", "\\\"") + "\"";
        }
        if (INTEGER.getTypeName().equals(type)
                || LONG.getTypeName().equals(type)
                || DOUBLE.getTypeName().equals(type)
                || FLOAT.getTypeName().equals(type)
                || BOOLEAN.getTypeName().equals(type)) {
            return value.toString();
        }
        if (type.startsWith(TYPE_JAVA_MATH_PREFIX)) {
            return "new " + type + "(\"" + value.toString() + "\")";
        }
        return "\"" + value.toString().replace("\"", "\\\"") + "\"";
    }

    /**
     * Detects Java type from the first non-null element in a value list.
     * <p>
     * Type detection logic:
     * <ol>
     *   <li>If list is null or empty, defaults to String</li>
     *   <li>If first element is null, defaults to String</li>
     *   <li>Uses instanceof checks to determine actual type</li>
     *   <li>Supports: Integer, Long, Double, Float, Boolean, BigDecimal, BigInteger</li>
     *   <li>Defaults to String for unknown types</li>
     * </ol>
     *
     * @param values list of values to analyze (may be null or empty)
     * @return detected Java type name (never null, defaults to "String")
     */
    private String detectFieldType(List<?> values) {
        if (values == null || values.isEmpty()) {
            return STRING.getTypeName();
        }

        var firstValue = values.get(0);
        if (firstValue == null) {
            return STRING.getTypeName();
        }
        if (firstValue instanceof Integer) {
            return INTEGER.getTypeName();
        }
        if (firstValue instanceof Long) {
            return LONG.getTypeName();
        }
        if (firstValue instanceof Double) {
            return DOUBLE.getTypeName();
        }
        if (firstValue instanceof Float) {
            return FLOAT.getTypeName();
        }
        if (firstValue instanceof Boolean) {
            return BOOLEAN.getTypeName();
        }
        if (firstValue instanceof BigDecimal) {
            return BIG_DECIMAL.getTypeName();
        }
        if (firstValue instanceof BigInteger) {
            return BIG_INTEGER.getTypeName();
        }
        return STRING.getTypeName();
    }
}
