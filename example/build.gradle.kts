buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("org.openapitools:openapi-generator-gradle-plugin:7.18.0")
        classpath("io.github.cef:generator:1.0.3")
    }
}

plugins {
    id("java")
    id("me.champeau.jmh") version "0.7.2"
}

apply(plugin = "org.openapi.generator")

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")

    // CEF dependencies (compile-only for now, can be upgraded later)
    compileOnly("me.friwi:jcefmaven:141.0.10")

    // IntelliJ Platform dependencies (compile-only)
    compileOnly("org.jetbrains:annotations:26.0.1")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
    testImplementation("org.mockito:mockito-inline:5.2.0") // For mocking final classes
    testImplementation("org.assertj:assertj-core:3.24.2")

    // Test needs CEF classes for mocking
    testImplementation("me.friwi:jcefmaven:141.0.10")

    // JMH dependencies (automatically added by plugin)
    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

val generateApi by tasks.registering(org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("cef")
    inputSpec.set("$projectDir/openapi.yaml")
    outputDir.set("$buildDir/generated")

    modelPackage.set("com.example.api.dto")
    apiPackage.set("com.example.api")

    configOptions.set(mapOf(
        "hideGenerationTimestamp" to "true"
    ))
}

tasks.compileJava {
    dependsOn(generateApi)
}

sourceSets {
    main {
        java {
            srcDir("$buildDir/generated/src/main/java")
        }
    }
}

tasks.test {
    useJUnitPlatform()

    // Add JVM arguments for Mockito compatibility with Java 24+
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

jmh {
    iterations.set(3)
    warmupIterations.set(2)
    fork.set(1)
    threads.set(1)

    benchmarkMode.set(listOf("thrpt", "avgt")) // Throughput and average time
    timeUnit.set("ms")

    includes.set(listOf(".*Benchmark.*"))

    resultFormat.set("JSON")
    resultsFile.set(project.file("${project.buildDir}/reports/jmh/results.json"))
}
