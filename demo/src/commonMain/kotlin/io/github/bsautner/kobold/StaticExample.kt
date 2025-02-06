package io.github.bsautner.kobold

import io.ktor.resources.Resource

@KoboldStatic("build/dist/wasmJs/productionExecutable")
@Resource("/")
class StaticExample(): KStatic