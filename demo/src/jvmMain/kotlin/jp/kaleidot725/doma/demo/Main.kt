package jp.kaleidot725.doma.demo

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import jp.kaleidot725.doma.demo.counter.app.CounterApp
import jp.kaleidot725.doma.demo.counter.app.CounterContainer
import jp.kaleidot725.doma.demo.counter.app.content.CounterOperatorStore
import jp.kaleidot725.doma.demo.counter.repository.CounterRepository

fun main() =
    application {
        val repository = remember { CounterRepository() }
        val store = remember { CounterOperatorStore(repository) }
        val container = remember { CounterContainer(stores = listOf(store)) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "DomaKt Demo - Counter",
        ) {
            MaterialTheme {
                CounterApp(container = container, store = store)
            }
        }
    }
