package com.programmersbox.testing.chess

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChessViewModel(private val engine: ChessEngine) : ViewModel() {
    var piecePicked by mutableStateOf<Piece?>(null)
    var squarePicked by mutableStateOf<Square?>(null)

    val possibleMoves =  mutableStateListOf<Square>()
    val attackedMoves = mutableStateListOf<Square>()

    val currentTurn = engine.currentTurn()

    init {
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
                    squarePicked?.let { engine.makeMove(it, square) }
                    null
                }
            }
        }
    }

    operator fun get(square: Square) = engine[square]
}