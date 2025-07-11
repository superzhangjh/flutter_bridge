package com.zjh.flutter.bridge.core.model

/**
 * 线程模型
 */
enum class ThreadMode {

    /**
     * Android 主线程
     */
    Main,

    /**
     * 异步线程
     */
    IO,

    /**
     * CPU密集型
     */
    DEFAULT,

    /**
     * 不执行线程，在原有的线程上执行
     */
    Unconfined
}