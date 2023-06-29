package com.programmersbox.testing.components

import android.util.Range
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import com.programmersbox.testing.ui.theme.LightAndDarkPreviews
import com.programmersbox.testing.ui.theme.TestingTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GaugeScreen() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.wrapContentSize()
    ) {
        var value by remember { mutableFloatStateOf(40f) }
        Gauge(
            modifier = Modifier.size(300.dp),
            dot = value,
            onDotChange = { value = it }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Gauge(
    modifier: Modifier = Modifier,
    dotsSize: Float = 10f,
    dotsCircleSize: Float = dotsSize * 2,
    sensitivity: Float = dotsSize,
    currentValue: Float = 50f,
    minValue: Float = 0f,
    maxValue: Float = 100f,
    dot: Float = 75f,
    linesStroke: Float = 30F,
    circleStroke: Stroke = Stroke(
        width = linesStroke,
        cap = StrokeCap.Round
    ),
    onDotChange: (Float) -> Unit = {},
    onDotChangeFinished: () -> Unit = {}
) {
    val dotsList = remember(dot) { mutableListOf<Dot>() }

    Canvas(
        modifier.pointerInteropFilter {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    dotsList.find { dots ->
                        it.x in Range(
                            dots.offset.x - sensitivity,
                            dots.offset.x + sensitivity
                        ) && it.y in Range(
                            dots.offset.y - sensitivity,
                            dots.offset.y + sensitivity
                        )
                    }?.let { dots ->

                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    //Connecting and adding
                    dotsList.find { dots ->
                        it.x in Range(
                            dots.offset.x - sensitivity,
                            dots.offset.x + sensitivity
                        ) && it.y in Range(
                            dots.offset.y - sensitivity,
                            dots.offset.y + sensitivity
                        )
                    }
                        ?.let { dots ->
                            val (x, y) = dots.offset

                            onDotChange(1f)
                        }
                }

                MotionEvent.ACTION_UP -> {
                    onDotChangeFinished()
                }
            }
            true
        }
    ) {

        val radius = (size.width / 2) - (dotsSize * 2) - circleStroke.width
        val angleInDegrees = (dot * 360.0) + 50.0
        val x = -(radius * sin(Math.toRadians(angleInDegrees))).toFloat() + (size.width / 2)
        val y = (radius * cos(Math.toRadians(angleInDegrees))).toFloat() + (size.height / 2)

        dotsList.add(Dot(Offset(x, y)))

        val minusOffset = 200f

        drawArc(
            color = Color.Green,
            useCenter = false,
            style = circleStroke,
            startAngle = 135f,
            sweepAngle = 270f,
            size = size.copy(
                width = size.width - minusOffset,
                height = size.height - minusOffset
            ),
            topLeft = Offset(minusOffset / 2, minusOffset / 2)
        )

        for (dots in dotsList) {
            val dotCenter = dots.offset.copy(
                x = dots.offset.x - circleStroke.width - minusOffset / 3,
                y = dots.offset.y - circleStroke.width - minusOffset / 3,
            )
            drawCircle(
                color = Color.Blue,
                radius = dotsCircleSize,
                style = Stroke(width = 2.dp.value),
                center = dotCenter
            )
            drawLine(
                color = Color.Red,
                start = dotCenter,
                end = center
            )
        }
    }
}

data class Dot(
    val offset: Offset
)

@Composable
fun Speedometer(
    progress: Float,
    modifier: Modifier = Modifier,
    minSpeed: Float = 0f,
    maxSpeed: Float = 100f,
    onDotChange: (Float) -> Unit = {}
) {
    val arcDegrees = 270f
    val startArcAngle = 135f

    val numberOfMarkers = 55
    val degreesMarkerStep = arcDegrees / numberOfMarkers

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                //handleCenter += dragAmount

                //angle = getRotationAngle(handleCenter, shapeCenter)
                //onDotChange(getRotationAngle(handleCenter, shapeCenter))
                change.consume()
            }
        },
        onDraw = {
            val (w, h) = size
            val centerOffset = Offset(w / 2f, h / 2f)
            val quarterOffset = Offset(w / 4f, h / 4f)

            val centerArcSize = Size(w / 2f, h / 2f)
            val centerArcStroke = Stroke(20f, 0f, StrokeCap.Round)
            drawArc(
                Color.Blue,
                startArcAngle,
                arcDegrees,
                false,
                topLeft = quarterOffset,
                size = centerArcSize,
                style = centerArcStroke
            )

            // Drawing Center Arc progress
            drawArc(
                Color.Red,
                startArcAngle,
                degreesMarkerStep * progress,
                false,
                topLeft = quarterOffset,
                size = centerArcSize,
                style = centerArcStroke
            )

            val dotCenter = Offset(
                x = h,
                y = w
            )
            drawLine(
                color = Color.Red,
                start = dotCenter,
                end = center,
                strokeWidth = 4f,
            )
            drawCircle(
                color = Color.Blue,
                radius = 40F,
                center = dotCenter,
            )
        }
    )
}

private fun degreeAtSpeed(
    speed: Float,
    minSpeed: Float,
    maxSpeed: Float,
    startDegree: Int,
    endDegree: Int,
): Float {
    return (speed - minSpeed) * (endDegree - startDegree) / (maxSpeed - minSpeed) + startDegree
}

private fun getRotationAngle(currentPosition: Offset, center: Offset): Float {
    val (dx, dy) = currentPosition - center
    val theta = atan2(dy, dx).toDouble()

    var angle = Math.toDegrees(theta)

    if (angle < 0) {
        angle += 360.0
    }
    return angle.toFloat()
}

@LightAndDarkPreviews
@Composable
private fun GaugePreview() {
    TestingTheme {
        Column {
            GaugeScreen()

            var value by remember { mutableFloatStateOf(10f) }
            Speedometer(
                progress = value,
                modifier = Modifier.size(300.dp),
                onDotChange = { value = it }
            )
        }
    }
}