package com.programmersbox.testing

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.programmersbox.testing.ui.theme.LocalNavController

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