package com.programmersbox.testing.pokedex.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.programmersbox.testing.pokedex.database.PokedexDatabase
import com.programmersbox.testing.pokedex.database.PokemonRemoteMediator

class PokedexViewModel(
    private val pokedexDatabase: PokedexDatabase
) : ViewModel() {

    @OptIn(ExperimentalPagingApi::class)
    val pager = Pager(
        PagingConfig(
            pageSize = 21,
            enablePlaceholders = true,
            initialLoadSize = 20
        ),
        remoteMediator = PokemonRemoteMediator(pokedexDatabase),
        pagingSourceFactory = { pokedexDatabase.pokemonDao().getPokemonPaging() }
    )
        .flow
        .cachedIn(viewModelScope)
}