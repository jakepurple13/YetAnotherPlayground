package com.programmersbox.testing

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.movableContentWithReceiverOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.programmersbox.testing.components.lookahead.SceneHost
import com.programmersbox.testing.components.lookahead.SceneScope
import com.programmersbox.testing.ui.theme.LocalNavController
import com.programmersbox.testing.ui.theme.TestingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            TestingTheme {
                AskForNotificationPermissions()
                val navController = rememberNavController()
                val textSharedMove = remember {
                    movableContentWithReceiverOf<SceneScope, Modifier> { modifier ->
                        Text(
                            "Hello!",
                            modifier = Modifier
                                .sharedElement()
                                .then(modifier)
                        )
                    }
                }
                CompositionLocalProvider(
                    LocalNavController provides navController,
                    LocalTextMoveComposable provides textSharedMove
                ) {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        SceneHost {
                            NavHost(
                                navController = navController,
                                startDestination = Screens.MainScreen.route,
                            ) {
                                Screens.values().forEach { screen ->
                                    composable(screen.route) { screen.screen(this@SceneHost) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun AskForNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            val permissions =
                rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
            LaunchedEffect(Unit) { permissions.launchPermissionRequest() }
        }
    }
}

val LocalTextMoveComposable =
    compositionLocalOf<@Composable SceneScope.(Modifier) -> Unit> { error("") }