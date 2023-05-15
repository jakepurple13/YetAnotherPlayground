package com.programmersbox.testing.chess

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.programmersbox.testing.ui.theme.DarkPreview
import com.programmersbox.testing.ui.theme.TestingTheme
import com.programmersbox.testing.chess.Color as ChessColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessScreenPreview() {
    val vm = viewModel<ChessViewModel>()

    val rows = ChessEngine.BOARD_SIZE
    val cols = ChessEngine.BOARD_SIZE

    Scaffold(
        topBar = { TopAppBar(title = { Text("Chess") }) },
        bottomBar = {
            BottomAppBar {
                Text(vm.currentTurn.collectAsStateWithLifecycle(ChessColor.White).value.name)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(modifier = Modifier.wrapContentWidth()) {
                for (i in 0 until rows) {
                    Column(modifier = Modifier.wrapContentHeight()) {
                        for (j in 0 until cols) {
                            val square = Square(j, i)
                            val piece = vm[square]
                            val isPossible = square in vm.possibleMoves
                            val isAttacked = square in vm.attackedMoves.map { it.square }
                            FilledTonalIconButton(
                                onClick = { vm.pickUpPiece(piece, square) },
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
                                                vm.piecePicked == piece -> 2.dp
                                                isAttacked -> 2.dp
                                                isPossible -> 2.dp
                                                else -> (-10).dp
                                            },
                                            label = ""
                                        ).value,
                                        color = animateColorAsState(
                                            targetValue = when {
                                                vm.piecePicked == piece -> Color.Yellow
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