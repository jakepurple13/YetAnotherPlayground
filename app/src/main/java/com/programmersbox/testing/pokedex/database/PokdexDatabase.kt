package com.programmersbox.testing.pokedex.database

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [PokemonDb::class, PokemonInfoDb::class],
    version = 1
)
@TypeConverters(PokemonConverters::class)
abstract class PokedexDatabase : RoomDatabase() {

    abstract fun pokemonDao(): PokemonDao
    abstract fun pokemonInfoDao(): PokemonInfoDao

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