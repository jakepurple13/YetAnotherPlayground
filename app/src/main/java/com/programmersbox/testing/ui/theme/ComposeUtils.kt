package com.programmersbox.testing.ui.theme

import java.util.Locale

fun String.firstCharCapital() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
