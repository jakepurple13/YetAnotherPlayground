package com.programmersbox.testing.chess

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.programmersbox.testing.Screens
import com.programmersbox.testing.ui.theme.DarkPreview
import com.programmersbox.testing.ui.theme.LocalNavController
import com.programmersbox.testing.ui.theme.TestingTheme
import com.programmersbox.testing.chess.Color as ChessColor

private const val ROWS = ChessEngine.BOARD_SIZE
private const val COLS = ChessEngine.BOARD_SIZE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessScreenPreview() {
    val navController = LocalNavController.current
    val vm = viewModel { ChessViewModel(createSavedStateHandle()) }

    var showChangeDifficultyLevel by remember { mutableStateOf(false) }

    if (showChangeDifficultyLevel) {
        DifficultyChoice(
            currentDifficultyLevel = vm.difficulty,
            onDifficultyLevelChange = {
                showChangeDifficultyLevel = false
                navController.navigate(Screens.ChessScreen.route.replace("{difficulty}", it.name)) {
                    launchSingleTop = true
                    popUpTo(Screens.MainScreen.route)
                }
            },
            onDismissRequest = { showChangeDifficultyLevel = false }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Chess") }) },
        bottomBar = {
            BottomAppBar(
                actions = {
                    Text(vm.currentTurn.collectAsStateWithLifecycle(ChessColor.White).value.name)
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        text = { Text(vm.difficulty.name) },
                        icon = { Icon(Icons.Default.AccountCircle, null) },
                        onClick = { showChangeDifficultyLevel = true })
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            LazyRow(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
            ) {
                items(vm.moves) {
                    AssistChip(onClick = {}, label = { Text(it.toString()) })
                }
            }
            ChessBoard(
                modifier = Modifier.align(Alignment.Center),
                board = vm.getBoard(),
                piecePicked = vm.piecePicked,
                isMovePossible = { it in vm.possibleMoves.map { m -> m.to } },
                isSquareAttacked = { it in vm.attackedMoves.map { a -> a.to } },
                pickUpPiece = { piece, square -> vm.pickUpPiece(piece, square) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DifficultyChoice(
    currentDifficultyLevel: ChessAi.DifficultyLevel,
    onDifficultyLevelChange: (ChessAi.DifficultyLevel) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        CenterAlignedTopAppBar(title = { Text("Choose a new Difficulty") })
        ChessAi.DifficultyLevel.values().forEach {
            var showDialog by remember { mutableStateOf(false) }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Are you sure?") },
                    text = { Text("Changing to ${it.name} will start a new game.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDialog = false
                                onDifficultyLevelChange(it)
                            }
                        ) { Text("Yes") }
                    },
                    dismissButton = { TextButton(onClick = { showDialog = false }) { Text("No") } },
                )
            }
            Card(onClick = { showDialog = true }) {
                ListItem(
                    headlineContent = { Text(it.name) },
                    leadingContent = {
                        RadioButton(
                            selected = currentDifficultyLevel == it,
                            onClick = {}
                        )
                    }
                )
            }
            Divider()
        }
    }
}

@Composable
private fun ChessBoard(
    modifier: Modifier = Modifier,
    board: Map<Square, Piece>,
    piecePicked: Piece?,
    isMovePossible: (Square) -> Boolean,
    isSquareAttacked: (Square) -> Boolean,
    pickUpPiece: (Piece?, Square) -> Unit
) {
    Row(
        modifier = modifier.wrapContentWidth()
    ) {
        for (i in 0 until ROWS) {
            Column(modifier = Modifier.wrapContentHeight()) {
                for (j in 0 until COLS) {
                    val square = Square(j, i)
                    val piece = board[square]
                    val isPossible = isMovePossible(square)
                    val isAttacked = isSquareAttacked(square)
                    FilledTonalIconButton(
                        onClick = { pickUpPiece(piece, square) },
                        shape = RectangleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if ((i + j) % 2 == 0) {
                                MaterialTheme.colorScheme.background
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            }
                        ),
                        modifier = Modifier
                            .border(
                                width = animateDpAsState(
                                    targetValue = when {
                                        piecePicked == piece -> 2.dp
                                        isAttacked -> 2.dp
                                        isPossible -> 2.dp
                                        else -> (-10).dp
                                    },
                                    label = ""
                                ).value,
                                color = animateColorAsState(
                                    targetValue = when {
                                        piecePicked == piece -> Color.Yellow
                                        isAttacked && isPossible && piece !is NoPiece -> Color.Blue
                                        isAttacked -> Color.Red
                                        isPossible -> Color.Green
                                        else -> Color.Transparent
                                    },
                                    label = ""
                                ).value,
                            )
                            .size(50.dp)
                    ) { Text(text = piece?.icon.orEmpty()) }
                }
            }
        }
    }
}

@Composable
@DarkPreview
private fun ChessPreview() {
    TestingTheme {
        Surface {
            ChessScreenPreview()
        }
    }
}