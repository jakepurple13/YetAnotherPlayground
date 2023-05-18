package com.programmersbox.testing.pokedex.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.programmersbox.testing.Screens
import com.programmersbox.testing.pokedex.Pokemon
import com.programmersbox.testing.pokedex.database.LocalPokedexDatabase
import com.programmersbox.testing.pokedex.database.toPokemon
import com.programmersbox.testing.ui.theme.LocalNavController
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.palette.PalettePlugin
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PokedexScreen() {
    val pokedexDatabase = LocalPokedexDatabase.current
    val vm = viewModel { PokedexViewModel(pokedexDatabase) }

    val entries = vm.pager.collectAsLazyPagingItems()

    val listState = rememberLazyGridState()
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pokedex") },
                actions = {
                    IconButton(
                        onClick = { scope.launch { pokedexDatabase.pokemonDao().clearAll() } }
                    ) { Icon(Icons.Default.Delete, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                state = listState
            ) {
                items(
                    count = entries.itemCount,
                    key = entries.itemKey { it.url },
                    contentType = entries.itemContentType { it }
                ) {
                    entries[it]?.toPokemon()?.let { pokemon ->
                        PokedexEntry(
                            pokemon = pokemon,
                            onClick = {
                                navController.navigate(
                                    Screens.PokedexDetail.route.replace("{name}", pokemon.name)
                                ) { launchSingleTop = true }
                            }
                        )
                    } ?: CircularProgressIndicator()
                }
            }
            AnimatedVisibility(
                visible = entries.loadState.append is LoadState.Loading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PokedexEntry(
    pokemon: Pokemon,
    onClick: () -> Unit
) {
    val surface = MaterialTheme.colorScheme.surface
    val defaultSwatch = SwatchInfo(
        rgb = surface,
        bodyColor = Color.Blue,
        titleColor = contentColorFor(surface)
    )
    var swatchInfo by remember { mutableStateOf(defaultSwatch) }
    ElevatedCard(
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = swatchInfo.rgb,
            contentColor = swatchInfo.titleColor
        ),
        modifier = Modifier.sizeIn(minHeight = 250.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxSize()
        ) {
            Text(pokemon.pokedexEntry)
            val latestSwatch by rememberUpdatedState(newValue = swatchInfo)
            GlideImage(
                imageModel = { pokemon.imageUrl },
                component = rememberImageComponent {
                    +PalettePlugin { p ->
                        if (latestSwatch == defaultSwatch) {
                            p.dominantSwatch?.let { s ->
                                swatchInfo = SwatchInfo(
                                    rgb = s.rgb.toComposeColor(),
                                    titleColor = s.titleTextColor.toComposeColor(),
                                    bodyColor = s.bodyTextColor.toComposeColor()
                                )
                            }
                        }
                    }
                },
                loading = {
                    Box(modifier = Modifier.matchParentSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            )
            Text(
                pokemon.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

private data class SwatchInfo(val rgb: Color, val titleColor: Color, val bodyColor: Color)

fun Int.toComposeColor() = Color(this)
