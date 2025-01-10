package io.github.bsautner.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.bsautner.ksp.KCompose
import io.github.bsautner.ksp.KJsonResponse
import io.github.bsautner.ksp.Kompose
import io.github.bsautner.ksp.annotations.KRouting
import io.github.bsautner.ksp.introspectSerializableClass
import io.ktor.resources.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class PostBodyExample(private val firstName: String, private val lastName: String) : KJsonResponse()

class MyUserInterface(render: @Composable () -> Unit) : KCompose(render)

@KRouting(PostBodyExample::class)
@Resource("/test")
data object ComposeExample: Kompose {
    @Contextual
    override val render: KCompose = MyUserInterface {
        ComposeExampleComposable()
    }
}


@Composable
fun SampleForm(
    onSubmit: (TestData) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ageString by remember { mutableStateOf("") }

    Form(
        attrs = {
            // Prevent the default HTML form submission and call our own onSubmit
            onSubmit {
                it.preventDefault()
                // Convert ageString to int safely, or handle parsing errors
                val age = ageString.toIntOrNull() ?: 0
                onSubmit(TestData(name, age))
            }
        }
    ) {
        // Name field
        Label(attrs = { style { marginRight(8.px) } }) {
            Text("Name:")
        }
        Input(
            type = InputType.Text,
            attrs = {
                value(name)
                onInput { event: SyntheticInputEvent<String> ->
                    name = event.value
                }
            }
        )
        Br()

        // Age field
        Label(attrs = { style { marginRight(8.px) } }) {
            Text("Age:")
        }
        Input(
            type = InputType.Number,
            attrs = {
                value(ageString)
                onInput { event: SyntheticInputEvent<String> ->
                    ageString = event.value
                }
            }
        )
        Br()

        // Submit button
        Button(
            attrs = {
                style { marginTop(16.px) }
            }
        ) {
            Text("Submit")
        }
    }
}

