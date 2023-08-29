package com.programmersbox.testing.components

import androidx.annotation.FloatRange
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.DragScope
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.intermediateLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

enum class TopAppBarState {
    None, Search, ExpandedBottomSheet, NormalTopBar
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTransitionApi::class, ExperimentalComposeUiApi::class)
@Composable
fun CustomBottomSheetScreen() {
    LookaheadScope {
        Modifier.intermediateLayout { measurable, constraints ->
            val f = measurable.measure(constraints)

            layout(constraints.maxWidth, constraints.maxHeight) {
                f.placeRelative(0, 0)
            }
        }
    }

    var selectedState by remember { mutableIntStateOf(1) }
    var query by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val state = rememberBottomSheetScaffoldState()

    val transition = updateTransition(targetState = state.bottomSheetState.targetValue, label = "")

    val topBarTargetState = when {
        state.bottomSheetState.targetValue == SheetValue.Expanded -> TopAppBarState.ExpandedBottomSheet
        selectedState % 5 == 0 -> TopAppBarState.None
        selectedState % 3 == 0 -> TopAppBarState.NormalTopBar
        else -> TopAppBarState.Search
    }

    val sheetShape by transition.animateDp(label = "") { if (it == SheetValue.Expanded) 0.dp else 28.dp }
    val handleAlpha by transition.animateFloat(label = "") { if (it == SheetValue.Expanded) 0f else 1f }

    CustomBottomSheetScaffold(
        scaffoldState = state,
        sheetContent = {
            LazyColumn {
                items(100) {
                    ListItem(
                        headlineContent = { Text(it.toString()) },
                        leadingContent = { RadioButton(selected = it == selectedState, onClick = { selectedState = it }) }
                    )
                }
            }
        },
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle(modifier = Modifier.graphicsLayer { alpha = handleAlpha })
        },
        sheetShape = MaterialTheme.shapes.extraLarge.copy(
            topStart = CornerSize(sheetShape),
            topEnd = CornerSize(sheetShape)
        ),
        topBar = {
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = topBarTargetState,
                    label = "",
                    transitionSpec = {
                        slideInVertically(
                            animationSpec = tween(durationMillis = 500),
                            initialOffsetY = { -it }
                        ) togetherWith slideOutVertically(
                            animationSpec = tween(durationMillis = 500),
                            targetOffsetY = { -it }
                        )
                    },
                    contentAlignment = Alignment.TopCenter
                ) { target ->
                    when (target) {
                        TopAppBarState.None -> {}

                        TopAppBarState.Search -> SearchBar(
                            query = query,
                            onQueryChange = { query = it },
                            onSearch = {},
                            active = isActive,
                            onActiveChange = { isActive = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LazyColumn {
                                items(100) {
                                    ListItem(
                                        headlineContent = { Text(it.toString()) },
                                    )
                                }
                            }
                        }

                        TopAppBarState.ExpandedBottomSheet -> TopAppBar(
                            title = {},
                            navigationIcon = {
                                IconButton(
                                    onClick = { scope.launch { state.bottomSheetState.partialExpand() } }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(BottomSheetDefaults.Elevation)
                            )
                        )

                        TopAppBarState.NormalTopBar -> TopAppBar(title = { Text("Hello World with $target") })
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding
        ) {
            items(20) {
                ListItem(
                    headlineContent = { Text(it.toString()) },
                    leadingContent = { RadioButton(selected = it == selectedState, onClick = { selectedState = it }) }
                )
            }
        }
    }
}

@Composable
@ExperimentalMaterial3Api
fun CustomBottomSheetScaffold(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    sheetPeekHeight: Dp = BottomSheetDefaults.SheetPeekHeight,
    sheetShape: Shape = BottomSheetDefaults.ExpandedShape,
    sheetContainerColor: Color = BottomSheetDefaults.ContainerColor,
    sheetContentColor: Color = contentColorFor(sheetContainerColor),
    sheetTonalElevation: Dp = BottomSheetDefaults.Elevation,
    sheetShadowElevation: Dp = BottomSheetDefaults.Elevation,
    sheetDragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    sheetSwipeEnabled: Boolean = true,
    topBar: @Composable (() -> Unit)? = null,
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable (PaddingValues) -> Unit
) {
    BottomSheetScaffoldLayout(
        modifier = modifier,
        topBar = topBar,
        body = content,
        snackbarHost = { snackbarHost(scaffoldState.snackbarHostState) },
        sheetPeekHeight = sheetPeekHeight,
        sheetOffset = { scaffoldState.bottomSheetState.requireOffset() },
        sheetState = scaffoldState.bottomSheetState,
        containerColor = containerColor,
        contentColor = contentColor,
        bottomSheet = { layoutHeight ->
            StandardBottomSheet(
                state = scaffoldState.bottomSheetState,
                peekHeight = sheetPeekHeight,
                sheetSwipeEnabled = sheetSwipeEnabled,
                layoutHeight = layoutHeight.toFloat(),
                shape = sheetShape,
                containerColor = sheetContainerColor,
                contentColor = sheetContentColor,
                tonalElevation = sheetTonalElevation,
                shadowElevation = sheetShadowElevation,
                dragHandle = sheetDragHandle,
                content = sheetContent
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetScaffoldLayout(
    topBar: @Composable (() -> Unit)?,
    body: @Composable (innerPadding: PaddingValues) -> Unit,
    bottomSheet: @Composable (layoutHeight: Int) -> Unit,
    snackbarHost: @Composable () -> Unit,
    sheetPeekHeight: Dp,
    sheetOffset: () -> Float,
    sheetState: SheetState,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    SubcomposeLayout { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        //Moved this above the sheet so we can get the top bar height
        val topBarPlaceable = topBar?.let {
            subcompose(BottomSheetScaffoldLayoutSlot.TopBar) { topBar() }[0]
                .measure(looseConstraints)
        }
        val topBarHeight = topBarPlaceable?.height ?: 0

        val sheetPlaceable = subcompose(BottomSheetScaffoldLayoutSlot.Sheet) {
            bottomSheet(layoutHeight)
        }[0].measure(looseConstraints.copy(maxHeight = layoutHeight - topBarHeight)) //Then limiting the height!
        val sheetOffsetY = sheetOffset().roundToInt()
        val sheetOffsetX = Integer.max(0, (layoutWidth - sheetPlaceable.width) / 2)

        val bodyConstraints = looseConstraints.copy(maxHeight = layoutHeight - topBarHeight)
        val bodyPlaceable = subcompose(BottomSheetScaffoldLayoutSlot.Body) {
            Surface(
                modifier = modifier,
                color = containerColor,
                contentColor = contentColor,
            ) { body(PaddingValues(bottom = sheetPeekHeight)) }
        }[0].measure(bodyConstraints)

        val snackbarPlaceable = subcompose(BottomSheetScaffoldLayoutSlot.Snackbar, snackbarHost)[0]
            .measure(looseConstraints)
        val snackbarOffsetX = (layoutWidth - snackbarPlaceable.width) / 2
        val snackbarOffsetY = when (sheetState.currentValue) {
            SheetValue.PartiallyExpanded -> sheetOffsetY - snackbarPlaceable.height
            SheetValue.Expanded, SheetValue.Hidden -> layoutHeight - snackbarPlaceable.height
        }

        layout(layoutWidth, layoutHeight) {
            // Placement order is important for elevation
            bodyPlaceable.placeRelative(0, topBarHeight)
            //topBarPlaceable?.placeRelative(0, 0)
            //Finally, placing this on an above layer so it goes above the sheet!
            topBarPlaceable?.placeRelativeWithLayer(0, 0, 1f)
            sheetPlaceable.placeRelative(sheetOffsetX, sheetOffsetY)
            snackbarPlaceable.placeRelative(snackbarOffsetX, snackbarOffsetY)
        }
    }
}

private enum class BottomSheetScaffoldLayoutSlot { TopBar, Body, Sheet, Snackbar }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StandardBottomSheet(
    state: SheetState,
    peekHeight: Dp,
    sheetSwipeEnabled: Boolean,
    layoutHeight: Float,
    shape: Shape,
    containerColor: Color,
    contentColor: Color,
    tonalElevation: Dp,
    shadowElevation: Dp,
    dragHandle: @Composable (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val peekHeightPx = with(LocalDensity.current) { peekHeight.toPx() }
    val orientation = Orientation.Vertical

    // Callback that is invoked when the anchors have changed.
    val anchorChangeHandler = remember(state, scope) {
        BottomSheetScaffoldAnchorChangeHandler(
            state = state,
            animateTo = { target, velocity ->
                scope.launch {
                    state.swipeableState.animateTo(
                        target, velocity = velocity
                    )
                }
            },
            snapTo = { target ->
                scope.launch { state.swipeableState.snapTo(target) }
            }
        )
    }
    Surface(
        modifier = Modifier
            .widthIn(max = BottomSheetMaxWidth)
            .fillMaxWidth()
            .requiredHeightIn(min = peekHeight)
            .nestedScroll(
                remember(state.swipeableState) {
                    ConsumeSwipeWithinBottomSheetBoundsNestedScrollConnection(
                        sheetState = state,
                        orientation = orientation,
                        onFling = { scope.launch { state.settle(it) } }
                    )
                }
            )
            .swipeableV2(
                state = state.swipeableState,
                orientation = orientation,
                enabled = sheetSwipeEnabled
            )
            .swipeAnchors(
                state.swipeableState,
                possibleValues = setOf(SheetValue.Hidden, SheetValue.PartiallyExpanded, SheetValue.Expanded),
                anchorChangeHandler = anchorChangeHandler
            ) { value, sheetSize ->
                when (value) {
                    SheetValue.PartiallyExpanded -> if (state.skipPartiallyExpanded)
                        null else layoutHeight - peekHeightPx

                    SheetValue.Expanded -> if (sheetSize.height == peekHeightPx.roundToInt()) {
                        null
                    } else {
                        max(0f, layoutHeight - sheetSize.height)
                    }

                    SheetValue.Hidden -> if (state.skipHiddenState) null else layoutHeight
                }
            },
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    ) {
        Column(Modifier.fillMaxWidth()) {
            if (dragHandle != null) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .semantics(mergeDescendants = true) {
                            with(state) {
                                // Provides semantics to interact with the bottomsheet if there is more
                                // than one anchor to swipe to and swiping is enabled.
                                if (swipeableState.anchors.size > 1 && sheetSwipeEnabled) {
                                    if (currentValue == SheetValue.PartiallyExpanded) {
                                        if (swipeableState.confirmValueChange(SheetValue.Expanded)) {
                                            expand("expandActionLabel") {
                                                scope.launch { expand() }; true
                                            }
                                        }
                                    } else {
                                        if (swipeableState.confirmValueChange(SheetValue.PartiallyExpanded)) {
                                            collapse("partialExpandActionLabel") {
                                                scope.launch { partialExpand() }; true
                                            }
                                        }
                                    }
                                    if (!state.skipHiddenState) {
                                        dismiss("dismissActionLabel") {
                                            scope.launch { hide() }
                                            true
                                        }
                                    }
                                }
                            }
                        },
                ) {
                    dragHandle()
                }
            }
            content()
        }
    }
}

@ExperimentalMaterial3Api
internal fun <T> Modifier.swipeableV2(
    state: SwipeableV2State<T>,
    orientation: Orientation,
    enabled: Boolean = true,
    reverseDirection: Boolean = false,
    interactionSource: MutableInteractionSource? = null
) = this.draggable(
    state = state.swipeDraggableState,
    orientation = orientation,
    enabled = enabled,
    interactionSource = interactionSource,
    reverseDirection = reverseDirection,
    startDragImmediately = state.isAnimationRunning,
    onDragStopped = { velocity -> launch { state.settle(velocity) } }
)

/**
 * Define anchor points for a given [SwipeableV2State] based on this node's layout size and update
 * the state with them.
 *
 * @param state The associated [SwipeableV2State]
 * @param possibleValues All possible values the [SwipeableV2State] could be in.
 * @param anchorChangeHandler A callback to be invoked when the anchors have changed,
 * `null` by default. Components with custom reconciliation logic should implement this callback,
 * i.e. to re-target an in-progress animation.
 * @param calculateAnchor This method will be invoked to calculate the position of all
 * [possibleValues], given this node's layout size. Return the anchor's offset from the initial
 * anchor, or `null` to indicate that a value does not have an anchor.
 */
@ExperimentalMaterial3Api
internal fun <T> Modifier.swipeAnchors(
    state: SwipeableV2State<T>,
    possibleValues: Set<T>,
    anchorChangeHandler: AnchorChangeHandler<T>? = null,
    calculateAnchor: (value: T, layoutSize: IntSize) -> Float?,
) = onSizeChanged { layoutSize ->
    val previousAnchors = state.anchors
    val newAnchors = mutableMapOf<T, Float>()
    possibleValues.forEach {
        val anchorValue = calculateAnchor(it, layoutSize)
        if (anchorValue != null) {
            newAnchors[it] = anchorValue
        }
    }
    if (previousAnchors != newAnchors) {
        val previousTarget = state.targetValue
        val stateRequiresCleanup = state.updateAnchors(newAnchors)
        if (stateRequiresCleanup) {
            anchorChangeHandler?.onAnchorsChanged(
                previousTarget,
                previousAnchors,
                newAnchors
            )
        }
    }
}

/**
 * State of the [swipeableV2] modifier.
 *
 * This contains necessary information about any ongoing swipe or animation and provides methods
 * to change the state either immediately or by starting an animation. To create and remember a
 * [SwipeableV2State] use [rememberSwipeableV2State].
 *
 * @param initialValue The initial value of the state.
 * @param animationSpec The default animation that will be used to animate to a new state.
 * @param confirmValueChange Optional callback invoked to confirm or veto a pending state change.
 * @param positionalThreshold The positional threshold, in px, to be used when calculating the
 * target state while a swipe is in progress and when settling after the swipe ends. This is the
 * distance from the start of a transition. It will be, depending on the direction of the
 * interaction, added or subtracted from/to the origin offset. It should always be a positive value.
 * @param velocityThreshold The velocity threshold (in px per second) that the end velocity has to
 * exceed in order to animate to the next state, even if the [positionalThreshold] has not been
 * reached.
 */
@Suppress("PrimitiveInLambda")
@Stable
@ExperimentalMaterial3Api
internal class SwipeableV2State<T>(
    initialValue: T,
    internal val positionalThreshold: (totalDistance: Float) -> Float,
    internal val velocityThreshold: () -> Float,
    internal val animationSpec: AnimationSpec<Float> = SwipeableV2Defaults.AnimationSpec,
    internal val confirmValueChange: (newValue: T) -> Boolean = { true },
) {

    private val swipeMutex = InternalMutatorMutex()

    internal val swipeDraggableState = object : DraggableState {
        private val dragScope = object : DragScope {
            override fun dragBy(pixels: Float) {
                this@SwipeableV2State.dispatchRawDelta(pixels)
            }
        }

        override suspend fun drag(
            dragPriority: MutatePriority,
            block: suspend DragScope.() -> Unit
        ) {
            swipe(dragPriority) { dragScope.block() }
        }

        override fun dispatchRawDelta(delta: Float) {
            this@SwipeableV2State.dispatchRawDelta(delta)
        }
    }

    /**
     * The current value of the [SwipeableV2State].
     */
    var currentValue: T by mutableStateOf(initialValue)
        private set

    /**
     * The target value. This is the closest value to the current offset (taking into account
     * positional thresholds). If no interactions like animations or drags are in progress, this
     * will be the current value.
     */
    val targetValue: T by derivedStateOf {
        animationTarget ?: run {
            val currentOffset = offset
            if (currentOffset != null) {
                computeTarget(currentOffset, currentValue, velocity = 0f)
            } else currentValue
        }
    }

    /**
     * The current offset, or null if it has not been initialized yet.
     *
     * The offset will be initialized during the first measurement phase of the node that the
     * [swipeableV2] modifier is attached to. These are the phases:
     * Composition { -> Effects } -> Layout { Measurement -> Placement } -> Drawing
     * During the first composition, the offset will be null. In subsequent compositions, the offset
     * will be derived from the anchors of the previous pass.
     * Always prefer accessing the offset from a LaunchedEffect as it will be scheduled to be
     * executed the next frame, after layout.
     *
     * To guarantee stricter semantics, consider using [requireOffset].
     */
    var offset: Float? by mutableStateOf(null)
        private set

    /**
     * Require the current offset.
     *
     * @throws IllegalStateException If the offset has not been initialized yet
     */
    fun requireOffset(): Float = runCatching {
        checkNotNull(offset) {
            "The offset was read before being initialized. Did you access the offset in a phase " +
                    "before layout, like effects or composition?"
        }
    }.getOrDefault(0f)

    /**
     * Whether an animation is currently in progress.
     */
    val isAnimationRunning: Boolean get() = animationTarget != null

    /**
     * The fraction of the progress going from [currentValue] to [targetValue], within [0f..1f]
     * bounds.
     */
    @get:FloatRange(from = 0.0, to = 1.0)
    val progress: Float by derivedStateOf {
        val a = anchors[currentValue] ?: 0f
        val b = anchors[targetValue] ?: 0f
        val distance = abs(b - a)
        if (distance > 1e-6f) {
            val progress = (this.requireOffset() - a) / (b - a)
            // If we are very close to 0f or 1f, we round to the closest
            if (progress < 1e-6f) 0f else if (progress > 1 - 1e-6f) 1f else progress
        } else 1f
    }

    /**
     * The velocity of the last known animation. Gets reset to 0f when an animation completes
     * successfully, but does not get reset when an animation gets interrupted.
     * You can use this value to provide smooth reconciliation behavior when re-targeting an
     * animation.
     */
    var lastVelocity: Float by mutableFloatStateOf(0f)
        private set

    /**
     * The minimum offset this state can reach. This will be the smallest anchor, or
     * [Float.NEGATIVE_INFINITY] if the anchors are not initialized yet.
     */
    val minOffset by derivedStateOf { anchors.minOrNull() ?: Float.NEGATIVE_INFINITY }

    /**
     * The maximum offset this state can reach. This will be the biggest anchor, or
     * [Float.POSITIVE_INFINITY] if the anchors are not initialized yet.
     */
    val maxOffset by derivedStateOf { anchors.maxOrNull() ?: Float.POSITIVE_INFINITY }

    private var animationTarget: T? by mutableStateOf(null)

    internal var anchors by mutableStateOf(emptyMap<T, Float>())

    /**
     * Update the anchors.
     * If the previous set of anchors was empty, attempt to update the offset to match the initial
     * value's anchor.
     *
     * @return true if the state needs to be adjusted after updating the anchors, e.g. if the
     * initial value is not found in the initial set of anchors. false if no further updates are
     * needed.
     */
    internal fun updateAnchors(newAnchors: Map<T, Float>): Boolean {
        val previousAnchorsEmpty = anchors.isEmpty()
        anchors = newAnchors
        val initialValueHasAnchor = if (previousAnchorsEmpty) {
            val initialValue = currentValue
            val initialValueAnchor = anchors[initialValue]
            val initialValueHasAnchor = initialValueAnchor != null
            if (initialValueHasAnchor) trySnapTo(initialValue)
            initialValueHasAnchor
        } else true
        return !initialValueHasAnchor || !previousAnchorsEmpty
    }

    /**
     * Whether the [value] has an anchor associated with it.
     */
    fun hasAnchorForValue(value: T): Boolean = anchors.containsKey(value)

    /**
     * Snap to a [targetValue] without any animation.
     * If the [targetValue] is not in the set of anchors, the [currentValue] will be updated to the
     * [targetValue] without updating the offset.
     *
     * @throws CancellationException if the interaction interrupted by another interaction like a
     * gesture interaction or another programmatic interaction like a [animateTo] or [snapTo] call.
     *
     * @param targetValue The target value of the animation
     */
    suspend fun snapTo(targetValue: T) {
        swipe { snap(targetValue) }
    }

    /**
     * Animate to a [targetValue].
     * If the [targetValue] is not in the set of anchors, the [currentValue] will be updated to the
     * [targetValue] without updating the offset.
     *
     * @throws CancellationException if the interaction interrupted by another interaction like a
     * gesture interaction or another programmatic interaction like a [animateTo] or [snapTo] call.
     *
     * @param targetValue The target value of the animation
     * @param velocity The velocity the animation should start with, [lastVelocity] by default
     */
    suspend fun animateTo(
        targetValue: T,
        velocity: Float = lastVelocity,
    ) {
        val targetOffset = anchors[targetValue]
        if (targetOffset != null) {
            try {
                swipe {
                    animationTarget = targetValue
                    var prev = offset ?: 0f
                    animate(prev, targetOffset, velocity, animationSpec) { value, velocity ->
                        // Our onDrag coerces the value within the bounds, but an animation may
                        // overshoot, for example a spring animation or an overshooting interpolator
                        // We respect the user's intention and allow the overshoot, but still use
                        // DraggableState's drag for its mutex.
                        offset = value
                        prev = value
                        lastVelocity = velocity
                    }
                    lastVelocity = 0f
                }
            } finally {
                animationTarget = null
                val endOffset = requireOffset()
                val endState = anchors
                    .entries
                    .firstOrNull { (_, anchorOffset) -> abs(anchorOffset - endOffset) < 0.5f }
                    ?.key
                this.currentValue = endState ?: currentValue
            }
        } else {
            currentValue = targetValue
        }
    }

    /**
     * Find the closest anchor taking into account the velocity and settle at it with an animation.
     */
    suspend fun settle(velocity: Float) {
        val previousValue = this.currentValue
        val targetValue = computeTarget(
            offset = requireOffset(),
            currentValue = previousValue,
            velocity = velocity
        )
        if (confirmValueChange(targetValue)) {
            animateTo(targetValue, velocity)
        } else {
            // If the user vetoed the state change, rollback to the previous state.
            animateTo(previousValue, velocity)
        }
    }

    /**
     * Swipe by the [delta], coerce it in the bounds and dispatch it to the [SwipeableV2State].
     *
     * @return The delta the consumed by the [SwipeableV2State]
     */
    fun dispatchRawDelta(delta: Float): Float {
        val currentDragPosition = offset ?: 0f
        val potentiallyConsumed = currentDragPosition + delta
        val clamped = potentiallyConsumed.coerceIn(minOffset, maxOffset)
        val deltaToConsume = clamped - currentDragPosition
        if (abs(deltaToConsume) >= 0) {
            offset = ((offset ?: 0f) + deltaToConsume).coerceIn(minOffset, maxOffset)
        }
        return deltaToConsume
    }

    private fun computeTarget(
        offset: Float,
        currentValue: T,
        velocity: Float
    ): T {
        val currentAnchors = anchors
        val currentAnchor = currentAnchors[currentValue]
        val velocityThresholdPx = velocityThreshold()
        return if (currentAnchor == offset || currentAnchor == null) {
            currentValue
        } else if (currentAnchor < offset) {
            // Swiping from lower to upper (positive).
            if (velocity >= velocityThresholdPx) {
                currentAnchors.closestAnchor(offset, true)
            } else {
                val upper = currentAnchors.closestAnchor(offset, true)
                val distance = abs(currentAnchors.getValue(upper) - currentAnchor)
                val relativeThreshold = abs(positionalThreshold(distance))
                val absoluteThreshold = abs(currentAnchor + relativeThreshold)
                if (offset < absoluteThreshold) currentValue else upper
            }
        } else {
            // Swiping from upper to lower (negative).
            if (velocity <= -velocityThresholdPx) {
                currentAnchors.closestAnchor(offset, false)
            } else {
                val lower = currentAnchors.closestAnchor(offset, false)
                val distance = abs(currentAnchor - currentAnchors.getValue(lower))
                val relativeThreshold = abs(positionalThreshold(distance))
                val absoluteThreshold = abs(currentAnchor - relativeThreshold)
                if (offset < 0) {
                    // For negative offsets, larger absolute thresholds are closer to lower anchors
                    // than smaller ones.
                    if (abs(offset) < absoluteThreshold) currentValue else lower
                } else {
                    if (offset > absoluteThreshold) currentValue else lower
                }
            }
        }
    }

    private suspend fun swipe(
        swipePriority: MutatePriority = MutatePriority.Default,
        action: suspend () -> Unit
    ): Unit = coroutineScope { swipeMutex.mutate(swipePriority, action) }

    /**
     * Attempt to snap synchronously. Snapping can happen synchronously when there is no other swipe
     * transaction like a drag or an animation is progress. If there is another interaction in
     * progress, the suspending [snapTo] overload needs to be used.
     *
     * @return true if the synchronous snap was successful, or false if we couldn't snap synchronous
     */
    internal fun trySnapTo(targetValue: T): Boolean = swipeMutex.tryMutate { snap(targetValue) }

    private fun snap(targetValue: T) {
        val targetOffset = anchors[targetValue]
        if (targetOffset != null) {
            dispatchRawDelta(targetOffset - (offset ?: 0f))
            currentValue = targetValue
            animationTarget = null
        } else {
            currentValue = targetValue
        }
    }

    companion object {
        /**
         * The default [Saver] implementation for [SwipeableV2State].
         */
        @ExperimentalMaterial3Api
        fun <T : Any> Saver(
            animationSpec: AnimationSpec<Float>,
            confirmValueChange: (T) -> Boolean,
            positionalThreshold: (distance: Float) -> Float,
            velocityThreshold: () -> Float
        ) = Saver<SwipeableV2State<T>, T>(
            save = { it.currentValue },
            restore = {
                SwipeableV2State(
                    initialValue = it,
                    animationSpec = animationSpec,
                    confirmValueChange = confirmValueChange,
                    positionalThreshold = positionalThreshold,
                    velocityThreshold = velocityThreshold
                )
            }
        )
    }
}

/**
 * Create and remember a [SwipeableV2State].
 *
 * @param initialValue The initial value.
 * @param animationSpec The default animation that will be used to animate to a new value.
 * @param confirmValueChange Optional callback invoked to confirm or veto a pending value change.
 */
@Suppress("PrimitiveInLambda")
@Composable
@ExperimentalMaterial3Api
internal fun <T : Any> rememberSwipeableV2State(
    initialValue: T,
    animationSpec: AnimationSpec<Float> = SwipeableV2Defaults.AnimationSpec,
    confirmValueChange: (newValue: T) -> Boolean = { true }
): SwipeableV2State<T> {
    val positionalThreshold = SwipeableV2Defaults.positionalThreshold
    val velocityThreshold = SwipeableV2Defaults.velocityThreshold

    return rememberSaveable(
        initialValue, animationSpec, confirmValueChange, positionalThreshold, velocityThreshold,
        saver = SwipeableV2State.Saver(
            animationSpec = animationSpec,
            confirmValueChange = confirmValueChange,
            positionalThreshold = positionalThreshold,
            velocityThreshold = velocityThreshold
        ),
    ) {
        SwipeableV2State(
            initialValue = initialValue,
            animationSpec = animationSpec,
            confirmValueChange = confirmValueChange,
            positionalThreshold = positionalThreshold,
            velocityThreshold = velocityThreshold
        )
    }
}

/**
 * Contains useful defaults for [swipeableV2] and [SwipeableV2State].
 */
@Suppress("PrimitiveInLambda")
@Stable
@ExperimentalMaterial3Api
internal object SwipeableV2Defaults {
    /**
     * The default animation used by [SwipeableV2State].
     */
    @ExperimentalMaterial3Api
    val AnimationSpec = SpringSpec<Float>()

    /**
     * The default velocity threshold (1.8 dp per millisecond) used by [rememberSwipeableV2State].
     */
    @ExperimentalMaterial3Api
    val velocityThreshold: () -> Float
        @Composable get() = with(LocalDensity.current) { { 125.dp.toPx() } }

    /**
     * The default positional threshold (56 dp) used by [rememberSwipeableV2State]
     */
    @ExperimentalMaterial3Api
    val positionalThreshold: (totalDistance: Float) -> Float
        @Composable get() = with(LocalDensity.current) {
            { 56.dp.toPx() }
        }

    /**
     * A [AnchorChangeHandler] implementation that attempts to reconcile an in-progress animation
     * by re-targeting it if necessary or finding the closest new anchor.
     * If the previous anchor is not in the new set of anchors, this implementation will snap to the
     * closest anchor.
     *
     * Consider implementing a custom handler for more complex components like sheets.
     * The [animate] and [snap] lambdas hoist the animation and snap logic. Usually these will just
     * delegate to [SwipeableV2State].
     *
     * @param state The [SwipeableV2State] the change handler will read from
     * @param animate A lambda that gets invoked to start an animation to a new target
     * @param snap A lambda that gets invoked to snap to a new target
     */
    @ExperimentalMaterial3Api
    internal fun <T> ReconcileAnimationOnAnchorChangeHandler(
        state: SwipeableV2State<T>,
        animate: (target: T, velocity: Float) -> Unit,
        snap: (target: T) -> Unit
    ) = AnchorChangeHandler { previousTarget, previousAnchors, newAnchors ->
        val previousTargetOffset = previousAnchors[previousTarget]
        val newTargetOffset = newAnchors[previousTarget]
        if (previousTargetOffset != newTargetOffset) {
            if (newTargetOffset != null) {
                animate(previousTarget, state.lastVelocity)
            } else {
                snap(newAnchors.closestAnchor(offset = state.requireOffset()))
            }
        }
    }
}

/**
 * Defines a callback that is invoked when the anchors have changed.
 *
 * Components with custom reconciliation logic should implement this callback, for example to
 * re-target an in-progress animation when the anchors change.
 *
 * @see SwipeableV2Defaults.ReconcileAnimationOnAnchorChangeHandler for a default implementation
 */
@ExperimentalMaterial3Api
internal fun interface AnchorChangeHandler<T> {

    /**
     * Callback that is invoked when the anchors have changed, after the [SwipeableV2State] has been
     * updated with them. Use this hook to re-launch animations or interrupt them if needed.
     *
     * @param previousTargetValue The target value before the anchors were updated
     * @param previousAnchors The previously set anchors
     * @param newAnchors The newly set anchors
     */
    fun onAnchorsChanged(
        previousTargetValue: T,
        previousAnchors: Map<T, Float>,
        newAnchors: Map<T, Float>
    )
}

private fun <T> Map<T, Float>.closestAnchor(
    offset: Float = 0f,
    searchUpwards: Boolean = false
): T {
    require(isNotEmpty()) { "The anchors were empty when trying to find the closest anchor" }
    return minBy { (_, anchor) ->
        val delta = if (searchUpwards) anchor - offset else offset - anchor
        if (delta < 0) Float.POSITIVE_INFINITY else delta
    }.key
}

private fun <T> Map<T, Float>.minOrNull() = minOfOrNull { (_, offset) -> offset }
private fun <T> Map<T, Float>.maxOrNull() = maxOfOrNull { (_, offset) -> offset }

@Stable
internal class InternalMutatorMutex {
    private class Mutator(val priority: MutatePriority, val job: Job) {
        fun canInterrupt(other: Mutator) = priority >= other.priority

        fun cancel() = job.cancel()
    }

    private val currentMutator = AtomicReference<Mutator?>(null)
    private val mutex = Mutex()

    private fun tryMutateOrCancel(mutator: Mutator) {
        while (true) {
            val oldMutator = currentMutator.get()
            if (oldMutator == null || mutator.canInterrupt(oldMutator)) {
                if (currentMutator.compareAndSet(oldMutator, mutator)) {
                    oldMutator?.cancel()
                    break
                }
            } else throw CancellationException("Current mutation had a higher priority")
        }
    }

    /**
     * Enforce that only a single caller may be active at a time.
     *
     * If [mutate] is called while another call to [mutate] or [mutateWith] is in progress, their
     * [priority] values are compared. If the new caller has a [priority] equal to or higher than
     * the call in progress, the call in progress will be cancelled, throwing
     * [CancellationException] and the new caller's [block] will be invoked. If the call in
     * progress had a higher [priority] than the new caller, the new caller will throw
     * [CancellationException] without invoking [block].
     *
     * @param priority the priority of this mutation; [MutatePriority.Default] by default.
     * Higher priority mutations will interrupt lower priority mutations.
     * @param block mutation code to run mutually exclusive with any other call to [mutate],
     * [mutateWith] or [tryMutate].
     */
    suspend fun <R> mutate(
        priority: MutatePriority = MutatePriority.Default,
        block: suspend () -> R
    ) = coroutineScope {
        val mutator = Mutator(priority, coroutineContext[Job]!!)

        tryMutateOrCancel(mutator)

        mutex.withLock {
            try {
                block()
            } finally {
                currentMutator.compareAndSet(mutator, null)
            }
        }
    }

    /**
     * Enforce that only a single caller may be active at a time.
     *
     * If [mutateWith] is called while another call to [mutate] or [mutateWith] is in progress,
     * their [priority] values are compared. If the new caller has a [priority] equal to or
     * higher than the call in progress, the call in progress will be cancelled, throwing
     * [CancellationException] and the new caller's [block] will be invoked. If the call in
     * progress had a higher [priority] than the new caller, the new caller will throw
     * [CancellationException] without invoking [block].
     *
     * This variant of [mutate] calls its [block] with a [receiver], removing the need to create
     * an additional capturing lambda to invoke it with a receiver object. This can be used to
     * expose a mutable scope to the provided [block] while leaving the rest of the state object
     * read-only. For example:
     *
     * @param receiver the receiver `this` that [block] will be called with
     * @param priority the priority of this mutation; [MutatePriority.Default] by default.
     * Higher priority mutations will interrupt lower priority mutations.
     * @param block mutation code to run mutually exclusive with any other call to [mutate],
     * [mutateWith] or [tryMutate].
     */
    suspend fun <T, R> mutateWith(
        receiver: T,
        priority: MutatePriority = MutatePriority.Default,
        block: suspend T.() -> R
    ) = coroutineScope {
        val mutator = Mutator(priority, coroutineContext[Job]!!)

        tryMutateOrCancel(mutator)

        mutex.withLock {
            try {
                receiver.block()
            } finally {
                currentMutator.compareAndSet(mutator, null)
            }
        }
    }

    /**
     * Attempt to mutate synchronously if there is no other active caller.
     * If there is no other active caller, the [block] will be executed in a lock. If there is
     * another active caller, this method will return false, indicating that the active caller
     * needs to be cancelled through a [mutate] or [mutateWith] call with an equal or higher
     * mutation priority.
     *
     * Calls to [mutate] and [mutateWith] will suspend until execution of the [block] has finished.
     *
     * @param block mutation code to run mutually exclusive with any other call to [mutate],
     * [mutateWith] or [tryMutate].
     * @return true if the [block] was executed, false if there was another active caller and the
     * [block] was not executed.
     */
    fun tryMutate(block: () -> Unit): Boolean {
        val didLock = mutex.tryLock()
        if (didLock) {
            try {
                block()
            } finally {
                mutex.unlock()
            }
        }
        return didLock
    }
}

@ExperimentalMaterial3Api
@Suppress("Deprecation")
fun SheetState(
    skipPartiallyExpanded: Boolean,
    density: Density,
    initialValue: SheetValue = SheetValue.Hidden,
    confirmValueChange: (SheetValue) -> Boolean = { true },
    skipHiddenState: Boolean = false,
) = SheetState(
    skipPartiallyExpanded, initialValue, confirmValueChange, skipHiddenState
).also {
    it.density = density
}

/**
 * State of a sheet composable, such as [ModalBottomSheet]
 *
 * Contains states relating to its swipe position as well as animations between state values.
 *
 * @param skipPartiallyExpanded Whether the partially expanded state, if the sheet is large
 * enough, should be skipped. If true, the sheet will always expand to the [Expanded] state and move
 * to the [Hidden] state if available when hiding the sheet, either programmatically or by user
 * interaction.
 * @param initialValue The initial value of the state.
 * @param confirmValueChange Optional callback invoked to confirm or veto a pending state change.
 * @param skipHiddenState Whether the hidden state should be skipped. If true, the sheet will always
 * expand to the [Expanded] state and move to the [PartiallyExpanded] if available, either
 * programmatically or by user interaction.
 */
@Stable
@ExperimentalMaterial3Api
class SheetState @Deprecated(
    message = "This constructor is deprecated. " +
            "Please use the constructor that provides a [Density]",
    replaceWith = ReplaceWith(
        "SheetState(" +
                "skipPartiallyExpanded, LocalDensity.current, initialValue, " +
                "confirmValueChange, skipHiddenState)"
    )
) constructor(
    internal val skipPartiallyExpanded: Boolean,
    initialValue: SheetValue = SheetValue.Hidden,
    confirmValueChange: (SheetValue) -> Boolean = { true },
    internal val skipHiddenState: Boolean = false,
) {
    init {
        if (skipPartiallyExpanded) {
            require(initialValue != SheetValue.PartiallyExpanded) {
                "The initial value must not be set to PartiallyExpanded if skipPartiallyExpanded " +
                        "is set to true."
            }
        }
        if (skipHiddenState) {
            require(initialValue != SheetValue.Hidden) {
                "The initial value must not be set to Hidden if skipHiddenState is set to true."
            }
        }
    }

    /**
     * The current value of the state.
     *
     * If no swipe or animation is in progress, this corresponds to the state the bottom sheet is
     * currently in. If a swipe or an animation is in progress, this corresponds the state the sheet
     * was in before the swipe or animation started.
     */

    val currentValue: SheetValue get() = swipeableState.currentValue

    /**
     * The target value of the bottom sheet state.
     *
     * If a swipe is in progress, this is the value that the sheet would animate to if the
     * swipe finishes. If an animation is running, this is the target value of that animation.
     * Finally, if no swipe or animation is in progress, this is the same as the [currentValue].
     */
    val targetValue: SheetValue get() = swipeableState.targetValue

    /**
     * Whether the modal bottom sheet is visible.
     */
    val isVisible: Boolean
        get() = swipeableState.currentValue != SheetValue.Hidden

    /**
     * Require the current offset (in pixels) of the bottom sheet.
     *
     * The offset will be initialized during the first measurement phase of the provided sheet
     * content.
     *
     * These are the phases:
     * Composition { -> Effects } -> Layout { Measurement -> Placement } -> Drawing
     *
     * During the first composition, an [IllegalStateException] is thrown. In subsequent
     * compositions, the offset will be derived from the anchors of the previous pass. Always prefer
     * accessing the offset from a LaunchedEffect as it will be scheduled to be executed the next
     * frame, after layout.
     *
     * @throws IllegalStateException If the offset has not been initialized yet
     */
    fun requireOffset(): Float = swipeableState.requireOffset()

    /**
     * Whether the sheet has an expanded state defined.
     */

    val hasExpandedState: Boolean
        get() = swipeableState.hasAnchorForValue(SheetValue.Expanded)

    /**
     * Whether the modal bottom sheet has a partially expanded state defined.
     */
    val hasPartiallyExpandedState: Boolean
        get() = swipeableState.hasAnchorForValue(SheetValue.PartiallyExpanded)

    /**
     * Fully expand the bottom sheet with animation and suspend until it is fully expanded or
     * animation has been cancelled.
     * *
     * @throws [CancellationException] if the animation is interrupted
     */
    suspend fun expand() {
        swipeableState.animateTo(SheetValue.Expanded)
    }

    /**
     * Animate the bottom sheet and suspend until it is partially expanded or animation has been
     * cancelled.
     * @throws [CancellationException] if the animation is interrupted
     * @throws [IllegalStateException] if [skipPartiallyExpanded] is set to true
     */
    suspend fun partialExpand() {
        check(!skipPartiallyExpanded) {
            "Attempted to animate to partial expanded when skipPartiallyExpanded was enabled. Set" +
                    " skipPartiallyExpanded to false to use this function."
        }
        animateTo(SheetValue.PartiallyExpanded)
    }

    /**
     * Expand the bottom sheet with animation and suspend until it is [PartiallyExpanded] if defined
     * else [Expanded].
     * @throws [CancellationException] if the animation is interrupted
     */
    suspend fun show() {
        val targetValue = when {
            hasPartiallyExpandedState -> SheetValue.PartiallyExpanded
            else -> SheetValue.Expanded
        }
        animateTo(targetValue)
    }

    /**
     * Hide the bottom sheet with animation and suspend until it is fully hidden or animation has
     * been cancelled.
     * @throws [CancellationException] if the animation is interrupted
     */
    suspend fun hide() {
        check(!skipHiddenState) {
            "Attempted to animate to hidden when skipHiddenState was enabled. Set skipHiddenState" +
                    " to false to use this function."
        }
        animateTo(SheetValue.Hidden)
    }

    /**
     * Animate to a [targetValue].
     * If the [targetValue] is not in the set of anchors, the [currentValue] will be updated to the
     * [targetValue] without updating the offset.
     *
     * @throws CancellationException if the interaction interrupted by another interaction like a
     * gesture interaction or another programmatic interaction like a [animateTo] or [snapTo] call.
     *
     * @param targetValue The target value of the animation
     */
    internal suspend fun animateTo(
        targetValue: SheetValue,
        velocity: Float = swipeableState.lastVelocity
    ) {
        swipeableState.animateTo(targetValue, velocity)
    }

    /**
     * Snap to a [targetValue] without any animation.
     *
     * @throws CancellationException if the interaction interrupted by another interaction like a
     * gesture interaction or another programmatic interaction like a [animateTo] or [snapTo] call.
     *
     * @param targetValue The target value of the animation
     */
    internal suspend fun snapTo(targetValue: SheetValue) {
        swipeableState.snapTo(targetValue)
    }

    /**
     * Attempt to snap synchronously. Snapping can happen synchronously when there is no other swipe
     * transaction like a drag or an animation is progress. If there is another interaction in
     * progress, the suspending [snapTo] overload needs to be used.
     *
     * @return true if the synchronous snap was successful, or false if we couldn't snap synchronous
     */
    internal fun trySnapTo(targetValue: SheetValue) = swipeableState.trySnapTo(targetValue)

    /**
     * Find the closest anchor taking into account the velocity and settle at it with an animation.
     */
    internal suspend fun settle(velocity: Float) {
        swipeableState.settle(velocity)
    }

    internal var swipeableState = SwipeableV2State(
        initialValue = initialValue,
        animationSpec = SwipeableV2Defaults.AnimationSpec,
        confirmValueChange = confirmValueChange,
        positionalThreshold = { with(requireDensity()) { 56.dp.toPx() } },
        velocityThreshold = { with(requireDensity()) { 125.dp.toPx() } }
    )

    internal val offset: Float? get() = swipeableState.offset

    internal var density: Density? = null
    private fun requireDensity() = requireNotNull(density) {
        "SheetState did not have a density attached. Are you using SheetState with " +
                "BottomSheetScaffold or ModalBottomSheet component?"
    }

    companion object {
        /**
         * The default [Saver] implementation for [SheetState].
         */
        fun Saver(
            skipPartiallyExpanded: Boolean,
            confirmValueChange: (SheetValue) -> Boolean,
            density: Density,
            skipHiddenState: Boolean
        ) = Saver<SheetState, SheetValue>(
            save = { it.currentValue },
            restore = { savedValue ->
                SheetState(skipPartiallyExpanded, density, savedValue, confirmValueChange, skipHiddenState)
            }
        )

        /**
         * The default [Saver] implementation for [SheetState].
         */
        @Deprecated(
            message = "This function is deprecated. Please use the overload where Density is" +
                    " provided.",
            replaceWith = ReplaceWith(
                "Saver(skipPartiallyExpanded, confirmValueChange, LocalDensity.current)"
            )
        )
        @Suppress("Deprecation")
        fun Saver(
            skipPartiallyExpanded: Boolean,
            confirmValueChange: (SheetValue) -> Boolean
        ) = Saver<SheetState, SheetValue>(
            save = { it.currentValue },
            restore = { savedValue ->
                SheetState(skipPartiallyExpanded, savedValue, confirmValueChange)
            }
        )
    }
}

/**
 * Possible values of [SheetState].
 */
@ExperimentalMaterial3Api
enum class SheetValue {
    /**
     * The sheet is not visible.
     */
    Hidden,

    /**
     * The sheet is visible at full height.
     */
    Expanded,

    /**
     * The sheet is partially visible.
     */
    PartiallyExpanded,
}

/**
 * Contains the default values used by [ModalBottomSheet] and [BottomSheetScaffold].
 */
@Stable
@ExperimentalMaterial3Api
object BottomSheetDefaults {
    /** The default shape for bottom sheets in a [Hidden] state. */
    val HiddenShape: Shape
        @Composable get() = MaterialTheme.shapes.extraSmall

    /** The default shape for a bottom sheets in [PartiallyExpanded] and [Expanded] states. */
    val ExpandedShape: Shape
        @Composable get() = MaterialTheme.shapes.extraLarge

    /** The default container color for a bottom sheet. */
    val ContainerColor: Color
        @Composable get() = MaterialTheme.colorScheme.surface

    /** The default elevation for a bottom sheet. */
    val Elevation = 1.dp

    /** The default color of the scrim overlay for background content. */
    val ScrimColor: Color
        @Composable get() = MaterialTheme.colorScheme.scrim.copy(0.32f)

    /**
     * The default peek height used by [BottomSheetScaffold].
     */
    val SheetPeekHeight = 56.dp

    /**
     * Default insets to be used and consumed by the [ModalBottomSheet] window.
     */
    val windowInsets: WindowInsets
        @Composable
        get() = WindowInsets.systemBars.only(WindowInsetsSides.Vertical)

    /**
     * The optional visual marker placed on top of a bottom sheet to indicate it may be dragged.
     */
    @Composable
    fun DragHandle(
        modifier: Modifier = Modifier,
        width: Dp = SheetBottomTokens.DockedDragHandleWidth,
        height: Dp = SheetBottomTokens.DockedDragHandleHeight,
        shape: Shape = MaterialTheme.shapes.extraLarge,
        color: Color = MaterialTheme.colorScheme.onSurfaceVariant
            .copy(SheetBottomTokens.DockedDragHandleOpacity),
    ) {
        Surface(
            modifier = modifier
                .padding(top = DragHandleVerticalPadding)
                .semantics { contentDescription = "dragHandleDescription" },
            color = color,
            shape = shape
        ) {
            Box(
                Modifier
                    .size(
                        width = width,
                        height = height
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
internal fun ConsumeSwipeWithinBottomSheetBoundsNestedScrollConnection(
    sheetState: SheetState,
    orientation: Orientation,
    onFling: (velocity: Float) -> Unit
): NestedScrollConnection = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.toFloat()
        return if (delta < 0 && source == NestedScrollSource.Drag) {
            sheetState.swipeableState.dispatchRawDelta(delta).toOffset()
        } else {
            Offset.Zero
        }
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        return if (source == NestedScrollSource.Drag) {
            sheetState.swipeableState.dispatchRawDelta(available.toFloat()).toOffset()
        } else {
            Offset.Zero
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        val toFling = available.toFloat()
        val currentOffset = sheetState.requireOffset()
        return if (toFling < 0 && currentOffset > sheetState.swipeableState.minOffset) {
            onFling(toFling)
            // since we go to the anchor with tween settling, consume all for the best UX
            available
        } else {
            Velocity.Zero
        }
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        onFling(available.toFloat())
        return available
    }

    private fun Float.toOffset(): Offset = Offset(
        x = if (orientation == Orientation.Horizontal) this else 0f,
        y = if (orientation == Orientation.Vertical) this else 0f
    )

    @JvmName("velocityToFloat")
    private fun Velocity.toFloat() = if (orientation == Orientation.Horizontal) x else y

    @JvmName("offsetToFloat")
    private fun Offset.toFloat(): Float = if (orientation == Orientation.Horizontal) x else y
}

@Composable
@ExperimentalMaterial3Api
internal fun rememberSheetState(
    skipPartiallyExpanded: Boolean = false,
    confirmValueChange: (SheetValue) -> Boolean = { true },
    initialValue: SheetValue = SheetValue.Hidden,
    skipHiddenState: Boolean = false,
): SheetState {

    val density = LocalDensity.current
    return rememberSaveable(
        skipPartiallyExpanded, confirmValueChange,
        saver = SheetState.Saver(
            skipPartiallyExpanded = skipPartiallyExpanded,
            confirmValueChange = confirmValueChange,
            density = density,
            skipHiddenState = skipHiddenState
        )
    ) {
        SheetState(
            skipPartiallyExpanded,
            density,
            initialValue,
            confirmValueChange,
            skipHiddenState
        )
    }
}

private val DragHandleVerticalPadding = 22.dp
internal val BottomSheetMaxWidth = 640.dp

@ExperimentalMaterial3Api
private fun BottomSheetScaffoldAnchorChangeHandler(
    state: SheetState,
    animateTo: (target: SheetValue, velocity: Float) -> Unit,
    snapTo: (target: SheetValue) -> Unit,
) = AnchorChangeHandler<SheetValue> { previousTarget, previousAnchors, newAnchors ->
    val previousTargetOffset = previousAnchors[previousTarget]
    val newTarget = when (previousTarget) {
        SheetValue.Hidden, SheetValue.PartiallyExpanded -> SheetValue.PartiallyExpanded
        SheetValue.Expanded -> if (newAnchors.containsKey(SheetValue.Expanded)) SheetValue.Expanded else SheetValue.PartiallyExpanded
    }
    val newTargetOffset = newAnchors.getValue(newTarget)
    if (newTargetOffset != previousTargetOffset) {
        if (state.swipeableState.isAnimationRunning) {
            // Re-target the animation to the new offset if it changed
            animateTo(newTarget, state.swipeableState.lastVelocity)
        } else {
            // Snap to the new offset value of the target if no animation was running
            snapTo(newTarget)
        }
    }
}

@ExperimentalMaterial3Api
@Stable
class BottomSheetScaffoldState(
    val bottomSheetState: SheetState,
    val snackbarHostState: SnackbarHostState
)

/**
 * Create and [remember] a [BottomSheetScaffoldState].
 *
 * @param bottomSheetState the state of the standard bottom sheet. See
 * [rememberStandardBottomSheetState]
 * @param snackbarHostState the [SnackbarHostState] used to show snackbars inside the scaffold
 */
@Composable
@ExperimentalMaterial3Api
fun rememberBottomSheetScaffoldState(
    bottomSheetState: SheetState = rememberStandardBottomSheetState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
): BottomSheetScaffoldState {
    return remember(bottomSheetState, snackbarHostState) {
        BottomSheetScaffoldState(
            bottomSheetState = bottomSheetState,
            snackbarHostState = snackbarHostState
        )
    }
}

/**
 * Create and [remember] a [SheetState] for [BottomSheetScaffold].
 *
 * @param initialValue the initial value of the state. Should be either [PartiallyExpanded] or
 * [Expanded] if [skipHiddenState] is true
 * @param confirmValueChange optional callback invoked to confirm or veto a pending state change
 * @param [skipHiddenState] whether Hidden state is skipped for [BottomSheetScaffold]
 */
@Composable
@ExperimentalMaterial3Api
fun rememberStandardBottomSheetState(
    initialValue: SheetValue = SheetValue.PartiallyExpanded,
    confirmValueChange: (SheetValue) -> Boolean = { true },
    skipHiddenState: Boolean = true,
) = rememberSheetState(false, confirmValueChange, initialValue, skipHiddenState)

internal object SheetBottomTokens {
    val DockedDragHandleHeight = 4.0.dp
    const val DockedDragHandleOpacity = 0.4f
    val DockedDragHandleWidth = 32.0.dp
}