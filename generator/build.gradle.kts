plugins {
    id("java")
    id("maven-publish")
    id("jacoco")
}

group = "io.github.cef"
version = "3.0.0"  // Major refactoring: modern structure, consistent naming, Kotlin idioms

dependencies {
    implementation("org.openapitools:openapi-generator:7.18.0")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    // Ensure compiled bytecode is compatible with JaCoCo 0.8.12
    options.release = 17
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("CEF OpenAPI Generator")
                description.set("OpenAPI Code Generator for Chromium Embedded Framework with JetBrains Platform integration")
                url.set("https://github.com/a-havrysh/cef-openapi-generator")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("a-havrysh")
                        name.set("Alexander Havrysh")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/a-havrysh/cef-openapi-generator.git")
                    developerConnection.set("scm:git:ssh://github.com/a-havrysh/cef-openapi-generator.git")
                    url.set("https://github.com/a-havrysh/cef-openapi-generator")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/a-havrysh/cef-openapi-generator")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.token") as String?
            }
        }
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
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()  // 80% coverage for generator code
            }
        }
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
