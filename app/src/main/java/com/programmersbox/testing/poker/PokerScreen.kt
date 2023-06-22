package com.programmersbox.testing.poker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                resetActiveCardKeys = arrayOf(vm.state),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                PokerHand.values().forEach { h ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .background(
                                animateColorAsState(
                                    if (check == h) MaterialTheme.colorScheme.surface else Color.Transparent,
                                    label = ""
                                ).value
                            )
                            .fillMaxWidth()
                    ) {
                        Text(
                            h.shortenedName,
                            color = animateColorAsState(
                                targetValue = if (check == h) Alizarin else LocalContentColor.current,
                                label = ""
                            ).value,
                            modifier = Modifier.weight(1f)
                        )
                        repeat(5) {
                            Text(
                                "${h.initialWinning * (it + 1)}",
                                color = animateColorAsState(
                                    targetValue = if (vm.currentBet == (it + 1)) Emerald else LocalContentColor.current,
                                    label = ""
                                ).value,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
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

            when (vm.state) {
                PokerState.Start -> Button(
                    onClick = { vm.start() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Play") }

                PokerState.Swap -> Button(
                    onClick = { vm.swap() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Draw") }

                PokerState.End -> Button(
                    onClick = { vm.end(snackbarHostState) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Play Again") }
            }
        }
    }
}
