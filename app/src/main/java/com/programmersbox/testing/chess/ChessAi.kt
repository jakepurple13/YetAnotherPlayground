package com.programmersbox.testing.chess

import com.programmersbox.testing.chess.ChessAi.Easy
import com.programmersbox.testing.chess.ChessAi.Hard
import com.programmersbox.testing.chess.ChessAi.Medium
import com.programmersbox.testing.chess.ChessAi.Multiplayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

sealed class ChessAi(
    val color: Color,
    val board: Board
) {

    class Easy(color: Color, board: Board) : ChessAi(color, board) {
        override suspend fun makeMove() {
            board.getAllPossibleMoves(color).randomOrNull()
                ?.let { board.movePiece(it.from, it.to).onSuccess { println(it) } }
        }
    }

    class Medium(color: Color, board: Board) : ChessAi(color, board) {

        override suspend fun makeMove() {
            val possibleMoves = runBlocking(Dispatchers.Default) {
                board.getAllPossibleMoves(color)
            }

            val bestMove = runBlocking(Dispatchers.Default) {
                val moveScores = possibleMoves.map { move ->
                    val tempBoard = board.tempBoard()
                    tempBoard.movePiece(move)
                    val score = evaluateBoard(tempBoard, color)
                    move to score
                }

                val maxScore = moveScores.maxByOrNull { it.second }?.second
                val bestMoves = moveScores.filter { it.second == maxScore }.map { it.first }
                bestMoves.randomOrNull()
            } ?: throw IllegalStateException("No valid move found")

            board.movePiece(bestMove)
        }

        private fun evaluateBoard(board: Board, color: Color): Int {
            val playerPieces = board.values.filter { it.color == color }
            val opponentPieces = board.values.filter { it.color != color }

            val playerScore = playerPieces.sumOf { it.value }
            val opponentScore = opponentPieces.sumOf { it.value }

            return playerScore - opponentScore
        }
    }

    class Hard(color: Color, board: Board) : ChessAi(color, board) {
        override suspend fun makeMove() {
            val bestMove = findBestMove(board, 3)
            board.movePiece(bestMove)
        }

        private suspend fun findBestMove(board: Board, depth: Int): Move {
            var bestMove: Move? = null
            var bestScore = Int.MIN_VALUE

            for (move in board.getAllPossibleMoves(color)) {
                val tempBoard = board.tempBoard()
                tempBoard.movePiece(move)

                val score = minimax(tempBoard, depth - 1, Int.MIN_VALUE, Int.MAX_VALUE, false)
                if (score > bestScore) {
                    bestScore = score
                    bestMove = move
                }
            }

            return bestMove ?: error("No move found")
        }

        private suspend fun minimax(
            board: Board,
            depth: Int,
            alpha: Int,
            beta: Int,
            isMaximizingPlayer: Boolean
        ): Int {
            if (depth == 0) {
                return evaluateBoard(board)
            }

            var alphaValue = alpha
            var betaValue = beta

            val possibleMoves = board.getAllPossibleMoves(color)

            for (move in possibleMoves) {
                val tempBoard = board.tempBoard()
                tempBoard.movePiece(move)

                val score =
                    minimax(tempBoard, depth - 1, alphaValue, betaValue, !isMaximizingPlayer)

                if (isMaximizingPlayer) {
                    alphaValue = maxOf(alphaValue, score)
                } else {
                    betaValue = minOf(betaValue, score)
                }

                if (alphaValue >= betaValue) {
                    break
                }
            }

            return if (isMaximizingPlayer) alphaValue else betaValue
        }

        private fun evaluateBoard(board: Board): Int {
            // Implement your own board evaluation function here
            // Assign scores to different pieces and positions based on their value and importance
            // Return a positive score if the board is favorable for the maximizing player (White)
            // Return a negative score if the board is favorable for the minimizing player (Black)
            // The magnitude of the score represents the strength of the board position
            val playerPieces = board.values.filter { it.color == color }
            val opponentPieces = board.values.filter { it.color != color }

            val playerScore = playerPieces.sumOf { it.value }
            val opponentScore = opponentPieces.sumOf { it.value }

            return playerScore - opponentScore
        }
    }

    class Multiplayer(color: Color, board: Board) : ChessAi(color, board) {
        override suspend fun makeMove() {}
    }

    abstract suspend fun makeMove()

    enum class DifficultyLevel {
        Easy, Medium, Hard, Multiplayer;

        fun create(color: Color, board: Board) = when (this) {
            Easy -> Easy(color, board)
            Medium -> Medium(color, board)
            Hard -> Hard(color, board)
            Multiplayer -> Multiplayer(color, board)
        }
    }
}
