package com.programmersbox.testing.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MutableStateTestScreen() {
    val vm = viewModel<MutableStateTestViewModel>()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = vm::addCounter) {
            Text(vm.showCounter().toString())
        }
    }
}

class MutableStateTestViewModel : ViewModel() {
    private var counter by mutableIntStateOf(0)

    fun showCounter() = counter.times(20)

    fun addCounter() {
        counter++
    }
}