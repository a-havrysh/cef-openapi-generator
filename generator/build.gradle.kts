plugins {
    id("java")
    id("maven-publish")
}

group = "io.github.cef"
version = "1.0.2"

dependencies {
    implementation("org.openapitools:openapi-generator:7.18.0")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            artifact(tasks.register("templateJar", Jar::class) {
                archiveClassifier.set("templates")
                from(projectDir.parent + "/templates")
            })

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
