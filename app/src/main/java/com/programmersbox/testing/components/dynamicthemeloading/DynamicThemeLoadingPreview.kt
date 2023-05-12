package com.programmersbox.testing.components.dynamicthemeloading

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicThemeLoadingPreview() {
    val vm = viewModel<DynamicThemeLoadingPreviewViewModel>()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val dominantColorState = rememberDominantColorState { color ->
        color.contrastAgainst(surfaceColor) >= 3f
    }

    LaunchedEffect(vm.selected) {
        vm.selected
            ?.download_url
            ?.let { dominantColorState.updateColorsFromImageUrl(it) } ?: dominantColorState.reset()
    }

    DynamicThemePrimaryColorsFromImage(dominantColorState) {
        val topScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Dynamic Theme") },
                    scrollBehavior = topScrollBehavior
                )
            },
            bottomBar = {
                BottomAppBar {
                    OutlinedButton(onClick = { vm.selectImage(null) }) {
                        Text("Clear Selection")
                    }
                }
            },
            modifier = Modifier.nestedScroll(topScrollBehavior.nestedScrollConnection)
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding)
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    CircularProgressIndicator()
                    ElevatedButton(onClick = {}) { Text("Elevated Button") }
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(200.dp),
                ) {
                    items(vm.pictureList) {
                        val isSelected = it == vm.selected
                        AsyncImage(
                            model = it.download_url,
                            contentDescription = null,
                            modifier = Modifier
                                .clickable { vm.selectImage(it) }
                                .size(200.dp)
                                .border(
                                    width = animateDpAsState(
                                        if (isSelected) 2.dp else 0.dp,
                                        label = ""
                                    ).value,
                                    color = animateColorAsState(
                                        if (isSelected) Color.Green else Color.Transparent,
                                        label = ""
                                    ).value
                                )
                        )
                    }
                }
            }
        }
    }
}

class DynamicThemeLoadingPreviewViewModel : ViewModel() {
    val pictureList = mutableStateListOf<PictureData>()
    var selected by mutableStateOf<PictureData?>(null)

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    init {
        viewModelScope.launch {
            runCatching {
                client.get("https://picsum.photos/v2/list?page=${Random.nextInt(1, 10)}")
                    .body<List<PictureData>>()
                    .map {
                        it.copy(
                            download_url = it.download_url
                                .split("/")
                                .dropLast(2)
                                .joinToString(separator = "/") + "/200"
                        )
                    }
                    .also { println(it) }
            }
                .onSuccess {
                    pictureList.clear()
                    pictureList.addAll(it)
                }
        }
    }

    fun selectImage(pictureData: PictureData?) {
        selected = if (selected == pictureData) {
            null
        } else {
            pictureData
        }
    }

}

@Serializable
data class PictureData(
    val id: String,
    val url: String,
    val author: String,
    val download_url: String
)

fun Color.contrastAgainst(background: Color): Float {
    val fg = if (alpha < 1f) compositeOver(background) else this

    val fgLuminance = fg.luminance() + 0.05f
    val bgLuminance = background.luminance() + 0.05f

    return max(fgLuminance, bgLuminance) / min(fgLuminance, bgLuminance)
}