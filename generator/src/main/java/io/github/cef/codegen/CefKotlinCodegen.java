package io.github.cef.codegen;

import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.model.ModelsMap;
import org.openapitools.codegen.model.ModelMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Kotlin code generator for CEF OpenAPI specifications.
 * Extends CefCodegen to generate idiomatic Kotlin code instead of Java.
 *
 * Generates the same CEF API architecture but in Kotlin:
 * - Service interfaces with business logic (two-layer pattern)
 * - Protocol layer (ApiRequest, ApiResponse, HttpMethod)
 * - Interceptor framework with authentication and CORS support
 * - Exception hierarchy (400, 401, 403, 404, 500, 501)
 * - Type-safe parameter validation
 * - Multipart file handling
 *
 * All generated code follows Kotlin idioms and best practices.
 * Uses Kotlin built-in types (Int, List, Map) instead of Java (Integer, List, Map).
 */
public class CefKotlinCodegen extends CefCodegen {

    private static final String GENERATOR_NAME = "cef-kotlin";
    private static final String TEMPLATE_DIR = "cef-kotlin";

    public CefKotlinCodegen() {
        super();
        // Override template directory to use Kotlin templates
        this.embeddedTemplateDir = this.templateDir = TEMPLATE_DIR;
        // Use src/main/kotlin for Kotlin source files instead of src/main/java
        this.sourceFolder = "src/main/kotlin";

        // Configure Kotlin type mappings
        configureKotlinTypeMappings();
    }

    /**
     * Configure type mappings to use Kotlin built-in types instead of Java boxed types.
     * This ensures generated code is idiomatic Kotlin.
     */
    private void configureKotlinTypeMappings() {
        // Override Java boxed types with Kotlin equivalents
        typeMapping.put("integer", "Int");
        typeMapping.put("long", "Long");
        typeMapping.put("float", "Float");
        typeMapping.put("double", "Double");
        typeMapping.put("boolean", "Boolean");
        typeMapping.put("string", "String");

        // Use Kotlin collections instead of java.util
        typeMapping.put("array", "List");
        typeMapping.put("map", "Map");

        // Clear import mappings - don't add unnecessary imports for Kotlin built-in types
        // Kotlin doesn't need imports for: Int, Long, String, List, Map, Set, etc.
        importMapping.clear();
    }

    @Override
    public String getName() {
        return GENERATOR_NAME;
    }

    @Override
    public String getHelp() {
        return "Generates Kotlin code for CEF-based OpenAPI APIs with full CEF framework support";
    }

    @Override
    public void processOpts() {
        super.processOpts();

        // Remove MockService from API template files - not needed for Kotlin
        apiTemplateFiles.remove("api/mockService.mustache");

        // Convert all .java file extensions to .kt for Kotlin code generation
        // Also exclude MockService files as they're not needed in Kotlin
        List<SupportingFile> kotlinFiles = new ArrayList<>();
        for (SupportingFile file : supportingFiles) {
            String template = file.getTemplateFile();

            // Skip MockService - not needed for Kotlin
            if (template != null && template.contains("mockService")) {
                continue;
            }

            String destFilename = file.getDestinationFilename();
            if (destFilename.endsWith(".java")) {
                String kotlinFilename = destFilename.replaceAll("\\.java$", ".kt");
                // Recreate SupportingFile with .kt extension
                SupportingFile kotlinFile = new SupportingFile(
                    template,
                    file.getFolder(),
                    kotlinFilename
                );
                // Preserve the canOverwrite setting if it was false
                if (!file.isCanOverwrite()) {
                    kotlinFile.doNotOverwrite();
                }
                kotlinFiles.add(kotlinFile);
            } else {
                kotlinFiles.add(file);
            }
        }
        supportingFiles.clear();
        supportingFiles.addAll(kotlinFiles);
    }

    @Override
    public String toModelFilename(String name) {
        // Convert Java model filename to Kotlin (.java -> .kt)
        String javaFilename = super.toModelFilename(name);
        return javaFilename.replaceAll("\\.java$", ".kt");
    }

    @Override
    public String toApiFilename(String name) {
        // Convert Java API filename to Kotlin (.java -> .kt)
        String javaFilename = super.toApiFilename(name);
        return javaFilename.replaceAll("\\.java$", ".kt");
    }

    @Override
    public ModelsMap postProcessModels(ModelsMap objs) {
        // Remove unnecessary imports from Kotlin models
        ModelsMap result = super.postProcessModels(objs);

        for (var modelMapObj : result.getModels()) {
            var modelMap = (ModelMap) modelMapObj;
            var model = modelMap.getModel();

            if (model != null && model.imports != null) {
                // Create a new list with only necessary imports
                List<String> filteredImports = new ArrayList<>();

                for (String importStr : model.imports) {
                    // Keep imports that are NOT Kotlin built-ins, Java utils, or bad patterns
                    boolean isKotlinBuiltin = importStr.endsWith(".Int") ||
                        importStr.endsWith(".Long") ||
                        importStr.endsWith(".Float") ||
                        importStr.endsWith(".Double") ||
                        importStr.endsWith(".Boolean") ||
                        importStr.endsWith(".String") ||
                        importStr.endsWith(".List") ||
                        importStr.endsWith(".Map") ||
                        importStr.endsWith(".Set");

                    boolean isJavaUtil = importStr.startsWith("java.util");
                    boolean isBadImport = importStr.contains("ArrayList") ||
                        importStr.contains("Arrays");

                    // Also skip imports from com.example.api.dto package (shouldn't import our own types)
                    boolean isSelfImport = importStr.startsWith("com.example.api.dto.");

                    if (!isKotlinBuiltin && !isJavaUtil && !isBadImport && !isSelfImport) {
                        filteredImports.add(importStr);
                    }
                }

                // Replace the imports list with filtered version
                model.imports.clear();
                model.imports.addAll(filteredImports);
            }
        }

        return result;
    }

}
