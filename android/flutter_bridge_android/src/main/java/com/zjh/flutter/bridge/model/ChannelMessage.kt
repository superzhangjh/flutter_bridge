package com.zjh.flutter.bridge.model

/**
 * Flutter与Native的通讯数据
 */

data class ChannelMessage(
    val methodName: String,
    val args: Map<String, Any?>,
    val result: Any?
) {

    companion object {

        fun request(methodName: String, args: Map<String, Any?> = emptyMap()): ChannelMessage {
            return ChannelMessage(methodName, args, null)
        }

        fun response(result: Any?): ChannelMessage {
            return ChannelMessage("", emptyMap(), result)
        }

    }

}