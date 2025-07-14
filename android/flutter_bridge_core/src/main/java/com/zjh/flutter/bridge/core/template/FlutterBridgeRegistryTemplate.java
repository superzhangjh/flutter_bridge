package com.zjh.flutter.bridge.core.template;

import com.zjh.flutter.bridge.core.cache.FlutterBridgeCache;
import com.zjh.flutter.bridge.core.cache.FlutterBridgeCacheManager;
import com.zjh.flutter.bridge.core.model.ThreadMode;
import com.zjh.flutter.bridge.core.registry.IFlutterBridgeRegistry;

import java.util.Collections;
import java.util.List;

public class FlutterBridgeRegistryTemplate implements IFlutterBridgeRegistry {

    @Override
    public void register() {
        FlutterBridgeCache.Delegate delegate = new FlutterBridgeCache.Delegate(
                FlutterBridgeDelegateTemplate.class,
                List.of(
                        new FlutterBridgeCache.Method(
                                "getBatteryInfo",
                                List.of(
                                        new FlutterBridgeCache.Arg("board", "String")
                                ),
                                ThreadMode.IO
                        )
                )
        );

        FlutterBridgeCache.Invoker invoker = new FlutterBridgeCache.Invoker(
                FlutterBridgeInvokerTemplate.class,
                Collections.emptyList()
        );

        FlutterBridgeCache cache = new FlutterBridgeCache("template", delegate, invoker);

        FlutterBridgeCacheManager.addCache(cache);
    }
}
