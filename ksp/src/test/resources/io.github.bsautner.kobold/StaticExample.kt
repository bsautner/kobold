package io.github.bsautner.kobold

import io.github.bsautner.kobold.KStatic
import io.github.bsautner.kobold.KoboldStatic
import io.ktor.resources.Resource

@KoboldStatic("wasmJs")
@Resource("/")
class StaticExample()  : KStatic