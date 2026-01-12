rootProject.name = "cef-java"

includeBuild("../..") {
    dependencySubstitution {
        substitute(module("io.github.cef:generator")).using(project(":generator"))
    }
}
