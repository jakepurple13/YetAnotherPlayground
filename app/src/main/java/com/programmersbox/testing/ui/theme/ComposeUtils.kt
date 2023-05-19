package com.programmersbox.testing.ui.theme

import androidx.compose.ui.graphics.Color
import java.util.Locale

fun String.firstCharCapital() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

fun Int.toComposeColor() = Color(this)