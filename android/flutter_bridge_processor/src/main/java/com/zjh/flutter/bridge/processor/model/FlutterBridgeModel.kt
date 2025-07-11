package com.zjh.flutter.bridge.processor.model

import com.zjh.flutter.bridge.core.model.ThreadMode
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

internal class BridgeModel(
    /**
     * 是否需要创建实现类（针对抽象类的情况）
     */
    val needGenerateImplClass: Boolean,
    val packageName: String,
    val className: String,
    val channelName: String,
    val nativeMethods: List<NativeMethodModel>,
    val flutterMethods: List<MethodModel>
) {

    val generatedName get() = "${className}\$\$ServiceImpl"

    val registryClassName get() = "${className}\$\$Registry"

    /**
     * 抽象实现类的名称
     */
    val implClassName get() = "${className}\$\$Impl"

    val delegateInterfaceName get() = "${className}Delegate"

    val implInstanceName get() = "bridgeImpl"

}

open class MethodModel(

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
) {

    fun isReturnVoid(): Boolean {
        return returnType.kind == TypeKind.VOID
    }
}

class NativeMethodModel(
    methodName: String,
    originalName: String,
    returnType: TypeMirror,
    parameters: List<VariableElement>,

    /**
     * 调度线程
     */
    val threadMode: ThreadMode
) : MethodModel(
    methodName,
    originalName,
    returnType,
    parameters,
)