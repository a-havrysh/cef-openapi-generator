buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("org.openapitools:openapi-generator-gradle-plugin:7.21.0")
        classpath("io.github.cef:generator:3.1.1")
    }
}

plugins {
    id("java")
    id("me.champeau.jmh") version "0.7.3"
    id("jacoco")
}

apply(plugin = "org.openapi.generator")

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")

    // CEF dependencies (compile-only — provided by IntelliJ at runtime)
    compileOnly("me.friwi:jcefmaven:141.0.10")

    // IntelliJ Platform dependencies (compile-only)
    compileOnly("org.jetbrains:annotations:26.0.1")

    // Test
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("me.friwi:jcefmaven:141.0.10")

    // JMH
    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

val generateApi by tasks.registering(org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("cef")
    inputSpec.set("$projectDir/openapi.yaml")
    outputDir.set("${layout.buildDirectory.get()}/generated")

    modelPackage.set("com.example.api.dto")
    apiPackage.set("com.example.api")

    configOptions.set(mapOf(
        "hideGenerationTimestamp" to "true",
        "generateBuilders" to "true"
    ))

    generateApiTests.set(false)
    generateModelTests.set(false)
    generateApiDocumentation.set(false)
    generateModelDocumentation.set(false)
}

tasks.compileJava {
    dependsOn(generateApi)
}

sourceSets {
    main {
        java {
            srcDir("${layout.buildDirectory.get()}/generated/src/main/java")
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

tasks.test {
    useJUnitPlatform()

    jvmArgs(
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens", "java.base/java.util=ALL-UNNAMED",
        "-Dnet.bytebuddy.experimental=true"
    )
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

jmh {
    iterations.set(3)
    warmupIterations.set(2)
    fork.set(1)
    threads.set(1)
    benchmarkMode.set(listOf("thrpt", "avgt"))
    timeUnit.set("ms")
    includes.set(listOf(".*Benchmark.*"))
    resultFormat.set("JSON")
    resultsFile.set(project.file("${layout.buildDirectory.get()}/reports/jmh/results.json"))
}
