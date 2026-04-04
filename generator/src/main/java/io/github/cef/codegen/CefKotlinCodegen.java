package io.github.cef.codegen;

import io.github.cef.codegen.config.FileSpec;
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
 *
 * Extends the Java generator to produce idiomatic Kotlin:
 * - Kotlin built-in types (Int, List, Map, Any, Unit)
 * - data class models with val properties
 * - by lazy, runCatching, expression bodies
 * - fun interface, companion object, sealed hierarchies
 */
public class CefKotlinCodegen extends CefCodegen {

    private static final String GENERATOR_NAME = "cef-kotlin";
    private static final String TEMPLATE_DIR = "cef-kotlin";

    public CefKotlinCodegen() {
        super();
        embeddedTemplateDir = templateDir = TEMPLATE_DIR;
        sourceFolder = "src/main/kotlin";
        configureKotlinTypes();
    }

    @Override
    public String getName() {
        return GENERATOR_NAME;
    }

    @Override
    public String getHelp() {
        return "Generates idiomatic Kotlin code for CEF-based OpenAPI APIs";
    }

    // ── Type system ─────────────────────────────────────────────────────

    private void configureKotlinTypes() {
        // Primitives
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

        // Collections
        typeMapping.put("array", "List");
        typeMapping.put("list", "List");
        typeMapping.put("map", "Map");
        typeMapping.put("set", "Set");

        // No java.util imports needed
        importMapping.clear();

        // Types that don't need imports
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
        return TypeConverter.kotlinifyDefaultValue(super.toDefaultValue(schema));
    }

    // ── Template configuration ──────────────────────────────────────────

    @Override
    public void processOpts() {
        super.processOpts();

        // Kotlin model/API templates
        modelTemplateFiles.clear();
        modelTemplateFiles.put("model/model.mustache", ".kt");

        apiTemplateFiles.clear();
        apiTemplateFiles.put("api/" + FileSpec.API_SERVICE.getTemplateName(), FileSpec.API_SERVICE.kotlinFileName());

        // Kotlin doc templates (Markdown — same format as Java)
        modelDocTemplateFiles.clear();
        modelDocTemplateFiles.put("model/model_doc.mustache", ".md");
        apiDocTemplateFiles.clear();
        apiDocTemplateFiles.put("api/api_doc.mustache", ".md");

        // Kotlin test templates
        modelTestTemplateFiles.clear();
        modelTestTemplateFiles.put("model/model_test.mustache", ".kt");
        apiTestTemplateFiles.clear();
        apiTestTemplateFiles.put("api/api_test.mustache", ".kt");

        // Convert supporting files: .java → .kt, skip MockService
        convertSupportingFilesToKotlin();
    }

    private void convertSupportingFilesToKotlin() {
        List<SupportingFile> kotlinFiles = new ArrayList<>();
        for (var file : supportingFiles) {
            var template = file.getTemplateFile();
            if (template != null && template.contains("mockService")) continue;

            var dest = file.getDestinationFilename();
            if (dest.endsWith(".java")) {
                var ktFile = new SupportingFile(template, file.getFolder(), dest.replace(".java", ".kt"));
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
        return super.toModelFilename(name).replace(".java", ".kt");
    }

    @Override
    public String toApiFilename(String name) {
        return super.toApiFilename(name).replace(".java", ".kt");
    }

    // ── Model post-processing ───────────────────────────────────────────

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
                model.imports.removeIf(imp -> ImportFilter.shouldFilterForKotlin(imp, modelPackage()));
            }

            // Kotlinify enum field types (Integer → Int, etc.)
            // Map.of() returns immutable maps, so we must create mutable copies
            if (model.vendorExtensions.containsKey("enumFields")) {
                @SuppressWarnings("unchecked")
                var fields = (List<Map<String, String>>) model.vendorExtensions.get("enumFields");
                var kotlinified = fields.stream()
                    .map(field -> {
                        var mutable = new HashMap<>(field);
                        var type = mutable.get("type");
                        if (type != null) mutable.put("type", TypeConverter.kotlinify(type));
                        return (Map<String, String>) mutable;
                    })
                    .toList();
                model.vendorExtensions.put("enumFields", kotlinified);
            }
        }

        return result;
    }

    // ── Kotlin-specific transformations ─────────────────────────────────

    /** Escapes $ in property names/baseName for Kotlin string literals and identifiers. */
    private void escapeKotlinIdentifiers(CodegenProperty property) {
        if (property.baseName != null && property.baseName.contains("$")) {
            property.baseName = property.baseName.replace("$", "\\$");
        }
        if (property.name != null && property.name.contains("$")) {
            property.name = "`" + property.name + "`";
        }
    }

    /** Converts Java types to Kotlin equivalents on a property. */
    private void kotlinifyProperty(CodegenProperty property) {
        property.dataType = TypeConverter.kotlinify(property.dataType);
        property.datatypeWithEnum = TypeConverter.kotlinify(property.datatypeWithEnum);
        property.baseType = TypeConverter.kotlinify(property.baseType);
        property.defaultValue = TypeConverter.kotlinifyDefaultValue(property.defaultValue);
    }
}
