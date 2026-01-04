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
    public org.openapitools.codegen.model.ModelsMap postProcessModels(org.openapitools.codegen.model.ModelsMap objs) {
        objs = super.postProcessModels(objs);

        for (var modelMap : objs.getModels()) {
            var model = modelMap.getModel();
            if (model != null && model.imports != null) {
                model.imports = model.imports.stream()
                        .filter(imp -> !imp.contains("swagger"))
                        .collect(Collectors.toSet());
            }

            if (model != null && model.isEnum && model.allowableValues != null) {
                processEnumVendorExtensions(model);
            }
        }

        return objs;
    }

    private void processEnumVendorExtensions(CodegenModel model) {
        var allowableValues = model.allowableValues;
        var enumVars = (List<Map<String, Object>>) allowableValues.get("enumVars");

        if (enumVars == null || enumVars.isEmpty()) {
            return;
        }

        var displayNames = getVendorExtensionList(model, "x-enum-display-names");
        var values = getVendorExtensionList(model, "x-enum-values");

        for (var i = 0; i < enumVars.size(); i++) {
            var enumVar = enumVars.get(i);

            if (displayNames != null && i < displayNames.size()) {
                enumVar.put("displayName", displayNames.get(i));
            }

            if (values != null && i < values.size()) {
                enumVar.put("enumValue", values.get(i));
            }
        }

        if (displayNames != null && !displayNames.isEmpty()) {
            model.vendorExtensions.put("hasDisplayNames", true);
        }
        if (values != null && !values.isEmpty()) {
            model.vendorExtensions.put("hasValues", true);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getVendorExtensionList(CodegenModel model, String extensionName) {
        var extension = model.vendorExtensions.get(extensionName);
        if (extension instanceof List list) {
            return list;
        }
        return null;
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
