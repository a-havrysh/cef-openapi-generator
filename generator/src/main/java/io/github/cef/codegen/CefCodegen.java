package io.github.cef.codegen;

import io.github.cef.codegen.config.FileSpec;
import io.github.cef.codegen.config.GeneratorLayer;
import io.github.cef.codegen.processing.EnumFieldProcessor;
import io.github.cef.codegen.processing.ImportFilter;
import io.github.cef.codegen.processing.ParameterConstraintExtractor;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.languages.AbstractJavaCodegen;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * CEF (Chromium Embedded Framework) code generator for OpenAPI specifications.
 *
 * Generates a layered API infrastructure for JetBrains JCEF plugins:
 * - Service interfaces (two-level: HTTP wrapper + business method)
 * - Protocol layer (ApiRequest, ApiResponse, HttpMethod)
 * - Routing layer (Trie-based RouteTree)
 * - CEF integration (request handlers, response serialization)
 * - Interceptors (CORS, auth, validation)
 * - Exception hierarchy (400, 404, 500, 501)
 *
 * @see CefKotlinCodegen
 */
public class CefCodegen extends AbstractJavaCodegen {

    private static final String GENERATOR_NAME = "cef";
    private static final String TEMPLATE_DIR = "cef-java";
    private static final String SERVICE_SUBDIR = "service";

    public CefCodegen() {
        super();
        this.hideGenerationTimestamp = true;
        embeddedTemplateDir = templateDir = TEMPLATE_DIR;

        setAnnotationLibrary(AnnotationLibrary.SWAGGER2);
        setOpenApiNullable(false);
        setUseBeanValidation(false);

        cliOptions.add(org.openapitools.codegen.CliOption.newString("modelSuffix",
            "Suffix to append to all model class names (e.g., 'Dto' -> TaskDto)"));
        cliOptions.add(org.openapitools.codegen.CliOption.newString("modelPrefix",
            "Prefix to prepend to all model class names"));
    }

    @Override
    public String getName() {
        return GENERATOR_NAME;
    }

    @Override
    public String getHelp() {
        return "CEF generator for BPMN Editor";
    }

    // ── Template configuration ──────────────────────────────────────────

    @Override
    public void processOpts() {
        super.processOpts();
        applyModelNamingOptions();
        configureTemplates();
        GeneratorLayer.registerAll(supportingFiles, apiPackage, sourceFolder);
    }

    private void applyModelNamingOptions() {
        if (additionalProperties.containsKey("modelSuffix")) {
            setModelNameSuffix(additionalProperties.get("modelSuffix").toString());
        }
        if (additionalProperties.containsKey("modelPrefix")) {
            setModelNamePrefix(additionalProperties.get("modelPrefix").toString());
        }
    }

    private void configureTemplates() {
        apiTemplateFiles.clear();
        apiTemplateFiles.put("api/" + FileSpec.API_SERVICE.getTemplateName(), FileSpec.API_SERVICE.getFileName());
        apiTemplateFiles.put("api/" + FileSpec.MOCK_SERVICE.getTemplateName(), FileSpec.MOCK_SERVICE.getFileName());

        modelTemplateFiles.clear();
        modelTemplateFiles.put("model/model.mustache", ".java");

        modelDocTemplateFiles.clear();
        modelDocTemplateFiles.put("model/model_doc.mustache", ".md");
        modelTestTemplateFiles.clear();
        modelTestTemplateFiles.put("model/model_test.mustache", ".java");
        apiDocTemplateFiles.clear();
        apiDocTemplateFiles.put("api/api_doc.mustache", ".md");
        apiTestTemplateFiles.clear();
        apiTestTemplateFiles.put("api/api_test.mustache", ".java");
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        if (FileSpec.API_SERVICE.getTemplateName().equals(templateName)) {
            return apiFileFolder() + File.separator + SERVICE_SUBDIR + File.separator
                + toApiFilename(tag) + FileSpec.API_SERVICE.getFileName();
        }
        return super.apiFilename(templateName, tag);
    }

    // ── Parameter processing ────────────────────────────────────────────

    @Override
    public CodegenParameter fromParameter(Parameter parameter, Set<String> imports) {
        var param = super.fromParameter(parameter, imports);
        ParameterConstraintExtractor.extract(param, parameter.getSchema());
        return param;
    }

    // ── Model post-processing ───────────────────────────────────────────

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);
        ImportFilter.cleanupJavaImports(model);
    }

    @Override
    public ModelsMap postProcessModels(ModelsMap objs) {
        var result = super.postProcessModels(objs);

        for (var modelMapObj : result.getModels()) {
            var model = ((ModelMap) modelMapObj).getModel();
            ImportFilter.cleanupJavaImports(model);
            EnumFieldProcessor.process(model);
        }

        return result;
    }

    // ── Supporting file data (server URLs) ──────────────────────────────

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> bundle) {
        var result = super.postProcessSupportingFileData(bundle);

        if (openAPI != null && openAPI.getServers() != null) {
            var urls = openAPI.getServers().stream()
                .map(Server::getUrl)
                .filter(url -> url != null && !url.isEmpty())
                .toList();

            if (!urls.isEmpty()) {
                result.put("serverUrls", urls);
                result.put("hasServers", true);
            }
        }

        return result;
    }
}
