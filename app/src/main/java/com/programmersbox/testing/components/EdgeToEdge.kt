package com.programmersbox.testing.components

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.programmersbox.testing.ScaffoldTop
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdgeToEdgeDemo() {
    val darkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    var statusBarColor by remember { mutableStateOf(Color.Transparent) }
    var navigationBarColor by remember { mutableStateOf(Color.Transparent) }

    val statusColor by animateColorAsState(targetValue = statusBarColor, label = "")
    val navigationColor by animateColorAsState(targetValue = navigationBarColor, label = "")
    DisposableEffect(darkTheme, statusColor, navigationColor) {
        (context as? ComponentActivity)?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = statusColor.toArgb(),
                darkScrim = statusColor.toArgb()
            ) { darkTheme },
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = navigationColor.toArgb(),
                darkScrim = navigationColor.toArgb()
            ) { darkTheme },
        )
        onDispose {}
    }

    ScaffoldTop(title = "Edge to Edge") { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Button(
                onClick = { statusBarColor = Random.nextColor() }
            ) {
                Text("Random Status Bar Color")
            }

            Button(
                onClick = { navigationBarColor = Random.nextColor() }
            ) {
                Text("Random Navigation Bar Color")
            }
        }
    }

    PredictiveBackHandler { progress ->
        // code for gesture back started
        try {
            progress.collect { backEvent ->
                // code for progress
                println(backEvent)
                println("BackEvent Progress: " + backEvent.progress)
                println("BackEvent SwipeEdge: " + backEvent.swipeEdge)
            }
            println("BackEvent completed!")
            // code for completion
        } catch (e: CancellationException) {
            // code for cancellation
            e.printStackTrace()
        }
    }
}

fun Random.nextColor(
    red: Int = nextInt(0, 0xFF),
    green: Int = nextInt(0, 0xFF),
    blue: Int = nextInt(0, 0xFF)
): Color = Color(red, green, blue)