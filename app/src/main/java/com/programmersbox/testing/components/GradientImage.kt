package com.programmersbox.testing.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun GradientImageScreen() {
    val imageUrl =
        "https://image.api.playstation.com/vulcan/ap/rnd/202207/1210/4xJ8XB3bi888QTLZYdl7Oi0s.png"

    var resetKey by remember { mutableIntStateOf(0) }
    var blur by remember(resetKey) { mutableStateOf(70.dp) }
    var alpha by remember(resetKey) { mutableFloatStateOf(.5f) }
    var saturation by remember(resetKey) { mutableFloatStateOf(3f) }
    var scale by remember(resetKey) { mutableStateOf(1f to 1.8f) }

    Scaffold(
        bottomBar = {
            Column {
                Button(
                    onClick = { resetKey++ },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Reset All") }
                OptionChange(
                    title = "Alpha",
                    value = alpha,
                    onValueChange = { alpha = it },
                    valueRange = 0f..1f,
                    onReset = { alpha = .5f }
                )

                OptionChange(
                    title = "Blur",
                    value = blur.value,
                    onValueChange = { blur = it.dp },
                    valueRange = 0f..100f,
                    onReset = { blur = 70.dp }
                )

                OptionChange(
                    title = "Saturation",
                    value = saturation,
                    onValueChange = { saturation = it },
                    valueRange = 0f..10f,
                    onReset = { saturation = 3f }
                )

                OptionChange(
                    title = "Scale X",
                    value = scale.first,
                    onValueChange = { scale = scale.copy(first = it) },
                    valueRange = 0f..5f,
                    onReset = { scale = scale.copy(first = 1f) }
                )

                OptionChange(
                    title = "Scale Y",
                    value = scale.second,
                    onValueChange = { scale = scale.copy(second = it) },
                    valueRange = 0f..5f,
                    onReset = { scale = scale.copy(second = 1.8f) }
                )
            }
        }
    ) { padding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GradientImage(
                url = imageUrl,
                blur = animateDpAsState(targetValue = blur, label = "").value,
                alpha = animateFloatAsState(targetValue = alpha, label = "").value,
                saturation = animateFloatAsState(targetValue = saturation, label = "").value,
                scaleX = animateFloatAsState(targetValue = scale.first, label = "").value,
                scaleY = animateFloatAsState(targetValue = scale.second, label = "").value
            )
        }
    }
}

@Composable
private fun OptionChange(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            title,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onReset,
                modifier = Modifier.weight(1f)
            ) { Icon(Icons.Default.LockReset, null) }

            Slider(
                value = animateFloatAsState(targetValue = value, label = "").value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier.weight(7f)
            )

            TextField(
                value = animateFloatAsState(targetValue = value, label = "").value.toString(),
                onValueChange = { onValueChange(it.toFloat()) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                ),
                singleLine = true,
                maxLines = 1,
                modifier = Modifier.weight(2f)
            )
        }
    }
}

@Composable
fun GradientImage(
    url: Any,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
    error: Painter? = placeholder,
    contentDescription: String? = null,
    blur: Dp = 70.dp,
    alpha: Float = .5f,
    saturation: Float = 3f,
    scaleX: Float = 1f,
    scaleY: Float = 1.8f
) {
    AsyncImage(
        model = url,
        placeholder = placeholder,
        error = error,
        contentScale = ContentScale.FillBounds,
        contentDescription = contentDescription,
        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(saturation) }),
        modifier = modifier
            .scale(scaleX, scaleY)
            .blur(blur, BlurredEdgeTreatment.Unbounded)
            .alpha(alpha)
    )

    AsyncImage(
        model = url,
        placeholder = placeholder,
        error = error,
        contentScale = ContentScale.FillBounds,
        contentDescription = contentDescription,
        modifier = modifier
    )
}
