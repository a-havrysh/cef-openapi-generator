package io.github.cef.codegen;

import io.github.cef.codegen.config.FileSpec;
import io.github.cef.codegen.processing.EnumFieldProcessor;
import io.github.cef.codegen.processing.ImportFilter;
import io.github.cef.codegen.processing.TypeConverter;
import io.swagger.v3.oas.models.media.Schema;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kotlin code generator for CEF OpenAPI specifications.
 * Produces idiomatic Kotlin: data class, by lazy, reified, Unit, fun interface.
 */
public class CefKotlinCodegen extends CefCodegen {

    private static final String GENERATOR_NAME = "cef-kotlin";
    private static final String GENERATOR_HELP =
        "Generates idiomatic Kotlin code for CEF-based OpenAPI APIs";
    private static final String TEMPLATE_DIR = "cef-kotlin";
    private static final String KOTLIN_SOURCE_FOLDER = "src/main/kotlin";

    // Vendor extension keys
    private static final String VE_ENUM_FIELDS = EnumFieldProcessor.ENUM_FIELDS_KEY;
    private static final String VE_FIELD_TYPE = "type";

    // Kotlin identifier escaping
    private static final String DOLLAR = "$";
    private static final String ESCAPED_DOLLAR = "\\$";
    private static final String BACKTICK = "`";
    private static final String MOCK_SERVICE_FILTER = "mockService";

    public CefKotlinCodegen() {
        super();
        embeddedTemplateDir = templateDir = TEMPLATE_DIR;
        sourceFolder = KOTLIN_SOURCE_FOLDER;
        configureKotlinTypes();
    }

    @Override
    public String getName() {
        return GENERATOR_NAME;
    }

    @Override
    public String getHelp() {
        return GENERATOR_HELP;
    }

    // ── Type system 

    private void configureKotlinTypes() {
        typeMapping.put("integer", "Int");
        typeMapping.put("long", "Long");
        typeMapping.put("float", "Float");
        typeMapping.put("double", "Double");
        typeMapping.put("boolean", "Boolean");
        typeMapping.put("string", "String");
        typeMapping.put("byte", "Byte");
        typeMapping.put("short", "Short");
        typeMapping.put("char", "Char");
        typeMapping.put("object", "Any");
        typeMapping.put("AnyType", "Any");
        typeMapping.put("Void", "Unit");
        typeMapping.put("void", "Unit");
        typeMapping.put("file", "ByteArray");
        typeMapping.put("binary", "ByteArray");
        typeMapping.put("array", "List");
        typeMapping.put("list", "List");
        typeMapping.put("map", "Map");
        typeMapping.put("set", "Set");

        importMapping.clear();

        languageSpecificPrimitives.addAll(Arrays.asList(
            "Int", "Long", "Float", "Double", "Boolean", "String", "Any",
            "List", "Map", "Set", "ByteArray", "Byte", "Short", "Char", "Unit"
        ));
    }

    @Override
    public String getTypeDeclaration(Schema p) {
        return TypeConverter.kotlinify(super.getTypeDeclaration(p));
    }

    @Override
    public String toDefaultValue(Schema schema) {
        return TypeConverter.kotlinifyDefaultValue(
            super.toDefaultValue(schema));
    }

    // ── Template configuration 

    @Override
    public void processOpts() {
        super.processOpts();

        modelTemplateFiles.clear();
        modelTemplateFiles.put(MODEL_TEMPLATE, KOTLIN_EXT);

        apiTemplateFiles.clear();
        apiTemplateFiles.put(
            API_TEMPLATE_PREFIX + FileSpec.API_SERVICE.getTemplateName(),
            FileSpec.API_SERVICE.kotlinFileName());

        modelDocTemplateFiles.clear();
        modelDocTemplateFiles.put(MODEL_DOC_TEMPLATE, MARKDOWN_EXT);
        apiDocTemplateFiles.clear();
        apiDocTemplateFiles.put(API_DOC_TEMPLATE, MARKDOWN_EXT);

        modelTestTemplateFiles.clear();
        modelTestTemplateFiles.put(MODEL_TEST_TEMPLATE, KOTLIN_EXT);
        apiTestTemplateFiles.clear();
        apiTestTemplateFiles.put(API_TEST_TEMPLATE, KOTLIN_EXT);

        convertSupportingFilesToKotlin();
    }

    private void convertSupportingFilesToKotlin() {
        List<SupportingFile> kotlinFiles = new ArrayList<>();
        for (var file : supportingFiles) {
            var template = file.getTemplateFile();
            if (template != null && template.contains(MOCK_SERVICE_FILTER)) continue;

            var dest = file.getDestinationFilename();
            if (dest.endsWith(JAVA_EXT)) {
                var ktDest = dest.replace(JAVA_EXT, KOTLIN_EXT);
                var ktFile = new SupportingFile(
                    template, file.getFolder(), ktDest);
                if (!file.isCanOverwrite()) ktFile.doNotOverwrite();
                kotlinFiles.add(ktFile);
            } else {
                kotlinFiles.add(file);
            }
        }
        supportingFiles.clear();
        supportingFiles.addAll(kotlinFiles);
    }

    @Override
    public String toModelFilename(String name) {
        return super.toModelFilename(name).replace(JAVA_EXT, KOTLIN_EXT);
    }

    @Override
    public String toApiFilename(String name) {
        return super.toApiFilename(name).replace(JAVA_EXT, KOTLIN_EXT);
    }

    // ── Model post-processing 

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);
        escapeKotlinIdentifiers(property);
        kotlinifyProperty(property);
        ImportFilter.cleanupKotlinImports(model);
    }

    @Override
    public ModelsMap postProcessModels(ModelsMap objs) {
        var result = super.postProcessModels(objs);

        for (var modelMapObj : result.getModels()) {
            var model = ((ModelMap) modelMapObj).getModel();
            if (model == null) continue;

            if (model.vars != null) {
                model.vars.forEach(this::kotlinifyProperty);
            }
            if (model.imports != null) {
                model.imports.removeIf(imp ->
                    ImportFilter.shouldFilterForKotlin(
                        imp, modelPackage()));
            }

            kotlinifyEnumFieldTypes(model);
        }

        return result;
    }

    // ── Kotlin-specific transformations 

    private void escapeKotlinIdentifiers(CodegenProperty property) {
        if (property.baseName != null && property.baseName.contains(DOLLAR)) {
            property.baseName = property.baseName.replace(DOLLAR, ESCAPED_DOLLAR);
        }
        if (property.name != null && property.name.contains(DOLLAR)) {
            property.name = BACKTICK + property.name + BACKTICK;
        }
    }

    private void kotlinifyProperty(CodegenProperty property) {
        property.dataType = TypeConverter.kotlinify(property.dataType);
        property.datatypeWithEnum = TypeConverter.kotlinify(property.datatypeWithEnum);
        property.baseType = TypeConverter.kotlinify(property.baseType);
        property.defaultValue = TypeConverter.kotlinifyDefaultValue(property.defaultValue);
    }

    /** Converts enum field types from Java (Integer) to Kotlin (Int). */
    @SuppressWarnings("unchecked")
    private void kotlinifyEnumFieldTypes(CodegenModel model) {
        if (!model.vendorExtensions.containsKey(VE_ENUM_FIELDS)) return;

        var fields = (List<Map<String, String>>) model.vendorExtensions.get(VE_ENUM_FIELDS);
        var kotlinified = fields.stream()
            .map(field -> {
                var mutable = new HashMap<>(field);
                var type = mutable.get(VE_FIELD_TYPE);
                if (type != null) mutable.put(VE_FIELD_TYPE, TypeConverter.kotlinify(type));
                return (Map<String, String>) mutable;
            })
            .toList();
        model.vendorExtensions.put(VE_ENUM_FIELDS, kotlinified);
    }
}
