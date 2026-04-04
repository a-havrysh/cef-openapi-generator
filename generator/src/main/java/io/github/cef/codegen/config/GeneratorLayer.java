package io.github.cef.codegen.config;

import lombok.experimental.UtilityClass;
import org.openapitools.codegen.SupportingFile;

import java.io.File;
import java.util.List;

import static io.github.cef.codegen.config.FileSpec.*;
import static io.github.cef.codegen.config.PackageSuffix.*;

/**
 * Registers supporting files for each architectural layer.
 * Template directory names derived from {@link PackageSuffix}.
 */
@UtilityClass
public class GeneratorLayer {

    public void registerAll(
        List<SupportingFile> files,
        String apiPackage,
        String sourceFolder
    ) {
        addLayer(files, apiPackage, sourceFolder, PROTOCOL,
            HTTP_METHOD, API_REQUEST, API_RESPONSE, MULTIPART_FILE);

        addLayer(files, apiPackage, sourceFolder, ROUTING,
            ROUTE_TREE, ROUTE_NODE);

        addLayer(files, apiPackage, sourceFolder, CEF,
            API_CEF_REQUEST_HANDLER, API_CEF_REQUEST_HANDLER_BUILDER,
            API_RESOURCE_REQUEST_HANDLER, API_RESPONSE_HANDLER);

        addLayer(files, apiPackage, sourceFolder, UTIL,
            CONTENT_TYPE_RESOLVER, MULTIPART_PARSER);

        addLayer(files, apiPackage, sourceFolder, EXCEPTION,
            API_EXCEPTION, BAD_REQUEST_EXCEPTION, NOT_FOUND_EXCEPTION,
            INTERNAL_SERVER_ERROR_EXCEPTION, NOT_IMPLEMENTED_EXCEPTION,
            VALIDATION_EXCEPTION);

        addLayer(files, apiPackage, sourceFolder, VALIDATION,
            PARAMETER_VALIDATOR);

        addLayer(files, apiPackage, sourceFolder, INTERCEPTOR,
            REQUEST_INTERCEPTOR, CORS_INTERCEPTOR, VALIDATION_INTERCEPTOR,
            URL_FILTER_INTERCEPTOR, API_KEY_AUTH_INTERCEPTOR,
            BEARER_AUTH_INTERCEPTOR, BASIC_AUTH_INTERCEPTOR,
            EXCEPTION_HANDLER, COMPOSITE_EXCEPTION_HANDLER);
    }

    private void addLayer(
        List<SupportingFile> files,
        String apiPackage,
        String sourceFolder,
        PackageSuffix layer,
        FileSpec... specs
    ) {
        var outputFolder = buildFolderPath(apiPackage, layer, sourceFolder);
        var templateDir = layer.templateDir();
        for (var spec : specs) {
            files.add(new SupportingFile(
                templateDir + "/" + spec.getTemplateName(),
                outputFolder,
                spec.getFileName()
            ));
        }
    }

    private String buildFolderPath(
        String apiPackage,
        PackageSuffix suffix,
        String sourceFolder
    ) {
        return (sourceFolder + File.separator + apiPackage + suffix.getValue())
            .replace(".", File.separator);
    }
}
