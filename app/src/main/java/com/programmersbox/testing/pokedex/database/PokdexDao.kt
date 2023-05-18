package com.programmersbox.testing.pokedex.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PokemonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonList(pokemonList: List<PokemonDb>)

    @Query("SELECT * FROM Pokemon WHERE page = :page")
    suspend fun getPokemonList(page: Int): List<PokemonDb>

    @Query("SELECT * FROM Pokemon")
    suspend fun getAllPokemonList(): List<PokemonDb>

    @Query("SELECT * FROM Pokemon")
    fun getPokemonPaging(): PagingSource<Int, PokemonDb>

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