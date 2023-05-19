package com.programmersbox.testing.pokedex.list

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import com.programmersbox.testing.pokedex.PokedexService
import com.programmersbox.testing.pokedex.database.PokedexDatabase
import com.programmersbox.testing.pokedex.database.PokemonRemoteMediator
import com.programmersbox.testing.pokedex.database.toPokemon
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class PokedexViewModel(
    pokedexDatabase: PokedexDatabase,
) : ViewModel() {

    private val dao = pokedexDatabase.pokemonDao()

    var pokemonSort by mutableStateOf(PokemonSort.Index)

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
}

enum class PokemonSort(val icon: ImageVector) {
    Index(Icons.Default.Numbers),
    Alphabetical(Icons.Default.SortByAlpha)
}