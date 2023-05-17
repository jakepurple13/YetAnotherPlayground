package com.programmersbox.testing.pokedex.detail

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.programmersbox.testing.pokedex.PokemonInfo
import com.programmersbox.testing.pokedex.list.toComposeColor
import com.programmersbox.testing.ui.theme.LightAndDarkPreviews
import com.programmersbox.testing.ui.theme.LocalNavController
import com.programmersbox.testing.ui.theme.TestingTheme
import com.programmersbox.testing.ui.theme.firstCharCapital
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.palette.PalettePlugin

@Composable
fun PokemonDetailScreen() {
    val vm = viewModel { PokemonDetailViewModel(createSavedStateHandle()) }

    Crossfade(targetState = vm.pokemonInfo, label = "") { target ->
        when (target) {
            DetailState.Error -> ErrorState(
                onTryAgain = {}
            )

            DetailState.Loading -> Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is DetailState.Success -> ContentScreen(pokemon = target.pokemonInfo)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorState(
    onTryAgain: () -> Unit
) {
    val navController = LocalNavController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pokedex") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
            )
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Something went wrong")
                OutlinedButton(onClick = onTryAgain) {
                    Text("Try Again")
                }
            }
        }
    }
}

@Composable
private fun ContentScreen(
    pokemon: PokemonInfo
) {
    Scaffold(
        topBar = { ContentHeader(pokemon = pokemon) }
    ) { padding -> ContentBody(pokemon = pokemon, paddingValues = padding) }
}

@Composable
private fun ContentBody(
    pokemon: PokemonInfo,
    paddingValues: PaddingValues
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        Text(
            pokemon.name.firstCharCapital(),
            style = MaterialTheme.typography.displayMedium
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            pokemon.types.forEach {
                val typeColor = Color(it.getTypeColor())
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = typeColor,
                ) {
                    Text(
                        it.type.name.firstCharCapital(),
                        color = if (typeColor.luminance() > .5)
                            MaterialTheme.colorScheme.surface
                        else
                            MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    pokemon.getWeightString(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Weight",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    pokemon.getHeightString(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Height",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Base Stats",
                style = MaterialTheme.typography.displaySmall
            )
            pokemon.stats.forEach {
                StatInfoBar(
                    color = MaterialTheme.colorScheme.primary,
                    statType = it.stat.shortenedName,
                    statAmount = "${it.baseStat}/300",
                    statCount = it.baseStat / 300f
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            pokemon.pokemonDescription
                ?.filtered
                ?.forEach {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            headlineContent = { Text(it.version.name) },
                            supportingContent = { Text(it.flavorText) }
                        )
                    }
                }
        }
    }
}

@Composable
private fun StatInfoBar(
    color: Color,
    statType: String,
    statAmount: String,
    statCount: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Spacer(Modifier.width(24.dp))
        Text(
            statType,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(5f)
        ) {
            LinearProgressIndicator(
                statCount,
                color = color,
                trackColor = MaterialTheme.colorScheme.onSurface,
                strokeCap = StrokeCap.Round,
                modifier = Modifier.height(16.dp),
            )
            Text(
                statAmount,
                color = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@LightAndDarkPreviews
@Composable
private fun ContentBodyPreview() {
    TestingTheme {
        Surface {
            ContentBody(
                pokemon = PokemonInfo(
                    id = 0,
                    name = "Missingno",
                    height = 10,
                    weight = 10,
                    experience = 10,
                    types = listOf(
                        PokemonInfo.TypeResponse(0, PokemonInfo.Type("fighting")),
                        PokemonInfo.TypeResponse(0, PokemonInfo.Type("electric")),
                    ),
                    stats = listOf(
                        PokemonInfo.Stats(
                            100,
                            PokemonInfo.Stat("hp")
                        ),
                        PokemonInfo.Stats(
                            100,
                            PokemonInfo.Stat("attack")
                        ),
                        PokemonInfo.Stats(
                            100,
                            PokemonInfo.Stat("special-attack")
                        )
                    )
                ),
                paddingValues = PaddingValues(0.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentHeader(pokemon: PokemonInfo) {
    val navController = LocalNavController.current
    val surface = MaterialTheme.colorScheme.surface
    val defaultSwatch = SwatchInfo(
        rgb = surface,
        bodyColor = Color.Blue,
        titleColor = contentColorFor(surface)
    )
    var swatchInfoDominant by remember { mutableStateOf(defaultSwatch) }
    var swatchInfoLightVibrant by remember { mutableStateOf(defaultSwatch) }
    Column(
        modifier = Modifier
            .wrapContentSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        swatchInfoLightVibrant.rgb,
                        swatchInfoDominant.rgb
                    )
                ),
                shape = MaterialTheme.shapes.extraLarge.copy(
                    topStart = CornerSize(0.dp),
                    topEnd = CornerSize(0.dp),
                )
            )
    ) {
        TopAppBar(
            title = { Text("Pokedex") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null)
                }
            },
            actions = { Text("#${pokemon.id}") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                navigationIconContentColor = swatchInfoLightVibrant.titleColor,
                titleContentColor = swatchInfoLightVibrant.titleColor,
                actionIconContentColor = swatchInfoLightVibrant.titleColor
            )
        )
        GlideImage(
            imageModel = { pokemon.imageUrl },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(240.dp),
            component = rememberImageComponent {
                +PalettePlugin { p ->
                    p.dominantSwatch?.let { s ->
                        swatchInfoDominant = SwatchInfo(
                            rgb = s.rgb.toComposeColor(),
                            titleColor = s.titleTextColor.toComposeColor(),
                            bodyColor = s.bodyTextColor.toComposeColor()
                        )
                    }
                    p.lightVibrantSwatch?.let { s ->
                        swatchInfoLightVibrant = SwatchInfo(
                            rgb = s.rgb.toComposeColor(),
                            titleColor = s.titleTextColor.toComposeColor(),
                            bodyColor = s.bodyTextColor.toComposeColor()
                        )
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
    }
}


data class SwatchInfo(val rgb: Color, val titleColor: Color, val bodyColor: Color)