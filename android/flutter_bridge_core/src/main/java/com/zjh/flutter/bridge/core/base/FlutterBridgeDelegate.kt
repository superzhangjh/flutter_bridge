package com.zjh.flutter.bridge.core.base

/**
 * Native方法的代表类，用于执行Native方法
 * 实际就是@FlutterBridge注解类映射类，然后注解类实现具体方法逻辑
 */
interface FlutterBridgeDelegate {

    /**
     * 调用Native，注解类实现逻辑
     * @return native方法的结果
     */
    fun onCallNativeMethod(methodName: String, args: Map<String, Any?>): Any?
}