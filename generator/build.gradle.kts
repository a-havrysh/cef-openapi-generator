plugins {
    id("java")
    id("maven-publish")
}

group = "io.github.cef"
version = "1.0.0-SNAPSHOT"

dependencies {
    implementation("org.openapitools:openapi-generator:7.18.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            artifact(tasks.register("templateJar", Jar::class) {
                archiveClassifier.set("templates")
                from(projectDir.parent + "/templates")
            })
        }
    }
}
