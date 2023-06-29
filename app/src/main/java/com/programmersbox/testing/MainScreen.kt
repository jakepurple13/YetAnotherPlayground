package com.programmersbox.testing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.programmersbox.testing.pokedex.database.LocalPokemonDao
import com.programmersbox.testing.ui.theme.LocalNavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = LocalNavController.current
    Scaffold(
        topBar = { TopAppBar(title = { Text("Testing Playground") }) }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = padding
        ) {
            items(Screens.destinations) {
                OutlinedButton(
                    onClick = { navController.navigate(it.route) { launchSingleTop = true } }
                ) { Text(it.name) }
            }
        }
    }
}

@Composable
fun ClearPokedexDb() {
    val scope = rememberCoroutineScope()
    val p = LocalPokemonDao.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(
            onClick = { scope.launch { p.clearAll() } }
        ) { Text("Clear Pokedex Database!") }
    }
}