package com.programmersbox.testing.chess

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChessViewModel(
    savedStateHandle: SavedStateHandle,
    difficultyString: ChessAi.DifficultyLevel = runCatching {
        ChessAi.DifficultyLevel.valueOf(savedStateHandle.get<String>("difficulty")!!)
    }.fold(
        onSuccess = { it },
        onFailure = { ChessAi.DifficultyLevel.Multiplayer }
    ),
    private var engine: ChessEngine = ChessEngine()
) : ViewModel() {
    var piecePicked by mutableStateOf<Piece?>(null)
    var squarePicked by mutableStateOf<Square?>(null)

    val possibleMoves = mutableStateListOf<Move>()
    val attackedMoves = mutableStateListOf<Move>()

    val moves = mutableStateListOf<Move>()

    val currentTurn = engine.currentTurn()

    private val ai: MutableStateFlow<ChessAi> =
        MutableStateFlow(difficultyString.create(Color.White, engine.chessBoard))

    var difficulty by mutableStateOf(difficultyString)

    init {
        engine.setChessListener(
            object : ChessListener {
                override fun promotion(piece: Piece, square: Square) {
                    engine.setPiece(Queen(piece.color), square)
                }

                override fun moved(move: Move) {
                    moves.add(move)
                }
            }
        )

        snapshotFlow { difficulty }
            .onEach { ai.value = it.create(Color.White, engine.chessBoard) }
            .launchIn(viewModelScope)

        snapshotFlow { piecePicked }
            .onEach {
                possibleMoves.clear()
                attackedMoves.clear()
                it?.let {
                    possibleMoves.addAll(engine.getPossibleMoves(it))
                    attackedMoves.addAll(engine.getPiecesAttacking(it))
                }
            }
            .launchIn(viewModelScope)
    }

    fun pickUpPiece(piece: Piece?, square: Square) {
        viewModelScope.launch {
            piecePicked = when {
                piecePicked == null && piece !is NoPiece -> {
                    squarePicked = square
                    piece
                }

                square == squarePicked -> null
                else -> {
                    squarePicked?.let {
                        engine.makeMove(it, square)
                            .onSuccess {
                                withContext(Dispatchers.IO) { ai.value.makeMove() }
                            }
                    }
                    null
                }
            }
        }
    }

    fun getBoard(): Map<Square, Piece> = engine.chessBoard

    operator fun get(square: Square) = engine[square]
}