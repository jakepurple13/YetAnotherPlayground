package com.programmersbox.testing.pokedex.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.programmersbox.testing.pokedex.PokedexService
import com.programmersbox.testing.pokedex.Pokemon
import kotlinx.coroutines.launch

class PokedexViewModel : ViewModel() {

    val entries = mutableStateListOf<Pokemon>()

    private var page = 0

    var isLoading by mutableStateOf(false)

    fun loadInformation() {
        isLoading = true
        viewModelScope.launch {
            PokedexService.fetchPokemonList(page)
                .onSuccess {
                    entries.addAll(it.results.distinctBy { p -> p.url })
                    page++
                }
                .onFailure { it.printStackTrace() }
            isLoading = false
        }
    }

}