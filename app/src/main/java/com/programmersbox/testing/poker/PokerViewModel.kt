package com.programmersbox.testing.poker

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PokerViewModel : ViewModel() {

    private val deck = Deck.defaultDeck().also {
        it.addDeckListener {
            onDraw { _, size ->
                if (size == 0) {
                    it.addDeck(Deck.defaultDeck())
                    it.shuffle()
                }
            }
        }
        it.shuffle()
    }

    val hand = mutableStateListOf<Card>()
    var currentAmount by mutableIntStateOf(500)
    var currentBet by mutableIntStateOf(1)
    var state by mutableStateOf(PokerState.Start)
    val cardsToDiscard = mutableStateListOf<Card>()

    private fun draw() {
        hand.removeAll(cardsToDiscard)
        while (hand.size != 5) {
            hand.add(deck.draw())
        }
    }

    private fun reset() {
        hand.clear()
        hand.addAll(deck.draw(5))
    }

    fun start() {
        currentAmount -= currentBet
        state = PokerState.Swap
        reset()
    }

    fun swap() {
        draw()
        cardsToDiscard.clear()
        state = PokerState.End
    }

    fun end(snackbarHostState: SnackbarHostState) {
        val winnings = PokerHand.entries
            .first { it.check(hand) }
            .let { it.initialWinning * currentBet }
        viewModelScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            if (winnings > 0) {
                snackbarHostState.showSnackbar("Won \$$winnings")
            } else {
                snackbarHostState.showSnackbar("Lost \$$currentBet")
            }
        }
        currentAmount += winnings
        cardsToDiscard.clear()
        hand.clear()
        state = PokerState.Start
    }

}

enum class PokerState { Start, Swap, End }
