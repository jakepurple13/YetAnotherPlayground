package com.programmersbox.testing

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.programmersbox.testing.chess.ChessScreenPreview
import com.programmersbox.testing.components.GaugeScreen
import com.programmersbox.testing.components.NeonScreen
import com.programmersbox.testing.components.dynamicthemeloading.DynamicThemeLoadingPreview
import com.programmersbox.testing.components.limitedbottomsheetscaffold.LimitedBottomSheetScaffoldPreview
import com.programmersbox.testing.components.lookahead.LookaheadCustomTest
import com.programmersbox.testing.components.lookahead.LookaheadWithLazyColumn
import com.programmersbox.testing.components.lookahead.SceneScope
import com.programmersbox.testing.components.lookahead.ScreenSizeChangeDemo
import com.programmersbox.testing.components.lookahead.SharedElementExplorationDemo
import com.programmersbox.testing.components.lookahead.SharedElementTestScreenA
import com.programmersbox.testing.components.lookahead.SharedElementTestScreenB
import com.programmersbox.testing.components.lookahead.SharedElementWithNavDemo
import com.programmersbox.testing.components.lookahead.SharedElementWithNavDemo1
import com.programmersbox.testing.components.lookahead.SharedElementWithNavDemo2
import com.programmersbox.testing.components.mediaplayer.AudioPlayerScreen
import com.programmersbox.testing.pokedex.detail.PokemonDetailScreen
import com.programmersbox.testing.pokedex.list.PokedexScreen
import com.programmersbox.testing.poker.GameScreen
import com.programmersbox.testing.poker.Poker

enum class Screens(
    val screen: @Composable SceneScope.() -> Unit
) {
    MainScreen(screen = { MainScreen() }),
    LimitedBottomSheetScaffoldScreen(screen = { LimitedBottomSheetScaffoldPreview() }),
    ChessScreen(screen = { ChessScreenPreview() }) {
        override val route: String get() = "$name/{difficulty}"
    },
    Pokedex(screen = { PokedexScreen() }),
    ClearPokedexDatabase(screen = { ClearPokedexDb() }),
    PokedexDetail(screen = { PokemonDetailScreen() }) {
        override val route: String get() = "$name/{name}"
    },
    DynamicThemeLoadingScreen(screen = { DynamicThemeLoadingPreview() }),
    AudioPlayer(screen = { AudioPlayerScreen() }),
    CardGame(screen = { GameScreen() }),
    PokerGame(screen = { Poker() }),
    LookaheadFun(screen = { LookaheadWithLazyColumn() }),
    LookaheadCustom(screen = { LookaheadCustomTest() }),
    SharedElementDemo(screen = { SharedElementExplorationDemo() }),
    SharedElementNavDemo(screen = { SharedElementWithNavDemo() }),
    SharedElementNav1Demo(screen = { SharedElementWithNavDemo1() }),
    SharedElementNav2Demo(screen = { SharedElementWithNavDemo2() }),
    ScreenSize(screen = { ScreenSizeChangeDemo() }),
    SharedElementScreenA(screen = { SharedElementTestScreenA() }),
    SharedElementScreenB(screen = { SharedElementTestScreenB() }),
    GaugeView(screen = { GaugeScreen() }),
    Neon(screen = { NeonScreen() })
    ;

    open val route: String = name

    companion object {
        private val noGoScreens = listOf(MainScreen, PokedexDetail)
        val destinations = values().filter { it !in noGoScreens }
    }
}

fun NavHostController.navigate(screens: Screens) = navigate(screens.route)