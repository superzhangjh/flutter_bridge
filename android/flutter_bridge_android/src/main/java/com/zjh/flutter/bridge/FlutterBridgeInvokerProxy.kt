package com.zjh.flutter.bridge

import com.zjh.flutter.bridge.core.base.FlutterBridgeInvoker
import com.zjh.flutter.bridge.core.cache.FlutterBridgeCacheManager
import com.zjh.flutter.bridge.manage.FlutterChannelManager
import com.zjh.flutter.bridge.model.ChannelMessage
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

internal class FlutterBridgeInvokerProxy<T : FlutterBridgeInvoker> private constructor(
    private val targetClass: Class<T>
) : InvocationHandler {

    companion object {

        private val proxyMap by lazy { mutableMapOf<Class<*>, Any>() }

        @Suppress("UNCHECKED_CAST")
        fun <T : FlutterBridgeInvoker> getProxy(clazz: Class<T>): T {
            var proxy = proxyMap[clazz]
            if (proxy == null) {
                proxy = Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), FlutterBridgeInvokerProxy(clazz))
                proxyMap[clazz] = proxy
            }
            return proxy as T
        }

    }

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any {
        val methodName = method.name
        val invoker = FlutterBridgeCacheManager.getInvoker(targetClass)
        invoker.methods
        val message = ChannelMessage.request(methodName, )
        FlutterChannelManager().of(channelName).send(message)
        return Void.TYPE
    }
}