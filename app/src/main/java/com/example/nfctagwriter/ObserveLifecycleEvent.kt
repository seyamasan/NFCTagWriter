package com.example.nfctagwriter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun ObserveLifecycleEvent(onEvent: (Lifecycle.Event) -> Unit = {}) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    val lifecycleOwner = LocalLifecycleOwner.current

    // `lifecycleOwner` が変更された場合、効果を破棄しリセットする
    DisposableEffect(lifecycleOwner) {
        // イベントの送信のために、記憶されているコールバックをトリガーするオブザーバーを作成する
        val observer = LifecycleEventObserver { _, event ->
            currentOnEvent(event)
        }

        // オブザーバーをライフサイクルに追加する
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            // オブザーバーを削除する
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}