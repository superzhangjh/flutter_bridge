package com.zjh.flutter.bridge.core.service

/**
 * 管理服务对象，用于初始化FlutterEngine的时候绑定Channel
 */
object FlutterBridgeServiceManager {

    private val services by lazy { mutableListOf<FlutterBridgeService>() }

    @JvmStatic
    fun addService(service: FlutterBridgeService) {
        if (services.find { it::class == service::class  } != null) {
            throw Throwable("Duplicate registration service，named `${service.javaClass.name}`")
        }
        services.add(service)
    }

    fun allServices(): List<FlutterBridgeService> {
        return services
    }

}