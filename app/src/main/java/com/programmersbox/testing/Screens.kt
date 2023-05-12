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
    ChessScreen(screen = { ChessScreenPreview() }),
    DynamicThemeLoadingScreen(screen = { DynamicThemeLoadingPreview() });

    val route: String = name

    companion object {
        val destinations = values().filter { it != MainScreen }
    }
}