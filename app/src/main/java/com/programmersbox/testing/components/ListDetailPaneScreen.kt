package com.programmersbox.testing.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.rememberListDetailPaneScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ListDetailPaneScreen() {
    var info by rememberSaveable { mutableStateOf<String?>(null) }
    val state = rememberListDetailPaneScaffoldState()

    ListDetailPaneScaffold(
        scaffoldState = state,
        listPane = {
            Scaffold { padding ->
                LazyColumn(
                    contentPadding = padding,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(100) {
                        Card(
                            onClick = {
                                info = "$it"
                                state.navigateTo(ListDetailPaneScaffoldRole.Detail)
                            }
                        ) {
                            ListItem(headlineContent = { Text("$it") })
                        }
                    }
                }
            }
        },
        detailPane = {
            info?.let {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(it) },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        state.navigateBack()
                                    }
                                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                            }
                        )
                    }
                ) { padding ->
                    ListItem(
                        headlineContent = { Text(it) },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    )
}