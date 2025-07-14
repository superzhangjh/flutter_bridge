package com.zjh.flutter.bridge.core.base

/**
 * Flutter端方法的映射
 * 1、使用了@FlutterMethod注解后会生成FlutterBridgeInvoker的子类，子类包含了具体的flutter方法
 * 2、通过代理FlutterBridgeInvoker子类，来实现调用flutter方法
 */
interface FlutterBridgeInvoker