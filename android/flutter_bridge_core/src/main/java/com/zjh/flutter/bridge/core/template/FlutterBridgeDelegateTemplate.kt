package com.zjh.flutter.bridge.core.template

import com.zjh.flutter.bridge.core.base.FlutterBridgeDelegate
import com.zjh.flutter.bridge.core.model.ThreadMode

class FlutterBridgeDelegateTemplate : FlutterBridgeDelegate {

    override fun channelName(): String {
    }

    override fun resolveThreadMode(methodName: String): ThreadMode {
    }

    override fun onCallNativeMethod(methodName: String, args: Array<Any?>?): Any? {
    }
}