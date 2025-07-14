package com.zjh.flutter.bridge

import com.zjh.flutter.bridge.core.model.ThreadMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

object FlutterBridgeSchedulers {

    val scope by lazy { CoroutineScope(SupervisorJob()) }

}

fun ThreadMode.toDispatchers(): CoroutineContext {
    return when (this) {
        ThreadMode.Unconfined -> Dispatchers.Unconfined
        ThreadMode.Main -> Dispatchers.Main
        ThreadMode.IO -> Dispatchers.IO
        ThreadMode.DEFAULT -> Dispatchers.Default
    }
}