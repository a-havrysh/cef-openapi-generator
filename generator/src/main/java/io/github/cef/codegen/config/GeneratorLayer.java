package io.github.cef.codegen.config;

import org.openapitools.codegen.SupportingFile;

import java.io.File;
import java.util.List;

import static io.github.cef.codegen.config.FileSpec.*;
import static io.github.cef.codegen.config.PackageSuffix.*;

/**
 * Registers supporting files for each architectural layer.
 * Centralizes the mapping: template directory + FileSpec → SupportingFile.
 */
public final class GeneratorLayer {

    private GeneratorLayer() {}

    /** Registers all layers into the provided supporting files list. */
    public static void registerAll(List<SupportingFile> files, String apiPackage, String sourceFolder) {
        addProtocol(files, apiPackage, sourceFolder);
        addRouting(files, apiPackage, sourceFolder);
        addCefIntegration(files, apiPackage, sourceFolder);
        addUtility(files, apiPackage, sourceFolder);
        addException(files, apiPackage, sourceFolder);
        addValidation(files, apiPackage, sourceFolder);
        addInterceptor(files, apiPackage, sourceFolder);
    }

    private static void addProtocol(List<SupportingFile> files, String apiPackage, String sourceFolder) {
        var folder = folderPath(apiPackage, PROTOCOL, sourceFolder);
        add(files, "protocol", folder, HTTP_METHOD, API_REQUEST, API_RESPONSE, MULTIPART_FILE);
    }

    private static void addRouting(List<SupportingFile> files, String apiPackage, String sourceFolder) {
        var folder = folderPath(apiPackage, ROUTING, sourceFolder);
        add(files, "routing", folder, ROUTE_TREE, ROUTE_NODE);
    }

    private static void addCefIntegration(List<SupportingFile> files, String apiPackage, String sourceFolder) {
        var folder = folderPath(apiPackage, CEF, sourceFolder);
        add(files, "cef", folder, API_CEF_REQUEST_HANDLER, API_CEF_REQUEST_HANDLER_BUILDER, API_RESOURCE_REQUEST_HANDLER);
        // ApiResponseHandler template lives in protocol/ but output goes to cef/
        files.add(new SupportingFile("protocol/" + API_RESPONSE_HANDLER.getTemplateName(), folder, API_RESPONSE_HANDLER.getFileName()));
    }

    private static void addUtility(List<SupportingFile> files, String apiPackage, String sourceFolder) {
        var folder = folderPath(apiPackage, UTIL, sourceFolder);
        // Templates live in protocol/ but output goes to util/
        files.add(new SupportingFile("protocol/" + CONTENT_TYPE_RESOLVER.getTemplateName(), folder, CONTENT_TYPE_RESOLVER.getFileName()));
        files.add(new SupportingFile("protocol/" + MULTIPART_PARSER.getTemplateName(), folder, MULTIPART_PARSER.getFileName()));
    }

    private static void addException(List<SupportingFile> files, String apiPackage, String sourceFolder) {
        var folder = folderPath(apiPackage, EXCEPTION, sourceFolder);
        add(files, "exception", folder,
            API_EXCEPTION, BAD_REQUEST_EXCEPTION, NOT_FOUND_EXCEPTION,
            INTERNAL_SERVER_ERROR_EXCEPTION, NOT_IMPLEMENTED_EXCEPTION, VALIDATION_EXCEPTION);
    }

    private static void addValidation(List<SupportingFile> files, String apiPackage, String sourceFolder) {
        var folder = folderPath(apiPackage, VALIDATION, sourceFolder);
        add(files, "validation", folder, PARAMETER_VALIDATOR);
    }

    private static void addInterceptor(List<SupportingFile> files, String apiPackage, String sourceFolder) {
        var folder = folderPath(apiPackage, INTERCEPTOR, sourceFolder);
        add(files, "interceptor", folder,
            REQUEST_INTERCEPTOR, CORS_INTERCEPTOR, VALIDATION_INTERCEPTOR,
            URL_FILTER_INTERCEPTOR, API_KEY_AUTH_INTERCEPTOR, BEARER_AUTH_INTERCEPTOR, BASIC_AUTH_INTERCEPTOR);
        // ExceptionHandler/CompositeExceptionHandler templates live in exception/ but output goes to interceptor/
        files.add(new SupportingFile("exception/" + EXCEPTION_HANDLER.getTemplateName(), folder, EXCEPTION_HANDLER.getFileName()));
        files.add(new SupportingFile("exception/" + COMPOSITE_EXCEPTION_HANDLER.getTemplateName(), folder, COMPOSITE_EXCEPTION_HANDLER.getFileName()));
    }

    // --- helpers ---

    private static void add(List<SupportingFile> files, String templateDir, String folder, FileSpec... specs) {
        for (var spec : specs) {
            files.add(new SupportingFile(templateDir + "/" + spec.getTemplateName(), folder, spec.getFileName()));
        }
    }

    private static String folderPath(String apiPackage, PackageSuffix suffix, String sourceFolder) {
        return (sourceFolder + File.separator + apiPackage + suffix.getValue()).replace(".", File.separator);
    }
}
