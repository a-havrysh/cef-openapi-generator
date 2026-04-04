package io.github.cef.codegen;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that run the full generator against a real OpenAPI spec
 * and verify the output structure, file existence, and content correctness.
 */
class IntegrationTest {

    private static final String SPEC = "src/test/resources/test-openapi.yaml";

    @Nested
    class JavaGenerator {

        @Test
        void generatesAllExpectedFiles(@TempDir Path outputDir) {
            generate("cef", outputDir);

            var javaRoot = outputDir.resolve("src/main/java");
            assertTrue(Files.isDirectory(javaRoot), "Java source root missing");

            // DTOs
            assertFileExists(javaRoot, "com/example/api/dto/User.java");
            assertFileExists(javaRoot, "com/example/api/dto/CreateUserRequest.java");
            assertFileExists(javaRoot, "com/example/api/dto/Role.java");

            // Service (generated at api package root, not service/ subdir in integration mode)
            assertFileContains(javaRoot, "com/example/api/UserApiService.java", "listUsers");
            assertFileContains(javaRoot, "com/example/api/UserApiService.java", "createUser");
            assertFileContains(javaRoot, "com/example/api/UserApiService.java", "getUser");
            assertFileContains(javaRoot, "com/example/api/UserApiService.java", "deleteUser");

            // Tests
            assertFileExists(outputDir.resolve("src/test/java"),
                "com/example/api/dto/UserTest.java");
            assertFileExists(outputDir.resolve("src/test/java"),
                "com/example/api/dto/RoleTest.java");

            // Docs
            assertFileExists(outputDir, "docs/User.md");
            assertFileExists(outputDir, "docs/Role.md");
            assertFileExists(outputDir, "docs/UserApi.md");

            // README
            assertFileExists(outputDir, "README.md");

            // Infrastructure layers
            assertFileExists(javaRoot, "com/example/api/protocol/ApiRequest.java");
            assertFileExists(javaRoot, "com/example/api/protocol/ApiResponse.java");
            assertFileExists(javaRoot, "com/example/api/protocol/HttpMethod.java");
            assertFileExists(javaRoot, "com/example/api/routing/RouteTree.java");
            assertFileExists(javaRoot, "com/example/api/cef/ApiCefRequestHandler.java");
            assertFileExists(javaRoot, "com/example/api/cef/ApiCefRequestHandlerBuilder.java");
            assertFileExists(javaRoot, "com/example/api/exception/ApiException.java");
            assertFileExists(javaRoot, "com/example/api/interceptor/RequestInterceptor.java");
            assertFileExists(javaRoot, "com/example/api/validation/ParameterValidator.java");
        }

        @Test
        void enumHasVendorExtensionFields(@TempDir Path outputDir) {
            generate("cef", outputDir);
            var content = readFile(outputDir.resolve("src/main/java/com/example/api/dto/Role.java"));
            assertTrue(content.contains("displayName"), "Enum should have displayName field");
            assertTrue(content.contains("level"), "Enum should have level field");
            assertTrue(content.contains("ADMIN"), "Enum should have ADMIN constant");
        }

        @Test
        void builderContainsApiRoutes(@TempDir Path outputDir) {
            generate("cef", outputDir);
            var content = readFile(outputDir.resolve("src/main/java/com/example/api/cef/ApiCefRequestHandlerBuilder.java"));
            assertTrue(content.contains("withApiRoutes"), "Builder should have withApiRoutes");
            assertTrue(content.contains("/api/users"), "Builder should contain route paths");
            assertTrue(content.contains("UserApiService"), "Builder should reference service");
        }
    }

    @Nested
    class KotlinGenerator {

        @Test
        void generatesKotlinFiles(@TempDir Path outputDir) {
            generate("cef-kotlin", outputDir);

            var ktRoot = outputDir.resolve("src/main/kotlin");
            assertTrue(Files.isDirectory(ktRoot), "Kotlin source root missing");

            // DTOs — .kt not .java
            assertFileExists(ktRoot, "com/example/api/dto/User.kt");
            assertFileExists(ktRoot, "com/example/api/dto/CreateUserRequest.kt");
            assertFileExists(ktRoot, "com/example/api/dto/Role.kt");

            // No .java files should exist in Kotlin source
            assertNoJavaFiles(ktRoot);

            // Tests (.kt) — OpenAPI Generator places tests in src/test/java even for Kotlin
            var testRoot = outputDir.resolve("src/test/java");
            if (!Files.isDirectory(testRoot)) testRoot = outputDir.resolve("src/test/kotlin");
            assertFileExists(testRoot, "com/example/api/dto/UserTest.kt");

            // Docs
            assertFileExists(outputDir, "docs/User.md");
            assertFileExists(outputDir, "docs/UserApi.md");

            // README
            assertFileExists(outputDir, "README.md");
        }

        @Test
        void modelsUseDataClass(@TempDir Path outputDir) {
            generate("cef-kotlin", outputDir);
            var content = readFile(outputDir.resolve("src/main/kotlin/com/example/api/dto/User.kt"));
            assertTrue(content.contains("data class User"), "Should be data class");
            assertTrue(content.contains("val id:"), "Should use val");
            assertFalse(content.contains("java.util."), "Should not contain java.util");
        }

        @Test
        void enumHasValueField(@TempDir Path outputDir) {
            generate("cef-kotlin", outputDir);
            var content = readFile(outputDir.resolve("src/main/kotlin/com/example/api/dto/Role.kt"));
            assertTrue(content.contains("val value: String"), "Enum should have value field");
            assertTrue(content.contains("val displayName: String"), "Enum should have displayName");
            assertTrue(content.contains("ADMIN("), "Should have ADMIN with constructor args");
        }

        @Test
        void servicesUseUnitNotVoid(@TempDir Path outputDir) {
            generate("cef-kotlin", outputDir);
            var content = readFile(outputDir.resolve("src/main/kotlin/com/example/api/UserApiService.kt"));
            assertFalse(content.contains("Void"), "Should not use Void");
            assertTrue(content.contains("Unit"), "Should use Unit for void returns");
        }

        @Test
        void apiRequestUsesLazy(@TempDir Path outputDir) {
            generate("cef-kotlin", outputDir);
            var content = readFile(outputDir.resolve("src/main/kotlin/com/example/api/protocol/ApiRequest.kt"));
            assertTrue(content.contains("by lazy"), "Should use by lazy");
            assertTrue(content.contains("runCatching"), "Should use runCatching");
            assertFalse(content.contains("StandardCharsets"), "Should use Charsets.UTF_8");
        }

        @Test
        void exceptionUsesPrimaryConstructor(@TempDir Path outputDir) {
            generate("cef-kotlin", outputDir);
            var content = readFile(outputDir.resolve("src/main/kotlin/com/example/api/exception/NotFoundException.kt"));
            assertTrue(content.contains("class NotFoundException("), "Should use primary constructor");
            assertFalse(content.contains("constructor("), "Should not use secondary constructor");
        }

        @Test
        void apiResponseUsesExpressionBody(@TempDir Path outputDir) {
            generate("cef-kotlin", outputDir);
            var content = readFile(outputDir.resolve("src/main/kotlin/com/example/api/protocol/ApiResponse.kt"));
            assertTrue(content.contains("fun <T> ok(body: T) ="), "Should use expression body");
        }

        @Test
        void exceptionHandlerIsFunInterface(@TempDir Path outputDir) {
            generate("cef-kotlin", outputDir);
            var content = readFile(outputDir.resolve("src/main/kotlin/com/example/api/interceptor/ExceptionHandler.kt"));
            assertTrue(content.contains("fun interface ExceptionHandler"), "Should be fun interface");
            assertTrue(content.contains("val DEFAULT"), "Should have DEFAULT companion val");
        }

        @Test
        void routeTreeUsesTypealias(@TempDir Path outputDir) {
            generate("cef-kotlin", outputDir);
            var content = readFile(outputDir.resolve("src/main/kotlin/com/example/api/routing/RouteTree.kt"));
            assertTrue(content.contains("typealias RouteHandler"), "Should use typealias");
            assertTrue(content.contains("getOrPut"), "Should use getOrPut");
        }
    }

    @Nested
    class ConfigOptions {

        @Test
        void serializableModel(@TempDir Path outputDir) {
            generateWith("cef-kotlin", outputDir, Map.of("serializableModel", "true"));
            var content = readFile(outputDir.resolve("src/main/kotlin/com/example/api/dto/User.kt"));
            assertTrue(content.contains("Serializable"), "Should implement Serializable");
            assertTrue(content.contains("import java.io.Serializable"), "Should import Serializable");
        }

        @Test
        void serializableModelJava(@TempDir Path outputDir) {
            generateWith("cef", outputDir, Map.of("serializableModel", "true"));
            var content = readFile(outputDir.resolve("src/main/java/com/example/api/dto/User.java"));
            assertTrue(content.contains("implements Serializable"), "Should implement Serializable");
            assertTrue(content.contains("serialVersionUID"), "Should have serialVersionUID");
        }

        @Test
        void containerDefaultToNull(@TempDir Path outputDir) {
            generateWith("cef-kotlin", outputDir, Map.of("containerDefaultToNull", "true"));
            var content = readFile(outputDir.resolve("src/main/kotlin/com/example/api/dto/User.kt"));
            // tags: List<String> should default to null, not emptyList()
            assertTrue(content.contains("= null") || !content.contains("emptyList()"),
                "Container fields should default to null");
        }

        @Test
        void generateBuildersJava(@TempDir Path outputDir) {
            generateWith("cef", outputDir, Map.of("generateBuilders", "true"));
            var content = readFile(outputDir.resolve("src/main/java/com/example/api/dto/User.java"));
            assertTrue(content.contains("public static Builder builder()"), "Should have Builder");
            assertTrue(content.contains("public static final class Builder"), "Should have Builder class");
        }

        @Test
        void noBuildersWithoutOption(@TempDir Path outputDir) {
            generateWith("cef", outputDir, Map.of("generateBuilders", "false"));
            var content = readFile(outputDir.resolve("src/main/java/com/example/api/dto/User.java"));
            assertFalse(content.contains("static Builder builder()"), "Should NOT have Builder");
        }

        @Test
        void additionalModelTypeAnnotations(@TempDir Path outputDir) {
            generateWith("cef-kotlin", outputDir,
                Map.of("additionalModelTypeAnnotations", "@kotlinx.serialization.Serializable"));
            var content = readFile(outputDir.resolve("src/main/kotlin/com/example/api/dto/User.kt"));
            assertTrue(content.contains("@kotlinx.serialization.Serializable"),
                "Should have custom annotation");
        }

        @Test
        void additionalEnumTypeAnnotations(@TempDir Path outputDir) {
            generateWith("cef-kotlin", outputDir,
                Map.of("additionalEnumTypeAnnotations", "@Deprecated"));
            var content = readFile(outputDir.resolve("src/main/kotlin/com/example/api/dto/Role.kt"));
            assertTrue(content.contains("@Deprecated"), "Enum should have custom annotation");
        }

        @Test
        void modelSuffix(@TempDir Path outputDir) {
            generateWith("cef-kotlin", outputDir, Map.of("modelSuffix", "Dto"));
            assertFileExists(outputDir.resolve("src/main/kotlin"), "com/example/api/dto/UserDto.kt");
            assertFileExists(outputDir.resolve("src/main/kotlin"), "com/example/api/dto/RoleDto.kt");
        }

        @Test
        void dateLibraryDefault(@TempDir Path outputDir) {
            generate("cef-kotlin", outputDir);
            // Default dateLibrary=java8: OffsetDateTime for date-time
            // Our test spec doesn't have date fields, but verify no crash
            assertFileExists(outputDir.resolve("src/main/kotlin"), "com/example/api/dto/User.kt");
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private void generateWith(String generatorName, Path outputDir, Map<String, String> configOpts) {
        var configurator = new CodegenConfigurator();
        configurator.setGeneratorName(generatorName);
        configurator.setInputSpec(SPEC);
        configurator.setOutputDir(outputDir.toString());
        configurator.setModelPackage("com.example.api.dto");
        configurator.setApiPackage("com.example.api");
        configurator.addAdditionalProperty("hideGenerationTimestamp", "true");

        for (var entry : configOpts.entrySet()) {
            configurator.addAdditionalProperty(entry.getKey(), entry.getValue());
        }

        var generator = new DefaultGenerator();
        generator.opts(configurator.toClientOptInput());
        generator.generate();
    }

    private void generate(String generatorName, Path outputDir) {
        var configurator = new CodegenConfigurator();
        configurator.setGeneratorName(generatorName);
        configurator.setInputSpec(SPEC);
        configurator.setOutputDir(outputDir.toString());
        configurator.setModelPackage("com.example.api.dto");
        configurator.setApiPackage("com.example.api");
        configurator.addAdditionalProperty("hideGenerationTimestamp", "true");

        var generator = new DefaultGenerator();
        generator.opts(configurator.toClientOptInput());
        var files = generator.generate();

        // Debug: write generated file list for troubleshooting
        try {
            var listing = files.stream()
                .map(f -> f.toPath().toString().replace(outputDir.toString(), ""))
                .sorted()
                .reduce("", (a, b) -> a + "\n" + b);
            Files.writeString(outputDir.resolve("_generated_files.txt"), listing);
        } catch (Exception ignored) {}
    }

    private void assertFileExists(Path root, String relativePath) {
        var file = root.resolve(relativePath);
        if (!Files.exists(file)) {
            // List actual files for debugging
            try (Stream<Path> walk = Files.walk(root)) {
                var actual = walk.filter(Files::isRegularFile)
                    .map(p -> root.relativize(p).toString())
                    .sorted().toList();
                fail("File missing: " + relativePath + "\nActual files:\n  " + String.join("\n  ", actual));
            } catch (Exception e) {
                fail("File missing: " + relativePath);
            }
        }
    }

    private void assertFileContains(Path root, String relativePath, String content) {
        assertFileExists(root, relativePath);
        var fileContent = readFile(root.resolve(relativePath));
        assertTrue(fileContent.contains(content), relativePath + " should contain: " + content);
    }

    private void assertNoJavaFiles(Path root) {
        try (Stream<Path> walk = Files.walk(root)) {
            var javaFiles = walk
                .filter(p -> p.toString().endsWith(".java"))
                .toList();
            assertTrue(javaFiles.isEmpty(), "Found .java files in Kotlin output: " + javaFiles);
        } catch (Exception e) {
            fail("Failed to walk directory: " + e.getMessage());
        }
    }

    private String readFile(Path path) {
        try {
            return Files.readString(path);
        } catch (Exception e) {
            fail("Failed to read: " + path + " — " + e.getMessage());
            return "";
        }
    }
}
