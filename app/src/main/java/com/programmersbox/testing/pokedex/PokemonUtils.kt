package com.programmersbox.testing.pokedex

import androidx.navigation.NavController
import com.programmersbox.testing.Screens

fun NavController.navigateToPokemonDetail(name: String) = navigate(
    Screens.PokedexDetail
        .route
        .replace("{name}", name)
) { launchSingleTop = true }
