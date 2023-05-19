package com.programmersbox.testing.pokedex.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonList(pokemonList: List<PokemonDb>)

    @Query("SELECT * FROM Pokemon")
    fun getAllPokemonList(): Flow<List<PokemonDb>>

    @Query("SELECT * FROM Pokemon WHERE name LIKE :searchText")
    fun searchPokemon(searchText: String): Flow<List<PokemonDb>>

    @Query("SELECT * FROM Pokemon")
    fun getPokemonPaging(): PagingSource<Int, PokemonDb>

    @Query("SELECT * FROM Pokemon ORDER BY name")
    fun getPokemonPagingAlphabet(): PagingSource<Int, PokemonDb>

    @Query("SELECT * FROM Pokemon WHERE name = :name")
    suspend fun getSinglePokemon(name: String): PokemonDb?

    @Query("DELETE FROM Pokemon")
    suspend fun clearAll()
}

@Dao
interface PokemonInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonInfo(pokemonInfo: PokemonInfoDb)

    @Query("SELECT * FROM PokemonInfo WHERE name = :name")
    suspend fun getPokemonInfo(name: String): PokemonInfoDb?
}

@Dao
interface SavedPokemonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(pokemon: SavedPokemon)

    @Delete
    suspend fun remove(pokemon: SavedPokemon)

    @Query("SELECT * FROM SavedPokemon")
    fun savedPokemon(): Flow<List<SavedPokemon>>

    @Query("SELECT * FROM SavedPokemon WHERE name = :name")
    fun saved(name: String): Flow<SavedPokemon?>
}