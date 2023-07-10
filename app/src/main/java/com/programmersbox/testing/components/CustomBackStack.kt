package com.programmersbox.testing.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@Composable
fun CustomNavigationScreen() {
    val vm = viewModel<CustomNavigationViewModel>()
    BackStackBackHandler(backStack = vm.customBackStack)

    Scaffold {
        Crossfade(
            targetState = vm.customBackStack.viewState,
            label = "",
            modifier = Modifier.padding(it)
        ) { target ->
            Text(target.toString())
            when (target) {
                CustomViewState.Default -> Default(
                    onGoStart = { vm.start = 4 },
                    onGoEnd = { vm.end = true }
                )

                CustomViewState.End -> End(
                    onGoStart = { vm.start = 1 },
                    onGoBack = { vm.end = null },
                    removeMiddle = { vm.start = null },
                    hasStartNav = vm.customBackStack.any { s -> s is CustomViewState.Start }
                )

                is CustomViewState.Start -> Start(
                    start = target,
                    onGoBack = { vm.start = null },
                    onGoEnd = { vm.end = false }
                )
            }
        }
    }
}

@Composable
private fun Default(
    onGoStart: () -> Unit,
    onGoEnd: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = onGoStart) {
            Text("Go to Start")
        }

        Button(onClick = onGoEnd) {
            Text("Go to End")
        }
    }
}

@Composable
private fun Start(
    start: CustomViewState.Start,
    onGoBack: () -> Unit,
    onGoEnd: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(start.i.toString())
        Button(onClick = onGoBack) {
            Text("Go Back")
        }

        Button(onClick = onGoEnd) {
            Text("Go to End")
        }
    }
}

@Composable
private fun End(
    onGoStart: () -> Unit,
    onGoBack: () -> Unit,
    removeMiddle: () -> Unit,
    hasStartNav: Boolean
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = onGoStart) {
            Text("Go to Start")
        }

        if (hasStartNav) {
            Button(onClick = removeMiddle) {
                Text("Remove Start Nav")
            }
        }

        Button(onClick = onGoBack) {
            Text("Go Back")
        }
    }
}

class CustomNavigationViewModel : ViewModel() {
    val customBackStack = CustomBackStack<CustomViewState>(CustomViewState.Default)

    var start: Int? by mutableStateOf(null)
    var end by mutableStateOf<Boolean?>(null)

    init {
        customBackStack.snapshotListener(
            state = { start },
            map = { CustomViewState.Start(it) },
            operationsBeforeAdd = { it.onEach { customBackStack.removeAll { s -> s is CustomViewState.Start } } }
        ).launchIn(viewModelScope)

        customBackStack.snapshotListener(
            state = { end },
            map = { CustomViewState.End }
        ).launchIn(viewModelScope)
    }
}

sealed class CustomViewState {
    object Default : CustomViewState()
    data class Start(val i: Int) : CustomViewState()
    object End : CustomViewState()
}

/**
 * For if you need navigation in a certain component (e.g. BottomSheet) and can't use normal navigation
 */
open class CustomBackStack<T>(
    defaultValue: T
) : MutableList<T> by mutableStateListOf() {
    open val viewState: T by derivedStateOf { lastOrNull() ?: defaultValue }

    inline fun <R, reified V : T> snapshotListener(
        noinline state: () -> R?,
        crossinline map: suspend (R) -> V,
        operationsBeforeAdd: (Flow<V>) -> Flow<V> = { it }
    ) = snapshotFlow(state)
        .distinctUntilChanged()
        .onEach { r -> if (r == null) lastOrNull { it is V }?.let(::remove) }
        .filterNotNull()
        .map(map)
        .let(operationsBeforeAdd)
        .onEach(::add)

    open fun popBackStack() = removeLastOrNull()
}

@Composable
fun BackStackBackHandler(backStack: CustomBackStack<*>) {
    BackHandler(
        enabled = backStack.isNotEmpty(),
        onBack = backStack::popBackStack
    )
}