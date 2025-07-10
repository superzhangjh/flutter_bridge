package com.galaxy.flutter.bridge.core.annotation

import com.galaxy.flutter.bridge.core.model.ThreadMode

/**
 * Flutter调用Native的方法
 * @param threadMode 方法执行线程
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class NativeMethod(

    val threadMode: ThreadMode = ThreadMode.IO,

    /**
     * 指定的方法名，空值则使用方法名(推荐)
     */
    val methodName: String = ""
)
