package com.programmersbox.testing

import androidx.compose.runtime.Composable
import com.programmersbox.testing.chess.ChessScreenPreview
import com.programmersbox.testing.components.dynamicthemeloading.DynamicThemeLoadingPreview
import com.programmersbox.testing.components.limitedbottomsheetscaffold.LimitedBottomSheetScaffoldPreview

enum class Screens(
    val screen: @Composable () -> Unit
) {
    MainScreen(screen = { MainScreen() }),
    LimitedBottomSheetScaffoldScreen(screen = { LimitedBottomSheetScaffoldPreview() }),
    ChessScreen(screen = { ChessScreenPreview() }) {
        override val route: String get() = "$name/{difficulty}"
    },
    DynamicThemeLoadingScreen(screen = { DynamicThemeLoadingPreview() });

    open val route: String = name

    companion object {
        val destinations = values().filter { it != MainScreen }
    }
}