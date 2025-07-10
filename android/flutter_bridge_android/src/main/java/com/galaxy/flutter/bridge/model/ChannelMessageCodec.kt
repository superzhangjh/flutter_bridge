package com.galaxy.flutter.bridge.model

import io.flutter.plugin.common.MessageCodec
import java.nio.ByteBuffer

class ChannelMessageCodec private constructor(): MessageCodec<ChannelMessage> {

    override fun encodeMessage(message: ChannelMessage?): ByteBuffer? {
        return message?.toByteBuffer()
    }

    override fun decodeMessage(message: ByteBuffer?): ChannelMessage? {
        return if (message == null) null else ChannelMessage.fromByteBuffer(message)
    }

    companion object {
        val INSTANCE: MessageCodec<ChannelMessage> = ChannelMessageCodec()
    }
}