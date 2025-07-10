package com.galaxy.flutter.bridge.core.annotation

/**
 * 实现自动注册逻辑
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class FlutterBridge(

    /**
     * 对应 flutter 端的 channelName，如为空则自动匹配类名的lower cast + 下划线，如 BatteryBridge -> battery_bridge
     */
    val channelName: String = ""
)
