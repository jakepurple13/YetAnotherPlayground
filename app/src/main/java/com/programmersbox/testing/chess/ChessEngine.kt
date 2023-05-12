package com.programmersbox.testing.chess

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking

class ChessEngine {
    private val board = Board()
    private val moves = mutableListOf<Move>()

    fun currentTurn(): Flow<Color> = board.turnToMove

    suspend fun makeMove(from: Square, to: Square) {
        if (board[from]?.getPossibleMoves(board, from)?.contains(to) == true) {
            moves.add(Move(from, to))
            board.movePiece(from, to)
        }
    }

    fun undoMove() {
        if (moves.isEmpty()) {
            return
        }

        val move = moves.removeLast()
        board.movePiece(move.to, move.from)
    }

    fun isCheckMate(): Boolean {
        // TODO: Implement this method.
        return false
    }

    fun isStalemate(): Boolean {
        // TODO: Implement this method.
        return false
    }

    fun isDraw(): Boolean {
        // TODO: Implement this method.
        return false
    }

    suspend fun getPossibleMoves(piece: Piece): List<Square> {
        return piece.getPossibleMoves(board, board[piece])
    }

    suspend fun getPiecesAttacking(piece: Piece): List<Square> {
        return piece.willBeAttacked(board, getPossibleMoves(piece))
    }

    operator fun get(square: Square) = board[square]

    companion object {
        const val BOARD_SIZE = 8
    }

}

data class Board(
    private val pieces: MutableMap<Square, Piece> = chessBoardMapSetup()
) : MutableMap<Square, Piece> by pieces {
    val turnToMove = MutableStateFlow(Color.White)

    operator fun get(row: Int, col: Int) = pieces[Square(row, col)]
    operator fun get(piece: Piece) = pieces.entries.find { it.value == piece }!!.key
    operator fun iterator() = pieces.iterator()

    fun movePiece(from: Square, to: Square): Result<Move> = runCatching {
        //require(pieces[from]?.color != turnToMove) { "Wrong Turn" }
        val piece = pieces.remove(from) ?: throw IllegalArgumentException("No piece at $from")
        pieces[to] = piece
        pieces[from] = NoPiece
        piece.moved(pieces)
        piece.hasMoved = true
        Move(from, to)
    }.onSuccess { turnToMove.value = !turnToMove.value }

    fun isEmpty(square: Square) = pieces.contains(square)

    companion object {
        private fun chessBoardMapSetup() = mutableMapOf<Square, Piece>().apply {
            for (i in 0..ChessEngine.BOARD_SIZE) {
                for (j in 0..ChessEngine.BOARD_SIZE) {
                    this[Square(i, j)] = NoPiece
                }
            }

            this[Square(0, 0)] = Rook(Color.White)
            this[Square(0, 1)] = Knight(Color.White)
            this[Square(0, 2)] = Bishop(Color.White)
            this[Square(0, 3)] = Queen(Color.White)
            this[Square(0, 4)] = King(Color.White)
            this[Square(0, 5)] = Bishop(Color.White)
            this[Square(0, 6)] = Knight(Color.White)
            this[Square(0, 7)] = Rook(Color.White)
            repeat(8) { this[Square(1, it)] = Pawn(Color.White) }

            this[Square(7, 0)] = Rook(Color.Black)
            this[Square(7, 1)] = Knight(Color.Black)
            this[Square(7, 2)] = Bishop(Color.Black)
            this[Square(7, 3)] = Queen(Color.Black)
            this[Square(7, 4)] = King(Color.Black)
            this[Square(7, 5)] = Bishop(Color.Black)
            this[Square(7, 6)] = Knight(Color.Black)
            this[Square(7, 7)] = Rook(Color.Black)
            repeat(8) { this[Square(6, it)] = Pawn(Color.Black) }
        }

        fun create() = Board(chessBoardMapSetup())
    }

}

data class Square(val row: Int, val col: Int)

data class Move(val from: Square, val to: Square)

enum class Color {
    Black,
    White,
    None;

    operator fun not() = when (this) {
        Black -> White
        White -> Black
        else -> None
    }
}

sealed class Piece(
    val symbol: String,
    val color: Color,
) {
    var hasMoved = false

    protected abstract suspend fun getMoves(board: Board, square: Square): List<Square>

    suspend fun getPossibleMoves(board: Board, square: Square): List<Square> {
        val piece = board[square] ?: return emptyList()
        return piece.getMoves(board, square).filter { board[it]?.color != piece.color }
    }

    abstract val icon: String

    open fun moved(pieces: MutableMap<Square, Piece>) {}

    suspend fun willBeAttacked(board: Board, moves: List<Square>) = runBlocking {
        async {
            moves.apmap { move ->
                val tempBoard = board.copy().toMutableMap()
                val from = tempBoard.entries
                        .find { it.value.color == color && it.value is King }!!.key
                tempBoard[move] = this@Piece
                tempBoard[from] = NoPiece
                if (isAttacked(tempBoard, move)) move else null
            }
        }.await().filterNotNull()
    }

    private fun <A, B> List<A>.apmap(f: suspend (A) -> B): List<B> = runBlocking {
        map { async { f(it) } }.map { it.await() }
    }

    private suspend fun isAttacked(map: MutableMap<Square, Piece>, square: Square): Boolean {
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = map[Square(row, col)]
                if (piece !is NoPiece && piece!!.color != color) {
                    val possibleMoves = map
                        .filter { it.value.color != color }
                        .filter { it.value !is King } //TODO: Need to change this
                        .flatMap { it.value.getPossibleMoves(Board(map), it.key) }
                    for (move in possibleMoves) {
                        if (move == square) {
                            return true
                        }
                    }
                }
            }
        }

        return false
    }
}

class Bishop(color: Color) : Piece("B", color) {
    override val icon: String = when (color) {
        Color.Black -> "♝"
        Color.White -> "♗"
        else -> ""
    }

    override suspend fun getMoves(board: Board, square: Square): List<Square> =
        getDiagonalMoves(this, board, square)
}

class Queen(color: Color) : Piece("Q", color) {
    override val icon: String = when (color) {
        Color.Black -> "♛"
        Color.White -> "♕"
        else -> ""
    }

    override suspend fun getMoves(board: Board, square: Square): List<Square> {
        val moves = mutableListOf<Square>()

        // Get bishop-like moves
        moves.addAll(getDiagonalMoves(this, board, square))

        // Get rook-like moves
        moves.addAll(getHorizontalAndVerticalMoves(board, square))

        return moves
    }
}

class King(color: Color) : Piece("K", color) {
    override val icon: String = when (color) {
        Color.Black -> "♚"
        Color.White -> "♔"
        else -> ""
    }

    override suspend fun getMoves(board: Board, square: Square): List<Square> {
        val moves = mutableListOf<Square>()
        val row = square.row
        val col = square.col

        for (i in -1..1) {
            for (j in -1..1) {
                if (i == 0 && j == 0) continue
                val targetRow = row + i
                val targetCol = col + j
                val targetSquare = Square(targetRow, targetCol)

                if (board[targetSquare]?.color != color) {
                    moves.add(targetSquare)
                }
            }
        }

        moves.removeAll(willBeAttacked(board, moves)) //TODO: Need to fiddle with this

        // TODO: add castling moves

        return moves
    }

    override fun moved(pieces: MutableMap<Square, Piece>) {

    }
}

class Pawn(color: Color) : Piece("P", color) {
    override val icon: String = when (color) {
        Color.Black -> "♟︎"
        Color.White -> "♙"
        else -> ""
    }

    override suspend fun getMoves(board: Board, square: Square): List<Square> {
        val moves = mutableListOf<Square>()

        // Determine the direction the pawn moves based on its color
        val forwardDirection = if (color == Color.White) 1 else -1

        // Check if the pawn can move forward one square
        val oneSquare = Square(square.row + forwardDirection, square.col)
        val forwardOneSquare = board[oneSquare]
        if (forwardOneSquare is NoPiece) {
            moves.add(oneSquare)
        }
        // If the pawn hasn't moved yet, check if it can move forward two squares
        if (!hasMoved) {
            val twoSquares = Square(square.row + 2 * forwardDirection, square.col)
            val forwardTwoSquares = board[twoSquares]
            if (forwardTwoSquares is NoPiece) moves.add(twoSquares)
        }

        // Check if the pawn can capture diagonally
        val leftSquare = Square(square.row + forwardDirection, square.col - 1)
        val leftDiagonal = board[square.row + forwardDirection, square.col - 1]
        if (leftDiagonal !is NoPiece && leftDiagonal?.color != color) {
            moves.add(leftSquare)
        }

        val rightSquare = Square(square.row + forwardDirection, square.col + 1)
        val rightDiagonal = board[rightSquare]
        if (rightDiagonal !is NoPiece && rightDiagonal?.color != color) {
            moves.add(rightSquare)
        }

        //TODO: add en passant

        return moves
    }
}

private fun getMovesInDirection(
    board: Board,
    square: Square,
    rowOffset: Int,
    colOffset: Int
): List<Square> {
    val moves = mutableListOf<Square>()
    var (row, col) = square
    do {
        row += rowOffset
        col += colOffset
        if (row in 0..7 && col in 0..7) {
            val dest = Square(row, col)
            val destPiece = board[dest]
            if (destPiece is NoPiece) {
                moves.add(dest)
            } else {
                moves.add(dest)
                break
            }
        } else {
            break
        }
    } while (true)
    return moves
}

class Rook(color: Color) : Piece("R", color) {
    override val icon: String = when (color) {
        Color.Black -> "♜"
        Color.White -> "♖"
        else -> ""
    }

    override suspend fun getMoves(board: Board, square: Square): List<Square> =
        getHorizontalAndVerticalMoves(board, square)
}

class Knight(color: Color) : Piece("N", color) {
    override val icon: String = when (color) {
        Color.Black -> "♞"
        Color.White -> "♘"
        else -> ""
    }

    override suspend fun getMoves(board: Board, square: Square): List<Square> {
        val (row, col) = square
        return listOf(
            Square(row + 2, col + 1),
            Square(row + 2, col - 1),
            Square(row - 2, col + 1),
            Square(row - 2, col - 1),
            Square(row + 1, col + 2),
            Square(row + 1, col - 2),
            Square(row - 1, col + 2),
            Square(row - 1, col - 2),
        )
            .filter { it.row in 0..7 && it.col in 0..7 }
            .filter { board[it]?.color != color }
    }
}

// Helper function to get diagonal moves
private fun getDiagonalMoves(
    piece: Piece,
    board: Board,
    square: Square
): List<Square> {
    val moves = mutableListOf<Square>()
    val color = piece.color

    // Check diagonal moves in direction (1, 1)
    for (i in 1..7) {
        val nextSquare = Square(square.row + i, square.col + i)
        if (nextSquare.row > 7 || nextSquare.col > 7) break // out of board
        if (board[nextSquare]?.color == color) break // can't capture own piece
        moves.add(nextSquare)
        if (board[nextSquare] !is NoPiece) break // can't move past opponent's piece
    }

    // Check diagonal moves in direction (1, -1)
    for (i in 1..7) {
        val nextSquare = Square(square.row + i, square.col - i)
        if (nextSquare.row > 7 || nextSquare.col < 0) break // out of board
        if (board[nextSquare]?.color == color) break // can't capture own piece
        moves.add(nextSquare)
        if (board[nextSquare] !is NoPiece) break // can't move past opponent's piece
    }

    // Check diagonal moves in direction (-1, 1)
    for (i in 1..7) {
        val nextSquare = Square(square.row - i, square.col + i)
        if (nextSquare.row < 0 || nextSquare.col > 7) break // out of board
        if (board[nextSquare]?.color == color) break // can't capture own piece
        moves.add(nextSquare)
        if (board[nextSquare] !is NoPiece) break // can't move past opponent's piece
    }

    // Check diagonal moves in direction (-1, -1)
    for (i in 1..7) {
        val nextSquare = Square(square.row - i, square.col - i)
        if (nextSquare.row < 0 || nextSquare.col < 0) break // out of board
        if (board[nextSquare]?.color == color) break // can't capture own piece
        moves.add(nextSquare)
        if (board[nextSquare] !is NoPiece) break // can't move past opponent's piece
    }

    return moves
}

private fun getHorizontalAndVerticalMoves(board: Board, square: Square) = listOf(
    getMovesInDirection(board, square, 1, 0),
    getMovesInDirection(board, square, -1, 0),
    getMovesInDirection(board, square, 0, 1),
    getMovesInDirection(board, square, 0, -1),
).flatten()

object NoPiece : Piece("", color = Color.None) {
    override suspend fun getMoves(board: Board, square: Square): List<Square> = emptyList()
    override val icon: String = ""
}
