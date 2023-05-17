package com.programmersbox.testing.pokedex.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmersbox.testing.pokedex.PokedexService
import com.programmersbox.testing.pokedex.PokemonInfo
import kotlinx.coroutines.launch

class PokemonDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val name: String? by lazy { savedStateHandle["name"] }
    var pokemonInfo: DetailState by mutableStateOf(DetailState.Loading)

    init {
        load()
    }

    fun load() {
        pokemonInfo = DetailState.Loading
        viewModelScope.launch {
            name?.let {
                pokemonInfo = PokedexService.fetchPokemon(it)
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