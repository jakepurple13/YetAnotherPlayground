package com.programmersbox.testing.pokedex.database

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [PokemonDb::class, PokemonInfoDb::class, SavedPokemon::class],
    version = 3,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)]
)
@TypeConverters(PokemonConverters::class)
abstract class PokedexDatabase : RoomDatabase() {

    abstract fun pokemonDao(): PokemonDao
    abstract fun pokemonInfoDao(): PokemonInfoDao
    abstract fun savedPokemonDao(): SavedPokemonDao

    companion object {

        @Volatile
        private var INSTANCE: PokedexDatabase? = null

        fun getInstance(context: Context): PokedexDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also {
                    INSTANCE = it
                }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                PokedexDatabase::class.java,
                "pokedex.db"
            ).build()
    }
}

val LocalPokedexDatabase = staticCompositionLocalOf<PokedexDatabase> { error("Nothing here!") }