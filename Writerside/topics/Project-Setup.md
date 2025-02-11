# Project Setup

Kobol uses KSP for code generation so you must apply the ksp plugin and kobol-ksp as a ksp dependence as welll as kobol-api as an api.

```
plugins {
     alias(libs.plugins.ksp)
}


ksp {
       arg("output-dir",  project.layout.buildDirectory.get().asFile.absolutePath + "/generated/ksp")
       arg("project", project.name)
}

dependencies {
    api("io.github.bsautner:kobold-api:$kobol_version")
    ksp("io.github.bsautner:kobold-api:$kobol_version")

}
```