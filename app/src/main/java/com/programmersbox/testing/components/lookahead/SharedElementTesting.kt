package com.programmersbox.testing.components.lookahead

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentWithReceiverOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.intermediateLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.programmersbox.testing.ui.theme.LocalNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SceneHost(content: @Composable SceneScope.() -> Unit) {
    LookaheadScope {
        val sceneScope = remember { SceneScope(this) }
        sceneScope.content()
    }
}

private const val debugSharedElement = true

@OptIn(ExperimentalComposeUiApi::class)
class SceneScope internal constructor(
    lookaheadScope: LookaheadScope
) : LookaheadScope by lookaheadScope {
    fun Modifier.sharedElement(): Modifier = composed {
        val offsetAnimation: DeferredAnimation<IntOffset, AnimationVector2D> =
            remember {
                DeferredAnimation(IntOffset.VectorConverter)
            }
        val sizeAnimation: DeferredAnimation<IntSize, AnimationVector2D> =
            remember { DeferredAnimation(IntSize.VectorConverter) }
        var placementOffset: IntOffset by remember { mutableStateOf(IntOffset.Zero) }
        this
            .drawBehind {
                if (debugSharedElement) {
                    drawRect(
                        color = Color.Black,
                        style = Stroke(2f),
                        topLeft = (offsetAnimation.target!! - placementOffset).toOffset(),
                        size = sizeAnimation.target!!.toSize()
                    )
                }
            }
            .intermediateLayout { measurable, _ ->
                val (width, height) = sizeAnimation.updateTarget(
                    lookaheadSize, spring(stiffness = Spring.StiffnessMediumLow)
                )
                val animatedConstraints = Constraints.fixed(width, height)
                val placeable = measurable.measure(animatedConstraints)
                layout(placeable.width, placeable.height) {
                    val (x, y) = offsetAnimation.updateTargetBasedOnCoordinates(
                        spring(stiffness = Spring.StiffnessMediumLow),
                    )
                    coordinates?.let {
                        placementOffset = lookaheadScopeCoordinates
                            .localPositionOf(
                                it, Offset.Zero
                            )
                            .round()
                    }
                    placeable.place(x, y)
                }
            }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.animateSizeAndSkipToFinalLayout() = composed {
    var sizeAnimation: Animatable<IntSize, AnimationVector2D>? by remember {
        mutableStateOf(null)
    }
    var targetSize: IntSize? by remember { mutableStateOf(null) }
    this
        .drawBehind {
            if (debugSharedElement) {
                drawRect(
                    color = Color.Black,
                    style = Stroke(2f),
                    topLeft = Offset.Zero,
                    size = targetSize!!.toSize()
                )
            }
        }
        .intermediateLayout { measurable, constraints ->
            targetSize = lookaheadSize
            if (lookaheadSize != sizeAnimation?.targetValue) {
                sizeAnimation?.run {
                    launch { animateTo(lookaheadSize) }
                } ?: Animatable(lookaheadSize, IntSize.VectorConverter).let {
                    sizeAnimation = it
                }
            }
            val (width, height) = sizeAnimation?.value ?: lookaheadSize
            val placeable = measurable.measure(
                Constraints.fixed(lookaheadSize.width, lookaheadSize.height)
            )
            // Make sure the content is aligned to topStart
            val wrapperWidth = width.coerceIn(constraints.minWidth, constraints.maxWidth)
            val wrapperHeight =
                height.coerceIn(constraints.minHeight, constraints.maxHeight)
            layout(width, height) {
                placeable.place(-(wrapperWidth - width) / 2, -(wrapperHeight - height) / 2)
            }
        }
}

@Composable
fun SharedElementExplorationDemo() {
    val A = remember {
        movableContentWithReceiverOf<SceneScope, Modifier> { modifier ->
            Box(
                modifier = Modifier
                    .sharedElement()
                    .then(modifier)
                    .background(color = Color(0xfff3722c), RoundedCornerShape(10))
            )
        }
    }
    val B = remember {
        movableContentWithReceiverOf<SceneScope, Modifier> { modifier ->
            Box(
                modifier = Modifier
                    .sharedElement()
                    .then(modifier)
                    .background(color = Color(0xff90be6d), RoundedCornerShape(10))
            )
        }
    }
    val C = remember {
        movableContentWithReceiverOf<SceneScope, @Composable () -> Unit> { content ->
            Box(
                Modifier
                    .sharedElement()
                    .background(Color(0xfff9c74f))
                    .padding(20.dp)
            ) {
                content()
            }
        }
    }
    var isHorizontal by remember { mutableStateOf(true) }
    SceneHost {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .clickable { isHorizontal = !isHorizontal }
        ) {
            if (isHorizontal) {
                C {
                    Row(
                        Modifier
                            .background(Color.Gray)
                            .padding(10.dp)
                    ) {
                        A(Modifier.size(40.dp))
                        B(Modifier.size(40.dp))
                        Box(
                            Modifier
                                .size(40.dp)
                                .background(Color(0xff4d908e))
                        )
                    }
                }
            } else {
                C {
                    Column(
                        Modifier
                            .background(Color.DarkGray)
                            .padding(10.dp)
                    ) {
                        A(Modifier.size(width = 120.dp, height = 60.dp))
                        B(Modifier.size(width = 120.dp, height = 60.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedElementWithNavDemo() {
    val A = remember {
        movableContentWithReceiverOf<SceneScope, Modifier> { modifier ->
            Box(
                modifier = Modifier
                    .sharedElement()
                    .then(modifier)
                    .background(color = Color(0xfff3722c), RoundedCornerShape(10))
            )
        }
    }
    val B = remember {
        movableContentWithReceiverOf<SceneScope, Modifier> { modifier ->
            Box(
                modifier = Modifier
                    .sharedElement()
                    .then(modifier)
                    .background(color = Color(0xff90be6d), RoundedCornerShape(10))
            )
        }
    }
    val C = remember {
        movableContentWithReceiverOf<SceneScope, @Composable () -> Unit> { content ->
            Box(
                Modifier
                    .sharedElement()
                    .background(Color(0xfff9c74f))
                    .padding(20.dp)
            ) {
                content()
            }
        }
    }
    val navController = rememberNavController()
    Scaffold(
        topBar = { TopAppBar(title = { Text("SharedElement Test") }) }
    ) { padding ->
        SceneHost {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .clickable {
                        if (navController.currentBackStackEntry?.destination?.route == "A") {
                            navController.navigate("B") { launchSingleTop = true }
                        } else {
                            navController.navigate("A") { launchSingleTop = true }
                        }
                    }
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "A"
                ) {
                    composable("A") {
                        C {
                            Row(
                                Modifier
                                    .background(Color.Gray)
                                    .padding(10.dp)
                            ) {
                                A(Modifier.size(40.dp))
                                B(Modifier.size(40.dp))
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .background(Color(0xff4d908e))
                                )
                            }
                        }
                    }

                    composable("B") {
                        C {
                            Column(
                                Modifier
                                    .background(Color.DarkGray)
                                    .padding(10.dp)
                            ) {
                                A(Modifier.size(width = 120.dp, height = 60.dp))
                                B(Modifier.size(width = 120.dp, height = 60.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedElementWithNavDemo1() {
    val A = remember {
        movableContentWithReceiverOf<SceneScope, Modifier> { modifier ->
            Box(
                modifier = Modifier
                    .sharedElement()
                    .then(modifier)
                    .background(color = Color(0xfff3722c), RoundedCornerShape(10))
            )
        }
    }
    val B = remember {
        movableContentWithReceiverOf<SceneScope, Modifier> { modifier ->
            Box(
                modifier = Modifier
                    .sharedElement()
                    .then(modifier)
                    .background(color = Color(0xff90be6d), RoundedCornerShape(10))
            )
        }
    }
    val C = remember {
        movableContentWithReceiverOf<SceneScope, @Composable () -> Unit> { content ->
            Box(
                Modifier
                    .sharedElement()
                    .background(Color(0xfff9c74f))
                    .padding(20.dp)
            ) {
                content()
            }
        }
    }

    @Composable
    fun SceneScope.AScreen() {
        C {
            Row(
                Modifier
                    .background(Color.Gray)
                    .padding(10.dp)
            ) {
                A(Modifier.size(40.dp))
                B(Modifier.size(40.dp))
                Box(
                    Modifier
                        .size(40.dp)
                        .background(Color(0xff4d908e))
                )
            }
        }
    }

    @Composable
    fun SceneScope.BScreen() {
        C {
            Column(
                Modifier
                    .background(Color.DarkGray)
                    .padding(10.dp)
            ) {
                A(Modifier.size(width = 120.dp, height = 60.dp))
                B(Modifier.size(width = 120.dp, height = 60.dp))
            }
        }
    }

    val navController = rememberNavController()
    Scaffold(
        topBar = { TopAppBar(title = { Text("SharedElement Test") }) }
    ) { padding ->
        SceneHost {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .clickable {
                        if (navController.currentBackStackEntry?.destination?.route == "A") {
                            navController.navigate("B") { launchSingleTop = true }
                        } else {
                            navController.navigate("A") { launchSingleTop = true }
                        }
                    }
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "A"
                ) {
                    composable("A") {
                        AScreen()
                    }

                    composable("B") {
                        var s by remember { mutableStateOf(true) }
                        LaunchedEffect(Unit) {
                            delay(1)
                            s = false
                        }
                        SceneHost {
                            if (s) {
                                AScreen()
                            } else {
                                BScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedElementWithNavDemo2() {
    val A = remember {
        movableContentWithReceiverOf<SceneScope, Modifier> { modifier ->
            Box(
                modifier = Modifier
                    .sharedElement()
                    .then(modifier)
                    .background(color = Color(0xfff3722c), RoundedCornerShape(10))
            )
        }
    }
    val B = remember {
        movableContentWithReceiverOf<SceneScope, Modifier> { modifier ->
            Box(
                modifier = Modifier
                    .sharedElement()
                    .then(modifier)
                    .background(color = Color(0xff90be6d), RoundedCornerShape(10))
            )
        }
    }
    val C = remember {
        movableContentWithReceiverOf<SceneScope, @Composable () -> Unit> { content ->
            Box(
                Modifier
                    .sharedElement()
                    .background(Color(0xfff9c74f))
                    .padding(20.dp)
            ) {
                content()
            }
        }
    }

    val D = remember {
        movableContentWithReceiverOf<SceneScope, Modifier> {
            TestThing(modifier = it.sharedElement())
        }
    }

    @Composable
    fun SceneScope.AScreen() {
        C {
            Row(
                Modifier
                    .background(Color.Gray)
                    .padding(10.dp)
            ) {
                A(Modifier.size(40.dp))
                B(Modifier.size(40.dp))
                Box(
                    Modifier
                        .size(40.dp)
                        .background(Color(0xff4d908e))
                )
                D(Modifier.size(40.dp))
            }
        }
    }

    @Composable
    fun SceneScope.BScreen() {
        C {
            Column(
                Modifier
                    .background(Color.DarkGray)
                    .padding(10.dp)
            ) {
                A(Modifier.size(width = 120.dp, height = 60.dp))
                B(Modifier.size(width = 120.dp, height = 60.dp))
                D(Modifier.size(width = 120.dp, height = 60.dp))
            }
        }
    }

    val navController = rememberNavController()

    Scaffold(
        topBar = {
            val pNav = LocalNavController.current
            TopAppBar(
                title = { Text("SharedElement Test") },
                navigationIcon = {
                    IconButton(onClick = pNav::popBackStack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        SceneHost {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .clickable {
                        if (navController.currentBackStackEntry?.destination?.route == "A") {
                            navController.navigate("B") { launchSingleTop = true }
                        } else {
                            navController.navigate("A") { launchSingleTop = true }
                        }
                    }
            ) {
                SceneHost {
                    NavHost(
                        navController = navController,
                        startDestination = "A",
                        //enterTransition = {  }
                    ) {
                        composable("A") {
                            AScreen()
                        }

                        composable("B") {
                            BScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TestThing(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(color = Color(0xff00bcd4), RoundedCornerShape(10))
    )
}