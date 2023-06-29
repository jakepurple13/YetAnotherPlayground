package com.programmersbox.testing.components.lookahead

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.intermediateLayout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LookaheadCustomTest() {
    val image1 = remember {
        movableContentOf<Int> {
            Image(
                imageVector = res[it.coerceAtMost(2)],
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp)
                    .animateBounds()
                    .clip(RoundedCornerShape(5.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
    LookaheadScope {
        LazyColumn {
            items(10, key = { it }) {
                val index = it % 4
                var expanded by rememberSaveable { mutableStateOf(false) }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = pastelColors[index],
                    onClick = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LookaheadScope {
                        val title = remember {
                            movableContentOf {
                                Text(
                                    names[index],
                                    Modifier
                                        .padding(20.dp)
                                        .animatePlacementInScope(this)
                                )
                            }
                        }
                        val image = remember {
                            if (index < 3) {
                                movableContentOf {
                                    Image(
                                        imageVector = res[index],
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .animateBounds()
                                            .clip(RoundedCornerShape(5.dp)),
                                        contentScale = if (expanded) {
                                            ContentScale.FillWidth
                                        } else {
                                            ContentScale.Crop
                                        }
                                    )
                                }
                            } else {
                                movableContentOf {
                                    Box(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .animateBounds()
                                            .background(
                                                Color.LightGray,
                                                RoundedCornerShape(5.dp)
                                            ),
                                    )
                                }
                            }
                        }
                        if (expanded) {
                            Column {
                                Text(index.toString())
                                title()
                                image()
                                image1(index)
                            }
                        } else {
                            Row {
                                Text(index.toString())
                                image()
                                title()
                                image1(index)
                            }
                        }
                    }
                }
            }

            item {
                var fullWidth by remember { mutableStateOf(false) }
                Row(
                    (if (fullWidth) Modifier.fillMaxWidth() else Modifier.width(100.dp))
                        .height(200.dp)
                        // Use the custom modifier created above to animate the constraints passed
                        // to the child, and therefore resize children in an animation.
                        .animateConstraints()
                        .clickable { fullWidth = !fullWidth }) {
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.Red)
                    )
                    Box(
                        Modifier
                            .weight(2f)
                            .fillMaxHeight()
                            .background(Color.Yellow)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun LookaheadWithLazyColumn() {
    LookaheadScope {
        LazyColumn {
            items(10, key = { it }) {
                val index = it % 4
                var expanded by rememberSaveable { mutableStateOf(false) }
                AnimatedVisibility(
                    remember { MutableTransitionState(false) }.apply { targetState = true },
                    enter = slideInHorizontally { 20 } + fadeIn()
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = pastelColors[index],
                        onClick = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LookaheadScope {
                            val title = remember {
                                movableContentOf {
                                    Text(
                                        names[index],
                                        Modifier
                                            .padding(20.dp)
                                            .animateBounds(Modifier)
                                    )
                                }
                            }
                            val image = remember {
                                if (index < 3) {
                                    movableContentOf {
                                        Image(
                                            imageVector = res[index],
                                            contentDescription = null,
                                            modifier = Modifier
                                                .padding(10.dp)
                                                .animateBounds(
                                                    if (expanded)
                                                        Modifier.fillMaxWidth()
                                                    else
                                                        Modifier.size(80.dp),
                                                    spring(stiffness = Spring.StiffnessLow)
                                                )
                                                .clip(RoundedCornerShape(5.dp)),
                                            contentScale = if (expanded) {
                                                ContentScale.FillWidth
                                            } else {
                                                ContentScale.Crop
                                            }
                                        )
                                    }
                                } else {
                                    movableContentOf {
                                        Box(
                                            modifier = Modifier
                                                .padding(10.dp)
                                                .animateBounds(
                                                    if (expanded) Modifier
                                                        .fillMaxWidth()
                                                        .aspectRatio(1f)
                                                    else Modifier.size(80.dp),
                                                    spring(stiffness = Spring.StiffnessLow)
                                                )
                                                .background(
                                                    Color.LightGray,
                                                    RoundedCornerShape(5.dp)
                                                ),
                                        )
                                    }
                                }
                            }
                            if (expanded) {
                                Column {
                                    Text(index.toString())
                                    title()
                                    image()
                                }
                            } else {
                                Row {
                                    Text(index.toString())
                                    image()
                                    title()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal val pastelColors = listOf(
    Color(0xFF00c2c7),
    Color(0xFF0086ad),
    Color(0xFF005582),
    Color(0xFF0086ad),
    Color(0xFF00c2c7),
    Color(0xFF97ebdb)
)

val names = listOf("YT", "Pepper", "Waffle", "Who?")
val res = listOf(
    Icons.Default.Android,
    Icons.Default.Add,
    Icons.Default.Adb
)

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.animateBounds(
    modifier: Modifier = Modifier,
    sizeAnimationSpec: FiniteAnimationSpec<IntSize> = spring(
        Spring.DampingRatioNoBouncy,
        Spring.StiffnessMediumLow
    ),
    positionAnimationSpec: FiniteAnimationSpec<IntOffset> = spring(
        Spring.DampingRatioNoBouncy,
        Spring.StiffnessMediumLow
    ),
    lookaheadScope: (closestLookaheadScope: LookaheadScope) -> LookaheadScope = { it }
) = composed {
    val outerOffsetAnimation = remember { DeferredAnimation(IntOffset.VectorConverter) }
    val outerSizeAnimation = remember { DeferredAnimation(IntSize.VectorConverter) }
    val offsetAnimation = remember { DeferredAnimation(IntOffset.VectorConverter) }
    val sizeAnimation = remember { DeferredAnimation(IntSize.VectorConverter) }
    // The measure logic in `intermediateLayout` is skipped in the lookahead pass, as
    // intermediateLayout is expected to produce intermediate stages of a layout transform.
    // When the measure block is invoked after lookahead pass, the lookahead size of the
    // child will be accessible as a parameter to the measure block.
    this
        .intermediateLayout { measurable, constraints ->
            val (w, h) = outerSizeAnimation.updateTarget(
                lookaheadSize,
                sizeAnimationSpec,
            )
            measurable
                .measure(constraints)
                .run {
                    layout(w, h) {
                        val (x, y) = outerOffsetAnimation.updateTargetBasedOnCoordinates(
                            positionAnimationSpec
                        )
                        place(x, y)
                    }
                }
        }
        .then(modifier)
        .intermediateLayout { measurable, _ ->
            // When layout changes, the lookahead pass will calculate a new final size for the
            // child modifier. This lookahead size can be used to animate the size
            // change, such that the animation starts from the current size and gradually
            // change towards `lookaheadSize`.
            val (width, height) = sizeAnimation.updateTarget(
                lookaheadSize,
                sizeAnimationSpec,
            )
            // Creates a fixed set of constraints using the animated size
            val animatedConstraints = Constraints.fixed(width, height)
            // Measure child/children with animated constraints.
            val placeable = measurable.measure(animatedConstraints)
            layout(placeable.width, placeable.height) {
                val (x, y) = with(lookaheadScope(this@intermediateLayout)) {
                    offsetAnimation.updateTargetBasedOnCoordinates(
                        positionAnimationSpec,
                    )
                }
                placeable.place(x, y)
            }
        }
}

context(LookaheadScope, Placeable.PlacementScope, CoroutineScope)
@OptIn(ExperimentalComposeUiApi::class)
internal fun DeferredAnimation<IntOffset, AnimationVector2D>.updateTargetBasedOnCoordinates(
    animationSpec: FiniteAnimationSpec<IntOffset>,
): IntOffset {
    coordinates?.let { coordinates ->
        with(this@PlacementScope) {
            val targetOffset = lookaheadScopeCoordinates.localLookaheadPositionOf(coordinates)
            val animOffset = updateTarget(
                targetOffset.round(),
                animationSpec,
            )
            val current = lookaheadScopeCoordinates.localPositionOf(
                coordinates,
                Offset.Zero
            ).round()
            return (animOffset - current)
        }
    }
    return IntOffset.Zero
}

// Experimenting with a way to initialize animation during measurement && only take the last target
// change in a frame (if the target was changed multiple times in the same frame) as the
// animation target.
internal class DeferredAnimation<T, V : AnimationVector>(
    private val vectorConverter: TwoWayConverter<T, V>
) {
    val value: T?
        get() = animatable?.value ?: target
    var target: T? by mutableStateOf(null)
        private set
    private var animatable: Animatable<T, V>? = null
    internal val isActive: Boolean
        get() = target != animatable?.targetValue || animatable?.isRunning == true

    context (CoroutineScope)
    fun updateTarget(
        targetValue: T,
        animationSpec: FiniteAnimationSpec<T>,
    ): T {
        target = targetValue
        if (target != null && target != animatable?.targetValue) {
            animatable?.run {
                launch {
                    animateTo(
                        targetValue,
                        animationSpec
                    )
                }
            } ?: Animatable(targetValue, vectorConverter).let {
                animatable = it
            }
        }
        return animatable?.value ?: targetValue
    }
}

// Creates a custom modifier to animate the local position of the layout within the
// given LookaheadScope, whenever the relative position changes.
@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.animatePlacementInScope(lookaheadScope: LookaheadScope) = composed {
    // Creates an offset animation
    var offsetAnimation: Animatable<IntOffset, AnimationVector2D>? by mutableStateOf(null)
    var targetOffset: IntOffset? by mutableStateOf(null)

    this.intermediateLayout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            // Converts coordinates of the current layout to LookaheadCoordinates
            val coordinates = coordinates
            if (coordinates != null) {
                // Calculates the target offset within the lookaheadScope
                val target = with(lookaheadScope) {
                    lookaheadScopeCoordinates
                        .localLookaheadPositionOf(coordinates)
                        .round().also { targetOffset = it }
                }

                // Uses the target offset to start an offset animation
                if (target != offsetAnimation?.targetValue) {
                    offsetAnimation?.run {
                        launch { animateTo(target) }
                    } ?: Animatable(target, IntOffset.VectorConverter).let {
                        offsetAnimation = it
                    }
                }
                // Calculates the *current* offset within the given LookaheadScope
                val placementOffset =
                    lookaheadScopeCoordinates.localPositionOf(
                        coordinates,
                        Offset.Zero
                    ).round()
                // Calculates the delta between animated position in scope and current
                // position in scope, and places the child at the delta offset. This puts
                // the child layout at the animated position.
                val (x, y) = requireNotNull(offsetAnimation).run { value - placementOffset }
                placeable.place(x, y)
            } else {
                placeable.place(0, 0)
            }
        }
    }
}

// Creates a custom modifier that animates the constraints and measures child with the
// animated constraints. This modifier is built on top of `Modifier.intermediateLayout`, which
// allows access to the lookahead size of the layout. A resize animation will be kicked off
// whenever the lookahead size changes, to animate children from current size to lookahead size.
// Fixed constraints created based on the animation value will be used to measure
// child, so the child layout gradually changes its size and potentially its child's placement
// to fit within the animated constraints.
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.animateConstraints() = composed {
    // Creates a size animation
    var sizeAnimation: Animatable<IntSize, AnimationVector2D>? by remember {
        mutableStateOf(null)
    }

    this.intermediateLayout { measurable, _ ->
        // When layout changes, the lookahead pass will calculate a new final size for the
        // child layout. This lookahead size can be used to animate the size
        // change, such that the animation starts from the current size and gradually
        // change towards `lookaheadSize`.
        if (lookaheadSize != sizeAnimation?.targetValue) {
            sizeAnimation?.run {
                launch { animateTo(lookaheadSize) }
            } ?: Animatable(lookaheadSize, IntSize.VectorConverter).let {
                sizeAnimation = it
            }
        }
        val (width, height) = sizeAnimation!!.value
        // Creates a fixed set of constraints using the animated size
        val animatedConstraints = Constraints.fixed(width, height)
        // Measure child with animated constraints.
        val placeable = measurable.measure(animatedConstraints)
        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}