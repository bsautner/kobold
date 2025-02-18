package io.github.bsautner.kobold.samples.compose

import io.github.bsautner.kobold.KMenu
import io.github.bsautner.kobold.Kobold
import io.github.bsautner.kobold.compose.ComposableLambda
import kotlinx.serialization.Transient

@Kobold
sealed class ProfileMenuSample(@Transient override val render: ComposableLambda = {},
                               @Transient override val onClick: (String) -> Unit = {}): KMenu<String>(render = render, onClick = onClick) {

	data object MenuItem1 : ProfileMenuSample(
		render = {
			println("I'm rendering 1!")
		},
		onClick = {
			println("Click 1!")
		}) {


		data object MenuItem2Sub1 : ProfileMenuSample(
			render = {
				println("I'm rendering 2 Sub 1!")
			},
			onClick = {
				println("Click 2 Sub 1!")
			})

	}

}