package com.programmersbox.testing.poker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.programmersbox.testing.ui.theme.Alizarin
import com.programmersbox.testing.ui.theme.Emerald
import com.programmersbox.testing.ui.theme.PokerTableGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Poker(vm: PokerViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Poker") },
                actions = { Text("\$${animateIntAsState(vm.currentAmount, label = "").value}") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PokerTableGreen)
            )
        },
        bottomBar = {
            HandAndCards(
                cards = vm.hand,
                droppedCards = vm.cardsToDiscard,
                canDrag = vm.state == PokerState.Swap,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        },
        containerColor = PokerTableGreen
    ) { p ->
        Column(
            modifier = Modifier
                .padding(p)
                .fillMaxSize()
        ) {

            val canCheck = vm.hand.size == 5

            val check = if (canCheck) {
                PokerHand.values().firstOrNull { it.check(vm.hand) }
            } else {
                null
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                PokerHand.values().forEach { h ->
                    item {
                        Text(
                            h.shortenedName,
                            color = animateColorAsState(
                                targetValue = if (check == h) Alizarin else LocalContentColor.current,
                                label = ""
                            ).value
                        )
                    }

                    items(5) {
                        Text(
                            "${h.initialWinning * (it + 1)}",
                            color = animateColorAsState(
                                targetValue = if (vm.currentBet == (it + 1)) Emerald else LocalContentColor.current,
                                label = ""
                            ).value,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            AnimatedVisibility(vm.state == PokerState.Start) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { vm.currentBet = (vm.currentBet - 1).coerceAtLeast(1) },
                        enabled = vm.state == PokerState.Start
                    ) { Icon(Icons.Default.RemoveCircle, null) }
                    Text("\$${vm.currentBet}")
                    IconButton(
                        onClick = { vm.currentBet = (vm.currentBet + 1).coerceAtMost(5) },
                        enabled = vm.state == PokerState.Start
                    ) { Icon(Icons.Default.AddCircle, null) }
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                if (vm.state == PokerState.Start) {
                    Button(
                        onClick = { vm.start() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Play") }
                }

                if (vm.state == PokerState.Swap) {
                    Button(
                        onClick = { vm.swap() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Draw") }
                }

                if (vm.state == PokerState.End) {
                    Button(
                        onClick = { vm.end(snackbarHostState) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Play Again") }
                }
            }
        }
    }
}
