package com.zjh.flutter.bridge.manage

import com.zjh.flutter.bridge.model.ChannelMessage
import com.zjh.flutter.bridge.model.ChannelMessageCodec
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BasicMessageChannel

class FlutterChannelManager(
    private val engine: FlutterEngine
) {

    private val channelMap by lazy { mutableMapOf<String, BasicMessageChannel<ChannelMessage>>() }

    fun of(channelName: String): BasicMessageChannel<ChannelMessage> {
        var channel = channelMap[channelName]
        if (channel == null) {
            channel = BasicMessageChannel(
                engine.dartExecutor.binaryMessenger,
                channelName,
                ChannelMessageCodec.INSTANCE
            )
            channelMap[channelName] = channel
        }
        return channel
    }

}