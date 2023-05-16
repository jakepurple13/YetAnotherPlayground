package com.programmersbox.testing.chess

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking

//Most of this was generated through Bard and ChatGPT
class ChessEngine {
    private val board = Board.create()
    private val moves = mutableListOf<Move>()

    val chessBoard get() = board

    fun setChessListener(chessListener: ChessListener?) {
        board.chessListener = chessListener
    }

    fun setPiece(piece: Piece, square: Square) {
        board[square] = piece
    }

    fun currentTurn(): Flow<Color> = board.turnToMove

    suspend fun makeMove(from: Square, to: Square): Result<Move> {
        return if (board[from]?.getPossibleMoves(board, from)?.any { it.to == to } == true) {
            //moves.add(Move(from, to))
            board.movePiece(from, to)
        } else {
            Result.failure(Throwable("Invalid move"))
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

    suspend fun getPossibleMoves(piece: Piece): List<Move> {
        return piece.getPossibleMoves(board, board[piece])
    }

    suspend fun getPiecesAttacking(piece: Piece): List<Move> {
        return piece.willBeAttacked(board, getPossibleMoves(piece).map { it.to })
    }

    operator fun get(square: Square) = board[square]

    companion object {
        const val BOARD_SIZE = 8
    }

}

interface ChessListener {
    fun promotion(piece: Piece, square: Square)

    fun taken(move: Move) {}

    fun moved(move: Move)
}

data class Board internal constructor(
    private val pieces: MutableMap<Square, Piece> = chessBoardMapSetup(),
    var chessListener: ChessListener? = null,
) : MutableMap<Square, Piece> by pieces {
    val turnToMove = MutableStateFlow(Color.White)

    operator fun get(row: Int, col: Int) = pieces[Square(row, col)]
    operator fun get(piece: Piece) = pieces.entries.find { it.value == piece }!!.key
    operator fun iterator() = pieces.iterator()

    fun movePiece(from: Square, to: Square): Result<Move> = runCatching {
        //require(pieces[from]?.color != turnToMove) { "Wrong Turn" }
        val piece = pieces.remove(from) ?: throw IllegalArgumentException("No piece at $from")
        if (pieces[to] !is NoPiece) {
            pieces[to]?.let { chessListener?.taken(Move(piece, from, to)) }
        }

        piece.moved(this, from, to)
        pieces[to] = piece
        pieces[from] = NoPiece
        piece.hasMoved = true
        chessListener?.moved(Move(piece, from, to))
        Move(piece, from, to)
    }.onSuccess { turnToMove.value = !turnToMove.value }

    fun movePiece(move: Move): Result<Move> = runCatching {
        //require(pieces[from]?.color != turnToMove) { "Wrong Turn" }
        if (pieces[move.to] !is NoPiece) {
            pieces[move.to]?.let { chessListener?.taken(move) }
        }

        move.piece.moved(this, move.from, move.to)
        pieces[move.to] = move.piece
        pieces[move.from] = NoPiece
        move.piece.hasMoved = true
        chessListener?.moved(move)
        move
    }.onSuccess { turnToMove.value = !turnToMove.value }

    fun isEmpty(square: Square) = pieces.contains(square)

    fun tempBoard() = Board(copy().toMutableMap())

    suspend fun getAllPossibleMoves(color: Color): List<Move> {
        val moves = mutableListOf<Move>()
        for (i in 0 until 8) {
            for (j in 0 until 8) {
                val piece = this[i, j] ?: continue
                if (piece.color == color) {
                    val pieceMoves = piece.getPossibleMoves(this, Square(i, j))
                    moves.addAll(pieceMoves)
                }
            }
        }
        return moves
    }

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

data class Square(val row: Int, val col: Int) {
    fun toShortString() = "($row, $col)"
}

data class Move(val piece: Piece, val from: Square, val to: Square) {
    override fun toString(): String =
        "${piece.symbol}(${piece.color.name.first()}) ${from.toShortString()} - ${to.toShortString()}"
}

class AttackedPiece(val piece: Piece, val fromSquare: Square, val squares: List<Move>)

enum class Color {
    Black,
    White,
    None;

    operator fun not() = when (this) {
        Black -> White
        White -> Black
        else -> None
    }

    val direction
        get() = when (this) {
            Black -> -1
            White -> 1
            None -> 0
        }
}

sealed class Piece(
    val symbol: String,
    val color: Color,
) {
    var hasMoved = false

    abstract val value: Int

    protected abstract suspend fun getMoves(
        board: Board,
        square: Square,
        includeKingAttacked: Boolean
    ): List<Move>

    suspend fun getPossibleMoves(
        board: Board,
        square: Square,
        includeKingAttacked: Boolean = true
    ): List<Move> {
        val piece = board[square] ?: return emptyList()
        return piece
            .getMoves(board, square, includeKingAttacked)
            .filter { board[it.to]?.color != piece.color }
    }

    abstract val icon: String

    open fun moved(board: Board, from: Square, to: Square) {}

    suspend fun willBeAttacked(
        board: Board,
        moves: List<Square>,
        includeKingAttacked: Boolean = true
    ) = runBlocking {
        async {
            moves.map { move ->
                val tempBoard = board.copy().toMutableMap()
                val from = tempBoard.entries
                    .find { it.value.color == color && it.value is King }!!.key
                tempBoard[move] = this@Piece
                tempBoard[from] = NoPiece
                if (isAttacked(tempBoard, move, includeKingAttacked)) Move(this@Piece, from, move)
                else null
            }
        }.await().filterNotNull()
    }

    private fun <A, B> List<A>.apmap(f: suspend (A) -> B): List<B> = runBlocking {
        map { async { f(it) } }.map { it.await() }
    }

    private suspend fun isAttacked(
        map: MutableMap<Square, Piece>,
        square: Square,
        includeKingAttacked: Boolean = true
    ): Boolean {
        val enemyPieces = map.values.filter { it.color != color }

        for (enemyPiece in enemyPieces) {
            val possibleMoves = enemyPiece.getPossibleMoves(
                board = Board(pieces = map),
                square = map.entries.first { it.value == enemyPiece }.key,
                includeKingAttacked = includeKingAttacked
            )
            if (possibleMoves.any { it.to == square }) {
                return true
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

    override val value: Int get() = 3

    override suspend fun getMoves(
        board: Board,
        square: Square,
        includeKingAttacked: Boolean
    ): List<Move> =
        getDiagonalMoves(this, board, square)
}

class Queen(color: Color) : Piece("Q", color) {
    override val icon: String = when (color) {
        Color.Black -> "♛"
        Color.White -> "♕"
        else -> ""
    }

    override val value: Int get() = 9

    override suspend fun getMoves(
        board: Board,
        square: Square,
        includeKingAttacked: Boolean
    ): List<Move> {
        val moves = mutableListOf<Move>()

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

    override val value: Int get() = 10

    override suspend fun getMoves(
        board: Board,
        square: Square,
        includeKingAttacked: Boolean
    ): List<Move> {
        val moves = mutableListOf<Move>()
        val row = square.row
        val col = square.col

        for (i in -1..1) {
            for (j in -1..1) {
                if (i == 0 && j == 0) continue
                val targetRow = row + i
                val targetCol = col + j
                if (
                    targetRow !in 0..ChessEngine.BOARD_SIZE ||
                    targetCol !in 0..ChessEngine.BOARD_SIZE
                ) continue

                val targetSquare = Square(targetRow, targetCol)

                if (board[targetSquare]?.color != color) {
                    moves.add(Move(this, square, targetSquare))
                }
            }
        }

        if (includeKingAttacked)
            moves.removeAll(willBeAttacked(board, moves.map { it.to }, false))

        // TODO: add castling moves

        return moves
    }

    override fun moved(board: Board, from: Square, to: Square) {

    }
}

class Pawn(color: Color) : Piece("P", color) {
    override val icon: String = when (color) {
        Color.Black -> "♟︎"
        Color.White -> "♙"
        else -> ""
    }

    override val value: Int get() = 1

    override suspend fun getMoves(
        board: Board,
        square: Square,
        includeKingAttacked: Boolean
    ): List<Move> {
        val moves = mutableListOf<Move>()

        // Determine the direction the pawn moves based on its color
        val forwardDirection = if (color == Color.White) 1 else -1

        // Check if the pawn can move forward one square
        val oneSquare = Square(square.row + forwardDirection, square.col)
        val forwardOneSquare = board[oneSquare]
        if (forwardOneSquare is NoPiece) {
            moves.add(Move(this, square, oneSquare))
        }
        // If the pawn hasn't moved yet, check if it can move forward two squares
        if (!hasMoved) {
            val twoSquares = Square(square.row + 2 * forwardDirection, square.col)
            val forwardTwoSquares = board[twoSquares]
            if (forwardTwoSquares is NoPiece) moves.add(Move(this, square, twoSquares))
        }

        // Check if the pawn can capture diagonally
        val leftSquare = Square(square.row + forwardDirection, square.col - 1)
        val leftDiagonal = board[square.row + forwardDirection, square.col - 1]
        if (leftDiagonal !is NoPiece && leftDiagonal?.color != color) {
            moves.add(Move(this, square, leftSquare))
        }

        val rightSquare = Square(square.row + forwardDirection, square.col + 1)
        val rightDiagonal = board[rightSquare]
        if (rightDiagonal !is NoPiece && rightDiagonal?.color != color) {
            moves.add(Move(this, square, rightSquare))
        }

        //TODO: add en passant

        val left = Square(square.row, square.col - 1)
        val leftB = board[left]
        if (
            leftB is Pawn && leftB.color != color && leftB.firstMove &&
            leftDiagonal is NoPiece
        ) {
            moves.add(Move(this, square, leftSquare))
        }

        val right = Square(square.row, square.col + 1)
        val rightB = board[right]
        if (
            rightB is Pawn && rightB.color != color && rightB.firstMove &&
            rightDiagonal is NoPiece
        ) {
            moves.add(Move(this, square, rightSquare))
        }

        return moves.distinct()
    }

    private var moveFirst = false
    var firstMove = false

    override fun moved(board: Board, from: Square, to: Square) {
        if (to.row == 0 || to.row == 7)
            board.chessListener?.promotion(this, to)

        if (firstMove) firstMove = false

        if (!moveFirst) {
            firstMove = true
            moveFirst = true
        }

        /*// Check if the pawn can capture diagonally
        val forwardDirection = if (color == Color.White) 1 else -1
        val leftSquare = Square(to.row + forwardDirection, to.col - 1)
        val leftDiagonal = board[leftSquare]
        val rightSquare = Square(to.row + forwardDirection, to.col + 1)
        val rightDiagonal = board[rightSquare]

        val left = Square(to.row, to.col - 1)
        val leftB = board[left]
        if (
            leftB is Pawn && leftB.color != color && leftB.firstMove &&
            leftDiagonal is NoPiece
        ) {
            board.chessListener?.taken(leftB, left)
            board[left] = NoPiece
        }

        val right = Square(to.row, to.col + 1)
        val rightB = board[right]
        if (
            rightB is Pawn && rightB.color != color && rightB.firstMove &&
            rightDiagonal is NoPiece
        ) {
            board.chessListener?.taken(rightB, right)
            board[right] = NoPiece
        }*/
    }
}

private fun getMovesInDirection(
    board: Board,
    square: Square,
    rowOffset: Int,
    colOffset: Int
): List<Move> {
    val moves = mutableListOf<Move>()
    var (row, col) = square
    do {
        row += rowOffset
        col += colOffset
        if (row in 0..7 && col in 0..7) {
            val dest = Square(row, col)
            val destPiece = board[dest]
            if (destPiece is NoPiece) {
                moves.add(Move(board[square]!!, square, dest))
            } else {
                moves.add(Move(board[square]!!, square, dest))
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

    override val value: Int get() = 5

    override suspend fun getMoves(
        board: Board,
        square: Square,
        includeKingAttacked: Boolean
    ): List<Move> =
        getHorizontalAndVerticalMoves(board, square)
}

class Knight(color: Color) : Piece("N", color) {
    override val icon: String = when (color) {
        Color.Black -> "♞"
        Color.White -> "♘"
        else -> ""
    }

    override val value: Int get() = 3

    override suspend fun getMoves(
        board: Board,
        square: Square,
        includeKingAttacked: Boolean
    ): List<Move> {
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
            .map { Move(this, square, it) }
            .filter { it.to.row in 0..7 && it.to.col in 0..7 }
            .filter { board[it.to]?.color != color }
    }
}

// Helper function to get diagonal moves
private fun getDiagonalMoves(
    piece: Piece,
    board: Board,
    square: Square
): List<Move> {
    val moves = mutableListOf<Move>()
    val color = piece.color

    // Check diagonal moves in direction (1, 1)
    for (i in 1..7) {
        val nextSquare = Square(square.row + i, square.col + i)
        if (nextSquare.row > 7 || nextSquare.col > 7) break // out of board
        if (board[nextSquare]?.color == color) break // can't capture own piece
        moves.add(Move(piece, square, nextSquare))
        if (board[nextSquare] !is NoPiece) break // can't move past opponent's piece
    }

    // Check diagonal moves in direction (1, -1)
    for (i in 1..7) {
        val nextSquare = Square(square.row + i, square.col - i)
        if (nextSquare.row > 7 || nextSquare.col < 0) break // out of board
        if (board[nextSquare]?.color == color) break // can't capture own piece
        moves.add(Move(piece, square, nextSquare))
        if (board[nextSquare] !is NoPiece) break // can't move past opponent's piece
    }

    // Check diagonal moves in direction (-1, 1)
    for (i in 1..7) {
        val nextSquare = Square(square.row - i, square.col + i)
        if (nextSquare.row < 0 || nextSquare.col > 7) break // out of board
        if (board[nextSquare]?.color == color) break // can't capture own piece
        moves.add(Move(piece, square, nextSquare))
        if (board[nextSquare] !is NoPiece) break // can't move past opponent's piece
    }

    // Check diagonal moves in direction (-1, -1)
    for (i in 1..7) {
        val nextSquare = Square(square.row - i, square.col - i)
        if (nextSquare.row < 0 || nextSquare.col < 0) break // out of board
        if (board[nextSquare]?.color == color) break // can't capture own piece
        moves.add(Move(piece, square, nextSquare))
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
    override suspend fun getMoves(
        board: Board,
        square: Square,
        includeKingAttacked: Boolean
    ): List<Move> = emptyList()

    override val value: Int get() = 0

    override val icon: String = ""
}
