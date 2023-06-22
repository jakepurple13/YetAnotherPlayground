package com.programmersbox.testing.components.limitedbottomsheetscaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.programmersbox.testing.ui.theme.LightAndDarkPreviews
import com.programmersbox.testing.ui.theme.TestingTheme
import kotlinx.coroutines.launch


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
    val state = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )
    val scope = rememberCoroutineScope()
    LimitedBottomSheetScaffold(
        scaffoldState = state,
        topAppBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                title = {
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
                }
            )
        },
        sheetContent = {
            TopAppBar(
                title = { Text(text = "Hello") },
                navigationIcon = {
                    IconButton(
                        onClick = { scope.launch { state.bottomSheetState.partialExpand() } }
                    ) { Icon(Icons.Default.Close, null) }
                }
            )
            LazyColumn {
                items(100) {
                    ListItem(
                        headlineContent = { Text(it.toString()) },
                    )
                }
            }
        },
        bottomSheet = LimitedBottomSheetScaffoldDefaults.bottomSheet(
            sheetSwipeEnabled = state.bottomSheetState.currentValue != SheetValue.Expanded
        ),
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
        Column(modifier = Modifier.padding(it)) {
            Text(state.bottomSheetState.currentValue.name)
            Text(state.bottomSheetState.targetValue.name)
            var f by remember { mutableIntStateOf(0) }
            Text(f.toString())
            Button(onClick = { f++ }) {
                Text("Hello!")
            }
        }
    }
}