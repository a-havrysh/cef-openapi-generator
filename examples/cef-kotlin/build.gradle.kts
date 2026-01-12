buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("org.openapitools:openapi-generator-gradle-plugin:7.18.0")
        classpath("io.github.cef:generator:3.0.0")
    }
}

plugins {
    kotlin("jvm") version "1.9.23"
    id("jacoco")
}

apply(plugin = "org.openapi.generator")

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
    explicitApi = org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Disabled
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    // CEF dependencies (compile-only for now, can be upgraded later)
    compileOnly("me.friwi:jcefmaven:141.0.10")

    // IntelliJ Platform dependencies (compile-only)
    compileOnly("org.jetbrains:annotations:26.0.1")

    // Test dependencies
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.23")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("io.mockk:mockk:1.13.8")

    // Test needs CEF classes for mocking
    testImplementation("me.friwi:jcefmaven:141.0.10")
}

val generateApi by tasks.registering(org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("cef-kotlin")
    inputSpec.set("$projectDir/openapi.yaml")
    outputDir.set("$buildDir/generated")

    modelPackage.set("com.example.api.dto")
    apiPackage.set("com.example.api")

    configOptions.set(mapOf(
        "hideGenerationTimestamp" to "true",
        "packageName" to "com.example.api"
    ))
}

// Temporary: rename .java files to .kt (API service files still generate as .java)
val renameKotlinFiles by tasks.registering {
    dependsOn(generateApi)
    doLast {
        val kotlinSrcDir = file("$buildDir/generated/src/main/kotlin")
        kotlinSrcDir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension == "java") {
                val newFile = File(file.parent, file.nameWithoutExtension + ".kt")
                file.renameTo(newFile)
            }
        }
    }
}

// Post-processing task to clean up generated Kotlin files
// Remove unnecessary Java imports and Kotlin built-in type imports
val cleanupKotlinImports by tasks.registering {
    dependsOn(renameKotlinFiles)
    doLast {
        val kotlinSrcDir = file("$buildDir/generated/src/main/kotlin")
        kotlinSrcDir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension == "kt") {
                val lines = file.readLines()
                val filteredLines = lines.filter { line ->
                    if (line.startsWith("import ")) {
                        val isBadJavaUtilImport = (line.contains("java.util.List") ||
                                line.contains("java.util.ArrayList") ||
                                line.contains("java.util.Arrays") ||
                                line.contains("java.util.Set") ||
                                line.contains("java.util.Map") ||
                                line.contains("java.util.HashMap") ||
                                line.contains("java.util.HashSet"))

                        val isKotlinTypeImport = (line.endsWith(".Int") ||
                                line.endsWith(".Long") ||
                                line.endsWith(".String") ||
                                line.endsWith(".List") ||
                                line.endsWith(".Map") ||
                                line.endsWith(".Set"))

                        // Only remove self-imports from model files (dto package)
                        // API service files NEED to import dto and exception classes
                        val isModelFile = file.path.contains("/dto/")
                        val isSelfImport = isModelFile && (
                                line.contains("import com.example.api.dto."))

                        !isBadJavaUtilImport && !isKotlinTypeImport && !isSelfImport
                    } else {
                        true
                    }
                }
                file.writeText(filteredLines.joinToString("\n") + "\n")
            }
        }
    }
}

tasks.compileKotlin {
    dependsOn(cleanupKotlinImports)
}

sourceSets {
    main {
        kotlin {
            srcDir("$buildDir/generated/src/main/kotlin")
        }
    }
}

tasks.test {
    useJUnitPlatform()

    jvmArgs(
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens", "java.base/java.util=ALL-UNNAMED",
        "--add-opens", "java.base/java.io=ALL-UNNAMED",
        "--add-opens", "java.base/java.net=ALL-UNNAMED",
        "-Dnet.bytebuddy.experimental=true"
    )

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = false
    }

    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    classDirectories.setFrom(files(classDirectories.files.map {
        fileTree(it) {
            include("**/com/example/api/**")
        }
    }))

    sourceDirectories.setFrom(files(
        "$buildDir/generated/src/main/kotlin",
        "src/main/kotlin"
    ))
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}
