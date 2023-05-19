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
import com.programmersbox.testing.pokedex.PokedexService
import com.programmersbox.testing.pokedex.database.PokedexDatabase
import com.programmersbox.testing.pokedex.database.PokemonRemoteMediator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest

class PokedexViewModel(
    private val pokedexDatabase: PokedexDatabase,
) : ViewModel() {

    var pokemonSort by mutableStateOf(PokemonSort.Index)

    @OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
    val pager = snapshotFlow { pokemonSort }
        .flatMapLatest {
            Pager(
                PagingConfig(
                    pageSize = PokedexService.PAGE_SIZE + 1,
                    enablePlaceholders = true,
                    initialLoadSize = PokedexService.PAGE_SIZE
                ),
                remoteMediator = PokemonRemoteMediator(pokedexDatabase),
                pagingSourceFactory = {
                    when (it) {
                        PokemonSort.Index -> pokedexDatabase.pokemonDao().getPokemonPaging()
                        PokemonSort.Alphabetical -> pokedexDatabase.pokemonDao()
                            .getPokemonPagingAlphabet()
                    }
                }
            )
                .flow
                .cachedIn(viewModelScope)
        }

    var searchQuery by mutableStateOf("")

    val searchList = snapshotFlow { searchQuery }
        .flatMapLatest { pokedexDatabase.pokemonDao().searchPokemon("%$it%") }
}

enum class PokemonSort(val icon: ImageVector) {
    Index(Icons.Default.Numbers),
    Alphabetical(Icons.Default.SortByAlpha)
}