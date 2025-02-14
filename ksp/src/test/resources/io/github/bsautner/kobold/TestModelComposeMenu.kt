package io.github.bsautner.captracks

import androidx.compose.runtime.Composable
import io.github.bsautner.kobold.KMenu
import io.github.bsautner.kobold.Kobold
import io.github.bsautner.kobold.compose.ComposableLambda
import kotlinx.serialization.Transient


@Kobold
sealed class TestModelComposeMenu(@Transient override val render: ComposableLambda = {},
                                  @Transient override val onClick: () -> Unit = {}): KMenu(render = render, onClick = onClick) {

    data object MenuItem1 : TestModelComposeMenu(
        render = {
            println("I'm rendering 1!")
        },
        onClick = {
            println("Click 1!")
        }

    ) {


    }

    data object MenuItem2 : TestModelComposeMenu(
        render = {
            println("I'm rendering 2!")
        },
        onClick = {
            println("Click 2!")
        }

    ) {

        data object MenuItem2Sub1 : TestModelComposeMenu(
            render = {
                println("I'm rendering 2 Sub 1!")
            },
            onClick = {
                println("Click 2 Sub 1!")
            })

        data object MenuItem2Sub2 : TestModelComposeMenu(
            render = {
                println("I'm rendering 2 Sub 1!")
            },
            onClick = {
                println("Click 2 Sub 1!")
            }) {

            data object MenuItemSub2Sub1 : TestModelComposeMenu(
                render = {
                    println("I'm rendering sub 2 Sub 1!")
                },
                onClick = {
                    println("Click sub 2 Sub 1!")
                })


            data object MenuItemSub2Sub2 : TestModelComposeMenu(
                render = {
                    println("I'm rendering sub 2 Sub 2!")
                },
                onClick = {
                    println("Click sub 2 Sub 2!")
                })

        }

    }
}