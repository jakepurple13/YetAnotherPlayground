package com.programmersbox.testing

import androidx.compose.runtime.Composable
import com.programmersbox.testing.chess.ChessScreenPreview
import com.programmersbox.testing.components.dynamicthemeloading.DynamicThemeLoadingPreview
import com.programmersbox.testing.components.limitedbottomsheetscaffold.LimitedBottomSheetScaffoldPreview
import com.programmersbox.testing.components.mediaplayer.AudioPlayerScreen
import com.programmersbox.testing.pokedex.detail.PokemonDetailScreen
import com.programmersbox.testing.pokedex.list.PokedexScreen

enum class Screens(
    val screen: @Composable () -> Unit
) {
    MainScreen(screen = { MainScreen() }),
    LimitedBottomSheetScaffoldScreen(screen = { LimitedBottomSheetScaffoldPreview() }),
    ChessScreen(screen = { ChessScreenPreview() }) {
        override val route: String get() = "$name/{difficulty}"
    },
    Pokedex(screen = { PokedexScreen() }),
    PokedexDetail(screen = { PokemonDetailScreen() }) {
        override val route: String get() = "$name/{name}"
    },
    DynamicThemeLoadingScreen(screen = { DynamicThemeLoadingPreview() }),
    AudioPlayer(screen = { AudioPlayerScreen() });

    open val route: String = name

    companion object {
        private val noGoScreens = listOf(MainScreen, PokedexDetail)
        val destinations = values().filter { it !in noGoScreens }
    }
}