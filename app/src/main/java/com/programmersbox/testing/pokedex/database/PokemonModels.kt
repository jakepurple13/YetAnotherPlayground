package com.programmersbox.testing.pokedex.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.programmersbox.testing.pokedex.Pokemon
import com.programmersbox.testing.pokedex.PokemonDescription
import com.programmersbox.testing.pokedex.PokemonInfo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity("Pokemon")
data class PokemonDb(
    @PrimaryKey
    val url: String,
    val name: String,
    val page: Int
)

@Entity("PokemonInfo")
data class PokemonInfoDb(
    @PrimaryKey
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<PokemonInfo.TypeResponse>,
    val stats: List<PokemonInfo.Stats>,
    val description: PokemonDescription?,
    val experience: Int
)

class PokemonConverters {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromTypes(type: List<PokemonInfo.TypeResponse>) = json.encodeToString(type)

    @TypeConverter
    fun toTypes(typeString: String) =
        json.decodeFromString<List<PokemonInfo.TypeResponse>>(typeString)

    @TypeConverter
    fun fromStats(stats: List<PokemonInfo.Stats>) = json.encodeToString(stats)

    @TypeConverter
    fun toStats(statsString: String) = json.decodeFromString<List<PokemonInfo.Stats>>(statsString)

    @TypeConverter
    fun fromDescription(description: PokemonDescription) = json.encodeToString(description)

    @TypeConverter
    fun toDescription(descriptionString: String) =
        json.decodeFromString<PokemonDescription>(descriptionString)
}

fun PokemonDb.toPokemon() = Pokemon(name = name, url = url, page = page)
fun Pokemon.toPokemonDb(newPage: Int = page) = PokemonDb(name = name, url = url, page = newPage)

fun PokemonInfoDb.toPokemonInfo() = PokemonInfo(
    id = id,
    name = name,
    height = height,
    weight = weight,
    types = types,
    stats = stats,
    pokemonDescription = description,
    experience = experience
)

fun PokemonInfo.toPokemonInfoDb() = PokemonInfoDb(
    id = id,
    name = name,
    height = height,
    weight = weight,
    types = types,
    stats = stats,
    description = pokemonDescription,
    experience = experience
)
