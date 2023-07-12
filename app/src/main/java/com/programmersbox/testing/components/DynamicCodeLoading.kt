package com.programmersbox.testing.components

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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
import com.skydoves.landscapist.rememberDrawablePainter
import dalvik.system.PathClassLoader
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

class ExtensionLoader<T, R>(
    private val context: Context,
    private val extensionFeature: String,
    private val metadataClass: String,
    private val mapping: (T, ApplicationInfo) -> R
) {
    suspend fun loadExtensions(): List<R> {
        val packages = context.packageManager
            ?.getInstalledPackages(PackageManager.GET_CONFIGURATIONS)
            ?.filter { it.reqFeatures.orEmpty().any { it.name == extensionFeature } }
            .orEmpty()

        return runBlocking {
            packages
                .map { async { loadOne(it) } }
                .flatMap { it.await() }
        }
    }

    private fun loadOne(packageInfo: PackageInfo): List<R> {
        val appInfo = context.packageManager.getApplicationInfo(
            packageInfo.packageName,
            PackageManager.GET_META_DATA
        )

        val classLoader = PathClassLoader(appInfo.sourceDir, null, context.classLoader)

        return appInfo.metaData.getString(metadataClass)
            .orEmpty()
            .split(";")
            .map {
                val sourceClass = it.trim()
                if (sourceClass.startsWith(".")) {
                    packageInfo.packageName + sourceClass
                } else {
                    sourceClass
                }
            }
            .mapNotNull {
                @Suppress("UNCHECKED_CAST")
                Class.forName(it, false, classLoader)
                    .getDeclaredConstructor()
                    .newInstance() as? T
            }
            .map { mapping(it, appInfo) }
    }
}