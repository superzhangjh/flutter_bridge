package com.zjh.flutter.bridge.core.annotation

/**
 * 建立Flutter与Native方法互相调用桥梁，提供全局的方法调用，并提供全局单例
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class FlutterBridge(

    /**
     * 对应 flutter 端的 channelName，如为空则自动匹配类名的lower cast + 下划线，如 BatteryBridge -> battery_bridge
     */
    val channelName: String = ""
)
