package com.zjh.flutter.bridge

import android.util.Log
import com.zjh.flutter.bridge.core.delegate.FlutterBridgeDelegate
import com.zjh.flutter.bridge.core.model.ThreadMode
import com.zjh.flutter.bridge.core.registry.IFlutterBridgeRegistry
import com.zjh.flutter.bridge.core.service.FlutterBridgeService
import com.zjh.flutter.bridge.core.service.FlutterBridgeServiceManager
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.StringCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.ServiceLoader
import kotlin.coroutines.CoroutineContext

object FlutterBridgeManager {

    /**
     * 注册使用了@FlutterBridge的插件
     */
    @JvmStatic
    fun init(engine: FlutterEngine) {
        registerServices()
        bindServices(engine)
    }

    private fun registerServices() {
        val loader = ServiceLoader.load(IFlutterBridgeRegistry::class.java)
        for (service in loader) {
            service.register()
        }
    }

    private fun bindServices(engine: FlutterEngine) {
        for (service in FlutterBridgeServiceManager.allServices()) {
            val channel = BasicMessageChannel(
                engine.dartExecutor.binaryMessenger,
                service.channelName(),
                StringCodec.INSTANCE
            )
            channel.setMessageHandler { message, reply ->
                Log.d("调试插件", "channel:${service.channelName()} message:$message")
                if (message == null) return@setMessageHandler
                Log.d("调试插件", "channel replace:${service.channelName()} message:${message.replaceFirst("/", "")}")
                val json = JSONObject(message)
                val methodName = json.getString("methodName")

                CoroutineScope(service.resolveContext(methodName)).launch {
                    val argsArray = if (json["args"] is JSONArray) {
                        val args = json.getJSONArray("args")
                        Array<Any?>(args.length()) { args[it] }
                    } else {
                        null
                    }
                    val result = service.onCallNativeMethod(methodName, argsArray)

                    val responseJson = JSONObject()
                    responseJson.put("methodName", methodName)
                    responseJson.put("result", if (result == Void.TYPE) null else result)
                    reply.reply(responseJson.toString())
                }
            }

            tempchannel = channel

        }
    }

    // todo: 临时写法，需要建立Delegate、Proxy之间的关系
    lateinit var tempchannel: BasicMessageChannel<String>

    inline fun <reified T : FlutterBridgeDelegate> getDelegate() : T {
        val proxy = Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java), FlutterBridgeDelegateProxy(tempchannel))
        return proxy as T
    }

    private fun FlutterBridgeService.resolveContext(methodName: String): CoroutineContext {
        return when (resolveThreadMode(methodName)) {
            ThreadMode.Unconfined -> Dispatchers.Unconfined
            ThreadMode.Main -> Dispatchers.Main
            ThreadMode.IO -> Dispatchers.IO
            ThreadMode.DEFAULT -> Dispatchers.Default
        }
    }

    fun setChannelFactory() {
        //todo: 渠道通信方案，可以自行决定使用哪种方式实现, 但是会提供默认的实现方式( BasicMessageChannel的方式 )
    }

    class FlutterBridgeDelegateProxy(private val channel: BasicMessageChannel<String>) : InvocationHandler {

        //todo: 这里应该是能通过channel来获取到对应的配置信息, 然后再通过方法名去调用
        override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any {
            val methodName = method.name

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