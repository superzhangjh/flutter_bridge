package com.galaxy.flutter.bridge.processor.model

import com.galaxy.flutter.bridge.core.model.ThreadMode
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

internal class BridgeModel(
    /**
     * 是否需要创建实现类（针对抽象类的情况）
     */
    val needGenerateDelegate: Boolean,
    val packageName: String,
    val className: String,
    val channelName: String,
    val nativeMethods: List<NativeMethodModel>
) {

    val generatedName get() = "${className}_ServiceImpl"

    val delegateClassName get() = "${className}_Delegate"

    val delegateInstanceName get() = "bridgeDelegate"

    val registryClassName get() = "${className}_Registry"
}

class NativeMethodModel(

    /**
     * 调用的方法名
     */
    val methodName: String,

    /**
     * 原始方法名
     */
    val originalName: String,

    /**
     * 返回值
     */
    val returnType: TypeMirror,

    /**
     * 参数
     */
    val parameters: List<VariableElement>,

    /**
     * 调度线程
     */
    val threadMode: ThreadMode
) {

    fun isReturnVoid(): Boolean {
        return returnType.kind == TypeKind.VOID
    }

}