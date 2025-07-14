package com.zjh.flutter.bridge.core.template

import com.zjh.flutter.bridge.core.base.FlutterBridgeInvoker

interface FlutterBridgeInvokerTemplate : FlutterBridgeInvoker {

    fun callWithArgs(stringArg: String, intArg: Int)

}