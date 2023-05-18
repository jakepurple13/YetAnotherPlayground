package com.programmersbox.testing.pokedex.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.programmersbox.testing.Screens
import com.programmersbox.testing.pokedex.Pokemon
import com.programmersbox.testing.pokedex.database.LocalPokedexDatabase
import com.programmersbox.testing.pokedex.database.PokemonDb
import com.programmersbox.testing.pokedex.database.SavedPokemon
import com.programmersbox.testing.pokedex.database.toPokemon
import com.programmersbox.testing.ui.theme.LocalNavController
import com.programmersbox.testing.ui.theme.firstCharCapital
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

    val saved by pokedexDatabase
        .savedPokemonDao()
        .savedPokemon()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val listState = rememberLazyGridState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showSearch by remember { mutableStateOf(false) }

    if (showSearch) {
        var q by remember { mutableStateOf("") }
        SearchPokemon(
            query = q,
            onQueryChange = { q = it },
            pokemonList = pokedexDatabase.pokemonDao()
                .searchPokemon("%$q%")
                .collectAsStateWithLifecycle(initialValue = emptyList())
                .value,
            onQueryClick = {
                navController.navigate(
                    Screens.PokedexDetail
                        .route
                        .replace("{name}", it.name)
                ) { launchSingleTop = true }
            },
            onDismiss = { showSearch = false }
        )
    }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(navController = navController, saved = saved)
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Pokedex") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, null)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showSearch = true }
                        ) { Icon(Icons.Default.Search, null) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFe74c3c)
                    ),
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = entries.loadState.append is LoadState.Error,
                    enter = slideInVertically { it / 2 },
                    exit = slideOutVertically { it / 2 }
                ) {
                    BottomAppBar(
                        containerColor = Color(0xFFe74c3c)
                    ) {
                        FilledTonalButton(
                            onClick = { entries.retry() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Error. Please Try Again") }
                    }
                }
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(vertical = 2.dp)
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
                        val pokemon = entries[it]?.toPokemon()
                        PokedexEntry(
                            pokemon = pokemon,
                            saved = saved,
                            onClick = {
                                pokemon?.let { p ->
                                    navController.navigate(
                                        Screens.PokedexDetail
                                            .route
                                            .replace("{name}", p.name)
                                    ) { launchSingleTop = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PokedexEntry(
    pokemon: Pokemon?,
    saved: List<SavedPokemon>,
    onClick: () -> Unit,
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
        if (pokemon != null) {
            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxSize()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(pokemon.pokedexEntry)
                    if (saved.any { it.url == pokemon.url }) {
                        Icon(Icons.Default.Favorite, null)
                    }
                }
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
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxSize()
            ) { CircularProgressIndicator() }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DrawerContent(
    navController: NavController,
    saved: List<SavedPokemon>,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        stickyHeader { TopAppBar(title = { Text("Saved Pokemon") }) }
        items(saved) {
            val surface = MaterialTheme.colorScheme.surface
            val defaultSwatch = SwatchInfo(
                rgb = surface,
                bodyColor = Color.Blue,
                titleColor = contentColorFor(surface)
            )
            var swatchInfo by remember { mutableStateOf(defaultSwatch) }
            Card(
                onClick = {
                    navController.navigate(
                        Screens.PokedexDetail
                            .route
                            .replace("{name}", it.name)
                    ) { launchSingleTop = true }
                },
                colors = CardDefaults.cardColors(
                    containerColor = swatchInfo.rgb,
                    contentColor = swatchInfo.titleColor
                )
            ) {
                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = swatchInfo.rgb,
                        headlineColor = swatchInfo.titleColor,
                        overlineColor = swatchInfo.titleColor
                    ),
                    headlineContent = { Text(it.name.firstCharCapital()) },
                    overlineContent = { Text("#${it.pokedexEntry}") },
                    leadingContent = {
                        val latestSwatch by rememberUpdatedState(newValue = swatchInfo)
                        GlideImage(
                            imageModel = { it.imageUrl },
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
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchPokemon(
    query: String,
    onQueryChange: (String) -> Unit,
    pokemonList: List<PokemonDb>,
    onQueryClick: (PokemonDb) -> Unit,
    onDismiss: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var active by rememberSaveable { mutableStateOf(true) }

    fun closeSearchBar() {
        focusManager.clearFocus()
        active = false
        onDismiss()
    }
    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { closeSearchBar() },
        active = active,
        onActiveChange = {
            active = it
            if (!active) focusManager.clearFocus()
        },
        placeholder = { Text("Search") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(Icons.Default.Cancel, null)
            }
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(pokemonList) { index, pokemon ->
                ListItem(
                    headlineContent = { Text(pokemon.name.firstCharCapital()) },
                    leadingContent = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.clickable {
                        closeSearchBar()
                        onQueryClick(pokemon)
                    }
                )
                if (index != pokemonList.lastIndex) {
                    Divider()
                }
            }
        }
    }
}

private data class SwatchInfo(val rgb: Color, val titleColor: Color, val bodyColor: Color)

fun Int.toComposeColor() = Color(this)
