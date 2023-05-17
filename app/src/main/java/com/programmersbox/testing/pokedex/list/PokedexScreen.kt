package com.programmersbox.testing.pokedex.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.programmersbox.testing.Screens
import com.programmersbox.testing.pokedex.Pokemon
import com.programmersbox.testing.ui.theme.LocalNavController
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.palette.PalettePlugin
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PokedexScreen() {
    val vm = viewModel<PokedexViewModel>()

    val listState = rememberLazyGridState()
    val navController = LocalNavController.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Pokedex") })
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
                items(vm.entries, key = { it.url }, contentType = { it }) {
                    PokedexEntry(
                        pokemon = it,
                        onClick = {
                            navController.navigate(
                                Screens.PokedexDetail.route.replace("{name}", it.name)
                            ) { launchSingleTop = true }
                        }
                    )
                }
            }
            AnimatedVisibility(
                visible = vm.isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                CircularProgressIndicator()
            }
        }
    }

    InfiniteListHandler(listState = listState) { vm.loadInformation() }
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
        )
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
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

@ExperimentalFoundationApi
@Composable
fun InfiniteListHandler(
    listState: LazyGridState,
    buffer: Int = 2,
    onLoadMore: () -> Unit
) {
    var lastTotalItems = remember { -1 }
    val loadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            lastVisibleItemIndex > (totalItemsNumber - buffer) && (lastTotalItems != totalItemsNumber)
        }
    }

    LaunchedEffect(loadMore) {
        snapshotFlow { loadMore.value }
            .distinctUntilChanged()
            .collect {
                if (it) {
                    lastTotalItems = listState.layoutInfo.totalItemsCount
                    onLoadMore()
                }
            }
    }
}

private data class SwatchInfo(val rgb: Color, val titleColor: Color, val bodyColor: Color)

fun Int.toComposeColor() = Color(this)
