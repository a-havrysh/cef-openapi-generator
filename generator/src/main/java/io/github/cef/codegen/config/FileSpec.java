package io.github.cef.codegen.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Maps Mustache template names to generated file names.
 * Each entry represents one file the generator produces.
 */
@Getter
@RequiredArgsConstructor
public enum FileSpec {

    // API layer
    API_SERVICE("apiService.mustache", "Service.java"),
    MOCK_SERVICE("mockService.mustache", "MockService.java"),

    // Protocol layer
    HTTP_METHOD("httpMethod.mustache", "HttpMethod.java"),
    API_REQUEST("apiRequest.mustache", "ApiRequest.java"),
    API_RESPONSE("apiResponse.mustache", "ApiResponse.java"),
    MULTIPART_FILE("multipartFile.mustache", "MultipartFile.java"),

    // Routing layer
    ROUTE_TREE("routeTree.mustache", "RouteTree.java"),
    ROUTE_NODE("routeNode.mustache", "RouteNode.java"),

    // CEF integration layer
    API_CEF_REQUEST_HANDLER("apiCefRequestHandler.mustache", "ApiCefRequestHandler.java"),
    API_CEF_REQUEST_HANDLER_BUILDER("apiCefRequestHandlerBuilder.mustache", "ApiCefRequestHandlerBuilder.java"),
    API_RESOURCE_REQUEST_HANDLER("apiResourceRequestHandler.mustache", "ApiResourceRequestHandler.java"),
    API_RESPONSE_HANDLER("apiResponseHandler.mustache", "ApiResponseHandler.java"),

    // Utility layer
    CONTENT_TYPE_RESOLVER("contentTypeResolver.mustache", "ContentTypeResolver.java"),
    MULTIPART_PARSER("multipartParser.mustache", "MultipartParser.java"),

    // Exception layer
    API_EXCEPTION("apiException.mustache", "ApiException.java"),
    BAD_REQUEST_EXCEPTION("badRequestException.mustache", "BadRequestException.java"),
    NOT_FOUND_EXCEPTION("notFoundException.mustache", "NotFoundException.java"),
    INTERNAL_SERVER_ERROR_EXCEPTION("internalServerErrorException.mustache", "InternalServerErrorException.java"),
    NOT_IMPLEMENTED_EXCEPTION("notImplementedException.mustache", "NotImplementedException.java"),
    VALIDATION_EXCEPTION("validationException.mustache", "ValidationException.java"),

    // Validation layer
    PARAMETER_VALIDATOR("parameterValidator.mustache", "ParameterValidator.java"),

    // Interceptor layer
    REQUEST_INTERCEPTOR("requestInterceptor.mustache", "RequestInterceptor.java"),
    EXCEPTION_HANDLER("exceptionHandler.mustache", "ExceptionHandler.java"),
    COMPOSITE_EXCEPTION_HANDLER("compositeExceptionHandler.mustache", "CompositeExceptionHandler.java"),
    CORS_INTERCEPTOR("corsInterceptor.mustache", "CorsInterceptor.java"),
    VALIDATION_INTERCEPTOR("validationInterceptor.mustache", "ValidationInterceptor.java"),
    URL_FILTER_INTERCEPTOR("urlFilterInterceptor.mustache", "UrlFilterInterceptor.java"),
    API_KEY_AUTH_INTERCEPTOR("apiKeyAuthInterceptor.mustache", "ApiKeyAuthInterceptor.java"),
    BEARER_AUTH_INTERCEPTOR("bearerAuthInterceptor.mustache", "BearerAuthInterceptor.java"),
    BASIC_AUTH_INTERCEPTOR("basicAuthInterceptor.mustache", "BasicAuthInterceptor.java");

    private final String templateName;
    private final String fileName;

    /** Returns file name with .kt extension instead of .java. */
    public String kotlinFileName() {
        return fileName.replace(".java", ".kt");
    }
}
