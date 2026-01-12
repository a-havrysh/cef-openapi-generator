# Changelog

All notable changes to the CEF OpenAPI Generator will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

**Upgrading?** See [MIGRATION.md](MIGRATION.md) for detailed migration guides.

---

## [3.0.0] - 2026-01-12

**⚠️ MAJOR RELEASE** - Complete Refactoring & Modernization

### Breaking Changes - Project Structure

**Template Organization:**
- Templates reorganized into 8 architectural layers (api/, model/, protocol/, routing/, cef/, exception/, validation/, interceptor/)
- Resource folders renamed: `cef/` → `cef-java/`, `kotlin/` → `cef-kotlin/`
- Example projects unified: `examples/cef-java/`, `examples/cef-kotlin/`
- KotlinCodegen class renamed to CefKotlinCodegen

**Cleanup:**
- Removed 19 kotlin-prefixed duplicate templates
- Deleted 8 redundant documentation files
- Total: -32,146 lines of duplicated code removed

**See MIGRATION.md for detailed upgrade guide.**

### Improvements

**Kotlin Code Quality (9/10):**
- data classes with named parameters (no Builder pattern in DTOs)
- Idiomatic Kotlin collections (List, Map)
- Proper companion objects and immutability
- All 53 Kotlin tests passing

**Complete OpenAPI 3.0 Standard Features Implementation

### Added

**OpenAPI Validation - Complete Coverage:**
- **Default Values**: Automatically applied from OpenAPI schema (default: 20 → actualValue = value != null ? value : 20)
- **Date/Time Parsing**:
  - format: date → java.time.LocalDate (YYYY-MM-DD validation)
  - format: date-time → java.time.OffsetDateTime (ISO 8601 with timezone)
  - Automatic parsing and validation in ValidationInterceptor
- **Boolean Parameters**: Accepts true/false, 1/0, yes/no, on/off (case-insensitive)
- **Array Parameter Validation**:
  - minItems, maxItems constraints
  - uniqueItems - duplicate detection
  - Item-level enum validation for array elements
  - Comma-separated query parameter parsing ("java,kotlin,spring" → List)
- **Format Validation** (OpenAPI 3.0 standard formats):
  - email - RFC 5322 email address validation
  - uuid - RFC 4122 UUID validation (with/without hyphens)
  - uri, uri-reference - RFC 3986 URI validation
  - hostname - RFC 1123 hostname validation
  - ipv4, ipv6 - IP address validation (full, compressed, IPv4-mapped)
- **Advanced Numeric Constraints**:
  - multipleOf - value must be multiple of specified number
  - exclusiveMinimum - value > minimum (not >=)
  - exclusiveMaximum - value < maximum (not <=)
- **Nullable Handling**: Proper null handling per OpenAPI schema.nullable flag

**Security & Authentication:**
- **ApiKeyAuthInterceptor**: API key authentication from header, query parameter, or cookie
- **BearerAuthInterceptor**: Bearer token (JWT) authentication with configurable format
- **BasicAuthInterceptor**: Basic HTTP authentication with Base64 credential decoding
- All auth interceptors: 401 (Unauthorized) for missing credentials, 403 (Forbidden) for invalid

**File Upload:**
- **MultipartFile**: Uploaded file representation with metadata (originalFilename, contentType, size)
- **MultipartParser**: RFC 7578 compliant multipart/form-data parser
- Support for mixed form fields and file uploads in single request
- Methods: getBytes(), getInputStream(), getContentAsString(), getSize(), isEmpty()

**Developer Experience:**
- **Enhanced JavaDoc**: Rich documentation from OpenAPI descriptions, constraints, validation rules
- **Deprecated Annotations**: @Deprecated annotation for operations marked deprecated in OpenAPI
- **Mock Service Generator**: Auto-generated mock service implementations from OpenAPI examples

### Enhanced
- **ParameterValidator** extended with 11 validation methods:
  - validateString, validateInteger, validateNumber (with all constraints)
  - validateAndParseInteger, validateAndParseNumber (parse + validate in one step)
  - validateAndParseDate, validateAndParseDateTime, validateAndParseBoolean
  - validateArray (minItems, maxItems, uniqueItems, item enum)
  - validateFormat (email, uuid, uri, hostname, ipv4, ipv6)
  - Private format validators: isValidEmail, isValidUuid, isValidUri, isValidHostname, isValidIpv4, isValidIpv6
- **ValidationInterceptor** supports 7 parameter types: STRING, INTEGER, NUMBER, DATE, DATE_TIME, BOOLEAN, ARRAY
- **CefCodegen.fromParameter** extracts all OpenAPI constraints:
  - String: minLength, maxLength, pattern, format
  - Numeric: minimum, maximum, exclusiveMinimum, exclusiveMaximum, multipleOf
  - Array: minItems, maxItems, uniqueItems, item enum
  - Date/Time: format detection (date, date-time)
  - Nullable flag extraction
- **Service Interface JavaDoc** includes:
  - Operation description from OpenAPI
  - Parameter constraints (min/max/pattern/enum/format)
  - Default values
  - @throws annotations for ValidationException and ApiException
  - @deprecated for deprecated operations

### Testing
- Comprehensive test suite with 100% code coverage target
- ParameterValidatorTest: 90+ tests covering all validation methods
- Auth interceptor tests: ApiKeyAuthInterceptorTest, BearerAuthInterceptorTest, BasicAuthInterceptorTest
- MultipartParserTest: multipart/form-data parsing validation
- JaCoCo code coverage plugin integrated

### Documentation
- README updated with complete feature list (Core Framework, OpenAPI Validation, Interceptors, Advanced Features)
- CHANGELOG consolidated into single 3.0.0 release
- MIGRATION guide for upgrading from 2.0.0 to 3.0.0 with all features documented

---

## [2.0.0] - 2026-01-11

**⚠️ BREAKING CHANGES** - See [MIGRATION.md](MIGRATION.md) for migration guide.

### Breaking Changes
- **RequestInterceptor interface simplified**: `beforeHandle()` and `onError()` methods now accept only `ApiRequest` (removed `pathVariables` parameter)
  - Changed from `CefRequest` to `ApiRequest` for better access to query params, headers, and body
  - Removed redundant `pathVariables` parameter - use `request.getPathVariable(name)` instead
  - Signature: `beforeHandle(ApiRequest request)` (was `beforeHandle(CefRequest request, Map<String, String> pathVariables)`)
  - Custom interceptors must be updated - see migration guide below
  - CorsInterceptor updated: `request.getHeaderByName()` → `request.getHeader()`
- **Exception classes no longer final**: BadRequestException, NotFoundException, InternalServerErrorException can now be extended
  - Allows creating custom exception types (e.g., ValidationException extends BadRequestException)
  - No action required for users - this is backward compatible

### Added
- **OpenAPI Parameter Validation**: Automatic validation of request parameters against OpenAPI constraints
  - Validates path, query, and header parameters before reaching service layer
  - Supported constraints: required, minLength, maxLength, minimum, maximum, pattern (regex), enum
  - `ValidationException` with detailed error information (HTTP 400)
  - `ParameterValidator` utility class for type-safe validation
  - `ValidationInterceptor` automatically generated with metadata from OpenAPI spec
  - Thread-safe regex pattern caching for performance
  - Enable with `.withValidation()` in builder
  - Example:
    ```java
    ApiCefRequestHandler handler = ApiCefRequestHandler.builder(project)
        .withApiRoutes()
        .withValidation()  // Enable OpenAPI validation
        .build();
    ```
- **Validation Layer**: New package `validation` with utilities
  - ParameterValidator with methods: validateString, validateInteger, validateNumber
  - Parsing and validation in one step: validateAndParseInteger, validateAndParseNumber
  - Collects all validation errors before throwing exception
- **ValidationException**: Specialized exception for parameter validation errors
  - Contains list of ValidationError objects with field, value, constraint type, message
  - HTTP 400 status code
  - Detailed error reporting for client debugging
- **Jackson @JsonProperty annotations**: All DTO fields now annotated with @JsonProperty
  - Uses baseName from OpenAPI spec for proper JSON mapping
  - Prevents naming collisions with special characters (e.g., $, -, reserved keywords)
  - Ensures correct serialization/deserialization even with field name transformations
- **Model Naming Options**: Added configOptions for consistent DTO naming
  - `modelSuffix` - append suffix to all model names (e.g., "Dto" → TaskDto, CreateTaskRequestDto)
  - `modelPrefix` - prepend prefix to all model names
  - Optional, disabled by default to preserve OpenAPI schema names
- **Type-Specific Exception Handlers**: CompositeExceptionHandler for handling different exception types
  - Register handlers for specific exception classes via `withExceptionHandler(Class<T>, handler)`
  - Chain of Responsibility pattern - handlers checked in registration order
  - Example: separate handlers for ValidationException, ApiException, generic Exception
  - Fallback to default handler if no type-specific handler matches

### Changed
- RequestInterceptor.beforeHandle signature: `beforeHandle(ApiRequest request)` - removed pathVariables parameter
- RequestInterceptor.onError signature: `onError(Exception e, ApiRequest request)` - changed from CefRequest to ApiRequest
- All exception classes (BadRequestException, NotFoundException, InternalServerErrorException) are now non-final
- beforeHandle exceptions now trigger onError callbacks for all interceptors
- DTO fields formatted with blank lines for better readability
- Removed wildcard imports from generated models

### Upgrading
See [MIGRATION.md](MIGRATION.md) for detailed migration guide from v1.x to v2.0.0.

---

## [1.1.0] - 2026-01-10

**⚠️ Breaking Changes** - See [MIGRATION.md](MIGRATION.md) for migration guide.

### Added
- **Type-Safe Parameters**: Query parameters, path variables, and request bodies now automatically extracted with proper typing
  - Query params auto-parsed to Integer, Long, String
  - Path variables typed as String
  - Request bodies typed to specific DTO classes
  - Wrapper methods signature: `handleXxx(String param1, Integer param2, BodyType body, CefBrowser browser, CefFrame frame, CefRequest cefRequest)`
  - Business methods signature: `xxx(String param1, Integer param2, BodyType body)`
  - No manual extraction needed - all parameters ready to use
- **Request/Response Interceptors**: Cross-cutting concerns support (logging, metrics, authentication)
  - `RequestInterceptor` interface with beforeHandle/afterHandle/onError hooks
  - `withInterceptor()` builder method for registering interceptors
  - Interceptors execute in registration order
  - beforeHandle can abort request by throwing exception
  - afterHandle receives response and duration for metrics
  - onError called when exceptions occur
- **Exception Handler**: Centralized exception handling
  - `ExceptionHandler` interface for custom error responses
  - `withExceptionHandler()` builder method
  - Default implementation converts ApiException to HTTP status, others to 500
  - Integration with monitoring services (Sentry, DataDog, etc.)
- **CORS Support**: Cross-Origin Resource Sharing
  - `withCors()` method for allowing all origins (Access-Control-Allow-Origin: *)
  - `withCors(origins...)` method for whitelisting specific origins
  - Automatic OPTIONS preflight handling
  - CORS headers: Allow-Origin, Allow-Credentials, Allow-Methods, Allow-Headers, Max-Age
  - Support for credentials with specific origins
- **Builder Method Enhancements**:
  - `withRoute(pattern, method, handler)` - add custom routes with path variables
  - `withInterceptor(interceptor)` - add request/response interceptors
  - `withExceptionHandler(handler)` - custom exception handling
- **ApiResponse Convenience Methods**:
  - `created(body)` - 201 Created response
  - `notFound(message)` - 404 Not Found response
  - `badRequest(message)` - 400 Bad Request response
  - `internalServerError(message)` - 500 Internal Server Error response
- **ApiRequest Helper Methods**:
  - `getQueryParam(name)` - convenience method for query parameter access
  - `getHeader(name)` - header access for CORS origin checking and custom logic
- **Comprehensive Testing Infrastructure**:
  - 186 unit tests with 100% pass rate
  - 12 test classes covering all generated components
  - 4 JMH benchmarks validating performance claims
  - MockCefFactory for centralized mock object creation
  - Test coverage: 90%+ overall
- **Documentation**:
  - TESTING.md - comprehensive testing guide (~300 lines)
  - example/README.md - example project documentation (~270 lines)
  - Updated README.md with all new features
  - Inline documentation in all templates
- **Enhanced Example Project**:
  - Task Management API with 8 endpoints (CRUD operations)
  - Query parameter support (filtering, pagination)
  - 2 rich enums (TaskStatus, TaskPriority) with 5+ custom fields each
  - Multiple DTOs (Task, CreateTaskRequest, UpdateTaskRequest, etc.)
  - Demonstrates all generator capabilities

### Changed
- **BREAKING**: Wrapper method signatures changed from `handleXxx(ApiRequest request)` to `handleXxx(params..., CefBrowser browser, CefFrame frame, CefRequest cefRequest)`
  - Parameters now explicitly typed and extracted
  - Direct CEF object access instead of through ApiRequest wrapper
  - ApiRequest only used internally for routing layer
- **BREAKING**: ApiCefRequestHandler constructor signature changed
  - Added interceptors parameter
  - Added exceptionHandler parameter
  - Removed corsAllowedOrigins parameter (CORS now via CorsInterceptor)
- ApiResourceRequestHandler now uses ExceptionHandler for all error responses
- Interceptor execution integrated into request processing pipeline
- README.md updated with new features and usage examples
- Generated service interfaces documentation improved with typed parameter examples

### Fixed
- Query parameter compilation error - empty parameter lists `(, , )` now generate proper typed signatures
- ApiRequest now has getHeader() method for CORS and custom header access
- ApiResponse now has convenience factory methods
- Template compilation with proper imports for all DTO classes in builder

### Performance
- All tests pass (186/186) with 100% success rate
- JMH benchmarks validate 2.6x faster claim for RouteTree vs regex
- LRU cache effectiveness demonstrated
- Scalability tested up to 10,000 routes

---

## [1.0.5] - 2026-01-04

### Added
- **URL Filtering Support**: Whitelist mode for handling specific domains only
  - `withUrlFilter()` - automatically uses URLs from OpenAPI `servers` section
  - `withUrlFilter(prefixes...)` - manually specify allowed URL prefixes
  - Returns null for non-whitelisted URLs (allows browser to handle them)
- **Enum Custom Fields with Type Auto-Detection**:
  - Universal `x-enum-field-*` support in OpenAPI specifications
  - Automatic type detection from YAML values (Integer, Long, Boolean, String, Double, etc.)
  - Supports: Integer, Long, Double, Float, Boolean, String, BigDecimal, BigInteger
  - Type detection rules: `1, 2, 3` (no quotes) → Integer, `"1"` (quotes) → String, `true/false` → Boolean
- Templates embedded in JAR for easier distribution
  - Moved from `/templates` to `src/main/resources/cef/`
  - Set `embeddedTemplateDir = "cef"` for classpath loading
- FileSpec enum with Lombok for clean template management
  - Combines template name and generated file name
  - Used throughout CefCodegen for maintainability

### Changed
- **Major Refactoring**: Production-ready code cleanup
  - Use `var` instead of explicit types throughout
  - Inline single-use variables for compact code
  - Static imports for enum constants
  - Helper methods for architectural layers (addProtocolLayer, addRoutingLayer, etc.)
- Clean import handling via cleanupModelImports()
  - Removes Swagger annotations (ApiModel, ApiModelProperty)
  - Removes Jackson annotations (JsonNullable)
  - Removes OAS schema annotations
- Java 17 source/target compatibility (was Java 8)
- Comprehensive JavaDoc for all public methods and classes
- Example OpenAPI spec updated with enum custom fields

### Fixed
- Template compilation errors with fallbackHandler field
- CEF API compatibility (getElements, getBytes methods)
- RouteTree field visibility issues

---

## [1.0.2] - 2026-01-03

### Fixed
- Template name references in CefCodegen
  - Fixed apiCefRequestHandler template name
  - Corrected file name mappings
  - Resolved build failures from incorrect template paths

---

## [1.0.1] - 2026-01-03

### Added
- **Fallback Handler Support**: Method-specific fallback routes
  - `withFallback(HttpMethod, handler)` - different fallback per HTTP method
  - Allows serving static files for GET, returning 404 for POST, etc.
  - `RouteTree.setFallback()` method
  - Documentation and examples in README

### Changed
- README updated with fallback handler documentation and examples
- Version bumped to 1.0.1

---

## [1.0.0] - 2026-01-01

### Added
- **Initial Release**: Complete CEF OpenAPI Generator implementation
- **RouteTree Routing**: Trie-based URL pattern matching
  - 2.6x faster than regex-based routing
  - Support for path variables: `/api/users/{id}`
  - Multiple path variables: `/api/users/{userId}/posts/{postId}`
  - Literal-first matching priority (literals beat templates)
  - LRU cache for pattern routes (100 entry limit)
  - Exact routes with O(1) lookup
  - Prefix routes for namespace matching (`/static/*`)
  - Contains routes for substring matching (`.min.js`)
- **Two-Level Service Architecture**:
  - Wrapper methods (`handleXxx`) - HTTP layer with ApiResponse control
  - Business methods (`xxx`) - pure domain logic
  - Default implementations throw NotImplementedException
  - Override either wrapper or business method based on needs
- **Protocol Layer**:
  - `ApiRequest` - request wrapper with lazy parsing for path variables, query params, body
  - `ApiResponse<T>` - generic response wrapper with builder pattern
  - `HttpMethod` enum - GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD
- **CEF Integration Layer**:
  - `ApiCefRequestHandler` - main request handler extending CefRequestHandlerAdapter
  - `ApiCefRequestHandlerBuilder` - fluent builder pattern for configuration
  - `ApiResourceRequestHandler` - resource-level handler converting CEF to ApiRequest/ApiResponse
  - `ApiResponseHandler` - converts ApiResponse to CEF ResourceHandler format
- **Routing Layer**:
  - `RouteTree` - trie structure with LRU caching
  - `RouteNode` - trie node representing path segments
- **Exception Hierarchy**:
  - `ApiException` - base exception with HTTP status codes
  - `BadRequestException` (400)
  - `NotFoundException` (404)
  - `InternalServerErrorException` (500)
  - `NotImplementedException` (501)
- **Utility Layer**:
  - `ContentTypeResolver` - MIME type resolution for 18+ file extensions
  - Supports: HTML, CSS, JS, JSON, images (PNG, JPEG, GIF, SVG), fonts (WOFF, TTF), documents (PDF, XML, ZIP, TXT)
- **Builder Pattern**:
  - `withApiRoutes()` - register all generated routes from OpenAPI spec
  - `withPrefix(prefix, method, handler)` - prefix-based routing
  - `withExact(path, method, handler)` - exact path matching
  - `withContains(substring, method, handler)` - substring matching
  - HTTP method-specific routing (different handlers for GET vs POST on same path)
- **Model Generation**:
  - Plain POJOs with manual builders (no Lombok in generated code)
  - Enum support with Jackson serialization
  - Clean imports (no Swagger or unnecessary annotations)
- **GitHub Packages Publishing**:
  - Automated publishing to GitHub Packages
  - Maven coordinates: `io.github.cef:generator:VERSION`
  - GitHub Actions CI/CD workflow
- **Documentation**:
  - Comprehensive README with installation, usage, examples
  - JavaDoc for all public APIs
  - Example project with openapi.yaml

### Technical Details
- Java 8+ compatible (later updated to Java 17+)
- Zero runtime dependencies (only Jackson for JSON)
- Extends OpenAPI Generator AbstractJavaCodegen
- Mustache templates for code generation
- MIT License

**See [MIGRATION.md](MIGRATION.md) for detailed migration guide from 1.0.x to 1.1.0.**

---

## Version History Summary

| Version | Date | Key Changes |
|---------|------|-------------|
| **1.1.0** | 2026-01-10 | Type-safe params, interceptors, exception handler, CORS, comprehensive testing (186 tests) |
| **1.0.5** | 2026-01-04 | URL filtering, enum custom fields, production refactoring |
| **1.0.2** | 2026-01-03 | Template name fixes |
| **1.0.1** | 2026-01-03 | Fallback handler support |
| **1.0.0** | 2026-01-01 | Initial release - RouteTree, two-level architecture, CEF integration |

---

## Future Roadmap (Potential Features)

### Under Consideration
- Request validation from OpenAPI constraints (minLength, maxLength, pattern, etc.)
- Boolean and Enum query parameter support
- Array/List query parameter support
- Default values for query parameters
- Async support (CompletableFuture)
- Health check endpoint generation
- Metrics endpoint (Prometheus format)

**Note:** Features listed above are under consideration and not yet implemented. Feedback and contributions welcome!

---

## Support

- **Issues**: https://github.com/a-havrysh/cef-openapi-generator/issues
- **Documentation**: [README.md](README.md)
- **Testing Guide**: [TESTING.md](TESTING.md)
- **Example Project**: [example/README.md](example/README.md)

---

## License

MIT License - see [LICENSE](LICENSE) file for details.
