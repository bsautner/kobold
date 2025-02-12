/*
 * Copyright (c) 2025 Benjamin Sautner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package io.github.bsautner.kobold

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import kotlin.jvm.Transient
import kotlin.reflect.KClass

@Serializable
open class KJsonResponse : KResponse

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Kobold()

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KoboldStatic(val path: String)


interface KResponse
interface KRequest


/**
 * Classes implementing this interface will result in Kobol creating Composables.
 * You can create data classes that @see KResponse
 *
 * @param render lambda to invoke as a composable
 *
 * @property invoke call this from inside a Composable to include the rendered output.
 *
 * @sample
 *
 * @Composable
 * fun App() {
 *     MaterialTheme {
 *         var showContent by remember { mutableStateOf(false) }
 *         Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
 *             ComposeExample.render()
 *         }
 *     }
 * }
 *
 */
open class KCompose(@Transient val render: @Composable () -> Unit) : KResponse {
	@Composable
	operator fun invoke() {
		render()
	}
}
