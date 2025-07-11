package com.example.flutter_bridge_demo

import com.zjh.flutter.bridge.FlutterBridgeManager
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity: FlutterActivity() {

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        FlutterBridgeManager.init(flutterEngine)
    }

}