package com.zjh.flutter.bridge.core.base

import com.zjh.flutter.bridge.core.model.ThreadMode

class FlutterBridgeDelegateTemplate : FlutterBridgeDelegate {

    override fun channelName(): String {
        return "template"
    }

    override fun resolveThreadMode(methodName: String): ThreadMode {
        return
    }

    override fun onCallNativeMethod(methodName: String, args: Array<Any?>?): Any? {
        TODO("Not yet implemented")
    }
}