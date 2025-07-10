package com.galaxy.flutter.bridge

import android.util.Log
import com.galaxy.flutter.bridge.core.model.ThreadMode
import com.galaxy.flutter.bridge.core.registry.IFlutterBridgeRegistry
import com.galaxy.flutter.bridge.core.service.FlutterBridgeService
import com.galaxy.flutter.bridge.core.service.FlutterBridgeServiceManager
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.StringCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
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
        }

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
}