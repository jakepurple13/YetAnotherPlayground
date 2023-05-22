package com.programmersbox.testing.pokedex.list

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import com.programmersbox.settings.PokedexSettings
import com.programmersbox.settings.PokedexViewType
import com.programmersbox.settings.copy
import com.programmersbox.testing.pokedex.PokedexService
import com.programmersbox.testing.pokedex.database.PokedexDatabase
import com.programmersbox.testing.pokedex.database.PokemonRemoteMediator
import com.programmersbox.testing.pokedex.database.toPokemon
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PokedexViewModel(
    pokedexDatabase: PokedexDatabase,
    private val pokedexSettings: DataStore<PokedexSettings>,
) : ViewModel() {

    private val dao = pokedexDatabase.pokemonDao()

    var pokemonSort by mutableStateOf(PokemonSort.Index)
    var pokemonListType by mutableStateOf(PokemonListType.Grid)

    init {
        pokedexSettings.data
            .map {
                when (it.viewType) {
                    PokedexViewType.Grid -> PokemonListType.Grid
                    PokedexViewType.List -> PokemonListType.List
                    else -> PokemonListType.Grid
                }
            }
            .onEach { pokemonListType = it }
            .launchIn(viewModelScope)
    }

    @OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
    val pager = snapshotFlow { pokemonSort }
        .flatMapLatest { sort ->
            Pager(
                PagingConfig(
                    pageSize = PokedexService.PAGE_SIZE + 1,
                    enablePlaceholders = true,
                    initialLoadSize = PokedexService.PAGE_SIZE
                ),
                remoteMediator = PokemonRemoteMediator(pokedexDatabase),
                pagingSourceFactory = {
                    when (sort) {
                        PokemonSort.Index -> dao.getPokemonPaging()
                        PokemonSort.Alphabetical -> dao.getPokemonPagingAlphabet()
                    }
                }
            )
                .flow
                .map { it.map { p -> p.toPokemon() } }
                .cachedIn(viewModelScope)
        }

    var searchQuery by mutableStateOf("")

    val searchList = snapshotFlow { searchQuery }
        .flatMapLatest { dao.searchPokemon("%$it%") }

    fun toggleViewType() {
        viewModelScope.launch {
            pokedexSettings.updateData {
                it.copy {
                    viewType = when (pokemonListType) {
                        PokemonListType.Grid -> PokedexViewType.List
                        PokemonListType.List -> PokedexViewType.Grid
                    }
                }
            }
        }
    }
}

enum class PokemonSort(val icon: ImageVector) {
    Index(Icons.Default.Numbers),
    Alphabetical(Icons.Default.SortByAlpha)
}

enum class PokemonListType(val icon: ImageVector) {
    Grid(Icons.Default.GridView),
    List(Icons.Default.ViewList);
}