package com.zjh.flutter.bridge.core.cache

import com.zjh.flutter.bridge.core.base.FlutterBridgeInvoker

/**
 * 注册类管理
 */
object FlutterBridgeCacheManager {

    private val delegateCache by lazy { mutableMapOf<String, FlutterBridgeCache.Delegate>() }
    private val invokerCache by lazy { mutableMapOf<String, FlutterBridgeCache.Invoker>() }

    @JvmStatic
    fun addCache(cache: FlutterBridgeCache) {
        cache.delegate?.let {
            delegateCache[cache.channelName] = it
        }
        cache.invoker?.let {
            invokerCache[cache.channelName] = it
        }
    }

    fun allDelegates(): Map<String, FlutterBridgeCache.Delegate> {
        return delegateCache
    }

    fun getInvoker(clazz: Class<out FlutterBridgeInvoker>): FlutterBridgeCache.Invoker {
        return invokerCache[clazz]
    }
}