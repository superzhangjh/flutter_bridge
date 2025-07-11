package com.zjh.flutter.bridge.model

import java.util.concurrent.CompletableFuture

//interface AsyncResult<T> : CompletableFuture {
//
//    /**
//     * 是否成功
//     */
//    fun isSuccess(): Boolean
//
//    /**
//     * 获取结果，如果失败了获取会抛出异常
//     */
//    fun value(): T
//
//    /**
//     * 获取结果, 如果失败了则为空
//     */
//    fun valueOrNull(): T?
//
//    /**
//     * 获取异常
//     */
//    fun error(): Throwable
//}