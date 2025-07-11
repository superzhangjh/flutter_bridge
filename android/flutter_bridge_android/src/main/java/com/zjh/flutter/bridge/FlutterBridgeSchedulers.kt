//import com.galaxy.flutter.bridge.core.IFlutterBridgeScheduler
//
//object FlutterBridgeSchedulers  {
//
//    private var scheduler: IFlutterBridgeScheduler? = null
//
//    /**
//     * 设置线程调度器，需要在 flutter-bridge-android 中实现 Android 主线程调度
//     */
//    @Synchronized
//    fun set(scheduler: IFlutterBridgeScheduler) {
//        FlutterBridgeSchedulers.scheduler = scheduler
//    }
//
//    @JvmStatic
//    fun get(): IFlutterBridgeScheduler {
//        assert(scheduler != null) { "Please call the set method first" }
//        return scheduler!!
//    }
//
//}