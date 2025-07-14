import 'dart:convert';
import 'dart:developer';

import 'package:flutter/services.dart';

class TestBridge {
  
  static const _channel = BasicMessageChannel('my_flutter_bridge', StringCodec());

  static init() {
    _channel.setMessageHandler((messageJson) {
      final message = _messageFromJson(messageJson);
      log('TestBridge >> RECEIVE:\nsource:$messageJson\nmessage：$message');
      return _messageToJson();
    });
  }

  static Map<String, dynamic> _messageFromJson(String? message) {
    try {
      return jsonDecode(message ?? '');
    } catch (e) {
      return {
        'methodName': '',
        'args': [],
        'result': null
      };
    }
  }

  static Future<String> _messageToJson({ String? methodName, List? args, dynamic result }) async {
    return jsonEncode({
      'methodName': methodName ?? '',
      'args': args ?? [],
      'result': result
    });
  }

  static callNative({ String? methodName, List? args }) async {
    final result = await _channel.send(await _messageToJson(
        methodName: methodName,
        args: args
    ));
    log('TestBridge >> callNative result: $result');
  }
}