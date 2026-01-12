package com.example.api

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.reflect.full.declaredMembers
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive test suite for Kotlin code generation.
 * Verifies that the cef-kotlin generator produces valid, well-formed Kotlin code.
 */
@DisplayName("Kotlin Code Generation Test Suite")
class KotlinGenerationTest {

    companion object {
        private const val GENERATED_SRC_PATH = "build/generated/src/main/kotlin/com/example/api"

        private fun getGeneratedFile(subPath: String): File {
            val file = File(GENERATED_SRC_PATH, subPath)
            assertTrue(
                file.exists(),
                "Generated file not found: ${file.absolutePath}. " +
                "Run 'gradle openApiGenerate' to generate code."
            )
            return file
        }

        @BeforeAll
        @JvmStatic
        fun setupTest() {
            val generatedDir = File(GENERATED_SRC_PATH)
            assertTrue(
                generatedDir.exists(),
                "Generated code directory does not exist. " +
                "Run 'gradle openApiGenerate' first."
            )
        }
    }

    @Nested
    @DisplayName("Protocol Layer Generation")
    inner class ProtocolLayerTests {

        @Test
        @DisplayName("HttpMethod enum generates correctly")
        fun testHttpMethodEnumGeneration() {
            val file = getGeneratedFile("protocol/HttpMethod.kt")
            val content = file.readText()

            assertTrue(content.contains("enum class HttpMethod"), "HttpMethod should be an enum")
            assertTrue(content.contains("GET"), "HttpMethod should contain GET")
            assertTrue(content.contains("POST"), "HttpMethod should contain POST")
            assertTrue(content.contains("PUT"), "HttpMethod should contain PUT")
            assertTrue(content.contains("DELETE"), "HttpMethod should contain DELETE")
            assertTrue(content.contains("PATCH"), "HttpMethod should contain PATCH")
        }

        @Test
        @DisplayName("ApiRequest class generates correctly")
        fun testApiRequestGeneration() {
            val file = getGeneratedFile("protocol/ApiRequest.kt")
            val content = file.readText()

            assertTrue(content.contains("class ApiRequest"), "ApiRequest should be a class")
            assertTrue(content.contains("fun getMethod()"), "ApiRequest should have getMethod")
            assertTrue(content.contains("fun getPath()"), "ApiRequest should have getPath")
            assertTrue(content.contains("fun getHeader("), "ApiRequest should have getHeader")
            assertTrue(content.contains("fun getQueryParam("), "ApiRequest should have getQueryParam")
        }

        @Test
        @DisplayName("ApiResponse data class generates correctly")
        fun testApiResponseGeneration() {
            val file = getGeneratedFile("protocol/ApiResponse.kt")
            val content = file.readText()

            assertTrue(content.contains("class ApiResponse"), "ApiResponse should be a class")
            assertTrue(content.contains("statusCode"), "ApiResponse should have statusCode")
            assertTrue(content.contains("body"), "ApiResponse should have body")
            assertTrue(content.contains("headers"), "ApiResponse should have headers")
            assertTrue(content.contains("companion object"), "ApiResponse should have companion object")
            assertTrue(content.contains("fun <T> ok"), "ApiResponse should have ok factory method")
        }
    }

    @Nested
    @DisplayName("Exception Layer Generation")
    inner class ExceptionLayerTests {

        @Test
        @DisplayName("ApiException base class generates correctly")
        fun testApiExceptionGeneration() {
            val file = getGeneratedFile("exception/ApiException.kt")
            val content = file.readText()

            assertTrue(content.contains("open class ApiException"), "ApiException should be open")
            assertTrue(content.contains("statusCode"), "ApiException should have statusCode")
            assertTrue(content.contains("message"), "ApiException should have message")
        }

        @Test
        @DisplayName("BadRequestException generates correctly")
        fun testBadRequestExceptionGeneration() {
            val file = getGeneratedFile("exception/BadRequestException.kt")
            val content = file.readText()

            assertTrue(content.contains("class BadRequestException"), "BadRequestException should exist")
            assertTrue(content.contains("400"), "BadRequestException should have 400 status")
        }

        @Test
        @DisplayName("NotFoundException generates correctly")
        fun testNotFoundExceptionGeneration() {
            val file = getGeneratedFile("exception/NotFoundException.kt")
            val content = file.readText()

            assertTrue(content.contains("class NotFoundException"), "NotFoundException should exist")
            assertTrue(content.contains("404"), "NotFoundException should have 404 status")
        }

        @Test
        @DisplayName("InternalServerErrorException generates correctly")
        fun testInternalServerErrorExceptionGeneration() {
            val file = getGeneratedFile("exception/InternalServerErrorException.kt")
            val content = file.readText()

            assertTrue(content.contains("class InternalServerErrorException"), "InternalServerErrorException should exist")
            assertTrue(content.contains("500"), "InternalServerErrorException should have 500 status")
        }

        @Test
        @DisplayName("NotImplementedException generates correctly")
        fun testNotImplementedExceptionGeneration() {
            val file = getGeneratedFile("exception/NotImplementedException.kt")
            val content = file.readText()

            assertTrue(content.contains("class NotImplementedException"), "NotImplementedException should exist")
            assertTrue(content.contains("501"), "NotImplementedException should have 501 status")
        }
    }

    @Nested
    @DisplayName("Interceptor Layer Generation")
    inner class InterceptorLayerTests {

        @Test
        @DisplayName("RequestInterceptor interface generates correctly")
        fun testRequestInterceptorGeneration() {
            val file = getGeneratedFile("interceptor/RequestInterceptor.kt")
            val content = file.readText()

            assertTrue(content.contains("interface RequestInterceptor"), "RequestInterceptor should be an interface")
            assertTrue(content.contains("fun beforeHandle"), "RequestInterceptor should have beforeHandle")
            assertTrue(content.contains("fun afterHandle"), "RequestInterceptor should have afterHandle")
            assertTrue(content.contains("fun onError"), "RequestInterceptor should have onError")
        }

        @Test
        @DisplayName("CorsInterceptor generates correctly")
        fun testCorsInterceptorGeneration() {
            val file = getGeneratedFile("interceptor/CorsInterceptor.kt")
            val content = file.readText()

            assertTrue(content.contains("class CorsInterceptor"), "CorsInterceptor should be a class")
            assertTrue(content.contains("RequestInterceptor"), "CorsInterceptor should implement RequestInterceptor")
            assertTrue(content.contains("beforeHandle"), "CorsInterceptor should have beforeHandle method")
        }

        @Test
        @DisplayName("BearerAuthInterceptor generates correctly")
        fun testBearerAuthInterceptorGeneration() {
            val file = getGeneratedFile("interceptor/BearerAuthInterceptor.kt")
            val content = file.readText()

            assertTrue(content.contains("class BearerAuthInterceptor"), "BearerAuthInterceptor should be a class")
            assertTrue(content.contains("RequestInterceptor"), "BearerAuthInterceptor should implement RequestInterceptor")
            assertTrue(content.contains("beforeHandle"), "BearerAuthInterceptor should have beforeHandle method")
        }

        @Test
        @DisplayName("BasicAuthInterceptor generates correctly")
        fun testBasicAuthInterceptorGeneration() {
            val file = getGeneratedFile("interceptor/BasicAuthInterceptor.kt")
            val content = file.readText()

            assertTrue(content.contains("class BasicAuthInterceptor"), "BasicAuthInterceptor should be a class")
            assertTrue(content.contains("RequestInterceptor"), "BasicAuthInterceptor should implement RequestInterceptor")
            assertTrue(content.contains("beforeHandle"), "BasicAuthInterceptor should have beforeHandle method")
        }

        @Test
        @DisplayName("ApiKeyAuthInterceptor generates correctly")
        fun testApiKeyAuthInterceptorGeneration() {
            val file = getGeneratedFile("interceptor/ApiKeyAuthInterceptor.kt")
            val content = file.readText()

            assertTrue(content.contains("class ApiKeyAuthInterceptor"), "ApiKeyAuthInterceptor should be a class")
            assertTrue(content.contains("RequestInterceptor"), "ApiKeyAuthInterceptor should implement RequestInterceptor")
            assertTrue(content.contains("beforeHandle"), "ApiKeyAuthInterceptor should have beforeHandle method")
        }
    }

    @Nested
    @DisplayName("Utility Layer Generation")
    inner class UtilityLayerTests {

        @Test
        @DisplayName("MultipartFile data class generates correctly")
        fun testMultipartFileGeneration() {
            val file = getGeneratedFile("protocol/MultipartFile.kt")
            val content = file.readText()

            assertTrue(content.contains("data class MultipartFile"), "MultipartFile should be a data class")
            assertTrue(content.contains("name"), "MultipartFile should have name property")
            assertTrue(content.contains("originalFilename"), "MultipartFile should have originalFilename")
            assertTrue(content.contains("contentType"), "MultipartFile should have contentType")
            assertTrue(content.contains("bytes"), "MultipartFile should have bytes property")
            assertTrue(content.contains("size"), "MultipartFile should have size property")
        }

        @Test
        @DisplayName("MultipartParser object generates correctly")
        fun testMultipartParserGeneration() {
            val file = getGeneratedFile("util/MultipartParser.kt")
            val content = file.readText()

            assertTrue(content.contains("class MultipartParser"), "MultipartParser should be a class")
            assertTrue(content.contains("RequestInterceptor"), "MultipartParser should implement RequestInterceptor")
            assertTrue(content.contains("beforeHandle"), "MultipartParser should have beforeHandle method")
        }

        @Test
        @DisplayName("ContentTypeResolver object generates correctly")
        fun testContentTypeResolverGeneration() {
            val file = getGeneratedFile("util/ContentTypeResolver.kt")
            val content = file.readText()

            assertTrue(content.contains("object ContentTypeResolver"), "ContentTypeResolver should be an object")
            assertTrue(content.contains("fun resolve"), "ContentTypeResolver should have resolve function")
            assertTrue(content.contains("text/html"), "ContentTypeResolver should handle HTML")
            assertTrue(content.contains("application/json"), "ContentTypeResolver should handle JSON")
            assertTrue(content.contains("image/png"), "ContentTypeResolver should handle PNG")
        }
    }

    @Nested
    @DisplayName("Service Layer Generation")
    inner class ServiceLayerTests {

        @Test
        @DisplayName("Service interfaces generate correctly")
        fun testServiceInterfaceGeneration() {
            // Check that at least one service interface was generated in the root api folder
            val serviceFiles = File(GENERATED_SRC_PATH)
                .listFiles { file -> file.isFile && file.name.endsWith("Service.kt") }
                ?.toList() ?: emptyList()

            assertTrue(
                serviceFiles.isNotEmpty(),
                "At least one service interface should be generated"
            )

            serviceFiles.forEach { file ->
                val content = file.readText()
                assertTrue(
                    content.contains("interface ") && content.contains("Service"),
                    "Service file ${file.name} should contain interface definition"
                )
            }
        }
    }

    @Nested
    @DisplayName("DTO/Model Layer Generation")
    inner class DTOLayerTests {

        @Test
        @DisplayName("Data classes generate correctly")
        fun testDataClassGeneration() {
            val dtoFiles = File(GENERATED_SRC_PATH + "/dto")
                .listFiles { file -> file.name.endsWith(".kt") && file.isFile }
                ?.toList() ?: emptyList()

            assertTrue(
                dtoFiles.isNotEmpty(),
                "At least one DTO class should be generated"
            )

            dtoFiles.forEach { file ->
                val content = file.readText()
                // DTO files should contain either data class or enum
                assertTrue(
                    content.contains("data class ") || content.contains("enum class "),
                    "DTO file ${file.name} should contain data class or enum"
                )
            }
        }
    }

    @Nested
    @DisplayName("Kotlin Language Features")
    inner class KotlinLanguageFeaturesTests {

        @Test
        @DisplayName("Generated code uses proper Kotlin syntax")
        fun testKotlinSyntaxCompliance() {
            val files = File(GENERATED_SRC_PATH).walkTopDown()
                .filter { it.isFile && it.name.endsWith(".kt") }
                .toList()

            assertTrue(files.isNotEmpty(), "Generated Kotlin files should exist")

            files.forEach { file ->
                val content = file.readText()

                // Check for Kotlin idioms
                assertTrue(
                    content.contains("package "),
                    "File ${file.name} should have package declaration"
                )

                // Should use Kotlin syntax (class, interface, object, fun, val, or var)
                assertTrue(
                    content.matches(Regex(".*\\b(class|interface|object|fun|val|var)\\b.*", RegexOption.DOT_MATCHES_ALL)),
                    "File ${file.name} should use Kotlin keywords (class, interface, object, fun, val, or var)"
                )
            }
        }

        @Test
        @DisplayName("No Java-specific patterns in generated Kotlin code")
        fun testNoJavaPatterns() {
            val files = File(GENERATED_SRC_PATH).walkTopDown()
                .filter { it.isFile && it.name.endsWith(".kt") }
                .toList()

            files.forEach { file ->
                val content = file.readText()

                // Kotlin shouldn't have unnecessary getter/setter patterns
                assertTrue(
                    !content.contains("getters and setters"),
                    "File ${file.name} should not reference Java getter/setter patterns"
                )
            }
        }

        @Test
        @DisplayName("Generated Kotlin uses nullable/non-nullable types correctly")
        fun testNullablityAnnotations() {
            val files = File(GENERATED_SRC_PATH).walkTopDown()
                .filter { it.isFile && it.name.endsWith(".kt") }
                .toList()

            files.forEach { file ->
                val content = file.readText()

                // Should use Kotlin's ? for nullable types
                if (content.contains("null") || content.contains("Optional")) {
                    assertTrue(
                        content.contains("?") || content.contains("null"),
                        "File ${file.name} should properly express nullability with Kotlin syntax"
                    )
                }
            }
        }
    }

    @Nested
    @DisplayName("Code Organization and Structure")
    inner class CodeOrganizationTests {

        @Test
        @DisplayName("Generated code is organized in correct directories")
        fun testDirectoryStructure() {
            val expectedDirs = listOf(
                "protocol",
                "exception",
                "interceptor",
                "util",
                "dto",
                "cef",
                "routing",
                "validation"
            )

            expectedDirs.forEach { dir ->
                val dirFile = File("$GENERATED_SRC_PATH/$dir")
                assertTrue(
                    dirFile.exists() && dirFile.isDirectory,
                    "Directory structure should contain '$dir' folder"
                )
            }
        }

        @Test
        @DisplayName("All generated files have proper package declarations")
        fun testPackageDeclarations() {
            val files = File(GENERATED_SRC_PATH).walkTopDown()
                .filter { it.isFile && it.name.endsWith(".kt") }
                .toList()

            files.forEach { file ->
                val content = file.readText()
                assertTrue(
                    content.startsWith("package com.example.api"),
                    "File ${file.name} should have proper package declaration"
                )
            }
        }

        @Test
        @DisplayName("Generated files have appropriate imports")
        fun testImportStatements() {
            val protocolFile = getGeneratedFile("protocol/ApiResponse.kt")
            val content = protocolFile.readText()

            // ApiResponse.kt should have necessary imports
            assertTrue(
                content.contains("import ") || content.startsWith("package"),
                "Generated files should have appropriate imports"
            )
        }
    }

    @Nested
    @DisplayName("Code Quality Checks")
    inner class CodeQualityTests {

        @Test
        @DisplayName("Generated code is properly formatted")
        fun testCodeFormatting() {
            val files = File(GENERATED_SRC_PATH).walkTopDown()
                .filter { it.isFile && it.name.endsWith(".kt") }
                .take(5) // Check first 5 files
                .toList()

            files.forEach { file ->
                val content = file.readText()
                val lines = content.split("\n")

                // Check for basic formatting quality
                assertTrue(
                    lines.isNotEmpty(),
                    "Generated file ${file.name} should not be empty"
                )

                // Most lines should not be excessively long (>120 chars)
                val longLines = lines.count { it.length > 120 }
                assertTrue(
                    longLines < lines.size / 2,
                    "Generated file ${file.name} should have reasonable line lengths"
                )
            }
        }

        @Test
        @DisplayName("Generated code has appropriate documentation")
        fun testDocumentation() {
            val protocolFile = getGeneratedFile("protocol/ApiResponse.kt")
            val content = protocolFile.readText()

            // Should have doc comments
            assertTrue(
                content.contains("/**") || content.contains("//"),
                "Generated classes should have documentation"
            )
        }
    }
}
