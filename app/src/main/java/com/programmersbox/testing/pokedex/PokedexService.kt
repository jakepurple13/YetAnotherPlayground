package com.programmersbox.testing.pokedex

import androidx.compose.ui.graphics.Color
import com.programmersbox.testing.ui.theme.firstCharCapital
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.appendEncodedPathSegments
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale


object PokedexService {
    private const val baseUrl = "https://pokeapi.co/api/v2/"
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.BODY
        }

        defaultRequest {
            contentType(ContentType.Application.Json)
            url.takeFrom(
                URLBuilder().takeFrom(baseUrl).appendEncodedPathSegments(url.encodedPathSegments)
            )
        }
    }

    const val PAGE_SIZE = 200

    suspend fun fetchPokemonList(page: Int) = fetchPokemonList(
        limit = PAGE_SIZE,
        offset = page * PAGE_SIZE
    )

    private suspend fun fetchPokemonList(
        limit: Int = PAGE_SIZE,
        offset: Int = 0,
    ) = runCatching {
        client.get("pokemon/?offset=$offset&limit=$limit").body<PokemonResponse>()
    }

    suspend fun fetchPokemon(name: String) = runCatching {
        client.get("pokemon/$name").body<PokemonInfo>()
            .copy(
                pokemonDescription = fetchPokemonDescription(name)
                    .onFailure { it.printStackTrace() }
                    .getOrNull()
            )
    }

    private suspend fun fetchPokemonDescription(name: String) = runCatching {
        client.get("pokemon-species/$name").body<PokemonDescription>()
    }
}

@Serializable
data class PokemonResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Pokemon>,
)

@Serializable
data class Pokemon(
    var page: Int = 0,
    val name: String,
    val url: String,
) {
    val pokedexEntry get() = url.split("/".toRegex()).dropLast(1).last()
    val imageUrl: String
        get() {
            val index = url.split("/".toRegex()).dropLast(1).last()
            return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$index.png"
        }

    val showdownImageUrl: String
        get() {
            val index = url.split("/".toRegex()).dropLast(1).last()
            return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/$index.png"
        }
}

@Serializable
data class PokemonInfo(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    @SerialName("base_experience") val experience: Int,
    val types: List<TypeResponse>,
    val stats: List<Stats>,
    var pokemonDescription: PokemonDescription? = null,
) {

    val cryUrl
        get() =
            "https://play.pokemonshowdown.com/audio/cries/${name.lowercase().replace("-", "")}.mp3"

    val imageUrl: String
        get() {
            return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
        }

    fun getWeightString(): String = String.format("%.1f KG", weight.toFloat() / 10)
    fun getHeightString(): String = String.format("%.1f M", height.toFloat() / 10)

    @Serializable
    data class TypeResponse(
        val slot: Int,
        val type: Type,
    ) {
        fun getTypeColor() = when (type.name) {
            "fighting" -> 0xff9F422A
            "flying" -> 0xff90B1C5
            "poison" -> 0xff642785
            "ground" -> 0xffAD7235
            "rock" -> 0xff4B190E
            "bug" -> 0xff179A55
            "ghost" -> 0xff363069
            "steel" -> 0xff5C756D
            "fire" -> 0xffB22328
            "water" -> 0xff2648DC
            "grass" -> 0xff007C42
            "electric" -> 0xffE0E64B
            "psychic" -> 0xffAC296B
            "ice" -> 0xff7ECFF2
            "dragon" -> 0xff378A94
            "fairy" -> 0xff9E1A44
            "dark" -> 0xff040706
            else -> 0xffB1A5A5
        }
    }

    @Serializable
    data class Type(
        val name: String,
    )

    @Serializable
    data class Stats(
        @SerialName("base_stat")
        val baseStat: Int,
        val stat: Stat,
    )

    @Serializable
    data class Stat(val name: String) {
        val shortenedName = when (name) {
            "hp" -> "HP"
            "attack" -> "ATK"
            "defense" -> "DEF"
            "speed" -> "SPD"
            "special-attack" -> "SP.ATK"
            "special-defense" -> "SP.DEF"
            else -> name
        }

        val statColor
            get() = when (name) {
                "hp" -> 0xffFF0000
                "attack" -> 0xffF08030
                "defense" -> 0xffF8D030
                "speed" -> 0xffF85888
                "special-attack" -> 0xff6890F0
                "special-defense" -> 0xff78C850
                else -> null
            }?.let { Color(it) }
    }
}

@Serializable
data class PokemonDescription(
    @SerialName("flavor_text_entries")
    val flavorTextEntries: List<FlavorText>,
) {
    val filtered by lazy {
        val tag = Locale.getDefault().language
        flavorTextEntries
            .filter { it.language.name == tag }
            .groupBy { it.flavorText }
            .map {
                FlavorText(
                    flavorText = it.key,
                    version = Version(
                        it.value.joinToString(", ") { v ->
                            v.version.name.firstCharCapital()
                        }
                    ),
                    language = FlavorLanguage(tag)
                )
            }
    }
}

@Serializable
data class FlavorText(
    @SerialName("flavor_text")
    val flavorText: String,
    val language: FlavorLanguage,
    val version: Version,
)

@Serializable
data class FlavorLanguage(val name: String)

@Serializable
data class Version(val name: String)