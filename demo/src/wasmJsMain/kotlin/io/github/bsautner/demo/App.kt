package io.github.bsautner.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
 @Composable
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                ComposeExample.render()
        }
    }
}

@Composable
fun formTest() {
   Box(Modifier.fillMaxSize()) {
       Text("Hello World!2", Modifier.align(Alignment.Center))
       var textState by remember { mutableStateOf("Hello") }

       TextField(
           value = textState,
           onValueChange = { textState = it },
           label = { Text("Label") }

       )
       Button(onClick = {
           println("Hello World!")
       }, Modifier.align(Alignment.Center)) {
           Text("Greeting")
       }

}

}
