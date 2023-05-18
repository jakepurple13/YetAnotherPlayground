package com.programmersbox.testing.pokedex.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmersbox.testing.pokedex.PokedexService
import com.programmersbox.testing.pokedex.PokemonInfo
import com.programmersbox.testing.pokedex.database.PokedexDatabase
import com.programmersbox.testing.pokedex.database.toPokemonInfo
import com.programmersbox.testing.pokedex.database.toPokemonInfoDb
import kotlinx.coroutines.launch

class PokemonDetailViewModel(
    savedStateHandle: SavedStateHandle,
    pokedexDatabase: PokedexDatabase
) : ViewModel() {
    private val name: String? by lazy { savedStateHandle["name"] }
    var pokemonInfo: DetailState by mutableStateOf(DetailState.Loading)

    private val dao = pokedexDatabase.pokemonInfoDao()

    init {
        load()
    }

    fun load() {
        pokemonInfo = DetailState.Loading
        viewModelScope.launch {
            name?.let { n ->
                val fromDb = dao
                    .getPokemonInfo(n)
                    ?.toPokemonInfo()
                    ?.let { DetailState.Success(it) }
                pokemonInfo = fromDb ?: PokedexService.fetchPokemon(n)
                    .onSuccess { dao.insertPokemonInfo(it.toPokemonInfoDb()) }
                    .fold(
                        onSuccess = { DetailState.Success(it) },
                        onFailure = { DetailState.Error }
                    )
            }
        }
    }
}

sealed class DetailState {
    class Success(val pokemonInfo: PokemonInfo) : DetailState()

    object Loading : DetailState()

    object Error : DetailState()
}