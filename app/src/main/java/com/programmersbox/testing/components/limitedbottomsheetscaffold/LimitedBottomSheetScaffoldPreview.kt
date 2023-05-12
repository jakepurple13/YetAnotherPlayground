package com.programmersbox.testing.components.limitedbottomsheetscaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.programmersbox.testing.ui.theme.LightAndDarkPreviews
import com.programmersbox.testing.ui.theme.TestingTheme


@OptIn(ExperimentalMaterial3Api::class)
@LightAndDarkPreviews
@Composable
private fun LimitedBottomSheetScaffoldPreviewScreen() {
    TestingTheme {
        LimitedBottomSheetScaffoldPreview()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitedBottomSheetScaffoldPreview() {
    LimitedBottomSheetScaffold(
        topAppBar = {
            var isActive by remember { mutableStateOf(false) }
            SearchBar(
                query = "",
                onQueryChange = {},
                onSearch = { },
                active = isActive,
                onActiveChange = { isActive = it },
                placeholder = { Text("Search") },
            ) {
                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(10) {
                        ListItem(
                            headlineContent = { Text(it.toString()) },
                        )
                    }
                }
            }
        },
        sheetContent = {
            LazyColumn {
                items(100) {
                    ListItem(
                        headlineContent = { Text(it.toString()) },
                    )
                }
            }
        },
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Blue,
                            Color.Cyan,
                            Color.Red,
                            Color.Green,
                            Color.Magenta,
                            Color.Yellow
                        )
                    )
                )
        )
    }
}