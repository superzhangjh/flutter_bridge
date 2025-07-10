package com.galaxy.flutter.bridge.listener

interface EventListener {

    /**
     * 事件名称
     */
    fun event(): String

    /**
     * 事件触发
     */
    fun onEvent(args: Map<String, Any?>)

    /**
     * 拦截方法，拦截后事件无法再传递
     */
    fun intercept() {}

    /**
     * 优先级
     * @return 值越大越先执行, 值相同按注册顺序执行
     */
    fun property(): Int = 0

    /**
     * 执行线程
     */
//    fun threadMode(): ThreadMode = ThreadMode.Main
}