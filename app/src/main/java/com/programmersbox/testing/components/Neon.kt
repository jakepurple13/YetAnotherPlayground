package com.programmersbox.testing.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

@Composable
fun NeonScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NeonSample()
    }
}

@Composable
private fun NeonSample() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(Color.Black)
    ) {

        val paint = remember {
            Paint().apply {
                style = PaintingStyle.Stroke
                strokeWidth = 30f
            }
        }

        val frameworkPaint = remember {
            paint.asFrameworkPaint()
        }

        val color = Color.Blue

        val transition: InfiniteTransition = rememberInfiniteTransition(label = "")

        // Infinite phase animation for PathEffect
        val phase by transition.animateFloat(
            initialValue = .9f,
            targetValue = .3f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1500,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = ""
        )


        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            this.drawIntoCanvas {

                val transparent = color
                    .copy(alpha = 0f)
                    .toArgb()

                frameworkPaint.color = transparent

                paint.asFrameworkPaint().setShadowLayer(
                    30f * phase,
                    0f,
                    0f,
                    color
                        .copy(alpha = phase)
                        .toArgb()
                )

                it.drawRoundRect(
                    left = 100f,
                    top = 100f,
                    right = 500f,
                    bottom = 500f,
                    radiusX = 5.dp.toPx(),
                    5.dp.toPx(),
                    paint = paint
                )

                drawRoundRect(
                    Color.White,
                    topLeft = Offset(100f, 100f),
                    size = Size(400f, 400f),
                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )


                frameworkPaint.setShadowLayer(
                    30f,
                    0f,
                    0f,
                    color
                        .copy(alpha = .5f)
                        .toArgb()
                )


                it.drawRoundRect(
                    left = 600f,
                    top = 100f,
                    right = 1000f,
                    bottom = 500f,
                    radiusX = 5.dp.toPx(),
                    5.dp.toPx(),
                    paint = paint
                )

                drawRoundRect(
                    Color.White,
                    topLeft = Offset(600f, 100f),
                    size = Size(400f, 400f),
                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}