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
import com.programmersbox.testing.pokedex.database.SavedPokemon
import com.programmersbox.testing.pokedex.database.toPokemonInfo
import com.programmersbox.testing.pokedex.database.toPokemonInfoDb
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PokemonDetailViewModel(
    savedStateHandle: SavedStateHandle,
    pokedexDatabase: PokedexDatabase,
) : ViewModel() {
    private val name: String? by lazy { savedStateHandle["name"] }
    var pokemonInfo: DetailState by mutableStateOf(DetailState.Loading)

    var savedPokemon by mutableStateOf<SavedPokemon?>(null)

    private val dao = pokedexDatabase.pokemonInfoDao()
    private val savedDao = pokedexDatabase.savedPokemonDao()
    private val listDao = pokedexDatabase.pokemonDao()

    init {
        load()
    }

    private fun load() {
        pokemonInfo = DetailState.Loading
        name?.let { n ->
            viewModelScope.launch {
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
            savedDao.saved(n)
                .onEach { savedPokemon = it }
                .launchIn(viewModelScope)
        }
    }

    fun save() {
        when (val state = pokemonInfo) {
            is DetailState.Success -> {
                viewModelScope.launch {
                    listDao.getSinglePokemon(state.pokemonInfo.name)?.url?.let { url ->
                        savedDao.save(
                            SavedPokemon(
                                url = url,
                                name = state.pokemonInfo.name,
                                imageUrl = state.pokemonInfo.imageUrl,
                                pokedexEntry = state.pokemonInfo.id
                            )
                        )
                    }
                }
            }

            else -> {}
        }
    }

    fun remove() {
        viewModelScope.launch {
            savedPokemon?.let { savedDao.remove(it) }
        }
    }
}

sealed class DetailState {
    class Success(val pokemonInfo: PokemonInfo) : DetailState()

    object Loading : DetailState()

    object Error : DetailState()
}