package com.programmersbox.testing.components.lookahead

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.programmersbox.testing.LocalTextMoveComposable
import com.programmersbox.testing.Screens
import com.programmersbox.testing.navigate
import com.programmersbox.testing.ui.theme.LocalNavController

@Composable
fun SceneScope.SharedElementTestScreenA() {
    val navController = LocalNavController.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { navController.navigate(Screens.SharedElementScreenB) },
        contentAlignment = Alignment.Center
    ) {
        LocalTextMoveComposable.current(this@SharedElementTestScreenA, Modifier)
    }
}

@Composable
fun SceneScope.SharedElementTestScreenB() {
    val navController = LocalNavController.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { navController.popBackStack() },
        contentAlignment = Alignment.CenterStart
    ) {
        LocalTextMoveComposable.current(this@SharedElementTestScreenB, Modifier)
    }
}