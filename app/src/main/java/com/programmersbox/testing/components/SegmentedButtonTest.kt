package com.programmersbox.testing.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Segmented() {
    Scaffold { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            var selected by remember { mutableIntStateOf(0) }

            SingleChoiceSegmentedButtonRow {
                repeat(2) {
                    SegmentedButton(
                        selected = it == selected,
                        onClick = { selected = it },
                        shape = if (it == 0)
                            MaterialTheme.shapes.extraLarge.copy(
                                topEnd = CornerSize(0),
                                bottomEnd = CornerSize(0)
                            )
                        else
                            RectangleShape
                    ) {
                        Text("Hello with: $it")
                    }
                }
                SegmentedButton(
                    selected = 3 == selected,
                    onClick = { selected = 3 },
                    icon = { Icon(Icons.Default.Update, null) },
                    shape = MaterialTheme.shapes.extraLarge.copy(
                        topStart = CornerSize(0),
                        bottomStart = CornerSize(0)
                    )
                ) {
                    Text("Hello with: 3")
                }
            }

            HorizontalDivider()

            val selectedList = remember { mutableStateMapOf<Int, Boolean>() }

            MultiChoiceSegmentedButtonRow {
                repeat(3) {
                    SegmentedButton(
                        checked = selectedList.getOrDefault(it, false),
                        onCheckedChange = { b -> selectedList[it] = b },
                        shape = if (it == 0)
                            MaterialTheme.shapes.extraLarge.copy(
                                topEnd = CornerSize(0),
                                bottomEnd = CornerSize(0)
                            )
                        else
                            RectangleShape
                    ) {
                        Text("Hello with: $it")
                    }
                }

                SegmentedButton(
                    checked = selectedList.getOrDefault(3, false),
                    onCheckedChange = { b -> selectedList[3] = b },
                    icon = { Icon(Icons.Default.Update, null) },
                    shape = MaterialTheme.shapes.extraLarge.copy(
                        topStart = CornerSize(0),
                        bottomStart = CornerSize(0)
                    )
                ) {
                    Text("Hello with: 3")
                }
            }

            HorizontalDivider()

            AnimatedContent(
                targetState = selected,
                label = "",
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                            scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                        .togetherWith(ExitTransition.KeepUntilTransitionsFinished)
                }
            ) { target ->
                Text("$target")
            }
        }
    }
}