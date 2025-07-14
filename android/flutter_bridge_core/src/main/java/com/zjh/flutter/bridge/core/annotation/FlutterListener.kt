package com.zjh.flutter.bridge.core.annotation

/**
 * 建立Flutter与Native方法的监听器，提供局部的方法监听，每次监听都是独立的实例
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class FlutterListener(

    /**
     * 对应 flutter 端的 channelName，如为空则自动匹配类名的lower cast + 下划线，如 BatteryBridge -> battery_bridge
     */
    val channelName: String = ""
)
