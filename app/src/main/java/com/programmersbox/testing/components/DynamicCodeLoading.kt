package com.programmersbox.testing.components

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.programmersbox.dynamiccodeloading.RandomNumbers
import com.programmersbox.extensionloader.ExtensionLoader
import com.skydoves.landscapist.rememberDrawablePainter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicCodeLoadingDemo() {
    val context = LocalContext.current
    val d = viewModel { DynamicCodeViewModel(context) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Dynamic Code Loading") }) }
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(animateIntAsState(targetValue = d.number, label = "").value.toString())
            Button(onClick = d::newNumber) { Text("New Number") }
            d.randomNumbers?.let {
                Icon(
                    painter = rememberDrawablePainter(drawable = it.icon),
                    contentDescription = null
                )
                Text("From: ${it.name}")
            }
        }
    }
}

const val EXTENSION_FEATURE = "programmersbox.extension"
const val METADATA_CLASS = "programmersbox.extension.class"
const val METADATA_NAME = "programmersbox.extension.name"

class DynamicCodeViewModel(
    context: Context
) : ViewModel() {
    var number by mutableIntStateOf(0)

    var randomNumbers: DynamicRandomNumber? by mutableStateOf(null)

    private val e = ExtensionLoader<RandomNumbers, DynamicRandomNumber>(
        context = context,
        extensionFeature = EXTENSION_FEATURE,
        metadataClass = METADATA_CLASS
    ) { t, a ->
        DynamicRandomNumber(
            name = a.metaData.getString(METADATA_NAME) ?: "Nothing",
            randomNumbers = t,
            icon = context.packageManager.getApplicationIcon(context.packageName)
        )
    }

    init {
        viewModelScope.launch {
            val b = e.loadExtensions()
            randomNumbers = b.randomOrNull()
        }
    }

    fun newNumber() {
        randomNumbers?.randomNumbers?.getNumber()?.let { number = it }
    }
}

data class DynamicRandomNumber(
    val name: String,
    val randomNumbers: RandomNumbers,
    val icon: Drawable?
)
