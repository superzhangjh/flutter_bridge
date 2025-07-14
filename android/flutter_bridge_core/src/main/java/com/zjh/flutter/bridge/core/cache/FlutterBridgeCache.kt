package com.zjh.flutter.bridge.core.cache

import com.zjh.flutter.bridge.core.base.FlutterBridgeDelegate
import com.zjh.flutter.bridge.core.base.FlutterBridgeInvoker
import com.zjh.flutter.bridge.core.model.ThreadMode

class FlutterBridgeCache(
    val channelName: String,
    val delegate: Delegate?,
    val invoker: Invoker?
) {

    class Delegate(
        // delegate 的实例
        val instance: FlutterBridgeDelegate,
        // key: methodName
        val methodThreadModeMap: Map<String, ThreadMode> = emptyMap()
    )

    class Invoker(
        val clazz: Class<out FlutterBridgeInvoker>,
        val methods: List<Method> = emptyList()
    )

    class Method(
        val name: String,
        val args: List<Arg> = emptyList(),
        val threadMode: ThreadMode = ThreadMode.Unconfined
    )

    class Arg(
        val name: String,
        val type: String
    )
}

