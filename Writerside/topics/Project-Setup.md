# Project Setup

```
ksp {
       arg("output-dir",  project.layout.buildDirectory.get().asFile.absolutePath + "/generated/ksp")
       arg("project", project.name)
}

dependencies {
    api("io.github.bsautner:kobold:0.0.1")
}
```