package com.galaxy.flutter.bridge.core.annotation

/**
 * Native调用Flutter的方法
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class FlutterMethod(

    /**
     * 指定的方法名，空值则使用方法名(推荐)
     */
    val methodName: String = ""
)
