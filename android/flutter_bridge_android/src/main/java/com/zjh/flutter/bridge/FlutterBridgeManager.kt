package com.zjh.flutter.bridge

import android.util.Log
import com.zjh.flutter.bridge.core.base.FlutterBridgeDelegate
import com.zjh.flutter.bridge.core.base.FlutterBridgeInvoker
import com.zjh.flutter.bridge.core.cache.FlutterBridgeCache
import com.zjh.flutter.bridge.core.model.ThreadMode
import com.zjh.flutter.bridge.core.registry.IFlutterBridgeRegistry
import com.zjh.flutter.bridge.core.cache.FlutterBridgeCacheManager
import com.zjh.flutter.bridge.manage.FlutterChannelManager
import com.zjh.flutter.bridge.model.ChannelMessage
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BasicMessageChannel
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.ServiceLoader

object FlutterBridgeManager {

    private lateinit var channelManager: FlutterChannelManager

    /**
     * 注册使用了@FlutterBridge的插件
     * //todo: engine可以提供接口通过外部获取，兼容多engine的情况
     */
    @JvmStatic
    fun init(engine: FlutterEngine) {
        channelManager = FlutterChannelManager(engine)

        registerServices()
        bindServices()
    }

    private fun registerServices() {
        val loader = ServiceLoader.load(IFlutterBridgeRegistry::class.java)
        for (service in loader) {
            service.register()
        }
    }

    private fun bindServices() {
        for (entry in FlutterBridgeCacheManager.allDelegates()) {
            channelManager.of(entry.key).setMessageHandler { message, reply ->
                val delegate = entry.value

                val messageName = message?.methodName
                if (messageName.isNullOrEmpty()) return@setMessageHandler

                // 调用方法的线程
                val threadMode = delegate.methodThreadModeMap[messageName] ?: ThreadMode.Unconfined

                // 在指定的线程执行任务
                FlutterBridgeSchedulers.scope.launch(threadMode.toDispatchers()) {
                    val result = delegate.instance.onCallNativeMethod(messageName, message.args)
                    //todo：检查允许的参数类型
                    reply.reply(ChannelMessage.response(result))
                }
            }
        }
    }

    /**
     * 获取Invoker，调用Flutter方法
     */
    inline fun <reified T : FlutterBridgeInvoker> getInvoker() : T {
        val proxy = FlutterBridgeInvokerProxy.getProxy(T::class.java)
        FlutterBridgeCacheManager.getInvokerInfo(T::class.java)
        //todo: 缓存代理类
        val proxy = Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java), FlutterBridgeDelegateProxy(tempchannel))
        return proxy as T
    }

    fun setChannelFactory() {
        //todo: 渠道通信方案，可以自行决定使用哪种方式实现, 但是会提供默认的实现方式( BasicMessageChannel的方式 )
    }

    class FlutterBridgeDelegateProxy(private val channel: BasicMessageChannel<String>) : InvocationHandler {

        //todo: 这里应该是能通过channel来获取到对应的配置信息, 然后再通过方法名去调用
        override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any {
            val methodName = method.name
            Log.d("调试插件", "call flutter >> methodName:$methodName args:${args?.joinToString { it.toString() }}")

            val jsonObject = JSONObject()
            jsonObject.put("methodName", methodName)

            if (args != null) {
                val jsonArray = JSONArray()
                args.forEachIndexed { index, any ->
                    jsonArray.put(index, any)
                }
                jsonObject.put("args", jsonArray)
            }

            channel.send(jsonObject.toString())
            return Void.TYPE
        }
    }
}