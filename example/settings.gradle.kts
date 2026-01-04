rootProject.name = "example"

includeBuild("..") {
    dependencySubstitution {
        substitute(module("io.github.cef:generator")).using(project(":generator"))
    }
}
