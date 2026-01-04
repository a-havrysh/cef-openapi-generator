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
}

apply(plugin = "org.openapi.generator")

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
}

val generateApi by tasks.registering(org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("cef")
    inputSpec.set("$projectDir/openapi.yaml")
    outputDir.set("$buildDir/generated")
    templateDir.set("${projectDir.parent}/templates")

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
