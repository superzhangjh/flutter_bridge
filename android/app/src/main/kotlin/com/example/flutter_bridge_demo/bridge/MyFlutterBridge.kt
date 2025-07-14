package com.example.flutter_bridge_demo.bridge

import android.util.Log
import com.zjh.flutter.bridge.FlutterBridgeManager
import com.zjh.flutter.bridge.core.annotation.FlutterBridge
import com.zjh.flutter.bridge.core.annotation.FlutterMethod
import com.zjh.flutter.bridge.core.annotation.NativeMethod
import com.zjh.flutter.bridge.core.model.ThreadMode

@FlutterBridge
abstract class MyFlutterBridge {

    @NativeMethod
    fun getVersion(board: String, flag: Int): Int {
        Log.d("调试插件", "native >> getVersion >> thread:${Thread.currentThread().name} board:$board, flag:$flag")
        return 0
    }

    @NativeMethod(threadMode = ThreadMode.Main)
    fun testVoid() {
        Log.d("调试插件", "native >> testVoid >> thread:${Thread.currentThread().name}")
    }

    @FlutterMethod
    abstract fun callFlutter(arg1: String, arg2: Int)

    /**
     * Result只能是Async的
     */
    @FlutterMethod
    abstract fun callFlutterWithResult()
}