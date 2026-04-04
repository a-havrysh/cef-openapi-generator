package io.github.cef.codegen;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.languages.AbstractJavaCodegen;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;

import java.io.File;
import java.util.Map;
import java.util.Set;

import io.github.cef.codegen.config.GeneratorLayer;
import io.github.cef.codegen.processing.EnumFieldProcessor;
import io.github.cef.codegen.processing.ImportFilter;
import io.github.cef.codegen.processing.ParameterConstraintExtractor;

import static io.github.cef.codegen.config.FileSpec.API_SERVICE;
import static io.github.cef.codegen.config.FileSpec.MOCK_SERVICE;
import static org.openapitools.codegen.CliOption.newString;
import static org.openapitools.codegen.CodegenConstants.SERIALIZABLE_MODEL;

/**
 * CEF (Chromium Embedded Framework) code generator
 * for OpenAPI specifications.
 *
 * @see CefKotlinCodegen
 */
public class CefCodegen extends AbstractJavaCodegen {

    // Generator identity
    private static final String GENERATOR_NAME = "cef";
    private static final String GENERATOR_HELP =
        "CEF generator for BPMN Editor";
    private static final String TEMPLATE_DIR = "cef-java";
    private static final String SERVICE_SUBDIR = "service";

    // File extensions
    static final String JAVA_EXT = ".java";
    static final String KOTLIN_EXT = ".kt";
    static final String MARKDOWN_EXT = ".md";

    // Template paths
    static final String MODEL_TEMPLATE = "model/model.mustache";
    static final String MODEL_DOC_TEMPLATE = "model/model_doc.mustache";
    static final String MODEL_TEST_TEMPLATE = "model/model_test.mustache";
    static final String API_DOC_TEMPLATE = "api/api_doc.mustache";
    static final String API_TEST_TEMPLATE = "api/api_test.mustache";
    static final String API_TEMPLATE_PREFIX = "api/";
    static final String README_TEMPLATE = "README.mustache";
    static final String README_OUTPUT = "README.md";

    // Config option keys
    static final String OPT_MODEL_SUFFIX = "modelSuffix";
    static final String OPT_MODEL_PREFIX = "modelPrefix";
    static final String OPT_CONTAINER_DEFAULT_TO_NULL =
        "containerDefaultToNull";
    static final String OPT_GENERATE_CONSTRUCTOR =
        "generateConstructorWithAllArgs";
    static final String OPT_GENERATE_BUILDERS = "generateBuilders";

    // Bundle keys for Mustache templates
    static final String BUNDLE_SERVER_URLS = "serverUrls";
    static final String BUNDLE_HAS_SERVERS = "hasServers";

    public CefCodegen() {
        super();
        this.hideGenerationTimestamp = true;
        embeddedTemplateDir = templateDir = TEMPLATE_DIR;

        setAnnotationLibrary(AnnotationLibrary.SWAGGER2);
        setOpenApiNullable(false);
        setUseBeanValidation(false);

        cliOptions.add(newString(OPT_MODEL_SUFFIX,
            "Suffix for model class names (e.g. 'Dto')"));
        cliOptions.add(newString(OPT_MODEL_PREFIX,
            "Prefix for model class names"));
    }

    @Override
    public String getName() {
        return GENERATOR_NAME;
    }

    @Override
    public String getHelp() {
        return GENERATOR_HELP;
    }

    // ── Template configuration

    @Override
    public void processOpts() {
        super.processOpts();
        applyModelNamingOptions();
        applyGenerationOptions();
        configureTemplates();
        GeneratorLayer.registerAll(supportingFiles, apiPackage, sourceFolder);
        supportingFiles.add(
            new SupportingFile(README_TEMPLATE, "", README_OUTPUT));
    }

    private void applyModelNamingOptions() {
        if (additionalProperties.containsKey(OPT_MODEL_SUFFIX)) {
            setModelNameSuffix(
                additionalProperties.get(OPT_MODEL_SUFFIX).toString());
        }
        if (additionalProperties.containsKey(OPT_MODEL_PREFIX)) {
            setModelNamePrefix(
                additionalProperties.get(OPT_MODEL_PREFIX).toString());
        }
    }

    private void applyGenerationOptions() {
        propagateBoolean(SERIALIZABLE_MODEL);
        propagateBoolean(OPT_CONTAINER_DEFAULT_TO_NULL);
        propagateBoolean(OPT_GENERATE_CONSTRUCTOR);
        propagateBoolean(OPT_GENERATE_BUILDERS);
    }

    private void propagateBoolean(String key) {
        if (additionalProperties.containsKey(key)) {
            additionalProperties.put(key,
                Boolean.parseBoolean(
                    additionalProperties.get(key).toString()));
        }
    }

    protected void configureTemplates() {
        apiTemplateFiles.clear();
        apiTemplateFiles.put(
            API_TEMPLATE_PREFIX + API_SERVICE.getTemplateName(),
            API_SERVICE.getFileName());
        apiTemplateFiles.put(
            API_TEMPLATE_PREFIX + MOCK_SERVICE.getTemplateName(),
            MOCK_SERVICE.getFileName());

        modelTemplateFiles.clear();
        modelTemplateFiles.put(MODEL_TEMPLATE, JAVA_EXT);

        modelDocTemplateFiles.clear();
        modelDocTemplateFiles.put(MODEL_DOC_TEMPLATE, MARKDOWN_EXT);

        modelTestTemplateFiles.clear();
        modelTestTemplateFiles.put(MODEL_TEST_TEMPLATE, JAVA_EXT);

        apiDocTemplateFiles.clear();
        apiDocTemplateFiles.put(API_DOC_TEMPLATE, MARKDOWN_EXT);

        apiTestTemplateFiles.clear();
        apiTestTemplateFiles.put(API_TEST_TEMPLATE, JAVA_EXT);
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        if (API_SERVICE.getTemplateName().equals(templateName)) {
            return apiFileFolder()
                + File.separator + SERVICE_SUBDIR
                + File.separator + toApiFilename(tag)
                + API_SERVICE.getFileName();
        }
        return super.apiFilename(templateName, tag);
    }

    // ── Parameter processing

    @Override
    public CodegenParameter fromParameter(
        Parameter parameter, Set<String> imports
    ) {
        var param = super.fromParameter(parameter, imports);
        ParameterConstraintExtractor.extract(param, parameter.getSchema());
        return param;
    }

    // ── Model post-processing

    @Override
    public void postProcessModelProperty(
        CodegenModel model, CodegenProperty property
    ) {
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

    // ── Supporting file data (server URLs)

    @Override
    public Map<String, Object> postProcessSupportingFileData(
        Map<String, Object> bundle
    ) {
        var result = super.postProcessSupportingFileData(bundle);

        if (openAPI != null && openAPI.getServers() != null) {
            var urls = openAPI.getServers().stream()
                .map(Server::getUrl)
                .filter(url -> url != null && !url.isEmpty())
                .toList();

            if (!urls.isEmpty()) {
                result.put(BUNDLE_SERVER_URLS, urls);
                result.put(BUNDLE_HAS_SERVERS, true);
            }
        }

        return result;
    }
}
