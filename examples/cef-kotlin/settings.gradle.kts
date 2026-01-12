rootProject.name = "cef-kotlin"

includeBuild("../..") {
    dependencySubstitution {
        substitute(module("io.github.cef:generator")).using(project(":generator"))
    }
}
