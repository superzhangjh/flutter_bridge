package com.galaxy.flutter.bridge.core.service

import com.galaxy.flutter.bridge.core.model.ThreadMode

/**
 * 插件抽象类，实际就是@FlutterBridge注解类映射类，然后注解类实现具体方法逻辑
 */
interface FlutterBridgeService {

    /**
     * 与Flutter通讯的渠道名
     */
    fun channelName(): String

    /**
     * 解析线程模式
     */
    fun resolveThreadMode(methodName: String): ThreadMode

    /**
     * 调用Native，注解类实现逻辑
     * @return native方法的结果
     */
    fun onCallNativeMethod(methodName: String, args: Array<Any?>?): Any?

}