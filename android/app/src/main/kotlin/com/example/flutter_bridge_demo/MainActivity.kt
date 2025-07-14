package com.example.flutter_bridge_demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.FrameLayout
import com.example.flutter_bridge_demo.bridge.MyFlutterBridgeDelegate
import com.zjh.flutter.bridge.FlutterBridgeManager
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity: FlutterActivity() {

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        FlutterBridgeManager.init(flutterEngine)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<FrameLayout>(android.R.id.content).let { contentView ->
            val button = Button(this).apply {
                text = "call flutter"
                setOnClickListener {
                    FlutterBridgeManager.getDelegate<MyFlutterBridgeDelegate>().callFlutter("aaa", 11)
                }
            }
            val lp = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                topMargin = 300
            }
            contentView.addView(button, lp)
        }
    }

}