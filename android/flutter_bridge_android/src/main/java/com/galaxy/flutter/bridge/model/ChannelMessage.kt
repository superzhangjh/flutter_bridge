package com.galaxy.flutter.bridge.model

import java.nio.ByteBuffer

/**
 * Flutter与Native的通讯数据
 */
import java.nio.charset.StandardCharsets

data class ChannelMessage(
    val methodName: String,
    val args: Array<Any?>? = null,
    val result: Any? = null
) {

    companion object {
        
        fun fromByteBuffer(buffer: ByteBuffer): ChannelMessage {
            buffer.rewind()

            // 读取方法名
            val methodNameLength = buffer.int
            val methodNameBytes = ByteArray(methodNameLength)
            buffer.get(methodNameBytes)
            val methodName = String(methodNameBytes, StandardCharsets.UTF_8)

            // 读取参数数组
            val argsLength = buffer.int
            val args = if (argsLength >= 0) {
                Array<Any?>(argsLength) {
                    when (buffer.get().toInt()) {
                        0 -> null
                        1 -> buffer.int
                        2 -> buffer.long
                        3 -> buffer.float
                        4 -> buffer.double
                        5 -> {
                            val strLength = buffer.int
                            val strBytes = ByteArray(strLength)
                            buffer.get(strBytes)
                            String(strBytes, StandardCharsets.UTF_8)
                        }
                        else -> throw IllegalArgumentException("Unknown type tag")
                    }
                }
            } else null

            // 读取结果
            val hasResult = buffer.get().toInt() == 1
            val result = if (hasResult) {
                when (buffer.get().toInt()) {
                    0 -> null
                    1 -> buffer.int
                    2 -> buffer.long
                    3 -> buffer.float
                    4 -> buffer.double
                    5 -> {
                        val strLength = buffer.int
                        val strBytes = ByteArray(strLength)
                        buffer.get(strBytes)
                        String(strBytes, StandardCharsets.UTF_8)
                    }
                    else -> throw IllegalArgumentException("Unknown result type tag")
                }
            } else null

            return ChannelMessage(methodName, args, result)
        }
    }

    fun toByteBuffer(): ByteBuffer {
        // 计算总大小
        val methodNameBytes = methodName.toByteArray(StandardCharsets.UTF_8)
        var size = 4 + methodNameBytes.size + 4 // 方法名长度+数据+args长度

        // 计算args大小
        val argsSize = args?.let { array ->
            var argsSize = 0
            array.forEach { arg ->
                argsSize += 1 // 类型标记
                argsSize += when (arg) {
                    null -> 0
                    is Int -> 4
                    is Long -> 8
                    is Float -> 4
                    is Double -> 8
                    is String -> 4 + arg.toByteArray(StandardCharsets.UTF_8).size
                    else -> throw IllegalArgumentException("Unsupported type: ${arg?.javaClass}")
                }
            }
            argsSize
        } ?: -1

        size += if (argsSize >= 0) argsSize else 0

        // 计算result大小
        val resultSize = result?.let {
            1 + // hasResult标志
                    when (it) {
                        is Int -> 1 + 4
                        is Long -> 1 + 8
                        is Float -> 1 + 4
                        is Double -> 1 + 8
                        is String -> 1 + 4 + it.toByteArray(StandardCharsets.UTF_8).size
                        else -> throw IllegalArgumentException("Unsupported result type: ${it.javaClass}")
                    }
        } ?: 1 // 只有hasResult=false

        size += resultSize

        // 分配缓冲区
        val buffer = ByteBuffer.allocate(size)

        // 写入方法名
        buffer.putInt(methodNameBytes.size)
        buffer.put(methodNameBytes)

        // 写入args
        if (args != null) {
            buffer.putInt(args.size)
            args.forEach { arg ->
                when (arg) {
                    null -> {
                        buffer.put(0.toByte())
                    }
                    is Int -> {
                        buffer.put(1.toByte())
                        buffer.putInt(arg)
                    }
                    is Long -> {
                        buffer.put(2.toByte())
                        buffer.putLong(arg)
                    }
                    is Float -> {
                        buffer.put(3.toByte())
                        buffer.putFloat(arg)
                    }
                    is Double -> {
                        buffer.put(4.toByte())
                        buffer.putDouble(arg)
                    }
                    is String -> {
                        buffer.put(5.toByte())
                        val bytes = arg.toByteArray(StandardCharsets.UTF_8)
                        buffer.putInt(bytes.size)
                        buffer.put(bytes)
                    }
                    else -> throw IllegalArgumentException("Unsupported type: ${arg.javaClass}")
                }
            }
        } else {
            buffer.putInt(-1) // 表示null
        }

        // 写入result
        if (result != null) {
            buffer.put(1.toByte()) // hasResult=true
            when (result) {
                is Int -> {
                    buffer.put(1.toByte())
                    buffer.putInt(result)
                }
                is Long -> {
                    buffer.put(2.toByte())
                    buffer.putLong(result)
                }
                is Float -> {
                    buffer.put(3.toByte())
                    buffer.putFloat(result)
                }
                is Double -> {
                    buffer.put(4.toByte())
                    buffer.putDouble(result)
                }
                is String -> {
                    buffer.put(5.toByte())
                    val bytes = result.toByteArray(StandardCharsets.UTF_8)
                    buffer.putInt(bytes.size)
                    buffer.put(bytes)
                }
                else -> throw IllegalArgumentException("Unsupported result type: ${result.javaClass}")
            }
        } else {
            buffer.put(0.toByte()) // hasResult=false
        }

        buffer.flip()
        return buffer
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChannelMessage

        if (methodName != other.methodName) return false
        if (args != null) {
            if (other.args == null) return false
            if (!args.contentEquals(other.args)) return false
        } else if (other.args != null) return false
        if (result != other.result) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = methodName.hashCode()
        result1 = 31 * result1 + (args?.contentHashCode() ?: 0)
        result1 = 31 * result1 + (result?.hashCode() ?: 0)
        return result1
    }
}