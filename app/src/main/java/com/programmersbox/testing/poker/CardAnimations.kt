package com.programmersbox.testing.poker

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * MOST of this code comes from https://github.com/MohamedRejeb/Card-Game-Animation/tree/main
 * The main animation and concept
 */
@Composable
fun GameScreen() {
    val cards = remember {
        mutableStateListOf(
            Card.RandomCard,
            Card.RandomCard,
            Card.RandomCard,
            Card.RandomCard,
            Card.RandomCard
        )
    }

    val density = LocalDensity.current
    var cardsSpreadDegree by remember { mutableFloatStateOf(10f) }
    var activeCard by remember { mutableStateOf<Card?>(null) }
    val droppedCards = remember { mutableStateListOf<Card>() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF266B35))
    ) {
        cards.forEachIndexed { index, card ->
            key(card.toSymbolString()) {
                CardItem(
                    card = card,
                    index = index,
                    transformOrigin = TransformOrigin(0f, 1f),
                    nonDroppedCardsSize = cards.size - droppedCards.size,
                    activeCard = activeCard,
                    cardsSpreadDegree = cardsSpreadDegree,
                    isDropped = card in droppedCards,
                    onCardDropped = { droppedCard ->
                        droppedCards.add(droppedCard)
                    },
                    onCardDropPress = {
                        droppedCards.remove(it)
                    },
                    setActiveCard = { activeCard = it },
                    getTargetOffset = {
                        val width = 50f * droppedCards.size
                        Offset(
                            x = width,
                            y = with(density) { maxHeight.toPx() / 2 } - 450f,
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = 60.dp, y = (-100).dp)
                        .then(
                            if (droppedCards.contains(card)) {
                                Modifier
                                    .zIndex(
                                        droppedCards
                                            .indexOf(card)
                                            .toFloat()
                                    )
                            } else {
                                Modifier
                                    .zIndex((droppedCards.size + index).toFloat())
                            }
                        )
                )
            }
        }

        PlayerHand(
            cardsSpreadDegree = cardsSpreadDegree,
            onHandDragged = { delta ->
                val newCardsSpreadDegree = max(
                    0f,
                    min(
                        12f,
                        cardsSpreadDegree + delta / 10f
                    )
                )
                cardsSpreadDegree = newCardsSpreadDegree
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = 20.dp, y = (-10).dp)
                .zIndex((droppedCards.size + cards.size).toFloat())
                .padding(bottom = 10.dp)
        )
    }

}

@Composable
fun HandAndCards(
    cards: List<Card>,
    modifier: Modifier = Modifier,
    droppedCards: MutableList<Card> = remember { mutableStateListOf() },
    onCardDropPress: (Card) -> Unit = { droppedCards.remove(it) },
    onCardDropped: (Card) -> Unit = { droppedCards.add(it) },
    canDrag: Boolean = true,
    resetActiveCardKeys: Array<Any> = arrayOf(),
) {
    val density = LocalDensity.current
    var cardsSpreadDegree by remember { mutableFloatStateOf(10f) }
    var activeCard by remember(*resetActiveCardKeys) { mutableStateOf<Card?>(null) }

    BoxWithConstraints(
        modifier = modifier
    ) {
        cards.forEachIndexed { index, card ->
            key(card.toSymbolString()) {
                CardItem(
                    card = card,
                    index = index,
                    transformOrigin = TransformOrigin(0f, 1f),
                    nonDroppedCardsSize = cards.size - droppedCards.size,
                    activeCard = activeCard,
                    cardsSpreadDegree = cardsSpreadDegree,
                    enableDrag = canDrag,
                    isDropped = card in droppedCards,
                    onCardDropped = onCardDropped,
                    onCardDropPress = onCardDropPress,
                    setActiveCard = { activeCard = it },
                    getTargetOffset = {
                        val width = 50f * droppedCards.size
                        Offset(
                            x = width - 100f,
                            y = with(density) { maxHeight.toPx() / 2 } - 100f// - 550f, //default is 450f
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = 60.dp, y = (-100).dp)
                        .zIndex(
                            if (card in droppedCards) {
                                droppedCards
                                    .indexOf(card)
                                    .toFloat()
                            } else {
                                (droppedCards.size + index).toFloat()
                            }
                        )
                )
            }
        }

        PlayerHand(
            cardsSpreadDegree = cardsSpreadDegree,
            onHandDragged = { delta ->
                val newCardsSpreadDegree = max(
                    0f,
                    min(
                        20f,//12f,
                        cardsSpreadDegree + delta / 10f
                    )
                )
                cardsSpreadDegree = newCardsSpreadDegree
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = 20.dp, y = (-10).dp)
                .padding(bottom = 10.dp)
        )
    }
}

@Composable
fun CardItem(
    card: Card,
    index: Int,
    transformOrigin: TransformOrigin,
    modifier: Modifier = Modifier,
    nonDroppedCardsSize: Int = 0,
    isDropped: Boolean = false,
    onCardDropped: (Card) -> Unit = {},
    onCardDropPress: (Card) -> Unit,
    getTargetOffset: () -> Offset = { Offset.Zero },
    enableDrag: Boolean = true,
    activeCard: Card? = null,
    setActiveCard: (Card?) -> Unit = {},
    cardsSpreadDegree: Float = 10f,
) {
    val scope = rememberCoroutineScope()
    var isBeingDragged by remember { mutableStateOf(false) }
    val activeCardOffset by animateFloatAsState(
        targetValue = if (activeCard == card && !isBeingDragged) -100f else 0f,
        label = "Active card ${card.toSymbolString()} offset animation"
    )
    val cardRotation by animateFloatAsState(
        targetValue = if (isDropped) 0f else cardsSpreadDegree * (index - nonDroppedCardsSize / 2) - 30f,
        label = "Card ${card.toSymbolString()} rotation animation"
    )
    val cardDropRotation by animateFloatAsState(
        targetValue = if (isDropped) 0f else 0f,
        label = "Card ${card.toSymbolString()} drop rotation animation",
        animationSpec = tween(
            durationMillis = 400,
            easing = EaseInOut,
        )
    )
    val cardDragX = remember { Animatable(initialValue = 0f) }
    val cardDragY = remember { Animatable(initialValue = 0f) }
    var cardOriginalOffset by remember { mutableStateOf(Offset.Zero) }
    val resetCard = { it: Card ->
        scope.launch {
            cardDragX.animateTo(0f)
        }
        scope.launch {
            cardDragY.animateTo(0f)
        }
        onCardDropPress(it)
    }
    val dropCard = { it: Card ->
        val targetOffset = getTargetOffset()
        val remainingOffset = targetOffset - cardOriginalOffset
        if (SHOW_LOGS) {
            println("targetOffset: $targetOffset")
            println("remainingOffset: $remainingOffset")
        }
        scope.launch {
            cardDragX.animateTo(
                targetValue = remainingOffset.x,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = EaseInOut
                )
            )
        }
        scope.launch {
            cardDragY.animateTo(
                targetValue = remainingOffset.y,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = EaseInOut
                )
            )
        }
        onCardDropped(it)
    }

    PlayingCard(
        card = card,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        modifier = modifier
            .width(120.dp)
            .wrapContentHeight()
            .onGloballyPositioned {
                cardOriginalOffset = it.positionInRoot() - Offset(
                    x = it.size.width / 2f,
                    y = it.size.height / 2f,
                )
            }
            .graphicsLayer {
                this.transformOrigin = transformOrigin
                rotationZ = cardRotation
            }
            .graphicsLayer {
                translationX = cardDragX.value
                translationY = activeCardOffset + cardDragY.value
            }
            .graphicsLayer {
                this.transformOrigin = TransformOrigin.Center
                rotationZ = cardDropRotation
            }
            .clip(MaterialTheme.shapes.small)
            .pointerInput(activeCard) {
                detectTapGestures(
                    onTap = { setActiveCard(if (activeCard == card) null else card) },
                    onDoubleTap = {
                        if (isDropped) {
                            resetCard(card)
                            return@detectTapGestures
                        } else {
                            dropCard(card)
                        }
                    }
                )
            }
            .then(
                if (enableDrag) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                if (SHOW_LOGS) println("startOffset: $startOffset")
                                isBeingDragged = true
                                setActiveCard(card)
                                onCardDropPress(card)
                            },
                            onDragEnd = {
                                isBeingDragged = false

                                val dragOffset = Offset(
                                    x = cardDragX.value,
                                    y = cardDragY.value,
                                )
                                val distance = calculateDistanceBetweenTwoPoints(
                                    dragOffset,
                                    Offset.Zero
                                )

                                if (SHOW_LOGS) {
                                    println("originalOffset: $cardOriginalOffset")
                                    println("drag offset: ${cardDragX.value}, ${cardDragY.value}")
                                    println("Distance: $distance")
                                }

                                if (distance > DISTANCE_TO_DROP) {
                                    dropCard(card)
                                } else {
                                    resetCard(card)
                                }

                                setActiveCard(null)
                            },
                            onDragCancel = {
                                isBeingDragged = false
                                scope.launch {
                                    cardDragX.animateTo(0f)
                                }
                                scope.launch {
                                    cardDragY.animateTo(0f)
                                }
                                setActiveCard(null)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()

                                scope.launch {
                                    cardDragX.snapTo(cardDragX.value + dragAmount.x)
                                }
                                scope.launch {
                                    cardDragY.snapTo(cardDragY.value + dragAmount.y)
                                }
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
            .shadow(
                elevation = 10.dp,
                shape = MaterialTheme.shapes.small,
            )
            .then(
                if (activeCard == card) {
                    Modifier
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        )
                } else {
                    Modifier
                }
            )
    )
}

@Composable
fun PlayerHand(
    cardsSpreadDegree: Float,
    onHandDragged: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isHandBeingDragged by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.RotateRight, null)
        Icon(
            Icons.Default.FrontHand,
            contentDescription = "hand",
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 0f)
                    rotationZ = cardsSpreadDegree - 10f
                }
                .draggable(
                    state = rememberDraggableState { delta -> onHandDragged(-delta) },
                    orientation = Orientation.Horizontal,
                    onDragStarted = { isHandBeingDragged = true },
                    onDragStopped = { isHandBeingDragged = false }
                )
        )
        Icon(Icons.Default.RotateLeft, null)
    }
}

fun calculateDistanceBetweenTwoPoints(p1: Offset, p2: Offset): Float {
    return calculateDistanceBetweenTwoPoints(p1.x, p1.y, p2.x, p2.y)
}

fun calculateDistanceBetweenTwoPoints(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return sqrt((x2 - x1).toDouble().pow(2.0) + (y2 - y1).toDouble().pow(2.0)).toFloat()
}

private const val DISTANCE_TO_DROP = 250 //defaults to 500
private const val SHOW_LOGS = false

@Preview(showBackground = true)
@Composable
private fun GameScreenPreview() {
    GameScreen()
}