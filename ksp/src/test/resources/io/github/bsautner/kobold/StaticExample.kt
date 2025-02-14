package io.github.bsautner.kobold

import io.ktor.resources.Resource

@KoboldStatic("wasmJs")
@Resource("/")
class StaticExample()  : KStatic