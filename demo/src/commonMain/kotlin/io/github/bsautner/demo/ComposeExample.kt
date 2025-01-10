package io.github.bsautner.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.bsautner.TestBuild
import io.github.bsautner.ksp.*
import io.github.bsautner.ksp.annotations.KRouting
import io.ktor.resources.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Transient



data class MyUserInterface(val composable: Unit) : KomposeResponse({ composable })



@KRouting()
@Resource("/test")
class ComposeExample: Kompose {


    @Contextual
    override var render: @Composable () -> @Contextual KomposeResponse = {
       MyUserInterface(ComposeExampleComposable())
    }


//    @Transient
//    override var render: () -> KResponse = { ComposeTest() }

}